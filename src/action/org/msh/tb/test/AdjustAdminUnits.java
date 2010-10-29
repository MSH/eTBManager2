package org.msh.tb.test;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.Workspace;

@Name("adjustAdminUnits")
public class AdjustAdminUnits {

//	@In(create=true) Workspace defaultWorkspace;
	
	private Workspace workspace;
	
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}
	
	/**
	 * Adjust {@link AdministrativeUnit} code property
	 * @throws Exception 
	 */
	@Asynchronous
	@Transactional
	public void execute(Workspace workspace) throws Exception {
		this.workspace = workspace;
		updateAdminUnits(null);
	}


	private void updateAdminUnits(AdministrativeUnit parent) throws Exception {
		String hql = "from AdministrativeUnit a where a.workspace.id = " + workspace.getId();
		if (parent != null)
			 hql += " and a.parent.id = " + parent.getId().toString();
		else hql += " and a.parent.id is null";
		
		List<AdministrativeUnit> lst = null;
		
//		UserTransaction tx = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
//		tx.begin();
		EntityManager em = getEntityManager();
//		em.joinTransaction();
		try {
			lst = getEntityManager()
				.createQuery(hql)
				.getResultList();

			if (parent != null)
				parent = em.merge(parent);
		
			String parentCode = (parent != null? parent.getCode(): "");
		
			int counter = 1;
			NumberFormat formatter = new DecimalFormat("000");
		
			for (AdministrativeUnit adm: lst) {
				String code = parentCode + formatter.format(counter);
				adm.setCode(code);
				em.createNativeQuery("update AdministrativeUnit set code=:code where id=:id")
					.setParameter("code", code)
					.setParameter("id", adm.getId())
					.executeUpdate();
				counter++;
//				em.persist(adm);
//				em.flush();
			}
			
			if (parent != null) {
				parent.setUnitsCount(lst.size());
				em.persist(parent);
				em.flush();
			}
			
//			tx.commit();
			em.clear();
		}
		catch (Exception e) {
//			tx.rollback();
			throw e;
		}
		
		for (AdministrativeUnit adm: lst) {
			updateAdminUnits(adm);
		}
	}

	
/*	private void save(AdministrativeUnit adm) throws Exception {
		UserTransaction tx = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		tx.begin();
		EntityManager em = getEntityManager();
		em.joinTransaction();
		try {
			em.persist(adm);
			tx.commit();
		}
		catch (Exception e) {
			tx.rollback();
		}
	}
*/}
