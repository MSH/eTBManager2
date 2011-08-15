package org.msh.tb.na;

import java.util.Calendar;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.Messages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.na.entities.TbCaseNA;
import org.msh.utils.date.DateUtils;


/**
 * Generating the TB case number for each TB center.
 * @author Utkarsh Srivastava
 *
 */
@Name("tbunitcasenumber")
@Scope(ScopeType.CONVERSATION)
public class TBUnitCaseNumber {

	@In(create = true) EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	
	private String prefix = Messages.instance().get("cases.na.prefix");
	
	/**
	 * generates a case number based upon the number of validated patients present in the site.
	 * @return
	 */
	public String generateTBUnitCaseNumber() {
		TbCaseNA  tbcasena = (TbCaseNA) caseHome.getTbCase();
		ValidationState vstate = tbcasena.getValidationState();
		String unitCaseNumber = "";
		Tbunit caseUnit = tbcasena.getNotificationUnit();
		
		if(vstate != ValidationState.VALIDATED)
			return "error";
		
		// generate new case number
		Long caseNum = (Long)entityManager.createQuery("select count(*) from TbCase c where c.notificationUnit.id = :id and c.validationState = :vstate")
			.setParameter("id", caseUnit.getId())
			.setParameter("vstate", vstate)
			.getSingleResult();
		
		if (caseNum == null)
			caseNum = new Long(1);
		else caseNum++;
		
		String formatMnth = DateUtils.FormatDateTime("MM", Calendar.getInstance().getTime());
		String formatYear = DateUtils.FormatDateTime("yy", Calendar.getInstance().getTime());
		String formatCaseNum = String.format("%03d", caseNum);
		
		unitCaseNumber = prefix + caseUnit.getLegacyId()+"-"+formatMnth+formatYear+"-"+formatCaseNum;
		tbcasena.setUnitRegCode(unitCaseNumber);

		entityManager.persist(tbcasena);
		entityManager.flush();
		
		return "update";
	}
	
	
	
	
}
