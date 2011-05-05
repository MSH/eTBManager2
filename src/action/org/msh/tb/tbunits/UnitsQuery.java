package org.msh.tb.tbunits;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.utils.EntityQuery;


@Name("unitspg")
public class UnitsQuery extends EntityQuery<Tbunit> {
	private static final long serialVersionUID = -2204952368088626551L;

	private static final String[] restrictions = {"u.workspace.id=#{defaultWorkspace.id}",
		"u.healthSystem.id = #{unitspg.healthSystem.id}",
		"u.adminUnit.code like #{unitspg.adminUnitCodeLike}"};
	
	private AdminUnitSelection auselection;
	private HealthSystem healthSystem;


	@Override
	protected String getCountEjbql() {
		return "select count(*) from Tbunit u" + staticHQLRestriction();
	}


	@Override
	public String getEjbql() {
		return "select u from Tbunit u join fetch u.adminUnit" + staticHQLRestriction();
	}


	@Override
	public Integer getMaxResults() {
		return 50;
	}

	
	protected String staticHQLRestriction() {
		AdministrativeUnit aux = getAuselection().getSelectedUnit(); 
		if (aux == null)
			return "";
		
		String sid = aux.getId().toString();
		
		String hql = " left join u.adminUnit.parent par1 left join u.adminUnit.parent.parent par2 " +
				"left join u.adminUnit.parent.parent.parent par3 " +
				"left join u.adminUnit.parent.parent.parent.parent par4 " +
				"where (u.adminUnit.id = " + sid + " or par1.id = " + sid + 
				" or par2.id = " + sid + " or par3.id = " + sid +
				" or par4.id = " + sid + ")";

		return hql;
	}

	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	public AdminUnitSelection getAuselection() {
		if (auselection == null) {
			auselection = new AdminUnitSelection();
		}
		return auselection;
	}


	/**
	 * @param healthSystem the healthSystem to set
	 */
	public void setHealthSystem(HealthSystem healthSystem) {
		this.healthSystem = healthSystem;
	}


	/**
	 * @return the healthSystem
	 */
	public HealthSystem getHealthSystem() {
		return healthSystem;
	}
	
	public String getAdminUnitCodeLike() {
		AdministrativeUnit adm = getAuselection().getSelectedUnit();
		if (adm == null)
			 return null;
		else return adm.getCode() + "%";
	}
	
}
