package org.msh.tb.tbunits;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.Component;
import org.msh.tb.adminunits.AdminUnitChangeListener;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;


/**
 * Wrapper class to help selection of TB units
 * @author Ricardo Memoria
 *
 */
public class TBUnitSelection {
	
	private UIComponent component;

	public TBUnitSelection(boolean applyUserRestrictions, TBUnitFilter filter) {
		super();
		setApplyUserRestrictions(applyUserRestrictions);
		setApplyHealthSystemRestrictions(true);
		this.filter = filter;
		ignoreReadOnlyRule = false;
	}
	
	public TBUnitSelection() {
		ignoreReadOnlyRule = false;
	}


	protected AdminUnitSelection auselection;
	private Tbunit tbunit;
	
	private boolean applyUserRestrictions;
	protected TBUnitFilter filter;
	private HealthSystem healthSystem;
	protected boolean applyHealthSystemRestrictions;
	protected boolean ignoreReadOnlyRule;
	
	protected TbunitSelectionList options;

	private List<TbunitChangeListener> listeners;
	
	/**
	 * Return the TB Unit selected by the user
	 * @return instance of {@link Tbunit} class
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}

	
	/**
	 * Change the TB Unit selected
	 * @param unit to be selected
	 */
	public void setTbunit(Tbunit unit) {
		if (this.tbunit == unit)
			return;
		
		if (unit != null) 
			getAuselection().setSelectedUnit( unit.getAdminUnit().getParentLevel1() );
		this.tbunit = unit;
		notifyChange();
	}
	
	/**
	 * Change the TB Unit selected
	 * @param unit to be selected
	 */
	public void setTbunitWithOptions(Tbunit unit) {
		if (this.tbunit == unit)
			return;
		
		if (unit != null) {
			getAuselection().setSelectedUnit( unit.getAdminUnit().getParentLevel1() );
			options = null;
			getOptions();
		}
		this.tbunit = unit;
		notifyChange();
	}

	
	/**
	 * Checks if administrative unit level 1 is read only
	 * @return true if is read only, otherwise return false
	 */
	public boolean isLevel1ReadOnly() {
		return getAuselection().isLevel1ReadOnly();
	}

	/**
	 * Return the list of user administrative units realted to the user restrictions
	 * @return list of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getParentUnits() {
		return getAuselection().getParentUnits();
	}

	
	/**
	 * Notify about selection changing 
	 */
	protected void notifyChange() {
		if (listeners == null)
			return;

		for (TbunitChangeListener lst: listeners)
			lst.notifyTbunitChange(this);
	}


	/**
	 * Return the {@link Tbunit} list from the administrative unit selected
	 * @return List of {@link Tbunit} instances
	 */
	public List<Tbunit> getOptions() {
		if (options == null) {
			options = new TbunitSelectionList();
			options.setApplyHealthSystemRestrictions(applyHealthSystemRestrictions);
			options.setRestriction(createHQLUnitFilter());
			options.setAdminUnit(getAuselection().getSelectedUnit());
		}
		return options.getUnits();
	}


	/**
	 * Create HQL filter condition for TB Unit options
	 * @return HQL instruction
	 */
	protected String createHQLUnitFilter() {
		if (filter == null)
			return null;
		
		switch (filter) {
			case NOTIFICATION_UNITS: return "u.notifHealthUnit = true";
			case HEALTH_UNITS: return "u.treatmentHealthUnit = true";
			case MDRHEALTH_UNITS: return "u.mdrHealthUnit = true";
			case TBHEALTH_UNITS: return "u.tbHealthUnit = true";
			case MEDICINE_ORDER_UNITS: return "u.treatmentHealthUnit = true and (u.firstLineSupplier != null or u.secondLineSupplier != null)";
			case MEDICINE_SUPPLIERS: return "u.medicineSupplier = true";
			case MEDICINE_WAREHOUSES: return "u.medicineStorage = true and (u.medManStartDate is not null)";
		}
		return null;
	}


