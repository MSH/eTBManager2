package org.msh.tb.adminunits;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.SessionData;

/**
 * Helper class to allow users to select an administrative unit
 * @author Ricardo Memoria
 *
 */
public class AdminUnitSelection {

	private AdministrativeUnit[] units = new AdministrativeUnit[5];
	private String[] labelLevel = new String[5];

	private boolean alreadySelected;
	private List<AdministrativeUnit> unitsList;

	private boolean applyUserRestrictions;
	//	private UserWorkspace userWorkspace;
	private List<AdminUnitChangeListener> listeners;
	private Integer patientAddrRequiredLevels;
	//	private Workspace defaultWorkspace;


	/**
	 * Constructor with default argument
	 * @param applyUserRestrictions if true, the selection will apply the restrictions of user view
	 */
	public AdminUnitSelection(boolean applyUserRestrictions) {
		super();
		this.applyUserRestrictions = applyUserRestrictions;
		if (applyUserRestrictions)
			selectUserRestrictions();
		initializeAddrLevel();
	}

	/**
	 * Default constructor
	 */
	public AdminUnitSelection() {
		super();
	}

	public AdministrativeUnit checkRequired() {
		AdministrativeUnit aux = getSelectedUnit();
		if (aux == null) {
			FacesMessages facesMessages = (FacesMessages)Component.getInstance("facesMessages", true);
			facesMessages.addToControlFromResourceBundle("cbselau1", "javax.faces.component.UIInput.REQUIRED");
		}
		return aux;
	}

	/**
	 * Get the selected unit, i.e., the unit at the end point of the selected tree
	 * @return instance of {@link AdministrativeUnit}
	 */
	public AdministrativeUnit getSelectedUnit() {
		for (int i = 4; i >= 0; i--) {
			if (units[i] != null)
				return units[i];
		}

		return null;
	}


	protected void initializeAddrLevel() {
		Workspace defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace", true);
		patientAddrRequiredLevels = defaultWorkspace.getPatientAddrRequiredLevels(); 		
	}

	/**
	 * Select the unit and fill the nodes with the parent units
	 * @param unit to be selected
	 */
	public void setSelectedUnit(AdministrativeUnit unit) {
		alreadySelected = true;

		// clear the array  
		for (int i=0; i < 5; i++) {
			units[i] = null;
		}

		if (unit == null)
			return;

		// calculates the level of the unit
		AdministrativeUnit aux = unit;
		int level = 1;
		while (aux.getParent() != null) {
			aux = aux.getParent();
			level++;
		}

		aux = unit;
		for (int i = level; i > 0; i--) {
			units[i - 1] = aux;
			aux = aux.getParent();
		}
		notifyChange();
	}


	/**
	 * Return the administrative level of the user
	 * @return 0 if there is no restriction, otherwise checks user level restriction
	 */
	public int getUserLevel() {
		if (!applyUserRestrictions) 
			return 0;

		UserWorkspace userWorkspace = getUserWorkspace();
		if (userWorkspace.getView() == UserView.COUNTRY)
			return 0;

		if (userWorkspace.getView() == UserView.ADMINUNIT) 
			return userWorkspace.getAdminUnit().getLevel();

		if (userWorkspace.getView() == UserView.TBUNIT)
			return userWorkspace.getTbunit().getAdminUnit().getLevel();
		return 0;
	}


	/**
	 * Return the {@link UserLogin} component instance in the SEAM session context
	 * @return {@link UserLogin} instance
	 */
	public UserWorkspace getUserWorkspace() {
		return (UserWorkspace)Component.getInstance("userWorkspace", true);
		/*		if (userWorkspace == null) {
			userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace", true);
		}
		return userWorkspace;
		 */	}

	/**
	 * Return the label for first level of the country administrative organization
	 * @return display name of the administrative unit
	 */
	public String getLabelLevel1() {
		return getLabelLevel(1);
	}


	/**
	 * Return the label for second level of the country administrative organization
	 * @return display name of the administrative unit
	 */
	public String getLabelLevel2() {
		return getLabelLevel(2);
	}


