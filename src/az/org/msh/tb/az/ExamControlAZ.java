package org.msh.tb.az;

import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.TbCase;
import org.msh.tb.workspaces.customizable.ExamControl;

@Name("examControl_az")
public class ExamControlAZ extends ExamControl{

	/**
	 * Extension of the {@link ExamControl} class for Azerbaijan, where they use specific rules for 
	 * displaying the month of treatment
	 * @param tbcase object representing the TB Case
	 * @param dt reference date for the month display
	 * @return
	 */
	@Override
	public String getMonthDisplay(TbCase tbcase, Date dt) {
		EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
		TbCaseAZ tc = (TbCaseAZ) em.find(TbCaseAZ.class, tbcase.getId());
		try { // I don't know how it is possible!!!!
			if (tc.isToThirdCategory() && !dt.before(tc.getThirdCatPeriod().getIniDate()))
				return "TbCase.toThirdCategory";
		} catch (Exception e) {
			return super.getMonthDisplay(tbcase, dt);
		}
		
		return super.getMonthDisplay(tbcase, dt);
	}
}
