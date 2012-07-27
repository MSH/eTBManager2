package org.msh.tb.az;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.cases.SearchCriteria;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.UserView;


/**
 * Query about cases. Use instead standard CasesQuery to fix error related
 * to search cases without TBUnit:
 * Also add possibility to search cases imported from EIDSS, but not binded to any health unit<br>
 * Made from CasesQueryUA, so suffixes UA rest in variables names
 * @author alexey
 */
@Name("casesAZ")
@BypassInterceptors
public class CasesQueryAZ extends CasesQuery{
	private static final long serialVersionUID = -7293313123644540770L;
	private final Integer casesEIDSSnotBindedIndex = CaseStateReportAZ.EIDSS_NOT_BINDED;
	private static final String notifCondUA = "(nu.id = #{caseFilters.tbunitselection.tbunit.id})";
	private static final String treatCondUA = "tu.id =  #{caseFilters.tbunitselection.tbunit.id}";
	private static final String notifRegCondUA = "(nu.id in (select id from org.msh.tb.entities.Tbunit tbu where tbu.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String treatRegCondUA = "(tu.id in (select id from org.msh.tb.entities.Tbunit tbu1 where tbu1.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String notifAdrAdmUnitUA="c.notifAddress.adminUnit.code like ";
	private static final String notifAdrAdmUnitRegUA="c.notifAddress.adminUnit.code = ";
	
	// static filters
	private static final String[] restrictions = {
		"p.recordNumber = #{caseFilters.patientRecordNumber}",
		"c.registrationCode = #{caseFilters.registrationCode}",
		"c.unitRegCode = #{caseFilters.unitRegCode}",
		"c.caseNumber = #{caseFilters.caseNumber}",
		"p.workspace.id = #{defaultWorkspace.id}",
		"c.state = #{caseFilters.caseState}",
		"c.notifAddress.adminUnit.code like #{caseFilters.adminUnitLike}",
		"c.classification = #{caseFilters.classification}",
		"upper(p.name) like #{caseFilters.nameLike}",
		"upper(p.middleName) like #{caseFilters.middleNameLike}",
		"upper(p.lastName) like #{caseFilters.lastNameLike}",
		"c.patientType = #{caseFilters.patientType}",
		"c.infectionSite = #{caseFilters.infectionSite}",
		"c.diagnosisType = #{caseFilters.diagnosisType}",
		"c.validationState = #{caseFilters.validationState}",
		"year(p.birthDate) = #{caseFilters.birthYear}",
	"exists(select t.id from c.tags t where t.id = #{caseFilters.tagid})"};
	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
	
	public String getAdminUnitLike(AdministrativeUnit adm) {
		UserWorkspace userWorkspace = (UserWorkspace) Component.getInstance("userWorkspace");
		if (UserView.ADMINUNIT.equals(userWorkspace.getView())){
			if (adm == null)
				return null;
			if	(adm.getLevel()==1)
			return "'"+adm.getCode() + "%'";
			else return "'"+adm.getCode()+"'";
		}
		return null;
	}
	
	@Override
	public String getEjbql() {
		if (isNotBindedEIDSS()){
			return getNotBindedEIDSSEjb();
		}else return getGeneralEjbql();

	}
	/**
	 * Get specific form of the EJBQl - used only for unbinded EIDSS export
	 * @return
	 */
	private String getNotBindedEIDSSEjb() {
		return "select p.name, c.age, p.gender, p.recordNumber, c.caseNumber, " + 
		"c.registrationDate, c.EIDSSComment, " +
		"c.state, c.classification, p.middleName, p.lastName, " +
		"c.registrationCode, c.diagnosisType, p.birthDate, c.id " +
		getFromHQL() + " join c.patient p ".concat(dynamicConditions());
	}
	/**
	 * Get general form of the EJBQL - used for all cases, except query for not binded EIDSS inport
	 * @return
	 */
	private String getGeneralEjbql() {
		return "select p.name, c.age, p.gender, p.recordNumber, c.caseNumber, " + 
		"c.treatmentPeriod.iniDate, c.registrationDate, nu.name.name1, " +
		"loc.name.name1, loc.code, c.id, " +
		"c.treatmentPeriod.endDate, c.state, c.classification, p.middleName, p.lastName, " +
		"c.validationState, c.registrationCode, c.diagnosisType, p.birthDate " +
		getFromHQL() + " join c.patient p " +
		"left outer join c.notificationUnit nu " +
		"left outer join c.treatmentUnit tu " +
		"join c.notifAddress.adminUnit loc ".concat(dynamicConditions());
	}
	@Override
	public String getCountEjbql() {
		if (isNotBindedEIDSS()){
			return getNotBindedCountEIDSSEjb();
		}else return getGeneralCountEjbql();

	}
	/**
	 * Get specific form of the count EJBQl - used only for unbinded EIDSS export
	 * @return
	 */
	private String getNotBindedCountEIDSSEjb() {
		return "select count(*) " + getFromHQL() + 
		" join c.patient p " +
		dynamicConditions();
	}
	/**
	 * Get general form of the count EJBQL - used for all cases, except query for not binded EIDSS inport
	 * @return
	 */
	private String getGeneralCountEjbql() {
		return "select count(*) " + getFromHQL() + 
		" join c.patient p " +
		"left outer join c.notificationUnit nu " + 
		"left outer join c.treatmentUnit tu " +
		dynamicConditions();
	}

	/**
	 * Generate HQL conditions for filters that cannot be included in the restrictions clause
	 * @return
	 */
	@Override
	protected String dynamicConditions() {
		if (hqlCondition != null)
			return hqlCondition;

		hqlCondition = "";

		FilterHealthUnit filterUnit = getCaseFilters().getFilterHealthUnit();

		// health unit condition
		if (filterUnit != null) {
			// health unit was set ?
			if (getCaseFilters().getTbunitselection().getTbunit() != null) {
				switch (filterUnit) {
				case NOTIFICATION_UNIT:
					addCondition(notifCondUA);
					break;
				case TREATMENT_UNIT:
					addCondition(treatCondUA);
					break;
				case BOTH:
					addCondition("(" + treatCondUA + " or " + notifCondUA + ")");
				}
			}
			else // region was set ? 
				if (getCaseFilters().getTbunitselection().getAdminUnit() != null) {
					switch (filterUnit) {
					case NOTIFICATION_UNIT:
						addCondition(notifRegCondUA);
						break;
					case TREATMENT_UNIT:
						addCondition(treatRegCondUA);
						break;
					case BOTH:{
						addCondition("(" + treatRegCondUA + " or " + notifRegCondUA);
						UserWorkspace userWorkspace = (UserWorkspace) Component.getInstance("userWorkspace");
						if (UserView.ADMINUNIT.equals(userWorkspace.getView())){
							hqlCondition += " or "+(userWorkspace.getAdminUnit().getLevel()==1 ? notifAdrAdmUnitUA : notifAdrAdmUnitRegUA) + getAdminUnitLike(userWorkspace.getAdminUnit());
						}
						hqlCondition += ")";
						}
					}
				}
		}

		mountAdvancedSearchConditions();
		mountSingleSearchConditions();

		if (!hqlCondition.isEmpty())
			hqlCondition = " where ".concat(hqlCondition);

		return hqlCondition;
	}
	/**
	 * Refine single search condition to help understand EIDSS related queries
	 * AK
	 */
	@Override
	protected void mountSingleSearchConditions() {
		if (isNotBindedEIDSS()){
			addCondition("(not c.legacyId is null) and (c.treatmentUnit is null) and (c.notificationUnit is null)"); 
			mountPatientNameCondition();  
			mountRegistrationDatesCondition();
		}else if(isBindedEIDSS())
			addCondition("(not c.legacyId is null) and (not (c.treatmentUnit is null) or (not c.notificationUnit is null))");
		else super.mountSingleSearchConditions();

	}
	/**
	 * condition by registration time interval, will be mounted together with StateIndex!
	 */
	private void mountRegistrationDatesCondition() {
		Date dtIni = caseFilters.getIniDate();
		Date dtEnd = caseFilters.getEndDate();
		if ((dtIni != null) || (dtEnd != null)) {
			generateHQLPeriod(dtIni, dtEnd, "c.registrationDate");
		}
		
	}
	/**
	 * Condition by first or last or middle name, will be mounted together with StateIndex!
	 */
	private void mountPatientNameCondition() {
		if (caseFilters.getPatient() != null) {
			String numberCond = generateHQLPatientNumber();

			String[] names = caseFilters.getPatient().split(" ");
			if (names.length == 0) 
				return;

			String s="(";
			for (String name: names) {
				name = name.replaceAll("'", "''");
				if (s.length() > 1)
					s += " and ";
				s += "((upper(p.name) like '%" + name.toUpperCase() + 
				"%') or (upper(p.middleName) like '%" + name.toUpperCase() + 
				"%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%'))";
			}

			addCondition(s + (numberCond == null? ")": " or (" + numberCond + "))"));	
		}
		
	}
	/**
	 * Refine result list based on the result query from the database by add capability to display EIDSS imported
	 * records (without any administration or health unit)
	 * @param lst
	 */
	@Override
	protected void fillResultList(List<Object[]> lst) {
		if (isNotBindedEIDSS()){
			Patient p = new Patient();
			resultList = new ArrayList<CaseResultItem>();
			for (Object[] obj: lst) {
				CaseResultItem item = new CaseResultItem();
				p.setName((String)obj[0]);
				p.setMiddleName((String)obj[9]);
				p.setLastName((String)obj[10]);
				item.setPatientName(p.getFullName());
				item.setPatientAge((Integer)obj[1]);
				item.setGender((Gender)obj[2]);
				item.setCaseId((Integer)obj[14]);
				item.setCaseState((CaseState)obj[7]);
				item.setClassification((CaseClassification)obj[8]);
				item.setBirthDate((Date)obj[13]);
				item.setNotificationDate((Date)obj[5]);
				item.setRegistrationCode((String)obj[6]);
				resultList.add(item);
			}
		}else super.fillResultList(lst);
	}
	/**
	 * Index for quick report, simple search
	 * @return
	 */
	public Integer getCasesEIDSSnotBindedIndex() {
		return casesEIDSSnotBindedIndex;
	}
	/**
	 * Does current job with unbinded EIDSS records
	 * @return
	 */
	public boolean isNotBindedEIDSS(){
		if (caseFilters == null) return false;
		Integer stateIndex = caseFilters.getStateIndex();
		if (stateIndex != null) {
			return stateIndex.equals(getCasesEIDSSnotBindedIndex());
		}	else	return false;
	}

	/**
	 * Does current job with binded EIDSS records
	 * @return
	 */
	public boolean isBindedEIDSS(){
		if (caseFilters == null) return false;
		Integer stateIndex = caseFilters.getStateIndex();
		if (stateIndex != null) {
			return stateIndex.equals(CaseStateReportAZ.EIDSS_BINDED);
		}	else	return false;
	}
	/**
	 * Need to change filters behavior in case not binded EIDSS records for search by the name also!
	 * @param searchCriteria
	 */
	public void setSearchCriteria(SearchCriteria searchCriteria) {
		boolean flag = getCasesEIDSSnotBindedIndex().equals(caseFilters.getStateIndex());
		caseFilters.setSearchCriteria(searchCriteria);
		if (flag)
			caseFilters.setStateIndex(getCasesEIDSSnotBindedIndex()); // now we are have possibility to use another creiteria from filters

	}
}
