package org.msh.tb.forecasting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingBatch;
import org.msh.tb.entities.ForecastingCasesOnTreat;
import org.msh.tb.entities.ForecastingItem;
import org.msh.tb.entities.ForecastingMedicine;
import org.msh.tb.entities.ForecastingNewCases;
import org.msh.tb.entities.ForecastingOrder;
import org.msh.tb.entities.ForecastingPeriod;
import org.msh.tb.entities.ForecastingRegimen;
import org.msh.tb.entities.ForecastingRegimenResult;
import org.msh.tb.entities.ForecastingResult;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.WeeklyFrequency;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.entities.enums.UserView;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Responsible for calculating and generation of the forecasting results
 * @author Ricardo Memoria
 *
 */
@Name("forecastingCalculation")
public class ForecastingCalculation {

	@In(create=true) ForecastingHome forecastingHome;
	@In(create=true) ForecastingView forecastingView;
	@In EntityManager entityManager;
	
	/**
	 * total number of months of the forecasting
	 */
	private int numMonths;

	
	/**
	 * Store the ending date of the forecasting, i.e, the forecasting.endDate + forecasting.bufferStock
	 */
	private Date endForecasting;


	/**
	 * The forecasting object being calculated
	 */
	private Forecasting forecasting;


	/**
	 * List of cases on treatment, used during calculations if cases are from database
	 */
	private List<Object[]> casesOnTreatment;

	/**
	 * Used inside the calculation
	 */
	private PrescribedMedicine pm;
	
	private Period leadTimePeriod;
	
	private Period reviewPeriod;
	private float controlTotal=0;
	
	
	/**
	 * Execute the forecasting
	 */
	@RaiseEvent("forecasting-execute")
	public String execute() {
		forecasting = forecastingHome.getInstance();

		// calculate the number of months for the whole forecasting calculation
		endForecasting = DateUtils.incMonths(forecasting.getEndDate(), forecasting.getBufferStock());
		numMonths = forecasting.getNumMonths();

		startCalculation();
		try {
			calculate();
		}
		finally {
			finishCalculation();
		}

		return "executed";
	}


	/**
	 * Return an instance of the {@link Forecasting} object in use for calculation
	 * @return instance of {@link Forecasting} object
	 */
	public Forecasting getForecasting() {
		return forecasting;
	}


	/**
	 * Set the {@link Forecasting} object to be used in calculations
	 * @param forecasting
	 */
	public void setForecasting(Forecasting forecasting) {
		this.forecasting = forecasting;
	}


