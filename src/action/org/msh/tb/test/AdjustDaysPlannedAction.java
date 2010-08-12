package org.msh.tb.test;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.CaseState;

@Name("adjustDaysPlannedAction")
public class AdjustDaysPlannedAction {

	@In EntityManager entityManager;
	
	@Transactional
	public boolean adjust(int ini, int max) {
		List<TbCase> lst = entityManager.createQuery("from TbCase c where c.state >= " + CaseState.ONTREATMENT.ordinal() +
			" and exists(select aux.id from PrescribedMedicine aux where aux.tbcase.id = c.id) " +
			"and c.patient.workspace.id = 940357")
				.setFirstResult(ini)
				.setMaxResults(max)
				.getResultList();

		if (lst.size() == 0)
			return false;

		for (TbCase tbcase: lst) {
			int plannedDays = 0;
			int numdays = 0;
			for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
				int num = pm.calcNumDaysDispensing(pm.getPeriod());
				if (num > numdays)
					numdays = num;
			}
			plannedDays += numdays;
			tbcase.setDaysTreatPlanned(plannedDays);
			entityManager.persist(tbcase);
			entityManager.flush();
		}
		entityManager.clear();
		
		return true;
	}

}
