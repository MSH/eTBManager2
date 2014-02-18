/**
 * 
 */
package org.msh.tb.adminunits;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.msh.tb.application.App;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.login.SessionData;

/**
 * Base class used by laboratory and TB unit selection where the administrative
 * unit must be selected first
 * 
 * @author Ricardo Memoria
 *
 */
public class AdminUnitSelector<E> {
	
	protected AdminUnitSelection auselection;
	private boolean applyUserRestrictions;
	private boolean applyHealthSystemRestrictions;
	private List<ValueChangeListener> listeners;
	private String clientId;
//	private Integer adminUnitId;

	// store the selected item
	private E selected;
	
	
	/**
	 * Default constructor
	 * @param clientId is the ID to be used in the HTML page of the select drop down of administrative units
	 */
	public AdminUnitSelector(String clientId) {
		super();
		this.clientId = clientId;
		initialize();
	}
	
	
	/**
	 * Constructor with extra parameter
	 * @param clientId
	 * @param applyUserRestrictions
	 */
	public AdminUnitSelector(String clientId, boolean applyUserRestrictions) {
		super();
		this.clientId = clientId;
		this.applyUserRestrictions = applyUserRestrictions;
		initialize();
	}
	

	/**
	 * Initialize the content of the selection box
	 */
	protected void initialize() {
		AdministrativeUnit adminunit = getAuselection().getUnitLevel1();
		if (adminunit != null)
			setAdminUnit(adminunit);
	}
	
	/**
	 * Return true if the administrative unit is selected
	 * @return boolean value
	 */
	public boolean isAdminUnitSelected() {
		return getAdminUnitId() != null;
	}

	
	/**
	 * Return the administrative unit ID selected
	 * @return Integer value
	 */
	public Integer getAdminUnitId() {
		return (Integer)SessionData.instance().getValue(clientId);
	}
	

	/**
	 * Return the label to be displayed of the administrative unit field
	 * @return String value
	 */
	public String getLabelAdminUnit() {
		return getAuselection().getLabelLevel1();
	}