	/**
	 * Start procedures prior to execute the calculation loop
	 */
	protected void startCalculation() {
		// clear current results
		forecasting.initialize();

		Date dt = forecasting.getIniDateLeadTime();
		
		leadTimePeriod = new Period(forecasting.getReferenceDate(), DateUtils.incDays(dt, -1));
		reviewPeriod = new Period(dt, endForecasting);
		
		forecasting.getMonthIndex(forecasting.getIniDateLeadTime());

		// cases are from the database ?
		// if so, load list of prescribed medicines in the period
		if (forecasting.isCasesFromDatabase()) {
			// update number of cases on treatment
			Long count = (Long)entityManager.createQuery("select count(*) from TbCase c " + 
					"where c.notificationUnit.workspace.id = #{defaultWorkspace.id} " +
					"and c.state = :state")
					.setParameter("state", CaseState.ONTREATMENT)
					.getSingleResult();
			forecasting.setNumCasesOnTreatment(count.intValue());

			// load information about cases on treatment
			String hql = "select pm.period.iniDate, pm.period.endDate, pm.doseUnit, pm.medicine.id, pm.frequency, tb.id, tb.regimen.id " +
					"from PrescribedMedicine pm " +
					"join pm.tbcase tb " +
					"where pm.medicine.workspace.id = #{defaultWorkspace.id} " +
					"and tb.state = :val " +
					"and pm.period.endDate >= :dtIni ";

			// set restriction by medicine line
			if (MedicineLine.FIRST_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.FIRST_LINE.ordinal();
			else
			if (MedicineLine.SECOND_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.SECOND_LINE.ordinal();

			// set restriction by view
			if ((forecasting.getView() == UserView.TBUNIT) && (forecastingHome.getTbunitSelection().getTbunit() != null)) 
				hql += "and exists(from TreatmentHealthUnit hu where hu.tbcase.id = pm.tbcase.id and hu.period.endDate = pm.tbcase.treatmentPeriod.endDate " +
					"and hu.tbunit.id = " + forecastingHome.getTbunitSelection().getTbunit().getId().toString() +")";
			else
			if ((forecasting.getView() == UserView.ADMINUNIT) && (forecastingHome.getAdminUnitSelection().getSelectedUnit() != null))
				hql += "and exists(from TreatmentHealthUnit hu where hu.tbcase.id = pm.tbcase.id and hu.period.endDate = pm.tbcase.treatmentPeriod.endDate " +
					"and hu.tbunit.adminUnit.code like '" + forecastingHome.getAdminUnitSelection().getSelectedUnit().getCode() + "%')";

			hql += " order by tb.id, pm.medicine.id";
			casesOnTreatment = entityManager.createQuery(hql)
					.setParameter("val", CaseState.ONTREATMENT)
					.setParameter("dtIni", forecasting.getReferenceDate())
					.getResultList();
			
			pm = new PrescribedMedicine();
		}

		// remove unused new cases that are blank
		int i = 0;
		while (i < forecasting.getCasesOnTreatment().size()) {
			ForecastingCasesOnTreat c = forecasting.getCasesOnTreatment().get(i);
			if (c.getNumCases() == null)
				 forecasting.getCasesOnTreatment().remove(i);
			else i++;
		}
		
		// sort batches by date
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			Collections.sort(fm.getBatchesToExpire(), new Comparator<ForecastingBatch>() {
				public int compare(ForecastingBatch o1, ForecastingBatch o2) {
					return o1.getExpiryDate().compareTo(o2.getExpiryDate());
				}
			});
		}
	}


	/**
	 * Procedures to be executed after calculation loop
	 */
	protected void finishCalculation() {
		// free internal variables
		casesOnTreatment = null;
		pm = null;
	}


	/**
	 * Calculate the forecasting for an specific month of the year
	 * @param monthIndex
	 */
	protected void calculate() {
		createPeriods();
		updateStockOnOrderAndQtdToExpire();

		calculateCasesOnTreatment();
		calculateNewCases();

		calculatePeriodsQuantities();
		
		calculateQuantityToBeProcured();
		
		// update total cases by regimen
		for (int i = 0; i < numMonths; i++) {
			int tot = 0;
			for (ForecastingRegimen fr: forecasting.getRegimens())
				tot += fr.getResults().get(i).getTotalCases();
			forecasting.getTotalRegimens().get(i).setQuantity(tot);
			Integer tot2 = forecasting.getTotalIndividualized().get(i).getQuantity();
			int total = tot + (tot2 != null? tot2: 0); 
			forecasting.getTotal().get(i).setQuantity(total);
		}
		
		calculateStockOutDate();
	}


	/**
	 * Calculate the first stock out date for the medicines
	 */
	private void calculateStockOutDate() {
		for (ForecastingMedicine fm: getForecasting().getMedicines()) {
			Date stockOutDate = forecasting.getEndDate();
			stockOutDate = DateUtils.incMonths(stockOutDate, forecasting.getBufferStock());
			for (ForecastingPeriod period: fm.getPeriods()) {
				if (period.getQuantityMissing() < 0) {
					stockOutDate = period.getPeriod().getEndDate();
					// calculate the number of missing days for the consumption
					int days = period.getPeriod().getDays() * period.getQuantityMissing() / period.getEstimatedConsumption();
					// estimate the stock out date
					stockOutDate = DateUtils.incDays(stockOutDate, days);
					break;
				}
			}

			fm.setStockOutDate(stockOutDate);
		}
	}


	/**
	 * Calculate the quantity to be procured
	 */
	protected void calculateQuantityToBeProcured() {
		for (ForecastingMedicine med: forecasting.getMedicines()) {
			int qtdToProcure = 0;

			// clear values to be calculated
			med.setQuantityToProcure(0);
			for (ForecastingResult res: med.getResults())
				res.setQuantityMissing(0);

			for (ForecastingPeriod fp: med.getPeriods()) {
				int qtdMissing = -fp.getQuantityMissing();
				// check if it's inside the review period
				if (!fp.getPeriod().getIniDate().before(getReviewPeriod().getIniDate()))
					qtdToProcure += qtdMissing;

				// update the quantity missing by medicine
				int index = forecasting.getMonthIndex(fp.getPeriod().getIniDate());
				ForecastingResult fr = med.findResultByMonthIndex(index);
				if (fr != null)
					fr.setQuantityMissing( fr.getQuantityMissing() + qtdMissing );
			}
			med.setQuantityToProcure(qtdToProcure);
		}
	}


	/**
	 * Calculate the quantity of medicine expired for each period of the medicines
	 * @param forMedicine
	 */
	protected void calculatePeriodsQuantities() {
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			int stockOnHand = fm.getStockOnHand();
			ForecastingPeriod prev = null;

			for (ForecastingPeriod p: fm.getPeriods()) {
				// there is a previous period calculated ?
				int qtdExp;
				if (prev != null) {
					// calculate quantity expired using the last period
					qtdExp = calcExpiredQuantity(fm, prev);
				}
				else qtdExp = 0;

				p.setQuantityExpired(qtdExp);

				// calculate the stock on hand at the beginning of the period
				stockOnHand += p.getQuantityInOrder() - qtdExp;
				p.setStockOnHand(stockOnHand);

				// update stock on hand based on estimated consumption
				int cons = p.getEstimatedConsumption();

				int qtdMissing;
				if (cons > stockOnHand) {
					qtdMissing = stockOnHand - cons;
					stockOnHand = 0;
				}
				else {
					qtdMissing = 0;
					stockOnHand += -cons;
				}

				// update quantity missing
				p.setQuantityMissing(qtdMissing);

				if (stockOnHand < 0)
					stockOnHand = 0;
				
				updateMonthQuantities(fm, p);

				// update previous period
				prev = p;
			}
		}
	}
	

