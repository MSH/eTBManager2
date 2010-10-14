package org.msh.tb.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.Forecasting;
import org.msh.mdrtb.entities.ForecastingBatch;
import org.msh.mdrtb.entities.ForecastingMedicine;
import org.msh.mdrtb.entities.ForecastingNewCases;
import org.msh.mdrtb.entities.ForecastingOrder;
import org.msh.mdrtb.entities.ForecastingRegimen;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.MedicineLine;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.RegimensQuery;
import org.msh.utils.date.DateUtils;


@Name("forecastingView")
@Scope(ScopeType.CONVERSATION)
public class ForecastingView {

	private static final MedicineLine[] medicineLines = {	MedicineLine.FIRST_LINE, MedicineLine.SECOND_LINE };

	@In(create=true) ForecastingHome forecastingHome;
	@In(create=true) EntityManager entityManager;
	@In(create=true) RegimensQuery regimens;
	@In(create=true) MedicinesQuery medicines;


	/**
	 * Displayable table of cases on treatment
	 */
	private CasesRegimenTable casesRegimenTable;

	
	/**
	 * New batch or batch being edited
	 */
	private ForecastingBatch batch;

	/**
	 * New order or order being edited
	 */
	private ForecastingOrder order;
	
	/**
	 * Options of months for buffer stock
	 */
	private List<SelectItem> bufferStockOptions;

	/**
	 * Table with results from forecasting
	 */
	private ForecastingResultTable resultTable;
	
	
	/**
	 * Initialize a new forecasting
	 * @return
	 */
	public String initialize() {
		Forecasting forecasting = forecastingHome.getInstance();
		if (forecasting.getMedicines().size() > 0)
			return "initialized";
		
		// default values
		forecasting.setReferenceDate(DateUtils.getDatePart(new Date()));
		forecasting.setBufferStock(3);
		forecasting.setName(Messages.instance().get("manag.forecast.new"));
		
		Date dt = DateUtils.incMonths(forecasting.getReferenceDate(), 6);
		int month = DateUtils.monthOf(dt);
		int year = DateUtils.yearOf(dt);
		dt = DateUtils.newDate(year, month, 1);
		forecasting.setIniDate(dt);
		dt = DateUtils.incMonths(dt, 6);
		forecasting.setEndDate(dt);

		medicineLineChangeListener();
		datesChangeListener();
		
		initializeStockOnHand();
		updateNumCasesOnTreatment();
		
		return "initialized";
	}

	
	/**
	 * Start a new forecasting
	 */
	public void startNew() {
		forecastingHome.setInstance(null);
		forecastingHome.setId(null);
		
		Contexts.getConversationContext().set("forecasting", forecastingHome.getInstance());		
	}
	

	/**
	 * Initialize quantity in stock at the present moment
	 */
	public void initializeStockOnHand() {
		// apply restriction according to the context
		String restriction;
		Forecasting forecasting = forecastingHome.getInstance();
		if ((forecasting.getView() == UserView.TBUNIT) && (forecastingHome.getTbunitSelection().getTbunit() != null)) 
			restriction = "and s.tbunit.id = " + forecastingHome.getTbunitSelection().getTbunit().getId().toString();
		else
		if ((forecasting.getView() == UserView.ADMINUNIT) && (forecastingHome.getAdminUnitSelection().getSelectedUnit() != null))
			restriction = "and s.tbunit.adminUnit.code like '" + forecastingHome.getAdminUnitSelection().getSelectedUnit().getCode() + "%'";
		else 
		if (forecasting.getView() == UserView.COUNTRY)
			restriction = "";
		else restriction = null;

		// reset values
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			fm.setStockOnHand(0);
			fm.setUnitPrice(0);
			fm.getBatchesToExpire().clear();
		}
		
		if (restriction == null)
			return;
		
		String hql = "select s.medicine.id, sum(s.quantity) " +
				"from StockPosition s " +
				"where s.date = (select max(aux.date) from StockPosition aux " +
				"where aux.tbunit = s.tbunit and aux.source = s.source and aux.medicine = s.medicine) " +
				"and s.medicine.workspace.id = #{defaultWorkspace.id} " +
				restriction + 
				" group by s.medicine.id";

