package org.msh.tb.test;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseState;

@Name("adjustDaysPlannedAction")
public class AdjustDaysPlannedAction {

	@In EntityManager entityManager;
	
	@Transactional
	public boolean adjust(Workspace ws, int ini, int max) {
		List<TbCase> lst = entityManager.createQuery("from TbCase c where c.state >= " + CaseState.ONTREATMENT.ordinal() +
			" and exists(select aux.id from PrescribedMedicine aux where aux.tbcase.id = c.id) " +
			"and c.patient.workspace.id = " + ws.getId().toString())
				.setFirstResult(ini)
				.setMaxResults(max)
				.getResultList();

		if (lst.size() == 0)
			return false;

		for (TbCase tbcase: lst) {
			tbcase.updateDaysTreatPlanned();
			entityManager.persist(tbcase);
			entityManager.flush();
		}
		entityManager.clear();
		
		return true;
	}

}