	/**
	 * Update the months quantities based on the given period. Called when the period values 
	 * are calculated and the month quantities must be updated
	 * @param fm the instance of {@link ForecastingMedicine} to be updated
	 * @param p the {@link ForecastingPeriod} that is given with quantities updated
	 */
	protected void updateMonthQuantities(ForecastingMedicine fm, ForecastingPeriod p) {
		int index = forecasting.getMonthIndex(p.getPeriod().getIniDate());
		ForecastingResult fr = fm.findResultByMonthIndex(index);

		// update month data
		if (fr.getStockOnHand() == 0)
			fr.setStockOnHand(p.getStockOnHand());

		fr.setQuantityExpired( fr.getQuantityExpired() + p.getQuantityExpired() );
		fr.setConsumptionCases( fr.getConsumptionCases() + p.getEstConsumptionCases() );
		fr.setConsumptionNewCases( fr.getConsumptionNewCases() + p.getEstConsumptionNewCases() );

		// update resume data

		// is during lead time ?
		if ( p.getPeriod().getIniDate().before(reviewPeriod.getIniDate()) ) {
			fm.addConsumptionLT( p.getEstimatedConsumption());
			fm.setQuantityMissingLT( fm.getQuantityMissingLT() + p.getQuantityMissing() );
			fm.addQuantityExpiredLT( p.getQuantityExpired() );
		}
		else {
			fm.addConsumptionCases( p.getEstConsumptionCases() );
			fm.addConsumptionNewCases( p.getEstConsumptionNewCases() );
			fm.addQuantityExpired( p.getQuantityExpired() );
		}
	}
	
	
	/**
	 * Calculates quantity expired of a specific medicine for a specific month
	 * based on the quantity available for each batch
	 * @param fm
	 * @param res
	 * @param consumptionMonth
	 * @return
	 */
	protected int calcExpiredQuantity(ForecastingMedicine fm, ForecastingPeriod per) {
		int con = per.getEstimatedConsumption();
		int qtdExpired = 0;
		
		Date dt = per.getPeriod().getIniDate();
		Date dtend = per.getPeriod().getEndDate();

		while (true) {
			ForecastingBatch batch = fm.findAvailableBatch(dt);
			if (batch == null)
				break;

			// check if batch will expire this month
			boolean expiresThisMonth = dtend.equals( batch.getExpiryDate() );

			// quantity available of the current batch
			int quantityAvailable = batch.getQuantityAvailable();
			int consumed;
			int exp = 0;

			if (quantityAvailable > con) {
				consumed = con;
				exp = (expiresThisMonth? quantityAvailable - con : 0);
				qtdExpired += exp;
				con = 0;
				batch.setQuantityExpired(exp);
			}
			else {
				consumed = quantityAvailable;
				con -= quantityAvailable;
			}

			batch.setQuantityAvailable( quantityAvailable - consumed - exp);

			if (consumed > 0) {
				ForecastingResult res = fm.findResultByMonthIndex( forecasting.getMonthIndex(per.getPeriod().getIniDate()) );
				res.addBatchConsumption(batch, consumed);
			}

			if (con == 0)
				break;
		}
		
		return qtdExpired;
	}



