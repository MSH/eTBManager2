package org.msh.tb.forecasting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingBatch;
import org.msh.tb.entities.ForecastingCasesOnTreat;
import org.msh.tb.entities.ForecastingMedicine;
import org.msh.tb.entities.ForecastingNewCases;
import org.msh.tb.entities.ForecastingOrder;
import org.msh.tb.entities.ForecastingRegimen;
import org.msh.tb.entities.ForecastingResult;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;

/**
 * Calculate a forecasting and save/restore its results to/from the database
 * @author Ricardo Memoria
 *
 */
@Name("forecastingHome")
public class ForecastingHome extends EntityHomeEx<Forecasting> {
	private static final long serialVersionUID = -3844270483725397280L;
	
	@In(create=true) EntityManager entityManager;
	
	
	private AdminUnitSelection adminUnitSelection;
	private TBUnitSelection tbunitSelection;
	
	private String newName;
	private boolean publicView;

	
	@Factory("forecasting")
	public Forecasting getForecasting() {
		return getInstance();
	}

	@Override
	public void setId(Object id) {
		super.setId(id);
		Contexts.getConversationContext().set("forecasting", getInstance());
		if (isManaged()) {
			getAdminUnitSelection().setSelectedUnit(getInstance().getAdministrativeUnit());
			getTbunitSelection().setTbunit(getInstance().getTbunit());
			newName = getInstance().getName();
			publicView = getInstance().isPublicView();
		}
	}


	/**
	 * Save forecasting as a different name. If it's a new forecasting, the execution is redirected to the persist method, otherwise
	 * a copy of the {@link Forecasting} instance is created with blank IDs and replaced by the current instance
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public String saveAs() throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		if (isManaged()) {
			// generate clone of the forecasting object
			Forecasting newForecasting = new Forecasting();
			BeanUtils.copyProperties(newForecasting, getInstance());
			newForecasting.setId(null);
			newForecasting.setUser(getUser());
			newForecasting.setRecordingDate(new Date());
			
			newForecasting.setMedicines(new ArrayList<ForecastingMedicine>());
			for (ForecastingMedicine fm: getInstance().getMedicines()) {
				ForecastingMedicine newFm = new ForecastingMedicine();
				BeanUtils.copyProperties(newFm, fm);
				newFm.setForecasting(newForecasting);
				newFm.setId(null);
				newForecasting.getMedicines().add(newFm);
				newFm.setBatchesToExpire(new ArrayList<ForecastingBatch>());
				newFm.setOrders(new ArrayList<ForecastingOrder>());

				// clone batches
				for (ForecastingBatch fb: fm.getBatchesToExpire()) {
					ForecastingBatch newfb = new ForecastingBatch(); 
					BeanUtils.copyProperties(newfb, fb);
					newfb.setForecastingMedicine(newFm);
					newfb.setId(null);
					newFm.getBatchesToExpire().add(newfb);
				}

				// clone orders
				for (ForecastingOrder fo: fm.getOrders()) {
					ForecastingOrder newfo = new ForecastingOrder(); 
					BeanUtils.copyProperties(newfo, fo);
					newfo.setForecastingMedicine(newFm);
					newfo.setId(null);
					newFm.getOrders().add(newfo);
				}
			}

			// copy new cases
			newForecasting.setNewCases(new ArrayList<ForecastingNewCases>());
			for (ForecastingNewCases nc: getInstance().getNewCases()) {
				ForecastingNewCases newnc = new ForecastingNewCases();
				BeanUtils.copyProperties(newnc, nc);
				newnc.setForecasting(newForecasting);
				newnc.setId(null);
				newForecasting.getNewCases().add(newnc);
			}

			// copy cases on treatment
			newForecasting.setCasesOnTreatment(new ArrayList<ForecastingCasesOnTreat>());
			for (ForecastingCasesOnTreat fc: getInstance().getCasesOnTreatment()) {
				ForecastingCasesOnTreat newfc = (ForecastingCasesOnTreat)BeanUtils.cloneBean(fc);
				newfc.setId(null);
				newForecasting.getCasesOnTreatment().add(newfc);
			}

			// copy regimens
			newForecasting.setRegimens(new ArrayList<ForecastingRegimen>());
			for (ForecastingRegimen reg: getInstance().getRegimens()) {
				ForecastingRegimen newreg = new ForecastingRegimen();
				BeanUtils.copyProperties(newreg, reg);
				newreg.setId(null);
				newForecasting.getRegimens().add(newreg);
			}
			
			newForecasting.setResults(new ArrayList<ForecastingResult>());
			
			setInstance(newForecasting);
		}

		getInstance().setName(newName);
		getInstance().setPublicView(publicView);
		
		return persist();
	}
	

	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#persist()
	 */
	public String persist() {
		Forecasting forecasting = getInstance();
		if (forecasting.getRecordingDate() == null)
			forecasting.setRecordingDate(new Date());
		
		forecasting.setAdministrativeUnit(getAdminUnitSelection().getSelectedUnit());
		forecasting.setTbunit(getTbunitSelection().getTbunit());

		forecasting.setWorkspace(getWorkspace());

		if (forecasting.getUser() == null)
			forecasting.setUser(getUser());
	
		return super.persist();
	}


	/**
	 * @return the adminUnitSelection
	 */
	public AdminUnitSelection getAdminUnitSelection() {
		if (adminUnitSelection == null) {
			adminUnitSelection = new AdminUnitSelection(true);
		}
		return adminUnitSelection;
	}

	/**
	 * @return the tbunitSelection
	 */
	public TBUnitSelection getTbunitSelection() {
		if (tbunitSelection == null)
			tbunitSelection = new TBUnitSelection(true, TBUnitFilter.MEDICINE_WAREHOUSES);
		return tbunitSelection;
	}

	/**
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		this.newName = newName;
	}

	/**
	 * @return the publicView
	 */
	public boolean isPublicView() {
		return publicView;
	}

	/**
	 * @param publicView the publicView to set
	 */
	public void setPublicView(boolean publicView) {
		this.publicView = publicView;
	}

}
