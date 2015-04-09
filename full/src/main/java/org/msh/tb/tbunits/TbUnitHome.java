package org.msh.tb.tbunits;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.etbm.transactionlog.mapping.LogInfo;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;



@Name("tbunitHome")
@LogInfo(roleName="TBUNITS", entityClass=Tbunit.class)
public class TbUnitHome extends EntityHomeEx<Tbunit> {
	private static final long serialVersionUID = 2457043551352981859L;
	
	private AdminUnitSelection ausel = new AdminUnitSelection();
	
	private TBUnitSelection slmSupplier = new TBUnitSelection("unitslm", false, TBUnitType.MEDICINE_SUPPLIERS);
	private TBUnitSelection flmSupplier = new TBUnitSelection("unitflm", false, TBUnitType.MEDICINE_SUPPLIERS);
	private TBUnitSelection orderAuthorizer = new TBUnitSelection("unitaut");

	@In(create=true) FacesMessages facesMessages;
	
	@Factory("tbunit")
	public Tbunit getTbunit() {
		return getInstance();
	}

	@Override
	public String persist() {
		getTbunit().getName().toUpper();
		Tbunit unit = getInstance();

		unit.setBatchControl(true);

		AdministrativeUnit au = getAusel().getSelectedUnit();
		if (au != null)
			unit.setAdminUnit(au);
		
		// check if it's not a medicine storage unit
		if (!unit.isMedicineStorage()) {
			unit.setChangeEstimatedQuantity(false);
			unit.setNumDaysOrder(null);
			unit.setReceivingFromSource(false);
			unit.setMedicineSupplier(false);
			unit.setFirstLineSupplier(null);
			unit.setSecondLineSupplier(null);
		}
		else {
			unit.setFirstLineSupplier(flmSupplier.getSelected());
			unit.setSecondLineSupplier(slmSupplier.getSelected());
			unit.setAuthorizerUnit(orderAuthorizer.getSelected());
		}
		
		if (!unit.isMedicineSupplier())
			unit.setAuthorizerUnit(null);
		
		if ((unit.getFirstLineSupplier() == null) && (unit.getSecondLineSupplier() == null))
			unit.setNumDaysOrder(null);
		
		return super.persist();
	}


	@Override
	public String remove() {
		if (getUserLogin().getUser().getDefaultWorkspace().getTbunit().getId().equals(getInstance().getId())) {
			facesMessages.addFromResourceBundle("admin.tbunits.remuserunit");
			return "error";
		}
		
		return super.remove();
	}


	@Override
	public UnitsQuery getEntityQuery() {
		return (UnitsQuery)Component.getInstance("unitspg", false);
	}


	/**
	 * @return the ausel
	 */
	public AdminUnitSelection getAusel() {
		return ausel;
	}

	public void initalizeEditing() {
		if (!getAusel().isAlreadySelected()) {
			AdministrativeUnit aux = getInstance().getAdminUnit();
			getAusel().setSelectedUnit(aux);
		}
	}

	/**
	 * @return the slmSupplier
	 */
	public TBUnitSelection getSlmSupplier() {
		return slmSupplier;
	}

	/**
	 * @return the flmSupplier
	 */
	public TBUnitSelection getFlmSupplier() {
		return flmSupplier;
	}

	/**
	 * @return the orderAuthorizer
	 */
	public TBUnitSelection getOrderAuthorizer() {
		return orderAuthorizer;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Home#setId(java.lang.Object)
	 */
	@Override
	public void setId(Object id) {
		super.setId(id);
		
		if (isManaged())
			initializeEditing();
	}
	
	public void initializeEditing() {
		Tbunit unit = getInstance();
		getFlmSupplier().setSelected(unit.getFirstLineSupplier());
		getSlmSupplier().setSelected(unit.getSecondLineSupplier());
		getOrderAuthorizer().setSelected(unit.getAuthorizerUnit());
	}
}