	/**
	 * Calculate stock on order for all medicines in every month of forecasting
	 */
	protected void updateStockOnOrderAndQtdToExpire() {
		Date dt = forecasting.getIniDateLeadTime();
		
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			for (ForecastingOrder fo: fm.getOrders()) {
				int qtd = fo.getQuantity();
				
				int index = forecasting.getMonthIndex(fo.getArrivalDate());
				ForecastingResult res = fm.findResultByMonthIndex(index);
				
				if (res != null)
					res.addStockOnOrder(qtd);

				// is during lead time ?
				if ((fo.getArrivalDate().before(dt)) && (!fo.getArrivalDate().before(forecasting.getReferenceDate()))) {
					fm.addStockOnOrderLT(qtd);
				}
				else {
					// add order if is during review period
					if ((!fo.getArrivalDate().before(dt)) && (fo.getArrivalDate().before(endForecasting)))
						fm.addStockOnOrder(qtd);
				}
			}

			// update batches to expire
			for (ForecastingBatch fb: fm.getBatchesToExpire()) {
				int index = forecasting.getMonthIndex(fb.getExpiryDate());
				
				ForecastingResult res = fm.findResultByMonthIndex(index);
				if (res != null)
					res.setQuantityToExpire(res.getQuantityToExpire() + fb.getQuantity());
			}
		}
	}
	
	
	/**
	 * Quantity of medicine on order to be received in the month. Medicine and month
	 * are indicated in the {@link ForecastingResult} result parameter
	 * @param result
	 * @return
	 */
