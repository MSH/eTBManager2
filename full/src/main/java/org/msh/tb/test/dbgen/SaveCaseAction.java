package org.msh.tb.test.dbgen;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.misc.SequenceGenerator;

import javax.persistence.EntityManager;

@Name("saveCase")
public class SaveCaseAction {

	@In(create=true) EntityManager entityManager;
	@In(create=true) SequenceGenerator sequenceGenerator;

	@Transactional
	public void save(TbCase tbcase) {
		// generates new patient number
		Patient patient = tbcase.getPatient();
		patient.setRecordNumber(sequenceGenerator.generateNewNumber(CaseValidationHome.caseGenId, patient.getWorkspace()));

		entityManager.persist(tbcase.getPatient());
		entityManager.persist(tbcase);
		
		entityManager.flush();
	}
	

	/**
	 * Remove all cases from the selected years
	 */
	@Transactional
	public void removeCases(GeneratorPreferences preferences, Workspace workspace) {
		String s = "";
		for (CaseInfo caseInfo: preferences.getCases()) {
			if (!s.isEmpty())
				s += ",";
			s += caseInfo.getYear();
		}
		
		String sYears = s;
		s = "(select aux.id from TbCase aux where year(aux.diagnosisDate) in (" + s + ")" +
				"and aux.patient.workspace.id = " + workspace.getId() + ")";

		entityManager.createQuery("delete from TbCase c where year(c.diagnosisDate) in (" + sYears + 
				") and c.patient.id in (select p.id from Patient p where p.workspace.id = " + workspace.getId() + ")")
				.executeUpdate();
		entityManager.createQuery("delete from Patient p " +
				"where not exists(select c.id from TbCase c where c.patient.id = p.id)" +
				"and p.workspace.id = " + workspace.getId()).executeUpdate();
	}

}