	/**
	 * Return the label for third level of the country administrative organization
	 * @return display name of the administrative unit
	 */
	public String getLabelLevel3() {
		return getLabelLevel(3);
	}


	/**
	 * Return the label for forth level of the country administrative organization
	 * @return display name of the administrative unit
	 */
	public String getLabelLevel4() {
		return getLabelLevel(4);
	}


	/**
	 * Return the label for fifth level of the country administrative organization
	 * @return display name of the administrative unit
	 */
	public String getLabelLevel5() {
		return getLabelLevel(5);
	}



	/**
	 * @return the level1
	 */
	public AdministrativeUnit getUnitLevel1() {
		return units[0];
	}



	/**
	 * @param level1 the level1 to set
	 */
	public void setUnitLevel1(AdministrativeUnit level1) {
		units[0] = level1;
		clearUnits(2);
		notifyChange();
	}


	public Integer getUnitIdLevel1() {
		return (units[0] != null? units[0].getId(): null);
	}

	public void setUnitIdLevel1(Integer id) {
		EntityManager em = getEntityManager();
		units[0] = em.find(AdministrativeUnit.class, id);
	}

	/**
	 * @return the level2
	 */
	public AdministrativeUnit getUnitLevel2() {
		return units[1];
	}


	/**
	 * @param level2 the level2 to set
	 */
	public void setUnitLevel2(AdministrativeUnit level2) {
		units[1] = level2;
		clearUnits(3);
		notifyChange();
	}


	/**
	 * @return the level3
	 */
	public AdministrativeUnit getUnitLevel3() {
		return units[2];
	}


	/**
	 * @param level3 the level3 to set
	 */
	public void setUnitLevel3(AdministrativeUnit level3) {
		units[2] = level3;
		clearUnits(4);
		notifyChange();
	}


	/**
	 * @return the level4
	 */
	public AdministrativeUnit getUnitLevel4() {
		return units[3];
	}


	/**
	 * @param level4 the level4 to set
	 */
	public void setUnitLevel4(AdministrativeUnit level4) {
		units[3] = level4;
		clearUnits(5);
		notifyChange();
	}


	/**
	 * @return the level5
	 */
	public AdministrativeUnit getUnitLevel5() {
		return units[4];
	}


	/**
	 * @param level5 the level5 to set
	 */
	public void setUnitLevel5(AdministrativeUnit level5) {
		units[4] = level5;
		clearUnits(6);
		notifyChange();
	}


	/**
	 * Return the administrative name for a given name of a specific parent administrative unit
	 * @param level
	 * @param parent
	 * @return
	 */
	public String getLabelLevel(int level) {
		// creates the component responsible for generating the list
		AdminUnitSelectionList aulist = getAdminUnitSelectionList();

		// level name is blank ?
		if (labelLevel[level-1] == null) {
			List<AdministrativeUnit> lst = null;
			switch (level) {
			case 1: lst = aulist.getUnitsLevel1();
			break;
			case 2: lst = aulist.getUnitsLevel2();
			break;
			case 3: lst = aulist.getUnitsLevel3();
			break;
			case 4: lst = aulist.getUnitsLevel4();
			break;
			case 5: lst = aulist.getUnitsLevel5();
			break;
			}

			if (lst == null)
				return null;

			List<String> names = new ArrayList<String>();

			for (AdministrativeUnit adm: lst) {
				String s = adm.getCountryStructure().getName().toString();

				if (!names.contains(s))
					names.add(s);
			}

			String txt = "";
			for (String name: names) {
				if (txt.length() > 0)
					txt += " / ";
				txt += name;
			}
			labelLevel[level-1] = txt;
		}


		return labelLevel[level-1];
	}


	protected AdminUnitSelectionList getAdminUnitSelectionList() {
		return ((AdminListWrapper)Component.getInstance("adminListWrapper", true)).getReference(this);
	}


