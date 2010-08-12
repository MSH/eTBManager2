package org.msh.tb.test;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.User;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.enums.DispensingFrequency;
import org.msh.mdrtb.entities.enums.UserState;
import org.msh.mdrtb.entities.enums.UserView;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.login.UserHome;
import org.msh.tb.tbunits.TbUnitHome;

@Name("trainningHome")
public class TrainningHome {

	@In EntityManager entityManager;
	@In(create=true) TbUnitHome tbunitHome;
	@In(create=true) UserHome userHome;
	//@In(create=true) AdminUnitSelectionList adminUnitSelectionList;
	
	@Transactional
	public void createUsers() {

		AdminUnitSelection auselection = new AdminUnitSelection();
		
		// adapted to Ukraine
		Tbunit centralWarehouse = entityManager.find(Tbunit.class, 940960);
		UserProfile profile = entityManager.find(UserProfile.class, 940425);

		
		int i = 1;
		for (AdministrativeUnit adm: auselection.getOptionsLevel1()) {
			Tbunit ntp = createUnit("NTP " + i, null, null, false, false, null, true, adm);
			Tbunit chd = createUnit("RAYON WAREHOUSE " + i, centralWarehouse, ntp, false, true, 360, false, adm);
			
//			Tbunit tdf = createUnit("TDF" + i, null, null, false, false, null, false, adm);
			Tbunit tc = createUnit("HEALTH UNIT " + i, chd, ntp, false, true, 120, false, adm);
//			createUnit("TREATMENT SITE " + i, tc, tdf, false, true, 120);
			
			userHome.setTransactionLogActive(false);
			userHome.clearInstance();
			User user = userHome.getInstance();
			user.setEmail("rmemoria@gmail.com");
			user.setLogin("USER" + i);
			userHome.setPassword("user" + i);
			user.setName("Training user " + i);
			user.setState(UserState.ACTIVE);
			userHome.getTbunitselection().setTbunit(tc);
			userHome.getUserWorkspace().setPlayOtherUnits(true);
			userHome.getUserWorkspace().setProfile(profile);
//			userHome.getUserWorkspace().setTbunit(tc);
			userHome.getUserWorkspace().setView(UserView.ADMINUNIT);
			userHome.setSelectedView('A' + adm.getId().toString());
			userHome.getUserWorkspace().setAdminUnit(adm);
			
			userHome.persist();
			
			String password = (String)Contexts.getEventContext().get("password");
			System.out.println("User=" + user.getLogin() + "  PASSWORD=" + password);
			i++;
			if (i == 16)
				break;
		}
		
		entityManager.flush();
	}
	
	public Tbunit createUnit(String name, Tbunit supplier, Tbunit authorizerUnit, boolean healthUnit, 
				boolean medicineSupplier, Integer numDaysOrder, boolean medReceiving, AdministrativeUnit region) {
		
		AdministrativeUnit local = loadChildAdminUnit(region);
		HealthSystem hs = entityManager.find(HealthSystem.class, 919);
		
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
		
		tbunitHome.getAusel().setSelectedUnit(local);
		tbunitHome.getFlmSupplier().setTbunit(supplier);
		tbunitHome.getSlmSupplier().setTbunit(supplier);
		tbunitHome.getOrderAuthorizer().setTbunit(authorizerUnit);

		tbunitHome.setTransactionLogActive(false);
		tbunitHome.persist();
		
		return unit;
	}
	
	protected AdministrativeUnit loadChildAdminUnit(AdministrativeUnit parent) {
		return (AdministrativeUnit) entityManager.createQuery("from AdministrativeUnit a " +
				"where a.id = (select max(aux.id) from AdministrativeUnit aux where aux.parent.id = :id)")
				.setParameter("id", parent.getId())
				.getSingleResult();
	}
}
