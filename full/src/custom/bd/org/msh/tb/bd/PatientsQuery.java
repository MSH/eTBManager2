package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;

@Name("patientsbd")
public class PatientsQuery extends org.msh.tb.cases.PatientsQuery{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7621209620958508636L;
	
	/**
	 * Generates dynamic conditions for patient search
	 * @return
	 */
	@Override
	public String conditions() {
		String cond = null;
		if (super.patient != null) {
			if (patient.getBirthDate() != null)
				cond = "(p.birthDate = #{patient.birthDate})";
			if (getNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.name like #{patientsbd.nameLike})";
			if (getMiddleNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.middleName like #{patientsbd.middleNameLike})";
			if (getLastNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.lastName like #{patientsbd.lastNameLike})";
			if (getFatherNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.fatherName like #{patientsbd.fatherNameLike})";
		}
		return (cond == null? "": " and (" + cond + ")");
	}	
	
	public String getFatherNameLike() {
		return (patient == null) || (patient.getFatherName() == null) || (patient.getFatherName().isEmpty()) ? null: "%" + patient.getFatherName().toUpperCase() + "%"; 
	}
	

}
