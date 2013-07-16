package org.msh.tb.na;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.international.Messages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.login.UserSession;
import org.msh.tb.na.entities.TbCaseNA;
import org.msh.utils.date.DateUtils;

import com.ibm.icu.text.DecimalFormat;


/**
 * Generating the TB case number for each TB center. The number is generated following a
 * rule set by NTP in Namibia using the pattern</p>
 * <code>TBZZZ-MMYY-SSS</code>
 * </p>
 * where:
 * <b>ZZZ</b> is the unit legacy ID number<br/>
 * <b>MM</b> is the month of the case registration date<br/>
 * <b>YY</b> is the year of the case registration date<br/>
 * <b>SSS</b> is the sequence number generated for the month and year in this unit
 * @author Utkarsh Srivastava
 *
 */
@Name("tbunitcasenumber")
@Synchronized(timeout=10000)
public class TBUnitCaseNumber {

	@In(create = true) EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	
//	private String prefix = Messages.instance().get("cases.na.prefix");
	
	/**
	 * generates a case number based upon the number of validated patients present in the site.
	 * The method observes the case validate event, and is called automatically when the case is validated
	 */
	@Observer("case.validate")
	public void generateTBUnitCaseNumber() {
		// prevent of executing that for other workspaces
		Workspace ws = UserSession.getWorkspace();
		if (!("na".equals(ws.getExtension())))
			return;

		TbCaseNA  tbcase = (TbCaseNA) caseHome.getTbCase();
//		String unitCaseNumber = "";
		Tbunit unit = tbcase.getOwnerUnit();
		if ((unit.getLegacyId() == null) || (unit.getLegacyId().isEmpty()))
			throw new IllegalAccessError("The legacy code in unit " + unit.getName().toString() + " was not defined");

		int month = DateUtils.monthOf(tbcase.getRegistrationDate()) + 1;
		int year = DateUtils.monthOf(tbcase.getRegistrationDate());
		
		String codePattern = "TB" + unit.getLegacyId() + "-"; 

		// get the last number generated
		String lastRegCode = (String)entityManager.createQuery("select max(registrationCode) from TbCase "
				+ "where ownerUnit.id = :unitid and year(registrationDate) = :year "
				+ "and registrationCode like :code")
				.setParameter("unitid", tbcase.getOwnerUnit().getId())
				.setParameter("year", year)
				.setParameter("code", codePattern + "%")
				.getSingleResult();

		// get the last part of the code and transform it to integer
		int num = 0;
		if (lastRegCode != null) {
			String[] s = lastRegCode.split("-");
			if (s.length >= 3) {
				try {
					num = Integer.parseInt(s[3]);
				} catch (Exception e) {
					throw new IllegalAccessError("Not expected last registration code with format " + lastRegCode);
				}
			}
			else num = 0;
		}

		// generate new number
		num++;
		
		String regCode = codePattern + String.format("%02d", month) + String.format("%02d", year) + "-" + String.format("%03d", num);
		if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)
			tbcase.setSuspectRegistrationCode(regCode);
		tbcase.setRegistrationCode(regCode);
		
		entityManager.persist(tbcase);
		entityManager.flush();
		
/*		Long caseNum = (Long)entityManager.createQuery("select count(*) from TbCase c where c.notificationUnit.id = :id and c.validationState = :vstate")
			.setParameter("id", caseUnit.getId())
//			.setParameter("vstate", vstate)
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
*/	}
	
	
	
	
}