/*	protected int quantityOnOrder(ForecastingResult result, boolean leadTimeOnly) {
		int qtd = 0;
		Date dt = (leadTimeOnly? forecasting.getIniDateLeadTime(): null);

		for (ForecastingMedicine med: forecasting.getMedicines()) {
			for (ForecastingOrder order: med.getOrders()) {
				if ((result.getMedicine().equals(med.getMedicine())) && (forecasting.getMonthIndex(order.getArrivalDate()) == result.getMonthIndex())) {
					if ((dt == null) || ((dt != null) && (order.getArrivalDate().before(dt))))
					qtd += order.getQuantity();
				}
			}
		}
		return qtd;
	}
*/


	/**
	 * Calculate the quantity necessary for the cases under treatment
	 */
	protected void calculateCasesOnTreatment() {
		if (forecasting.isCasesFromDatabase())
			 calculateCasesOnTreatmentFromDB();
		else calculateCasesOnTreatmentInForecasting();
	}
	

	/**
	 * Calculate the forecasting for new cases
	 */
	protected void calculateNewCases() {
		for (ForecastingNewCases c: forecasting.getNewCases()) {
			for (ForecastingRegimen reg: forecasting.getRegimens()) {
				calculateNewCasesRegimen(c, reg);
			}
		}
	}


	
	/**
	 * Calculate the consumption of medicine for each one of the cases registered in the database
	 */
	private void calculateCasesOnTreatmentFromDB() {
		Map<Integer, List<Integer>> caseids = new HashMap<Integer, List<Integer>>();

		Integer prevCaseId = null;
		Integer prevMedId = null;

		for (Object[] prescDrug: casesOnTreatment) {
			ForecastingMedicine fm = forecasting.findMedicineById((Integer)prescDrug[3]);

			// update consumption for each period
			for (ForecastingPeriod forPeriod: fm.getPeriods()) {
				int qtd = calcConsumptionCaseOnTreatment(prescDrug, forPeriod.getPeriod());
				forPeriod.setEstConsumptionCases(forPeriod.getEstConsumptionCases() + qtd);
			}
			
			ForecastingRegimen freg = forecasting.findRegimenById((Integer)prescDrug[6]);

			Integer caseId = (Integer)prescDrug[5];
			Integer medId = (Integer)prescDrug[3];
			
			// count number of cases
			boolean newcase = !caseId.equals(prevCaseId);
			boolean newmed = newcase || (!medId.equals(prevMedId));
			if (newmed) {
				int indexini = forecasting.getMonthIndex((Date)prescDrug[0]);
				int indexend = forecasting.getMonthIndex((Date)prescDrug[1]);
				int max = forecasting.getNumMonths();
				if ((indexini <= max) || (indexend >= 0)) {
					if (indexini < 0)
						indexini = 0;
					if (indexend > max)
						indexend = max;

					for (int i = indexini; i <= indexend; i++) {
						ForecastingResult res = fm.findResultByMonthIndex(i);
						res.setNumCasesOnTreatment( res.getNumCasesOnTreatment() + 1);

						List<Integer> lst = caseids.get(i);
						if (lst == null) {
							lst = new ArrayList<Integer>();
							caseids.put(i, lst);
						}

						if ((freg != null) && (!lst.contains(caseId))) {
							ForecastingRegimenResult regres = freg.findResultByMonthIndex(i);
							regres.setNumCasesOnTreatment( regres.getNumCasesOnTreatment() + 1);
							lst.add(caseId);
						}

						// update regimen information
						if (newcase) {
							if (freg != null) {
								// increment quantity of patients in standard regimen
								ForecastingItem item = forecasting.getTotalRegimens().get(i);
								item.addQuantity(1);
							}
							else {
								// increment quantity of patients in individualized regimen
								ForecastingItem item = forecasting.getIndividualizedResults().get(i);
								item.addQuantity(1);
							}
						}
					}
				}
			}
		}
	}


	
	/**
	 * Calculate the quantity of medicine necessary for a specific case in a period
	 * @param caseInfo record from the array casesOnTreatment
	 * @param dtIni
	 * @param dtEnd
	 * @return quantity of the medicine
	 */
	private int calcConsumptionCaseOnTreatment(Object[] caseInfo, Period period) {
		pm.getPeriod().setIniDate((Date)caseInfo[0]);
		pm.getPeriod().setEndDate((Date)caseInfo[1]);

		if (!pm.getPeriod().isIntersected(period))
			return 0;

		pm.setDoseUnit((Integer)caseInfo[2]);
		pm.setFrequency((Integer)caseInfo[4]);
		Integer medId = (Integer)caseInfo[3];
		ForecastingMedicine fm = forecasting.findMedicineById(medId);
		pm.setMedicine(fm.getMedicine());

		Period p = new Period(period);
		p.intersect(pm.getPeriod());
		
		return pm.calcEstimatedDispensing(p);
	}

	

	/**
	 * Calculate the consumption based on the number of cases on treatment entered by the user in the forecasting tool
	 * @param monthIndex
	 */
	private void calculateCasesOnTreatmentInForecasting() {
		for (int monthIndex = 0; monthIndex <= numMonths; monthIndex++) {
			for (ForecastingCasesOnTreat c: forecasting.getCasesOnTreatment()) {
				Regimen reg = c.getRegimen();
				int numMonths = reg.getMonthsPhase(RegimenPhase.INTENSIVE) + reg.getMonthsPhase(RegimenPhase.CONTINUOUS);

				// get month of treatment in the regimen starting in 0
				int monthTreat = monthIndex - c.getMonthIndex();

				// is month of treatment valid for this regimen ?
				int qtd = 0;
				if (monthTreat < numMonths) {
					for (ForecastingMedicine fm: forecasting.getMedicines()) {
						qtd += calculateConsumptionCasesInForecasting(monthIndex, monthTreat, c, fm);
					}
				}

				if (qtd > 0) {
					ForecastingRegimen fr = forecasting.findRegimen(c.getRegimen());
					ForecastingRegimenResult res = fr.findResultByMonthIndex(monthIndex);
					res.setNumCasesOnTreatment( res.getNumCasesOnTreatment() + c.getNumCases() );
				}
			}
		}
	}
	
	
	/**
	 * Calculate the consumption for cases defined in the forecasting.
	 * It was broke from the original method just to make it simpler
	 */
	private int calculateConsumptionCasesInForecasting(int monthIndex, int monthTreat, ForecastingCasesOnTreat fcot, ForecastingMedicine fm) {
		Regimen reg = fcot.getRegimen();
		if (!reg.isMedicineInRegimen(fm.getMedicine()))
			return 0;
		ForecastingResult res = forecasting.findResult(fm.getMedicine(), monthIndex);
		if (res == null)
			RaiseResultException(fm.getMedicine(), monthIndex);

		Date dtIni = forecasting.getIniDateMonthIndex(monthIndex);
		Date dtEnd = forecasting.getEndDateMonthIndex(monthIndex);
		
		// calculate the consumption of all periods in the month
		Period p = new Period(dtIni, dtEnd);
		int qtd = 0;
		for (ForecastingPeriod forper: fm.getPeriods()) {
			if (forper.getPeriod().isInside(p)) {
				int qtyperiod = calcQuantityRegimen(reg, forper.getPeriod(), monthTreat, fm.getMedicine()) * fcot.getNumCases();
				forper.setEstConsumptionCases( forper.getEstConsumptionCases() + qtyperiod );
				qtd += qtyperiod;
			}
		}
		
		if (qtd > 0)
			res.setNumCasesOnTreatment( res.getNumCasesOnTreatment() + fcot.getNumCases());
		
		return qtd;
	}



	/**
	 * Calculates the quantity dispensed by regimen
	 * @param forRegimen
	 * @param dtini
	 * @param dtfim
	 */
	protected void calculateNewCasesRegimen(ForecastingNewCases forNewCases, ForecastingRegimen forRegimen) {
		
		// calculate total percentage of regimens
		float totalPerc = 0;
//		String wsExt = (String)Component.getInstance("workspaceExtension");
		for (ForecastingRegimen aux: forecasting.getRegimens())
				totalPerc += aux.getPercNewCases();
			if (totalPerc == 0)
				return;		
		this.controlTotal = totalPerc;
		float newCases = Math.round( forNewCases.getNumNewCases() * forRegimen.getPercNewCases() / totalPerc );
		if (newCases == 0)
			return;

		Regimen reg = forRegimen.getRegimen();
		Date dtini = forecasting.getIniDateMonthIndex(forNewCases.getMonthIndex());

		// initialize number of new cases
		int months = reg.getMonthsIntensivePhase() + reg.getMonthsContinuousPhase();
		for (int i = 0; i < months; i++) {
			int index = i + forNewCases.getMonthIndex();

			ForecastingRegimenResult resreg = forRegimen.findResultByMonthIndex(index);
			if (resreg != null)
				resreg.setNumNewCases( resreg.getNumNewCases() + newCases );
		}
		
		int prevMonthIndex = -1;
		// calculate number of new cases and quantity for each medicine
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			if (reg.isMedicineInRegimen(fm.getMedicine())) {
				for (ForecastingPeriod forPer: fm.getPeriods()) {
					int qty = calcEstimatedConsumptionRegimen(reg, fm.getMedicine(), dtini, forPer.getPeriod());

					if (qty > 0) {
						qty = Math.round( (float)qty * newCases );

						forPer.setEstConsumptionNewCases(forPer.getEstConsumptionNewCases() + qty);

						// update number of new cases
						ForecastingResult res = fm.findResultByMonthIndex( forecasting.getMonthIndex(forPer.getPeriod().getIniDate()) );
						if (prevMonthIndex != res.getMonthIndex()) {
							res.setNumNewCases( res.getNumNewCases() + newCases );
							prevMonthIndex = res.getMonthIndex();
						}
					}
				}
			}
		}
	}


	/**
	 * Calculate estimated consumption for a regimen where treatment started in dtIniTreatment date using a 
	 * specific medicine. The estimated consumption is for a period specified by p
	 * @param reg
	 * @param medicine
	 * @param dtIniTreatment
	 * @param p
	 * @return
	 */
	public int calcEstimatedConsumptionRegimen(Regimen reg, Medicine medicine, Date dtIniTreatment, Period p) {
		int monthsInt = reg.getMonthsIntensivePhase();
		int result = 0;
		
		for (MedicineRegimen medreg: reg.getMedicines()) {
			if (medreg.getMedicine().equals(medicine)) {
				// get initial date of prescription of the medicine
				Date ini = (medreg.getPhase() == RegimenPhase.INTENSIVE? dtIniTreatment : DateUtils.incMonths(dtIniTreatment, monthsInt));
				Date end = DateUtils.incDays( DateUtils.incMonths(ini, medreg.getMonthsTreatment()), -1);
				Period regper = new Period(ini, end);
				Period aux = new Period(p);
				if (aux.intersect(regper)) {
					WeeklyFrequency wf = forecastingHome.getWorkspace().getWeeklyFrequency(medreg.getDefaultFrequency());
					int numDays = wf.calcNumDays(aux);
					result += numDays * medreg.getDefaultDoseUnit();
				}
			}
		}
		return result;
	}
	

	/**
	 * Calculate the estimated quantity based on the initial month (0 is the first) and the medicine
	 * @param month
	 * @param medicine
	 * @return
	 */
	public int calcQuantityRegimen(Regimen reg, Period p, int monthOfTreatment, Medicine medicine) {
		int intPhaseMonths = reg.getMonthsPhase(RegimenPhase.INTENSIVE);
		RegimenPhase phase = (monthOfTreatment < intPhaseMonths? RegimenPhase.INTENSIVE: RegimenPhase.CONTINUOUS);

		MedicineRegimen medReg = null;
		for (MedicineRegimen aux: reg.getMedicines()) {
			if ((aux.getMedicine().equals(medicine)) && (aux.getPhase().equals(phase))) {
				medReg = aux;
				break;
			}
		}
		
		if (medReg == null)
			return 0;
		
		WeeklyFrequency wf = forecastingHome.getWorkspace().getWeeklyFrequency(medReg.getDefaultFrequency());
		int numDays = wf.calcNumDays(p);
		return numDays * medReg.getDefaultDoseUnit();
	}

	
	/**
	 * Create periods to be calculated. A period must be within a month. It cannot cross
	 * several months.
	 */
	protected void createPeriods() {
		Map<Date, Integer> dates = new HashMap<Date, Integer>();
		Map<Date, Integer> orders = new HashMap<Date, Integer>();
		
		Period forPeriod = new Period(forecasting.getReferenceDate(), endForecasting);

		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			dates.clear();

			// set forecasting boundaries
			Date dtini = forecasting.getReferenceDate();
			dates.put(endForecasting, 0);
			dates.put(DateUtils.incDays(forecasting.getIniDateLeadTime(), -1), 0);
			
			for (int n = 0; n <= forecasting.getNumMonths(); n++) {
				Date dt1 = forecasting.getIniDateMonthIndex(n);
				dates.put(dt1, 0);
			}
			
			// add dates of batches to expire
			for (ForecastingBatch b: fm.getBatchesToExpire()) {
				Date dt = DateUtils.getDatePart( b.getExpiryDate() );
				if (forPeriod.isDateInside(dt)) {
					
					if (DateUtils.dayOf(dt) == 1) {
						dates.put(dt, 0);
						dates.put( DateUtils.incDays(dt, 1), -b.getQuantity());
					}
					else dates.put(dt, b.getQuantity());
				}
			}
			
			// add dates of orders to arrive
			for (ForecastingOrder order: fm.getOrders()) {
				Date dt = DateUtils.incDays( DateUtils.getDatePart( order.getArrivalDate() ), -1);
				if (forPeriod.isDateInside(dt)) {
					if (dates.get(dt) == null)
						dates.put(dt, 0);
					orders.put(dt, order.getQuantity());
				}
			}

			// when all relevant dates are included, just sort them
			List<Date> lst = new ArrayList(dates.keySet());
			Collections.sort(lst);
			
			if (lst.contains(dtini))
				lst.remove(dtini);

			// create all periods starting by the reference date
			Integer qtdOrder = null;
			for (Date dt: lst) {
				if (!dtini.equals(dt)) {
					Integer qtd = dates.get(dt);

					ForecastingPeriod mov = new ForecastingPeriod();

					Date dtend;
					// if is before end of forecasting, the date is reduced in 1 day to avoid
					// overlapping with the next period
					boolean sameMonth = (DateUtils.monthOf(dt) == DateUtils.monthOf(dtini)); 
//					if (dt.before(endForecasting))
					if (!sameMonth)
						 dtend = DateUtils.incDays(dt, -1);
					else dtend = dt;

					Period p = new Period(dtini, dtend);
					mov.setPeriod(p);
					mov.setQuantityToExpire(qtd);

					if (qtdOrder != null)
						mov.setQuantityInOrder(qtdOrder);
					fm.getPeriods().add(mov);
					
					// add in the next loop
					qtdOrder = orders.get(dt);
//					dtini = dt;
					if (sameMonth)
						 dtini = DateUtils.incDays(dt, 1);
					else dtini = dt;
				}
			}
		}
	}
	
	
	/**
	 * Return batches of a medicine in a specific month of treatment
	 * @param fm
	 * @param monthIndex
	 * @return
	 */