	/**
	 * Return the list of {@link AdministrativeUnit} options for level 1 to be selected by the user
	 * @return List of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getOptionsLevel1() {
		if (isLevelReadOnly(1))
			return null;
		return getAdminUnitSelectionList().getUnitsLevel1();
	}


	/**
	 * Return the list of {@link AdministrativeUnit} options for level 2 to be selected by the user
	 * @return List of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getOptionsLevel2() {
		if (isLevelReadOnly(2))
			return null;
		return getAdminUnitSelectionList().getUnitsLevel2();
	}


	/**
	 * Return the list of {@link AdministrativeUnit} options for level 3 to be selected by the user
	 * @return List of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getOptionsLevel3() {
		if (isLevelReadOnly(3))
			return null;
		return getAdminUnitSelectionList().getUnitsLevel3();
	}


	/**
	 * Return the list of {@link AdministrativeUnit} options for level 4 to be selected by the user
	 * @return List of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getOptionsLevel4() {
		if (isLevelReadOnly(4))
			return null;
		return getAdminUnitSelectionList().getUnitsLevel4();
	}


	/**
	 * Return the list of {@link AdministrativeUnit} options for level 5 to be selected by the user
	 * @return List of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getOptionsLevel5() {
		if (isLevelReadOnly(5))
			return null;
		return getAdminUnitSelectionList().getUnitsLevel5();
	}

	/**
	 * @return the alreadSelected
	 */
	public boolean isAlreadySelected() {
		return alreadySelected;
	}


