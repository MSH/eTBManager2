package org.msh.mdrtb.entities.enums;

/**
 * Classification of the cases
 * @author Ricardo Memoria
 *
 */
public enum CaseClassification {
	TB_DOCUMENTED ("TBCASES"),
	MDRTB_DOCUMENTED ("MDRCASES"),
	NMT ("NMTCASES");

	/**
	 * User role associated with the case classification used to open the case
	 */
	private String userrole;
	
	
	CaseClassification(String userrole) {
		this.userrole = userrole;
	}
	
	public String getKey() {
		return getClass().getSimpleName().concat("." + name());
	}
	
	/**
	 * User role associated with the case classification used to open the case
	 * @return
	 */
	public String getUserrole() {
		return userrole;
	}
	
	/**
	 * Return the user role associated with the case classification used to edit the case 
	 * @return
	 */
	public String getUserroleChange() {
		return userrole + "_EDT";
	}
}
