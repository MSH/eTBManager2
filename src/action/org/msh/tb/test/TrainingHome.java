package org.msh.tb.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.enums.DispensingFrequency;
import org.msh.mdrtb.entities.enums.UserState;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.tb.adminunits.AdminUnitHome;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.login.UserHome;
import org.msh.tb.tbunits.TbUnitHome;
import org.msh.utils.date.DateUtils;

@Name("trainingHome")
public class TrainingHome {

	@In EntityManager entityManager;
	@In(create=true) TbUnitHome tbunitHome;
	@In(create=true) UserHome userHome;
	
	private int numberOfUsers = 10;
	private Tbunit centralLevel;
	private UserProfile profile;
	
	private String userPrefix = "USER";
	private String regionPrefix = "REGION";
	private String ntpPrefix = "NTP";
	private String warehousePrefix = "REGIONAL WAREHOUSE";
	private String healthUnitPrefix = "HEALTH UNIT";
	
	private List<SelectItem> numbers;
	
	private AdminUnitSelection auselection = new AdminUnitSelection();
	
	@Transactional
	public void createUsers() {
		// adapted to Ukraine
		int i = 1;
		while (i <= numberOfUsers) {
			AdministrativeUnit adm = getAdminUnit(i);

			Date dt = DateUtils.getDate();
			Tbunit ntp = createUnit(ntpPrefix + " " + i, null, null, false, false, null, true, adm, DateUtils.incYears(dt, -1));
			Tbunit chd = createUnit(warehousePrefix + " " + i, centralLevel, ntp, false, true, 360, false, adm, null);
			Tbunit tc = createUnit(healthUnitPrefix + " " + i, chd, ntp, true, true, 120, false, adm, null);
			
			userHome.setTransactionLogActive(false);
			userHome.clearInstance();
			User user = userHome.getInstance();
			user.setEmail("rmemoria@gmail.com");
			user.setLogin(userPrefix + i);
			userHome.setPassword(userPrefix.toLowerCase() + i);
			user.setName("Training user " + i);
			user.setState(UserState.ACTIVE);
			userHome.getTbunitselection().setTbunit(tc);
			userHome.getUserWorkspace().setPlayOtherUnits(true);
			userHome.getUserWorkspace().setProfile(profile);
//			userHome.getUserWorkspace().setTbunit(tc);
			userHome.getUserWorkspace().setView(UserView.ADMINUNIT);
			userHome.setSelectedView('A' + adm.getId().toString());
			userHome.getUserWorkspace().setAdminUnit(adm);
			
			userHome.setSendEmail(false);
			userHome.setDisplayMessage(false);
			userHome.setTransactionLogActive(false);
			userHome.persist();
			
			String password = (String)Contexts.getEventContext().get("password");
			System.out.println("User=" + user.getLogin() + "  PASSWORD=" + password);
			i++;
			
		}
		
		entityManager.flush();
		
		FacesMessages.instance().add("Users created");
	}