	/**
	 * Include a listener to receive notification about selection changing
	 * @param listener
	 */
	public void addListener(TbunitChangeListener listener) {
		if (listeners == null)
			listeners = new ArrayList<TbunitChangeListener>();
		listeners.add(listener);
	}


	/**
	 * Remove a notification changing listener
	 * @param listener
	 */
	public void remListener(TbunitChangeListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}


	public String getLabelAdminUnit() {
		return getAuselection().getLabelLevel1();
	}


	public List<AdministrativeUnit> getAdminUnits() {
		return getAuselection().getOptionsLevel1();
	}
	
	public AdministrativeUnit getAdminUnit() {
		return getAuselection().getUnitLevel1();
	}
	
	public void setAdminUnit(AdministrativeUnit admin) {
		getAuselection().setUnitLevel1(admin);
		tbunit = null;
		options = null;

		if (component != null) {
			String s = component.getClientId(FacesContext.getCurrentInstance());
			System.out.println(s);
		}
	}

	
	protected void applyUserTBUnitRestriction() {
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if ((userWorkspace != null) && (userWorkspace.getView() == UserView.TBUNIT))
			setTbunit(userWorkspace.getTbunit());
	}

	/**
	 * @param applyUserRestrictions the applyUserRestrictions to set
	 */
	public void setApplyUserRestrictions(boolean applyUserRestrictions) {
		this.applyUserRestrictions = applyUserRestrictions;
		if (auselection != null)
			auselection.setApplyUserRestrictions(applyUserRestrictions);

		if (applyUserRestrictions)
			applyUserTBUnitRestriction();
	}


	/**
	 * @return the applyUserRestrictions
	 */
	public boolean isApplyUserRestrictions() {
		return applyUserRestrictions;
	}


	/**
	 * @param filter the filter to set
	 */
	public void setFilter(TBUnitFilter filter) {
		this.filter = filter;
	}


	/**
	 * @return the filter
	 */
	public TBUnitFilter getFilter() {
		return filter;
	}

	/**
	 * @return the auselection
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			createAuselection();
		return auselection;
	}
	
	protected void createAuselection() {
		auselection = new AdminUnitSelection(applyUserRestrictions);

		auselection.addListener(new AdminUnitChangeListener() {
			public void notifyAdminUnitChange(AdminUnitSelection auselection) {
				setTbunit(null);
			}
		});
	}
	
	public boolean isReadOnly() {
		if(ignoreReadOnlyRule)
			return false;
		
		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		return (userWorkspace.getView() == UserView.TBUNIT);
	}

	/**
	 * @return the healthSystem
	 */
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}

	/**
	 * @param healthSystem the healthSystem to set
	 */
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}

	/**
	 * @return the applyHealthSystemRestrictions
	 */
	public boolean isApplyHealthSystemRestrictions() {
		return applyHealthSystemRestrictions;
	}

	/**
	 * @return the ignoreReadOnlyRule
	 */
	public boolean isIgnoreReadOnlyRule() {
		return ignoreReadOnlyRule;
	}

	/**
	 * @param ignoreReadOnlyRule the ignoreReadOnlyRule to set
	 */
	public void setIgnoreReadOnlyRule(boolean ignoreReadOnlyRule) {
		this.ignoreReadOnlyRule = ignoreReadOnlyRule;
	}

	/**
	 * @param applyHealthSystemRestrictions the applyHealthSystemRestrictions to set
	 */
	public void setApplyHealthSystemRestrictions(
			boolean applyHealthSystemRestrictions) {
		this.applyHealthSystemRestrictions = applyHealthSystemRestrictions;
	}
	
	/**
	 * @return the component
	 */
	public UIComponent getComponent() {
		return null;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(UIComponent component) {
		this.component = component;
	}
	
	public void changeListener(ValueChangeEvent evt) {
		System.out.println(evt.getComponent().getId());
	}
}
