package org.msh.tb.bd;

import org.jboss.seam.Component;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection2;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class QSPUtils {
	 
	/**
	 * Returns the list of tbunits that haven't closed the selected quarter according to the passed parameters.
	 */
	public static List<Tbunit> getPendCloseQuarterUnits(Quarter selectedQuarter, TBUnitSelection2 tbunitselection){
		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		List<Tbunit> ret = null;

		if(tbunitselection.getTbunit() != null){
			ret = null;
		
			if(tbunitselection.getTbunit().getLimitDateMedicineMovement() != null && tbunitselection.getTbunit().getLimitDateMedicineMovement().compareTo(selectedQuarter.getIniDate()) <=0){
				ret = new ArrayList<Tbunit>();
				ret.add(tbunitselection.getTbunit());
			}
			
		}else if(tbunitselection.getAuselection().getSelectedUnit()!=null){
			String queryString = "from Tbunit u where u.adminUnit.code like :code and u.workspace.id = :workspaceId " +
								"and u.limitDateMedicineMovement <= :iniQuarterDate " +
								"and u.treatmentHealthUnit = :true and u.medManStartDate is not null and u.active = :true " +
								"order by u.adminUnit.code, u.name.name1";
			
			ret = entityManager.createQuery(queryString)
								.setParameter("code", tbunitselection.getAuselection().getSelectedUnit().getCode()+'%')
								.setParameter("workspaceId", UserSession.getWorkspace().getId())
								.setParameter("iniQuarterDate", selectedQuarter.getIniDate())
								.setParameter("true", true)
								.getResultList();
		}else{
			String queryString = "from Tbunit u where u.workspace.id = :workspaceId " +
					"and u.limitDateMedicineMovement <= :iniQuarterDate " +
					"and u.treatmentHealthUnit = :true and u.medManStartDate is not null and u.active = :true " +
					"order by u.adminUnit.code, u.name.name1 ";

			ret = entityManager.createQuery(queryString)
								.setParameter("workspaceId", UserSession.getWorkspace().getId())
								.setParameter("iniQuarterDate", selectedQuarter.getIniDate())
								.setParameter("true", true)
								.getResultList();
		}
		
		return ret;
	}
	
	/**
	 * Returns the list of tbunits that haven't initialized the medicine module according to the passed parameters.
	 */
	public static List<Tbunit> getNotInitializedUnits(Quarter selectedQuarter, TBUnitSelection2 tbunitselection){
		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		List<Tbunit> ret = null;

		if(tbunitselection.getTbunit() != null){
			ret = null;
		
			if(tbunitselection.getTbunit().getLimitDateMedicineMovement() == null){
				ret = new ArrayList<Tbunit>();
				ret.add(tbunitselection.getTbunit());
			}
			
		}else if(tbunitselection.getAuselection().getSelectedUnit()!=null){
			String queryString = "from Tbunit u where u.adminUnit.code like :code and u.workspace.id = :workspaceId " +
								"and u.limitDateMedicineMovement is null " +
								"and u.treatmentHealthUnit = :true and u.active = :true " +
								"order by u.adminUnit.code, u.name.name1";
			
			ret = entityManager.createQuery(queryString)
								.setParameter("code", tbunitselection.getAuselection().getSelectedUnit().getCode()+'%')
								.setParameter("workspaceId", UserSession.getWorkspace().getId())
								.setParameter("true", true)
								.getResultList();
		}else{
			String queryString = "from Tbunit u where u.workspace.id = :workspaceId " +
					"and u.limitDateMedicineMovement is null " +
					"and u.treatmentHealthUnit = :true and u.active = :true " +
					"order by u.adminUnit.code, u.name.name1 ";

			ret = entityManager.createQuery(queryString)
								.setParameter("workspaceId", UserSession.getWorkspace().getId())
								.setParameter("true", true)
								.getResultList();
		}
		
		return ret;
	}

	/**
	 * Returns where clause depending on the tbunitselection filter
	 */
	public static String getLocationWhereClause(TBUnitSelection2 tbunitselection){
		if(tbunitselection == null)
			return null;
		
		String ret = "where mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId() + " and mov.tbunit.treatmentHealthUnit = true " +
						"and mov.tbunit.medManStartDate is not null and mov.tbunit.active = true ";
		
		if(tbunitselection.getTbunit()!=null){
			ret = ret + "and mov.tbunit.id = " + tbunitselection.getTbunit().getId();
		}else if(tbunitselection.getAuselection().getSelectedUnit()!=null){
			ret = ret + "and mov.tbunit.adminUnit.code like '" + tbunitselection.getAuselection().getSelectedUnit().getCode() + "%' ";
		}
		return ret;
	}
}
