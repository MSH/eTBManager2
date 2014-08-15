package org.msh.tb.laboratories;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.PatientSample;

@Name("sampleHome")
public class SampleHome extends EntityHomeEx<PatientSample> {
	private static final long serialVersionUID = -9053795322781960977L;

	@Factory("patientSample")
	public PatientSample getPatientSample() {
		return getInstance();
	}
	
	public void initialize() {
		if (isManaged()) {
			CaseHome caseHome = (CaseHome)Component.getInstance("caseHome", true);
			caseHome.setId( getInstance().getTbcase().getId() );
		}
	}
}
