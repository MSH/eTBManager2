package org.msh.tb.az;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.cases.SearchCriteria;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.utils.date.Period;


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
	private Boolean eidssSearch;

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
		if (getEidssSearch()) 
			return null;
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
		if (isNotBindedEIDSS() || getEidssSearch()){
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
		"c.validationState, c.registrationCode, c.diagnosisType, p.birthDate, c.diagnosisDate, c.outcomeDate " +
		getFromHQL() + " join c.patient p " +
		"left outer join c.notificationUnit nu " +
		"left outer join c.ownerUnit tu " +
		"join c.notifAddress.adminUnit loc ".concat(dynamicConditions());
	}

	public Boolean getEidssSearch() {
		EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
		if (caseFilters!=null)
			if (getStateIndex()!=null || SearchCriteria.VALIDATION_STATE.equals(caseFilters.getSearchCriteria())){
				eidssSearch = false;
				return false;
			}
		eidssSearch = eidssFilters.isSearch();

		/*if (eidssSearch==true)
			super.setRestrictionExpressionStrings(new ArrayList<String>());*/
		
		/*if (eidssSearch==null){
			//eidssFilters.setSearch(false);
			if (eidssSearch==null)
				eidssSearch = false;
		}
		else
		if (eidssSearch==false)
			if (eidssFilters.isSearch()){
				eidssSearch = true;
				eidssFilters.setSearch(false);
			}*/
		return eidssSearch;
	}


	@Override
	public String getCountEjbql() {
		if (isNotBindedEIDSS() || getEidssSearch()){
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
		"left outer join c.ownerUnit tu " +
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

		if (!getEidssSearch())
			mountAdvancedSearchConditions();
		mountSingleSearchConditions();

		if (!hqlCondition.isEmpty())
			hqlCondition = " where ".concat(hqlCondition);

		return hqlCondition;
	}

	private void mountEIDSSCondition() {
		EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
		if (eidssFilters.getName()!=null){
			String[] names = eidssFilters.getName().split(" ");
			if (names.length != 0){
				String s="";
				for (String name: names) {
					if (s.length() > 1)
						s += " and ";
					name = name.replaceAll("'", "''");
					if (!"".equals(name))
						s += "(((upper(p.name) like '%" + name.toUpperCase() + 
						"%') or (upper(p.middleName) like '%" + name.toUpperCase() + 
						"%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%')) or "+
						"(upper(c.EIDSSComment) like '%"+name.toUpperCase()+"%'))";
				}
				addCondition(s);
			}
		}
		if (eidssFilters.getAddress()!=null && !"".equals(eidssFilters.getAddress())){
			//String s="(c.eidssData.address like '%"+eidssFilters.getAddress()+"%')";
			String s="(c.EIDSSComment like '%"+eidssFilters.getAddress()+"%')";
			addCondition(s);
		}
		if (eidssFilters.getId()!=null && !"".equals(eidssFilters.getId())){
			String s="(c.legacyId like '"+eidssFilters.getId()+"')";
			addCondition(s);
		}
		if (eidssFilters.getNotifUnit()!=null && !"".equals(eidssFilters.getNotifUnit())){
			//String s="(c.eidssData.notifUnit like '%"+eidssFilters.getNotifUnit()+"%')";
			String s="(c.EIDSSComment like '%"+eidssFilters.getNotifUnit()+"%')";
			addCondition(s);
		}
		if (eidssFilters.getTbunit().getAdminUnit()!=null){
			String s="";
			if (eidssFilters.getTbunit().getTbunit()!=null)
				s += "c.notificationUnit.id = "+eidssFilters.getTbunit().getTbunit().getId();
			else
				s += "c.notificationUnit.adminUnit.code like '"+eidssFilters.getTbunit().getAdminUnit().getCode()+"%'";
			addCondition(s);
		}
		if (eidssFilters.getYearBirth()!=null){
			//String s = "c.eidssData.yearBirth = "+eidssFilters.getYearBirth();
			String s = "((c.EIDSSComment like '%"+eidssFilters.getYearBirth()+"%') or " +
			"(year(p.birthDate) = "+eidssFilters.getYearBirth()+"))";
			addCondition(s);
		}
		//=====DATES=====
		Period d = eidssFilters.getInDate();
		if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
			//generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.eidssData.inDate","inDate");
			generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.inEIDSSDate","inDate");
		}
		d = eidssFilters.getRegDate();
		if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
			generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.registrationDate","regDate");
		}
		d = eidssFilters.getSysDate();
		if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
			generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.systemDate","sysDate");
		}
	}


	/**
	 * Refine single search condition to help understand EIDSS related queries
	 * AK
	 */
	@Override
	protected void mountSingleSearchConditions() {
		if (getEidssSearch()){
			mountEIDSSCondition();
			return;
		}		
		if (isNotBindedEIDSS()){
			addCondition("(not c.legacyId is null) and (c.ownerUnit is null) and (c.notificationUnit is null)"); 
			mountPatientNameCondition();  
			mountRegistrationDatesCondition();
			return;
		}
		if(isBindedEIDSS()){
			addCondition("(not c.legacyId is null) and (not (c.ownerUnit is null) or (not c.notificationUnit is null))");
			return;
		}
		if (isThirdCat()){
			addCondition("c.toThirdCategory = 1");
			return;
		}

		addCondition("nu.workspace.id=#{defaultWorkspace.id}");
		if (getValidationState()!=null){
			addCondition("c.validationState="+getValidationState().ordinal());
		}
		
		if (ValidationState.WAITING_VALIDATION.equals(getValidationState()))
			addCondition("c.diagnosisType in (0,1)");
		if (getStateIndex()==null){
			super.mountSingleSearchConditions();
		}
		else
			/*if (getStateIndex().equals(CaseFilters.ON_TREATMENT))
					hqlCondition += "c.state = " + CaseState.ONTREATMENT.ordinal();
				else*/ if (getStateIndex().equals(CaseFilters.CLOSED))
					addCondition("c.state > " + CaseState.TRANSFERRING.ordinal());
				else
					super.mountSingleSearchConditions();
		//hqlCondition += " or (c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.SUSPECT.ordinal()+")";
	}
	private boolean isThirdCat() {
		if (getStateIndex() != null) {
			return getStateIndex().equals(CaseStateReportAZ.thirdCat);
		}	else	return false;
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
	 * Generates HQL condition to a date field filter
	 * @param dtIni - Initial date of the filter
	 * @param dtEnd - Ending date of the filter
	 * @param dateFieldCase - Date field name in the HQL query
	 * @param dateFieldEIDSS - name field in eidssFilters
	 */
	protected void generateHQLPeriodEIDSS(Date dtIni, Date dtEnd, String dateFieldCase, String dateFieldEIDSS) {
		if (dtIni != null)
			addCondition("(" + dateFieldCase + ">=#{eidssFilters."+dateFieldEIDSS+".iniDate})");
		if (dtEnd != null)
			addCondition("(" + dateFieldCase + "<=#{eidssFilters."+dateFieldEIDSS+".endDate})");		
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
		if (isNotBindedEIDSS() || getEidssSearch()){
			Patient p = new Patient();
			resultList = new ArrayList<CaseResultItem>();
			for (Object[] obj: lst) {
				CaseResultItem item = new CaseResultItem();
				p.setName((String)obj[0]);
				p.setMiddleName((String)obj[9]);
				p.setLastName((String)obj[10]);

				TbCase tbcase = item.getTbcase();
				tbcase.setPatient(p);
				tbcase.setAge((Integer)obj[1]);
				p.setGender((Gender)obj[2]);
				tbcase.setId((Integer)obj[14]);
				tbcase.setState((CaseState)obj[7]);
				tbcase.setClassification((CaseClassification)obj[8]);
				p.setBirthDate((Date)obj[13]);
				tbcase.setRegistrationDate((Date)obj[5]);
				tbcase.setRegistrationCode((String)obj[6]);
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
		if (getStateIndex() != null) {
			return getStateIndex().equals(getCasesEIDSSnotBindedIndex());
		}	else	return false;
	}

	/**
	 * Does current job with binded EIDSS records
	 * @return
	 */
	public boolean isBindedEIDSS(){
		if (getStateIndex() != null) {
			return getStateIndex().equals(CaseStateReportAZ.EIDSS_BINDED);
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

	private Integer getStateIndex(){
		if (caseFilters == null) return null;
		return caseFilters.getStateIndex();
	}

	private ValidationState getValidationState(){
		if (caseFilters == null) return null;
		return caseFilters.getValidationState();
	}
}
