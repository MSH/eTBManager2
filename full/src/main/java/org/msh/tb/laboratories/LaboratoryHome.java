package org.msh.tb.laboratories;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.utils.EntityQuery;



@Name("laboratoryHome")
@Scope(ScopeType.CONVERSATION)
@LogInfo(roleName="LABS", entityClass=Laboratory.class)
public class LaboratoryHome extends EntityHomeEx<Laboratory>{
	private static final long serialVersionUID = -4352110924970540613L;

	@In(required=false) LaboratoriesQuery laboratories;

	private AdminUnitSelection auselection = new AdminUnitSelection();

	@Factory("laboratory")
	public Laboratory getLaboratory() {
		return getInstance();
	}

	@Override
	public String remove() {
		if (laboratories != null)
			laboratories.refresh();
		return super.remove();
	}

	
	@Override
	public EntityQuery<Laboratory> getEntityQuery() {
		return (EntityQuery)Component.getInstance("laboratories", false);
	}

	
	@Override
	public String persist() {
		getInstance().setAdminUnit(getAuselection().getSelectedUnit());
		
		getInstance().setAbbrevName(getInstance().getAbbrevName().toUpperCase());

		if (laboratories != null)
			laboratories.refresh();
		return super.persist();
	}
		
	public AdminUnitSelection getAuselection() {
		return auselection;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Home#setId(java.lang.Object)
	 */
	@Override
	public void setId(Object id) {
		super.setId(id);
		
		if ((isManaged()) && (getAuselection().getSelectedUnit() == null)) {
			getAuselection().setSelectedUnit(getInstance().getAdminUnit());
		}
	}

}