/*	private List<ForecastingBatch> getBatchesMonth(ForecastingMedicine fm, int monthIndex) {
		List<ForecastingBatch> lst = new ArrayList<ForecastingBatch>();
		for (ForecastingBatch b: fm.getBatchesToExpire()) {
			if (forecasting.getMonthIndex(b.getExpiryDate()) == monthIndex) {
				lst.add(b);
			}
		}
		
		Collections.sort(lst, new Comparator<ForecastingBatch>() {
			public int compare(ForecastingBatch o1, ForecastingBatch o2) {
				return o1.getExpiryDate().compareTo(o2.getExpiryDate());
			}
			
		});
		return lst;
	}
*/	
	/**
	 * Raise exception if result is not found for an specific medicine and monthIndex (it's always suposed to be found)
	 * @param med
	 * @param monthIndex
	 */
	private void RaiseResultException(Medicine med, int monthIndex) {
		throw new RuntimeException("Forecasting result not found for med=" + med.toString() + " monthIndex=" + monthIndex);
	}

	public int getNumMonths() {
		return numMonths;
	}


	public Date getEndForecasting() {
		return endForecasting;
	}


	/**
	 * @return the leadTimePeriod
	 */
	public Period getLeadTimePeriod() {
		return leadTimePeriod;
	}


	/**
	 * @return the reviewPeriod
	 */
	public Period getReviewPeriod() {
		return reviewPeriod;
	}


	public float getControlTotal() {
		return controlTotal;
	}
	
}
