package org.msh.tb.cases;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Patient;
import org.msh.tb.EntityHomeEx;


@Name("patientHome")
public class PatientHome extends EntityHomeEx<Patient>{
	private static final long serialVersionUID = -2541635761448195490L;

	@Factory("patient")
	public Patient getPatient() {
		return getInstance();
	}

	@Override
	public void createdMessage() {
	}

	@Override
	public void updatedMessage() {
	}
}
