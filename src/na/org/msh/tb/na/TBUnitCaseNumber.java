package org.msh.tb.na;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Synchronized;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;


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
 * @author Ricardo Memoria
 *
 */
@Name("tbunitcasenumber")
@Synchronized(timeout=10000)
public class TBUnitCaseNumber {

	@In(create = true) EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	
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

		TbCase  tbcase = caseHome.getTbCase();

		String regCode = tbcase.getRegistrationCode();
		// there is already a registration code provided by the user ?
		if ((regCode != null) && (!regCode.isEmpty()))
			return;
		
		Tbunit unit = tbcase.getOwnerUnit();
		if ((unit.getLegacyId() == null) || (unit.getLegacyId().isEmpty()))
			throw new IllegalAccessError("The legacy code in unit " + unit.getName().toString() + " was not defined");

		int month = DateUtils.monthOf(tbcase.getRegistrationDate()) + 1;
		int year = DateUtils.yearOf(tbcase.getRegistrationDate());
		
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
					num = Integer.parseInt(s[2]);
				} catch (Exception e) {
					throw new IllegalAccessError("Not expected last registration code with format " + lastRegCode);
				}
			}
			else num = 0;
		}

		// generate new number
		num++;

		// update in the tbcase table
		regCode = codePattern + String.format("%02d", month) + String.format("%02d", year % 100) + "-" + String.format("%03d", num);
		if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)
			tbcase.setSuspectRegistrationCode(regCode);
		tbcase.setRegistrationCode(regCode);
		
		entityManager.persist(tbcase);
		entityManager.flush();
	}
	
	
	
	
}