		List<Object[]> lst = entityManager.createQuery(hql)
			.getResultList();
		
		for (Object[] vals: lst) {
			Integer medId = (Integer)vals[0];
			Long qtd = (Long)vals[1];
			ForecastingMedicine fm = forecasting.findMedicineById(medId);
			if (fm != null)
				fm.setStockOnHand(qtd.intValue());
		}
		
		// update unit price
		hql = "select m.medicine.id, avg(m.unitPrice) " +
				"from Movement m " +
				"where m.date = (select max(aux.date) from Movement aux " +
				"where aux.tbunit = m.tbunit and aux.source = m.source and aux.medicine = m.medicine) " +
				"and m.medicine.workspace.id = #{defaultWorkspace.id} " +
				"group by m.medicine.id";

		lst = entityManager.createQuery(hql)
			.getResultList();

		for (Object[] vals: lst) {
			Integer medId = (Integer)vals[0];
			Double price = (Double)vals[1];
			ForecastingMedicine fm = forecasting.findMedicineById(medId);
			if (fm != null) {
				fm.setUnitPrice(price.floatValue());
			}
		}
		
		// update batches to expire
		hql = "select b.medicine.id, b.expiryDate, sum(a.quantity) " +
			"from BatchQuantity a join a.batch b " +
			"where a.quantity > 0 and b.expiryDate > :dt " +
			"and b.medicine.workspace.id = #{defaultWorkspace.id} " +
			"group by b.medicine.id, b.expiryDate";
		lst = entityManager
				.createQuery(hql)
				.setParameter("dt", new Date())
				.getResultList();
		