	public AdministrativeUnit getAdminUnit(int index) {
		List<AdministrativeUnit> lst = auselection.getOptionsLevel1();
		if (index < lst.size())
			return lst.get(index);
		
		AdminUnitHome home = (AdminUnitHome)Component.getInstance("adminUnitHome", true);
		home.clearInstance();
		AdministrativeUnit adm = home.getInstance();
		adm.getName().setName1(regionPrefix + " " + Integer.toString(index));
		adm.setCountryStructure(home.getStructures().get(0));
		
		home.setDisplayMessage(false);
		home.setTransactionLogActive(false);
		home.persist();
		return adm;
	}
	
	
	public Tbunit createUnit(String name, Tbunit supplier, Tbunit authorizerUnit, boolean healthUnit, 
				boolean medicineSupplier, Integer numDaysOrder, boolean medReceiving, AdministrativeUnit region, Date dtIniMedicine) {
		
		AdministrativeUnit local = region; //loadChildAdminUnit(region);
		HealthSystem hs = centralLevel.getHealthSystem();
		if (hs != null)
			hs = entityManager.find(HealthSystem.class, hs.getId());
		
		tbunitHome.clearInstance();
		Tbunit unit = tbunitHome.getInstance();
		unit.setAddress("XPTO street");
		unit.setAuthorizerUnit(authorizerUnit);
		unit.setChangeEstimatedQuantity(true);
		unit.setDispensingFrequency(DispensingFrequency.MONTHLY);
		unit.setDistrict("Atlantic");
		unit.setFirstLineSupplier(supplier);
		unit.setSecondLineSupplier(supplier);
		unit.setMedicineStorage(true);
		unit.setMedicineSupplier(medicineSupplier);
		unit.getName().setName1(name);
		unit.setNumDaysOrder(numDaysOrder);
		unit.setOrderOverMinimum(true);
		unit.setReceivingFromSource(medReceiving);
		unit.setTreatmentHealthUnit(healthUnit);
		unit.setAdminUnit(local);
		unit.setHealthSystem(hs);
		unit.setActive(true);
		unit.setMedManStartDate(dtIniMedicine);
		
		tbunitHome.getAusel().setSelectedUnit(local);
		tbunitHome.getFlmSupplier().setTbunit(supplier);
		tbunitHome.getSlmSupplier().setTbunit(supplier);
		tbunitHome.getOrderAuthorizer().setTbunit(authorizerUnit);

		tbunitHome.setTransactionLogActive(false);
		tbunitHome.setDisplayMessage(false);
		tbunitHome.persist();
		
		return unit;
	}
	
	protected AdministrativeUnit loadChildAdminUnit(AdministrativeUnit parent) {
		return (AdministrativeUnit) entityManager.createQuery("from AdministrativeUnit a " +
				"where a.id = (select max(aux.id) from AdministrativeUnit aux where aux.parent.id = :id)")
				.setParameter("id", parent.getId())
				.getSingleResult();
	}

	/**
	 * @return the numberOfUsers
	 */
	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	/**
	 * @param numberOfUsers the numberOfUsers to set
	 */
	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}


	/**
	 * @return the centralLevel
	 */
	public Tbunit getCentralLevel() {
		return centralLevel;
	}


	/**
	 * @param centralLevel the centralLevel to set
	 */
	public void setCentralLevel(Tbunit centralLevel) {
		this.centralLevel = centralLevel;
	}


	/**
	 * @return the profile
	 */
	public UserProfile getProfile() {
		return profile;
	}


	/**
	 * @param profile the profile to set
	 */
	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}
	
	
	public List<SelectItem> getNumbers() {
		if (numbers == null) {
			numbers = new ArrayList<SelectItem>();
			for (int i = 1; i <= 30; i++)
				numbers.add(new SelectItem(i, Integer.toString(i)));
		}
		return numbers;
	}


	/**
	 * @return the regionPrefix
	 */
	public String getRegionPrefix() {
		return regionPrefix;
	}


	/**
	 * @param regionPrefix the regionPrefix to set
	 */
	public void setRegionPrefix(String regionPrefix) {
		this.regionPrefix = regionPrefix;
	}


	/**
	 * @return the ntpPrefix
	 */
	public String getNtpPrefix() {
		return ntpPrefix;
	}


	/**
	 * @param ntpPrefix the ntpPrefix to set
	 */
	public void setNtpPrefix(String ntpPrefix) {
		this.ntpPrefix = ntpPrefix;
	}


	/**
	 * @return the warehousePrefix
	 */
	public String getWarehousePrefix() {
		return warehousePrefix;
	}


	/**
	 * @param warehousePrefix the warehousePrefix to set
	 */
	public void setWarehousePrefix(String warehousePrefix) {
		this.warehousePrefix = warehousePrefix;
	}


	/**
	 * @return the healthUnitPrefix
	 */
	public String getHealthUnitPrefix() {
		return healthUnitPrefix;
	}


	/**
	 * @param healthUnitPrefix the healthUnitPrefix to set
	 */
	public void setHealthUnitPrefix(String healthUnitPrefix) {
		this.healthUnitPrefix = healthUnitPrefix;
	}


	/**
	 * @return the userPrefix
	 */
	public String getUserPrefix() {
		return userPrefix;
	}


	/**
	 * @param userPrefix the userPrefix to set
	 */
	public void setUserPrefix(String userPrefix) {
		this.userPrefix = userPrefix;
	}
}
