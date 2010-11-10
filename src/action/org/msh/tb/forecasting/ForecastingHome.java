package org.msh.tb.forecasting;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.mdrtb.entities.Forecasting;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
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
		}
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

}
