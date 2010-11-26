package org.msh.tb.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserPermission;
import org.msh.mdrtb.entities.UserProfile;
import org.msh.mdrtb.entities.UserRole;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;

@Name("workspaceInit")
public class WorkspaceInit {

	@In(create=true) EntityManager entityManager;
	@In(required=true) UserLogin userLogin;
	@In(create=true) FacesMessages facesMessages;
	
	public void initMedicineManagement() {
		execUpdate("delete from BatchQuantity bm where bm.batch.id in (select b.id from Batch b where b.medicine.workspace.id = :id)");
		execUpdate("delete from BatchMovement bm where bm.batch.id in (select b.id from Batch b where b.medicine.workspace.id = :id)");
		
		execUpdate("delete from TransferBatch it where it.batch.id in (select aux.id from Batch aux where aux.medicine.workspace.id = :id)");
		execUpdate("delete from OrderBatch b where b.batch.id in (select aux.id from Batch aux where aux.medicine.workspace.id = :id)");

		execUpdate("delete from Batch b where b.medicine.id in (select aux.id from Medicine aux where aux.workspace.id = :id)");
		
		execUpdate("delete from MedicineReceivingItem it where it.medicineReceiving.id in (select id from MedicineReceiving d where d.tbunit.workspace.id = :id)");
		execUpdate("delete from MedicineReceiving d where d.source.id in (select aux.id from Source aux where aux.workspace.id = :id)");

		execUpdate("delete from OrderCase oc where oc.item.id in (select aux.id from OrderItem aux where aux.source.workspace.id = :id)");
		execUpdate("delete from OrderItem it where it.source.id in (select aux.id from Source aux where aux.workspace.id = :id)");
		execUpdate("delete from Order c where c.tbunitFrom.id in (select aux.id from Tbunit aux where aux.workspace.id = :id)");
		
		execUpdate("delete from TransferItem it where it.transfer.id in (select aux.id from Transfer aux where aux.unitFrom.workspace.id = :id)");
		execUpdate("delete from Transfer c where c.unitFrom.id in (select aux.id from Tbunit aux where aux.workspace.id = :id)");

		execUpdate("delete from MedicineDispensing d where d.tbunit.id in (select aux.id from Tbunit aux where aux.workspace.id = :id)");
//		execUpdate("delete from UnitDispensing u where u.id in (select ud.id from UnitDispensing ud where ud.tbunit.workspace.id = :id)");
		
		execUpdate("delete from Movement m where m.source.id in (select aux.id from Source aux where aux.workspace.id = :id)");
		execUpdate("delete from StockPosition s where s.source.id in (select aux.id from Source aux where aux.workspace.id = :id)");
	}
	
	protected void execUpdate(String hql) {
		Workspace ws = userLogin.getDefaultWorkspace();
		entityManager.createQuery(hql)
			.setParameter("id", ws.getId())
			.executeUpdate();
	}
	
	
	/**
	 * Update all administrator profiles of each workspace to guarantee that all permissions are included
	 */
	@Transactional
	public void updateAdministratorProfile() {
		List<UserProfile> lst = entityManager.createQuery("from UserProfile p where upper(p.name) like :name")
				.setParameter("name", "Admin%".toUpperCase())
				.getResultList();
		
		List<UserRole> roles = entityManager.createQuery("from UserRole a order by a.code").getResultList();

		// store the roles that are by case classification
		List<UserRole> roleClassifs = new ArrayList<UserRole>();
		
		for (UserProfile prof: lst) {
			for (UserRole role: roles) {
				// check if role is by case classification, either by the attribute "byCaseClassification"
				// or if it's a child of a role by case classification
				boolean b = role.isByCaseClassification();
				if (!b) {
					for (UserRole r: roleClassifs)
						if (role.isChildOf(r)) {
							b = true;
							break;
						}
				}
				
				// is by case classification ?
				if (b) {
					for (CaseClassification cla: CaseClassification.values())
						addRole(prof, role, cla);
					roleClassifs.add(role);
				}
				else addRole(prof, role, null);
			}
			
			entityManager.persist(prof);
		}
		entityManager.flush();
		
		facesMessages.add("All user profiles with name 'Administrator' were updated");
	}


	/**
	 * Add a role to a given profile
	 * @param prof
	 * @param role
	 * @param classif
	 */
	private void addRole(UserProfile prof, UserRole role, CaseClassification classif) {
		UserPermission perm = prof.permissionByRole(role, classif); 
		if (perm == null) {
			perm = new UserPermission();
			perm.setUserRole(role);
			perm.setUserProfile(prof);
			perm.setCaseClassification(classif);
			
			prof.getPermissions().add(perm);
		}
		perm.setGrantPermission(true);
		if (perm.getUserRole().isChangeable()) {
			perm.setCanChange(true);
		}
	}
}
