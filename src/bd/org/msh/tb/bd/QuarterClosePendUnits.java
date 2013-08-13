package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.tbunits.TBUnitSelection2;

public class QuarterClosePendUnits {
	
	/**
	 * Update the list of tbunits that haven't closed the selected quarter according to the passed parameters.
	 */
	public static List<Tbunit> getPendCloseQuarterUnits(Quarter selectedQuarter, TBUnitSelection2 tbunitselection){
		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		List<Tbunit> ret = null;
 		
		if(getLocationWhereClause(tbunitselection) == null || tbunitselection.getTbunit() != null){
			ret = null;
			if(tbunitselection.getTbunit().getLimitDateMedicineMovement().compareTo(selectedQuarter.getIniDate()) <=0){
				ret = new ArrayList<Tbunit>();
				ret.add(tbunitselection.getTbunit());
			}
		}else{
			String queryString = "from Tbunit u where u.adminUnit.code like :code and u.workspace.id = :workspaceId " +
								"and (u.limitDateMedicineMovement is null or u.limitDateMedicineMovement <= :iniQuarterDate) " +
								"and u.treatmentHealthUnit = :true " +
								"order by u.adminUnit.code, u.name.name1";
			
			ret = entityManager.createQuery(queryString)
								.setParameter("code", tbunitselection.getAuselection().getSelectedUnit().getCode()+'%')
								.setParameter("workspaceId", UserSession.getWorkspace().getId())
								.setParameter("iniQuarterDate", selectedQuarter.getIniDate())
								.setParameter("true", true)
								.getResultList();
		}
		
		return ret;
	}

	/**
	 * Returns where clause depending on the tbunitselection filter
	 */
	private static String getLocationWhereClause(TBUnitSelection2 tbunitselection){
		if(tbunitselection == null)
			return null;
		
		if(tbunitselection.getTbunit()!=null){
			return "where mov.tbunit.id = " + tbunitselection.getTbunit().getId() + " and mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId();
		}else if(tbunitselection.getAuselection().getSelectedUnit()!=null){
			return "where mov.tbunit.adminUnit.code like '" + tbunitselection.getAuselection().getSelectedUnit().getCode() + "%' " +
					"and mov.tbunit.workspace.id = " + UserSession.getWorkspace().getId() + " and mov.tbunit.treatmentHealthUnit = true";
		}
		return null;
	}
}
