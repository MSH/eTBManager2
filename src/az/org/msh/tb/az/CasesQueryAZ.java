package org.msh.tb.az;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.TbCaseAZ;
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
	private static final String notifCondAZ = "(c.notificationUnit.id = #{caseFilters.tbunitselection.tbunit.id})";
	private static final String treatCondAZ = "c.ownerUnit.id =  #{caseFilters.tbunitselection.tbunit.id}";
	private static final String notifRegCondAZ = "(c.notificationUnit.id in (select id from org.msh.tb.entities.Tbunit tbu where tbu.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String treatRegCondAZ = "(c.ownerUnit.id in (select id from org.msh.tb.entities.Tbunit tbu1 where tbu1.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String notifAdrAdmUnitAZ="c.notifAddress.adminUnit.code like ";
	private static final String notifAdrAdmUnitRegAZ="c.notifAddress.adminUnit.code = ";
	//private static final Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);
	
	private static final String[] orderValues = {"p.recordNumber, c.caseNumber", "p.gender,p.name", 
		"c.classification", "#{cases.namesOrderBy}", "c.age", "upper(nu.name.name1)", "c.notifAddress.adminUnit.name.name1", 
		"c.notifAddress.adminUnit.name.name1", "c.state", "c.registrationDate", "c.treatmentPeriod.iniDate", "c.validationState"};

	private static final String[] inverseOrderValues = {"p.recordNumber desc, c.caseNumber", "p.gender desc, upper(p.name) desc, upper(p.middleName) desc, upper(p.lastName) desc", 
		"c.classification desc", "#{cases.namesOrderBy}", "c.age desc", "upper(nu.name.name1) desc", "c.notifAddress.adminUnit.name.name1 desc", 
		"c.notifAddress.adminUnit.name.name1 desc", "c.state desc", "c.registrationDate desc", "c.treatmentPeriod.iniDate desc", "c.validationState desc"};

	
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
	public String getOrder() {
		if (getCaseFilters().getOrder()>100)
			return null;
		String s = getCaseFilters().isInverseOrder()? inverseOrderValues[getCaseFilters().getOrder()] : orderValues[getCaseFilters().getOrder()];
		s = Expressions.instance().createValueExpression(s).getValue().toString();
		return s;
	}

	
	@Override
	public String getEjbql() {
		if (isNotBindedEIDSS() || isTagSearch()){
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
	@Override
	public String getCountEjbql() {
		if (isNotBindedEIDSS() || isTagSearch()){
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

/*	@Override
	protected Query createQuery() {
      parseEjbql();

      evaluateAllParameters();

      joinTransaction();
	  
      String hql = getEjbql();
		StringBuilder builder = new StringBuilder().append(hql);

		boolean bWhere = WHERE_PATTERN.matcher(builder).find();
		
		for (int i = 0; i < getRestrictions().size(); i++) {
			Object parameterValue = getRestrictionParameters().get(i)
					.getValue();
			if (isRestrictionParameterSet(parameterValue)) {
				if (bWhere) {
					builder.append(" and ");
				} else {
					builder.append(" where ");
					bWhere = true;
				}
				builder.append(getRestrictions().get(i).getExpressionString());
			}
		}
		hql = builder.toString();
      javax.persistence.Query query = getEntityManager().createQuery( hql );
      return query;
	}*/
	
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
		
		CaseFiltersAZ cfaz = (CaseFiltersAZ)App.getComponent("caseFiltersAZ");
		if (SearchCriteria.CUSTOM_FILTER.equals(getSearchCriteria())){
			if (cfaz.getTag()!=null)
				getCaseFilters().setTagid(cfaz.getTag().getId());
		}
		else{
			cfaz.setTag(null);
		}
		if (!isNotBindedEIDSS()){
			// health unit condition
			if (filterUnit != null) {
				// health unit was set ?
				if (getCaseFilters().getTbunitselection().getTbunit() != null) {
					switch (filterUnit) {
					case NOTIFICATION_UNIT:
						addCondition(notifCondAZ);
						break;
					case TREATMENT_UNIT:
						addCondition(treatCondAZ);
						break;
					case BOTH:
						addCondition("(" + treatCondAZ + " or " + notifCondAZ + ")");
					}
				}
				else // region was set ? 
					if (getCaseFilters().getTbunitselection().getAdminUnit() != null) {
						switch (filterUnit) {
						case NOTIFICATION_UNIT:
							addCondition(notifRegCondAZ);
							break;
						case TREATMENT_UNIT:
							if((caseFilters.getStateIndex()==null && caseFilters.getSearchCriteria().equals(SearchCriteria.CASE_TAG))
									|| caseFilters.getStateIndex()!= CaseFilters.TRANSFER_OUT)
									addCondition(treatRegCondAZ);
							break;
						case BOTH:{
							addCondition("(" + treatRegCondAZ + " or " + notifRegCondAZ);
							UserWorkspace userWorkspace = (UserWorkspace) Component.getInstance("userWorkspace");
							if (UserView.ADMINUNIT.equals(userWorkspace.getView())){
								hqlCondition += " or "+(userWorkspace.getAdminUnit().getLevel()==1 ? notifAdrAdmUnitAZ : notifAdrAdmUnitRegAZ) + getAdminUnitLike(userWorkspace.getAdminUnit());
							}
							hqlCondition += ")";
							}
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
	
	@Override
	protected void mountAdvancedSearchConditions() {
		CaseFiltersAZ cf = (CaseFiltersAZ) App.getComponent("caseFiltersAZ");
		if (!"".equals(cf.getCaseNums()) && cf.getCaseNums() != null){
			String hql = "p.recordNumber in (";
			String[] nums = cf.getCaseNums().split(",");
			for (String num:nums){
				if (num.contains("-")){
					String [] range = num.split("-");
					try{
						int beg = Integer.parseInt(range[0].trim());
						int end = Integer.parseInt(range[range.length-1].trim());
						for (int i = beg; i <= end; i++) {
							hql += i+",";
						}
					} catch (Exception e){
						
					}
				}
				else{
					hql += Integer.parseInt(num.trim())+",";
				}
			}
			hql = hql.substring(0, hql.length()-1)+")";
			addCondition(hql);
		}
		super.mountAdvancedSearchConditions();
	}
	
	/**
	 * Refine single search condition to help understand EIDSS related queries
	 * AK
	 */
	@Override
	protected void mountSingleSearchConditions() {
		if (isNotBindedEIDSS()){
			addCondition("(not c.legacyId is null) and (c.ownerUnit is null) and (c.notificationUnit is null)"); 
			mountPatientNameCondition();  
			mountRegistrationDatesCondition();
		}
		else 
			if(isBindedEIDSS())
			addCondition("(not c.legacyId is null) and (not (c.ownerUnit is null) or (not c.notificationUnit is null))");
		else 
			if (isThirdCat())
				addCondition("c.toThirdCategory = 1");
		else 
			if (isTransferToUserUnit())
				addCondition("c.state = " + CaseState.TRANSFERRING.ordinal() + " and c.ownerUnit.id = #{userWorkspace.tbunit.id}");
		else
			if (!isTagSearch())
			{
			addCondition("c.notificationUnit.workspace.id=#{defaultWorkspace.id}");
			if (ValidationState.WAITING_VALIDATION.equals(getValidationState()))
				addCondition("c.diagnosisType in (0,1)");
			if (getStateIndex()==null){
				super.mountSingleSearchConditions();
			}
			else
				/*if (getStateIndex()==400)
					hqlCondition += "c.state = " + CaseState.ONTREATMENT.ordinal();
				else*/ if (getStateIndex().equals(CaseFilters.CLOSED))
					addCondition("c.state > " + CaseState.TRANSFERRING.ordinal());
				else
					super.mountSingleSearchConditions();
				//hqlCondition += " or (c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.SUSPECT.ordinal()+")";
			
		}

	}
	private boolean isThirdCat() {
		if (getStateIndex() != null) {
			return getStateIndex().equals(CaseStateReportAZ.thirdCat);
		}	else	return false;
	}
	
	private boolean isTransferToUserUnit() {
		if (getStateIndex() != null) {
			return getStateIndex().equals(CaseStateReportAZ.transferToUserUnit);
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
	 * Change order sorting patient list
	 * */
	public void changeOrder(){
		int ind = caseFilters.getNewOrder(); 
		final boolean inverseOrder = caseFilters.isInverseOrder();
		getResultList();
		if (ind > 100)
		switch (ind) {
			//====================SORT BY EIDSS FIELDS==========================
			case 101: { //eidss name
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						String name1 = az1.getEIDSSName();
						String name2 = az2.getEIDSSName();
						
						if (name1.equals(name2)){
							name2 = name1+"_"+o2.getPatient().getId();
						}
						Collator myCollator = Collator.getInstance();			    
						return (!inverseOrder ? myCollator.compare(name1,name2) : myCollator.compare(name2,name1));
					}
				});
				break;
			}
			case 102: { //eidss date of birth
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						Date d1 = az1.getEIDSSBirthDate();
						Date d2 = az2.getEIDSSBirthDate();
						
						if (d1 == null || d2 == null){
							Integer a1 = az1.getEIDSSAge();
							Integer a2 = az2.getEIDSSAge();
							return a2.compareTo(a1);
						}
						
						return compareDates(o1, o2, d1, d2);
					}
				});
				break;
			}
			case 103: { //eidss address
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						String name1 = az1.getEIDSSNotifAddress();
						String name2 = az2.getEIDSSNotifAddress();
						    
						if (name1.equals(name2)){
							name2 = name1+"_"+o2.getPatient().getId();
						}
						Collator myCollator = Collator.getInstance();		
						return (!inverseOrder ? myCollator.compare(name1,name2) : myCollator.compare(name2,name1));
					}
				});
				break;
			}
			case 104: { //eidss notification date
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						Date d1 = az1.getEIDSSNotifDate();
						Date d2 = az2.getEIDSSNotifDate();
						
						return compareDates(o1, o2, d1, d2);
					}
				});
				break;
			}
			case 105: { //eidss inner date
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						Date d1 = az1.getEIDSSInnerDate();
						Date d2 = az2.getEIDSSInnerDate();
						
						return compareDates(o1, o2, d1, d2);
					}
				});
				break;
			}
			case 106: { //eidss notif unit
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
						TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
						
						String name1 = az1.getEIDSSNotifUnit();
						String name2 = az2.getEIDSSNotifUnit();
						    
						if (name1.equals(name2)){
							name2 = name1+"_"+o2.getPatient().getId();
						}
						
						Collator myCollator = Collator.getInstance();		
						return (!inverseOrder ? myCollator.compare(name1,name2) : myCollator.compare(name2,name1));
					}
				});
				break;
			}
			case 107: { //eidss id
				Collections.sort(resultList, new Comparator<CaseResultItem>() {

					@Override
					public int compare(CaseResultItem o1, CaseResultItem o2) {
						String name1 = o1.getTbcase().getLegacyId();
						String name2 = o2.getTbcase().getLegacyId();
						if (name1 == null)	name1 = "";
						if (name2 == null)	name2 = "";
						if (name1.equals(name2)){
							name2 = name1+"_"+o2.getPatient().getId();
						}
						
						Collator myCollator = Collator.getInstance();		
						return (!inverseOrder ? myCollator.compare(name1,name2) : myCollator.compare(name2,name1));
					}
				});
				break;
			}
		}
	}
	
	/**
	 * Compare two patients by full names
	 * */
	protected int compareNames(CaseResultItem o1, CaseResultItem o2) {
		String name1, name2;
		name1 = o1.getPatient().getLastName() == null ? o1.getPatient().getName() : o1.getPatient().getLastName();
		name2 = o2.getPatient().getLastName() == null ? o2.getPatient().getName() : o2.getPatient().getLastName();
		
		if (name1.equals(name2)){
			name1 = o1.getPatient().getName();
			name2 = o2.getPatient().getName();
		}
		if (name1.equals(name2)){
			name1 = o1.getPatient().getMiddleName() == null ? o1.getPatient().getName() : o1.getPatient().getMiddleName();
			name2 = o2.getPatient().getMiddleName() == null ? o2.getPatient().getName() : o2.getPatient().getMiddleName();
		}
		if (name1.equals(name2)){
			name2 = name1+"_"+o2.getPatient().getId();
		}
		Collator myCollator = Collator.getInstance();			    
		return (!caseFilters.isInverseOrder() ? myCollator.compare(name1,name2) : myCollator.compare(name2,name1));
	}

	/**
	 * Compare two dates
	 */
	protected int compareDates(CaseResultItem o1, CaseResultItem o2, Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return compareNames(o1,o2);
		else{
			if (d1 == null)
				return -1;
			if (d2 == null)
				return 1;
		}
		if (d1.equals(d2))
			return compareNames(o1, o2);
		return (!caseFilters.isInverseOrder() ? d1.compareTo(d2) : d2.compareTo(d1));
	}
	
	/**
	 * Refine result list based on the result query from the database by add capability to display EIDSS imported
	 * records (without any administration or health unit)
	 * @param lst
	 */
	@Override
	protected void fillResultList(List<Object[]> lst) {
		if (isNotBindedEIDSS() || isTagSearch()){
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
		boolean res = false;
		/*if (getSearchCriteria()!=null)
			res = getSearchCriteria().equals(SearchCriteria.CUSTOM_FILTER);*/
		if (getStateIndex() != null) {
			res = res||getStateIndex().equals(getCasesEIDSSnotBindedIndex());
		}	
		return res;
	}
	
	public boolean isTagSearch(){
		if (getSearchCriteria()!=null)
			return getSearchCriteria().equals(SearchCriteria.CASE_TAG);
		return false;
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
	
	private SearchCriteria getSearchCriteria(){
		if (caseFilters == null) 
			caseFilters = (CaseFilters)Component.getInstance("caseFilters");
		return caseFilters.getSearchCriteria();
	}
	
	private ValidationState getValidationState(){
		if (caseFilters == null) return null;
		return caseFilters.getValidationState();
	}
}