	/**
	 * Return the list of administrative unit options
	 * @return {@link List} of {@link SelectItem} objects
	 */
	public List<SelectItem> getAdminUnitOptions() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		lst.add(new SelectItem(null, "-"));
		for (AdministrativeUnit au: getAdminUnits()) {
			lst.add(new SelectItem(au.getId(), au.getName().toString()));
		}
		return lst;
	}

	/**
	 * Include a listener to receive notification about selection changing
	 * @param listener
	 */
	public void addListener(ValueChangeListener listener) {
		if (listeners == null)
			listeners = new ArrayList<ValueChangeListener>();
		listeners.add(listener);
	}


	/**
	 * Remove a notification changing listener
	 * @param listener
	 */
	public void remListener(ValueChangeListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}

	
	/**
	 * Notify about selection changing 
	 */
	protected void notifyChange() {
		if (listeners == null)
			return;

		for (ValueChangeListener lst: listeners)
			lst.notifyChange(this);
	}

	
	/**
	 * Render the HTML code to display the select drop down menu
	 * @return String value
	 */
	public String getHtmlSelectAdminUnit() {
		StringBuilder builder = new StringBuilder();
		builder.append("<select id='");
		builder.append(clientId);
		builder.append("' onchange='adminUnitChanged(this);'>");

		Integer adminUnitId = getAdminUnitId();

		for (SelectItem item: getAdminUnitOptions()) {
			Object val = item.getValue(); 
			boolean selected = ((adminUnitId == val) || ((adminUnitId != null) && (adminUnitId.equals(val))));
			addOption(builder, val != null? val.toString(): null, item.getLabel(), selected);
		}
		
		builder.append("</select>");
		
		return builder.toString();
	}
	
	/**
	 * Render an option of a select tag
	 * @param builder the instance of {@link StringBuilder} to render option to
	 * @param value the value to render
	 * @param label
	 * @return
	 */
	private void addOption(StringBuilder builder, String value, String label, boolean selected) {
		builder.append("<option");
		if (value != null) {
			builder.append(" value='");
			builder.append(value);
			builder.append("'");
		}
		if (selected) {
			builder.append(" selected='selected'");
		}
		builder.append('>');
		builder.append(label);
		builder.append("</option>");
	}
	
	
	/**
	 * Return the list of administrative units available for selection
	 * @return list of {@link AdministrativeUnit} objects
	 */
	public List<AdministrativeUnit> getAdminUnits() {
		return getAuselection().getOptionsLevel1();
	}
	
	/**
	 * Return the selected administrative unit
	 * @return instance of {@link AdministrativeUnit}
	 */
	public AdministrativeUnit getAdminUnit() {
		// it's not using the basic selection
		if ((auselection == null) || (auselection.getUnitLevel1() == null)) {
			// try to get it from the request body
			Integer id = getAdminUnitId();
			if (id == null)
				return null;
			return App.getEntityManager().find(AdministrativeUnit.class, id);
		}
		
		return getAuselection().getUnitLevel1();
	}
	
	/**
	 * Change the selected administrative unit
	 * @param admin instance of {@link AdministrativeUnit}
	 */
	public void setAdminUnit(AdministrativeUnit admin) {
		getAuselection().setUnitLevel1(admin);
		if (admin == null)
			 setAdminUnitId(null);
		else setAdminUnitId(admin.getId());
		selected = null;
	}


	/**
	 * Provide an easier way to include the selected administrative unit in a SQL/HQL
	 * LIKE declaration to select all units under the given unit
	 * @return
	 */
	public String getAdminUnitCodeLike() {
		AdministrativeUnit adm = getAdminUnit();
		if (adm == null)
			 return null;
		else return adm.getCode() + "%";
	}

	/**
	 * @param auselection the auselection to set
	 */
	public void setAuselection(AdminUnitSelection auselection) {
		this.auselection = auselection;
	}


	/**
	 * Checks if administrative unit level 1 is read only
	 * @return true if is read only, otherwise return false
	 */
	public boolean isLevel1ReadOnly() {
		return getAuselection().isLevel1ReadOnly();
	}

	/**
	 * @return the auselection
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			createAuselection();
		return auselection;
	}
	
	/**
	 * Create the object {@link AdminUnitSelection} that will be used by the UI
	 * to select the administrative unit
	 */
	protected void createAuselection() {
		auselection = new AdminUnitSelection(applyUserRestrictions);
	}

	/**
	 * @return the applyUserRestrictions
	 */
	public boolean isApplyUserRestrictions() {
		return applyUserRestrictions;
	}

	/**
	 * @param applyUserRestrictions the applyUserRestrictions to set
	 */
	public void setApplyUserRestrictions(boolean applyUserRestrictions) {
		this.applyUserRestrictions = applyUserRestrictions;
		if (auselection != null)
			auselection.setApplyUserRestrictions(applyUserRestrictions);
		initialize();
	}

	/**
	 * @return the selected
	 */
	public E getSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(E selected) {
		this.selected = selected;
	}
	
	/**
	 * @param selected the selected to set
	 */
	public void setSelected2(E selected) {
		this.selected = selected;
	}

	/**
	 * @return the applyHealthSystemRestrictions
	 */
	public boolean isApplyHealthSystemRestrictions() {
		return applyHealthSystemRestrictions;
	}

	/**
	 * @param applyHealthSystemRestrictions the applyHealthSystemRestrictions to set
	 */
	public void setApplyHealthSystemRestrictions(
			boolean applyHealthSystemRestrictions) {
		this.applyHealthSystemRestrictions = applyHealthSystemRestrictions;
	}
	
	
	/**
	 * Interface to notify about changes in the selection
	 * @author Ricardo Memoria
	 *
	 */
	public interface ValueChangeListener {
		void notifyChange(AdminUnitSelector selector);
	}


	/**
	 * @param adminUnitId the adminUnitId to set
	 */
	public void setAdminUnitId(Integer adminUnitId) {
		SessionData.instance().setValue(clientId, adminUnitId);
	}

    /**
     * Return the default administrative unit. If the administrative unit is null,
     * it returns the administrative unit of the user
     */
    public AdministrativeUnit getAdminUnitDefault() {
    	if (getAdminUnit() == null) {
    		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace",true);
            if (userWorkspace != null)
            	setAdminUnit(userWorkspace.getTbunit().getAdminUnit().getParentLevel1());
        }
        return getAdminUnit();
    }
   
    /**
     * Set the default administrative unit 
     * @param admin instance of {@link AdministrativeUnit}
     */
    public void setAdminUnitDefault(AdministrativeUnit admin) {
        setAdminUnit(admin);
    }
    
    /**
     * Return the ID of the default administrative unit
     * @return Integer value
     */
    public Integer getAdminUnitDefaultId() {
    	AdministrativeUnit adminUnit = getAdminUnitDefault();
    	return adminUnit != null? adminUnit.getId(): null;
    }
    
    /**
     * Set the id of the selected administrative unit
     * @param id is the primary key of the administrative unit
     */
    public void setAdminUnitDefaultId(Integer id) {
    	setAdminUnitId(id);
    }
}