	/**
	 * Clear the lower level units
	 * @param level
	 */
	protected void clearUnits(int level) {
		for (int i = level-1; i < 5; i++) {
			labelLevel[i] = null;
			units[i] = null;
		}
		unitsList = null;
	}


	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}

	/**
	 * Return the list of parent units and the selected one
	 * @return {@link List} of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getParentUnits() {
		unitsList = new ArrayList<AdministrativeUnit>();
		for (int i = 0; i < 5; i++) {
			if (units[i] == null)
				break;
			unitsList.add(units[i]);
		}
		return unitsList;
	}


	/**
	 * Load the list of administrative units based on the user view
	 */
	protected void mountUserTree() {
		UserWorkspace uw = getUserWorkspace();
		if (uw.getView() == UserView.COUNTRY)
			return;

		AdministrativeUnit adm = null;
		if (uw.getView() == UserView.ADMINUNIT)
			adm = uw.getAdminUnit();

		if (uw.getView() == UserView.TBUNIT)
			adm = uw.getTbunit().getAdminUnit();

		List<AdministrativeUnit> lst = adm.getParentsTreeList(true);
		for (int i = 0; i < 5; i++) {
			if (i < lst.size())
				units[i] = lst.get(i);
			else units[i] = null;
		}
	}


	/**
	 * @param applyUserRestriction the applyUserRestriction to set
	 */
	public void setApplyUserRestrictions(boolean applyUserRestriction) {
		this.applyUserRestrictions = applyUserRestriction;
		if (applyUserRestriction)
			mountUserTree();
	}

	/**
	 * @return the applyUserRestriction
	 */
	public boolean isApplyUserRestrictions() {
		return applyUserRestrictions;
	}


	/**
	 * Check if an administrative level can be changed or is fixed according to user restrictions
	 * @param level from 1 to 5
	 * @return true if administrative unit at the level is fixed and cannot be changed, otherwise returns false 
	 */
	public boolean isLevelReadOnly(int level) {
		int userLevel = getUserLevel();
		return userLevel >= level;
	}


	/**
	 * Check if the administrative unit at level 1 can be changed
	 * @return true if administrative unit at level 1 is fixed and cannot be changed, otherwise returns false
	 */
	public boolean isLevel1ReadOnly() {
		return isLevelReadOnly(1);
	}


	/**
	 * Check if the administrative unit at level 2 can be changed
	 * @return true if administrative unit at level 2 is fixed and cannot be changed, otherwise returns false
	 */
	public boolean isLevel2ReadOnly() {
		return isLevelReadOnly(2);
	}


	/**
	 * Check if the administrative unit at level 3 can be changed
	 * @return true if administrative unit at level 3 is fixed and cannot be changed, otherwise returns false
	 */
	public boolean isLevel3ReadOnly() {
		return isLevelReadOnly(3);
	}


	/**
	 * Check if the administrative unit at level 4 can be changed
	 * @return true if administrative unit at level 4 is fixed and cannot be changed, otherwise returns false
	 */
	public boolean isLevel4ReadOnly() {
		return isLevelReadOnly(4);
	}


	/**
	 * Check if the administrative unit at level 5 can be changed
	 * @return true if administrative unit at level 5 is fixed and cannot be changed, otherwise returns false
	 */
	public boolean isLevel5ReadOnly() {
		return isLevelReadOnly(5);
	}


	/**
	 * Check if the administrative unit in the 1st level is required for the patient address
	 * @return true if is required
	 */
	public boolean isLevel1Required() {
		return ((patientAddrRequiredLevels != null) && (patientAddrRequiredLevels >= 1));
	}


	/**
	 * Check if the administrative unit in the 2nd level is required for the patient address
	 * @return true if is required
	 */
	public boolean isLevel2Required() {
		return ((patientAddrRequiredLevels != null) && (patientAddrRequiredLevels >= 2));
	}


	/**
	 * Check if the administrative unit in the 3rd level is required for the patient address
	 * @return true if is required
	 */
	public boolean isLevel3Required() {
		return ((patientAddrRequiredLevels != null) && (patientAddrRequiredLevels >= 3));
	}


	/**
	 * Check if the administrative unit in the 4th level is required for the patient address
	 * @return true if is required
	 */
	public boolean isLevel4Required() {
		return ((patientAddrRequiredLevels != null) && (patientAddrRequiredLevels >= 4));
	}


	/**
	 * Check if the administrative unit in the 5th level is required for the patient address
	 * @return true if is required
	 */
	public boolean isLevel5Required() {
		return ((patientAddrRequiredLevels != null) && (patientAddrRequiredLevels >= 5));
	}

	/**
	 * Include a listener to receive notification about selection changing
	 * @param listener
	 */
	public void addListener(AdminUnitChangeListener listener) {
		if (listeners == null)
			listeners = new ArrayList<AdminUnitChangeListener>();
		listeners.add(listener);
	}


	/**
	 * Remove a notification changing listener
	 * @param listener
	 */
	public void remListener(AdminUnitChangeListener listener) {
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

		for (AdminUnitChangeListener lst: listeners)
			lst.notifyAdminUnitChange(this);
	}


	/**
	 * Return the id of the selected administrative unit
	 * @return id in {@link Integer} format
	 */
	public Integer getSelectedUnitId() {
		AdministrativeUnit adm = getSelectedUnit();
		if (adm == null)
			return null;
		else return adm.getId();
	}


	/**
	 * Set the id of the selected administrative unit
	 * @param id
	 */
	public void setSelectedUnitId(Integer id) {
		//AK 20131111 set the admin unit id on the session level
			SessionData.instance().setValue("uaid", id);
		if (id == null)
			setSelectedUnit(null);
		else {
			EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
			setSelectedUnit(em.find(AdministrativeUnit.class, id));
		}
	}


	/**
	 * Select units based on user restrictions  
	 */
	protected void selectUserRestrictions() {
		if (!applyUserRestrictions)
			return;

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");

		if (userWorkspace == null)
			return;

		UserView view = userWorkspace.getView();
		if (view == UserView.COUNTRY)
			return;

		if (view == UserView.ADMINUNIT)
			setSelectedUnitId(userWorkspace.getAdminUnit() != null? userWorkspace.getAdminUnit().getId(): null);
		else setSelectedUnitId(userWorkspace.getTbunit().getAdminUnit().getId());
	}



	public String getSelectedUnitCodeLike() {
		AdministrativeUnit adm = getSelectedUnit();
		if (adm == null)
			return null;
		else return adm.getCode() + "%";
	}

	/**
	 * @return the patientAddrRequiredLevels
	 */
	public Integer getPatientAddrRequiredLevels() {
		return patientAddrRequiredLevels;
	}
}
