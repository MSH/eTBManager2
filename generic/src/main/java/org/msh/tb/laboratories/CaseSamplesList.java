package org.msh.tb.laboratories;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.PatientSample;

@Name("caseSamplesList")
public class CaseSamplesList {

	@In CaseHome caseHome;
	@In EntityManager entityManager;
	
	List<PatientSample> samples;
	
	/**
	 * Create the list of samples of the selected case
	 */
	protected void createSampleList() {
		samples = entityManager.createQuery("from PatientSample a where a.tbcase.id = #{caseHome.id}")
			.getResultList();
	}
	
	/**
	 * Return the list of samples from the selected case
	 * @return
	 */
	public List<PatientSample> getSamples() {
		if (samples == null)
			createSampleList();
		return samples;
	}
	
}
