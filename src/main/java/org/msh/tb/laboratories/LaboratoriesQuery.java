package org.msh.tb.laboratories;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Laboratory;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.List;


@Name("laboratories")
public class LaboratoriesQuery extends EntityQuery<Laboratory> {
	private static final long serialVersionUID = 4287113806099961147L;

	private static final String[] restrictions = {
		"lab.workspace.id = #{defaultWorkspace.id}",
		"lab.healthSystem.id = #{userWorkspace.healthSystem.id}",
		"lab.adminUnit.code like #{laboratories.auselection.selectedUnitCodeLike}",
		"lab.healthSystem.id = #{laboratories.healthSystem.id}"};
	
	private AdminUnitSelection auselection = new AdminUnitSelection(true);
	private HealthSystem healthSystem;

	@Override
	public String getEjbql() {
		return "from Laboratory lab join fetch lab.adminUnit";
	}


	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "lab.abbrevName";
		else return s;
	}


	@Override
	protected String getCountEjbql() {
		return "select count(*) from Laboratory lab";
	}

	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}


	/**
	 * @return the auselection
	 */
	public AdminUnitSelection getAuselection() {
		return auselection;
	}


	/**
	 * @param auselection the auselection to set
	 */
	public void setAuselection(AdminUnitSelection auselection) {
		this.auselection = auselection;
	}


	/**
	 * @return the healthSystem
	 */
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}


	/**
	 * @param healthSystem the healthSystem to set
	 */
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}
}
