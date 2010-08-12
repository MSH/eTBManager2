package org.msh.tb.cases;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Patient;
import org.msh.utils.EntityQuery;


@Name("patients")
public class PatientsQuery extends EntityQuery<Patient> {
	private static final long serialVersionUID = -2441729659688297288L;

/*	private static final String[] restrictions = {
		"p.birthDate = #{patient.birthDate}",
		"p.name like #{patients.nameLike}",
		"p.middleName like #{patients.middleNameLike}",
		"p.lastName like #{patients.lastNameLike}"
	};
*/	
	@In(required=false) Patient patient;

	private boolean searching;


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from Patient p where p.workspace.id = #{defaultWorkspace.id} " + conditions();
	}

	/**
	 * Generates dynamic conditions for patient search
	 * @return
	 */
	public String conditions() {
		String cond = null;
		if (patient != null) {
			if (patient.getBirthDate() != null)
				cond = "(p.birthDate = #{patient.birthDate})";
			if (getNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.name like #{patients.nameLike})";
			if (getMiddleNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.middleName like #{patients.middleNameLike})";
			if (getLastNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.lastName like #{patients.lastNameLike})";
		}
		return (cond == null? "": " and (" + cond + ")");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from Patient p where p.workspace.id = #{defaultWorkspace.id} " + conditions();
	}

	@Override
	public Integer getMaxResults() {
		return 50;
	}

	@Override
	public String getOrder() {
		return "p.name";
	}

	public void search() {
		searching = true;
		refresh();
	}

	public String getNameLike() {
		return (patient == null) || (patient.getName() == null) || (patient.getName().isEmpty()) ? null: patient.getName().toUpperCase() + "%"; 
	}

	public String getMiddleNameLike() {
		return (patient == null) || (patient.getMiddleName() == null) || (patient.getMiddleName().isEmpty()) ? null: patient.getMiddleName().toUpperCase() + "%"; 
	}

	public String getLastNameLike() {
		return (patient == null) || (patient.getLastName() == null) || (patient.getLastName().isEmpty()) ? null: patient.getLastName().toUpperCase() + "%"; 
	}

	public boolean isSearching() {
		return searching;
	}

	public void setSearching(boolean searching) {
		this.searching = searching;
	}


	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityQuery#getStringRestrictions()
	 */
/*	@Override
	protected List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
*/}
