package org.msh.tb.misc;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.Tag;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;
import java.util.List;

@Name("fixDataHome")
public class FixDataHome {

	@In(create=true) EntityManager entityManager;
	
	/**
	 * Updates tbcase.daysTreatPlanned null for all tbcases with treatment registered
	 */
	@Transactional
	public String fixNulledDaysTreatPlanned() {
		List<TbCase> cases = entityManager.createQuery("from TbCase c " +
														"where c.daysTreatPlanned is null " +
														"and c.treatmentPeriod.iniDate is not null " +
														"and c.treatmentPeriod.endDate is not null").getResultList();

		for(TbCase c : cases){
			c.updateDaysTreatPlanned();
			entityManager.merge(c);
			entityManager.flush();
		}
		FacesMessages facesMessages = (FacesMessages)Component.getInstance("facesMessages");
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "fixed";
	}


	static public FixDataHome instance() {
		return (FixDataHome)Component.getInstance("fixDataHome", true);
	}
}
