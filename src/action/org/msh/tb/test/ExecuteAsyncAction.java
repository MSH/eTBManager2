package org.msh.tb.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;

@Name("executeAsyncAction")
public class ExecuteAsyncAction {

	@In(create=true) AdjustDaysPlannedAction adjustDaysPlannedAction;
	@In(create=true) AdjustAdminUnits adjustAdminUnits;
	@In(create=true) EntityManager entityManager;

	private Random random;
	private UserTransaction tx;
	private Map<Regimen, Integer> regmap;

	/**
	 * Initialize the field {@link TbCase}.numDaysPlanned
	 */
	@Asynchronous
	public void adjustDaysPlanned(Workspace ws) {
		int ini = 0;
		int max = 1;
		
		while (true) {
			if (!adjustDaysPlannedAction.adjust(ws, ini, max))
				break;
			ini += max;
		}
	}

	
	/**
	 * Adjust administrative unit codes
	 * @throws Exception 
	 */
	public void adjustAdminUnitsCode() throws Exception {
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");
		adjustAdminUnits.execute(workspace);
	}


	@Asynchronous
	public void updateRegimens(Workspace workspace) throws Exception {
		Contexts.getEventContext().set("defaultWorkspace", workspace);
		
		tx = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		tx.begin();
		try {
			entityManager.joinTransaction();
			List<Regimen> regs = entityManager.createQuery("from Regimen r where r.workspace.id = #{defaultWorkspace.id} order by r.name")
				.getResultList();
		
			random = new Random();
			regmap = new HashMap<Regimen, Integer>();
			int max = 70;
			for (Regimen reg: regs) {
				regmap.put(reg, max);
				max -= 10;
				if (max < 10)
					max = 10;
			}

			entityManager.createQuery("delete from PrescribedMedicine p where p.tbcase.id in (select aux.id from TbCase aux where aux.patient.workspace.id = #{defaultWorkspace.id})")
				.executeUpdate();

			entityManager.createQuery("delete from TreatmentHealthUnit p where p.tbcase.id in (select aux.id from TbCase aux where aux.patient.workspace.id = #{defaultWorkspace.id})")
				.executeUpdate();
			
			List<TbCase> cases = entityManager
				.createQuery("from TbCase c join fetch c.patient p where p.workspace.id = #{defaultWorkspace.id} and c.treatmentPeriod.iniDate is not null")
				.getResultList();
			
			for (TbCase tbcase: cases) {
				if (!tx.isActive())
					tx.begin();
				
				startRegimen(tbcase);
				
				entityManager.clear();
				tx.commit();
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
		}

	}

	protected void startRegimen(TbCase tbcase) {
		Regimen reg = newValue(regmap);
		System.out.println(tbcase.getPatient().getFullName() + "=" + reg.toString());
		
		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome", true);
		StartTreatmentHome startTreatHome = (StartTreatmentHome)Component.getInstance("startTreatmentHome", true);
		
		caseHome.setCheckSecurityOnOpen(false);
		caseHome.setTransactionLogActive(false);
		caseHome.clearInstance();
		caseHome.setId(tbcase.getId());
		reg = entityManager.merge(reg);
		
		tbcase = caseHome.getInstance();
		tbcase.setState(CaseState.WAITING_TREATMENT);
		
		startTreatHome.setSaveChages(true);
		startTreatHome.setUseDefaultDoseUnit(true);
		startTreatHome.setRegimen(reg);
		startTreatHome.setIniTreatmentDate(tbcase.getTreatmentPeriod().getIniDate());
		startTreatHome.setEndTreatmentDate(tbcase.getTreatmentPeriod().getEndDate());
		startTreatHome.getTbunitselection().setTbunit(tbcase.getNotificationUnit());
		startTreatHome.updatePhases();
		startTreatHome.startStandardRegimen();
	}
	
	protected Regimen newValue(Map<Regimen, Integer> values) {
		if (values.keySet().size() == 0)
			return null;
		
		int max = 0;
		for (Object key: values.keySet()) {
			max += values.get(key);
		}
		
		int val = random.nextInt(max) + 1;
		for (Regimen key: values.keySet()) {
			val -= values.get(key);
			if (val <= 0)
				return key;
		}
		return null;
	}

}