		for (Object[] vals: lst) {
			Integer medid = (Integer)vals[0];
			Date dt = (Date)vals[1];
			Long total = (Long)vals[2];
			ForecastingMedicine fm = forecasting.findMedicineById(medid);
			if (fm != null) {
				ForecastingBatch bt = new ForecastingBatch();
				bt.setExpiryDate(dt);
				bt.setForecastingMedicine(fm);
				bt.setQuantity(total.intValue());
				fm.getBatchesToExpire().add(bt);
			}
		}
	}

	
	/**
	 * Update information about number of new cases on treatment
	 */
	public void updateNumCasesOnTreatment() {
		Long num = (Long) entityManager.createQuery("select count(*) from TbCase c where c.patient.workspace.id = #{defaultWorkspace.id} " +
				"and c.state = :state")
				.setParameter("state", CaseState.ONTREATMENT)
				.getSingleResult();
		Forecasting forecasting = forecastingHome.getInstance();
		forecasting.setNumCasesOnTreatment(num.intValue());
	}


	/**
	 * Called when the user changes the forecasting dates (reference date, review period, lead time)
	 */
	public void datesChangeListener() {
		Forecasting forecasting = forecastingHome.getForecasting();
		
		// update new cases info
		int num = DateUtils.monthsBetween(forecasting.getReferenceDate(), forecasting.getEndDate()) + forecasting.getBufferStock();

		for (int i = 0; i <= num; i++) {
			ForecastingNewCases c = forecasting.findNewCasesInfo(i);
			if (c == null) {
				c = new ForecastingNewCases();
				c.setForecasting(forecasting);
				c.setMonthIndex(i);
				forecasting.getNewCases().add(c);
			}
		}
		
		// remove unused objects
		int i = 0;
		while (i < forecasting.getNewCases().size()) {
			ForecastingNewCases c = forecasting.getNewCases().get(i);
			if (c.getMonthIndex() > num) 
				forecasting.getNewCases().remove(c);
			else i++;
		}
	}


	/**
	 * Start entering data for a new batch
	 * @param med ForecastingMedicine of the batch
	 */
	public void newBatch(ForecastingMedicine med) {
		batch = new ForecastingBatch();
		batch.setForecastingMedicine(med);
	}

	
	/**
	 * Start editing of an existing batch
	 * @param batch
	 */
	public void editBatch(ForecastingBatch batch) {
		this.batch = batch;
	}
	

	/**
	 * Confirm adding a new batch or changing data of an existing batch
	 */
	public void confirmBatchChanges() {
		List<ForecastingBatch> batches = batch.getForecastingMedicine().getBatchesToExpire();
		if (!batches.contains(batch))
			batch.getForecastingMedicine().getBatchesToExpire().add(batch);
		batch = null;
	}
	
	/**
	 * Start entering data for a new order
	 * @param med {@link ForecastingMedicine} instance to include the order
	 */
	public void newOrder(ForecastingMedicine med) {
		order = new ForecastingOrder();
		order.setForecastingMedicine(med);
	}
	
	/**
	 * Start editing an existing order 
	 * @param order to be edited
	 */
	public void editOrder(ForecastingOrder order) {
		this.order = order;
	}


	/**
	 * Confirm the changes in the order
	 */
	public void confirmOrderChanges() {
		List<ForecastingOrder> orders = order.getForecastingMedicine().getOrders();
		if (!orders.contains(order))
			orders.add(order);
		order = null;
	}

	/**
	 * update medicines and regimens according to the medicine line selected
	 */
	public void medicineLineChangeListener() {
		Forecasting forecasting = forecastingHome.getInstance();

		MedicineLine line = forecasting.getMedicineLine();

		forecasting.getRegimens().clear();
		forecasting.getMedicines().clear();

		// mount list of regimens
		for (Regimen reg: regimens.getResultList()) {
			if ((line == null) || 
				((line.equals(MedicineLine.FIRST_LINE)) && (reg.isTbTreatment())) ||
				((line.equals(MedicineLine.SECOND_LINE)) && (reg.isMdrTreatment())))
			{
				ForecastingRegimen fr = new ForecastingRegimen();
				fr.setRegimen(reg);
				forecasting.getRegimens().add(fr);
				fr.setForecasting(forecasting);
			}
		}

		// mount list of medicines
		for (Medicine med: medicines.getResultList()) {
			if ((line == null) || (line.equals(med.getLine()))) {
				ForecastingMedicine fm = new ForecastingMedicine();
				fm.setMedicine(med);
				forecasting.getMedicines().add(fm);
				fm.setForecasting(forecasting);
			}
		}	
		casesRegimenTable = null;
	}



	/**
	 * Return an instance of the helper class to display a table of cases on treatment
	 * @return
	 */
	public CasesRegimenTable getCasesRegimenTable() {
		if (casesRegimenTable == null)
			casesRegimenTable = new CasesRegimenTable(forecastingHome.getInstance());
		return casesRegimenTable;
	}
	

	/**
	 * Return list of medicine lines to be selected by the user in the forecasting
	 * @return
	 */
	public MedicineLine[] getMedicineLines() {
		return medicineLines;
	}



	/**
	 * @return
	 */
	public ForecastingBatch getBatch() {
		return batch;
	}



	public ForecastingOrder getOrder() {
		return order;
	}



	/**
	 * Return buffer stock options
	 * @return
	 */
	public List<SelectItem> getMonthsOptions() {
		if (bufferStockOptions == null) {
			bufferStockOptions = new ArrayList<SelectItem>();
			for (int i = 0; i < 25; i++) {
				bufferStockOptions.add(new SelectItem(i, Integer.toString(i)));
			}
		}
		return bufferStockOptions;
	}

	
	/**
	 * Called when a forecasting is executed
	 */
	@Observer("forecasting-execute")
	public void forecastingExecutionListener() {
		if (resultTable != null)
			resultTable.refresh();

		if (casesRegimenTable != null)
			casesRegimenTable = null;
	}


	/**
	 * Return results in a table
	 * @return
	 */
	public ForecastingResultTable getResultTable() {
		if (resultTable == null) {
			resultTable = new ForecastingResultTable(forecastingHome.getForecasting());
		}
		return resultTable;
	}
	
}
