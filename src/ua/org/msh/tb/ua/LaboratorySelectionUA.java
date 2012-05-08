package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.laboratories.LaboratorySelection;

public class LaboratorySelectionUA extends LaboratorySelection {
	private static final long serialVersionUID = -1749061457663113197L;

	@Override
	protected void createOptions() {
		AdministrativeUnit adminUnit;
		adminUnit = getAuselection().getUnitLevel1();
		
		if (adminUnit == null)
			return;

		List<Laboratory> options = new ArrayList<Laboratory>();
		
		String hql = "from Laboratory lab where lab.adminUnit.code like :admincode and lab.workspace.id = #{defaultWorkspace.id}";
		if (getHealthSystem() != null)
			hql += " and lab.healthSystem.id = " + getHealthSystem().getId().toString();
		options = getEntityManager().createQuery(hql)
					.setParameter("admincode",  adminUnit.getCode() + "%")
					.getResultList();
		setOptions(options);
	}

	@Override
	public AdministrativeUnit getAdminUnit() {
		if (getAuselection().getUnitLevel1()==null){
			UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
			if (userWorkspace != null)
					getAuselection().setUnitLevel1(userWorkspace.getAdminUnit());
		}
		return getAuselection().getUnitLevel1();
	}
}
