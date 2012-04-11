package org.msh.tb.ua;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.PatientsQuery;
@Name("patientsUA")
@BypassInterceptors
public class PatientQueryUA extends PatientsQuery {
	private static final long serialVersionUID = -961510543312956656L;
	private PatientFilter patientFilter;
	
	/**
	 * Generates dynamic conditions for patient search
	 * @return
	 */
	@Override
	public String conditions() {
		String cond = null;
		getPatientFilter();
		if (patientFilter != null) {
			if (patientFilter.getBirthDate() != null)
				cond = "(p.birthDate = #{patientFilter.birthDate})";
			if (patientFilter.getNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.name like #{patientFilter.nameLike})";
			if (patientFilter.getMiddleNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.middleName like #{patientFilter.middleNameLike})";
			if (patientFilter.getLastNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.lastName like #{patientFilter.lastNameLike})";
		}
		return (cond == null? "": " and (" + cond + ")");
	}
	
	public PatientFilter getPatientFilter() {
		if (patientFilter == null)
			patientFilter = (PatientFilter)Component.getInstance("patientFilter", true);
		return patientFilter;
	}
	@Override
	@Transactional
	public boolean isNextExists()
	{
		boolean b = (getPatientList()!=null) && (getPatientList().size() > getMaxResults());
		return b;
	}
	@Override
	public void validate(){
			super.validate();
	}
}
