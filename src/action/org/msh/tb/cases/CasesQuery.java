package org.msh.tb.cases;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.msh.tb.ETB;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.Period;

import org.msh.tb.entities.Tag;

// 02.04.2012 Change some from public to to protected Alexey Kurasov
/**
 * Return the result search of cases based on the filters defined in {@link CaseFilters} 
 * @author Ricardo Memoria
 *
 */
@Name("cases")
@BypassInterceptors
public class CasesQuery extends EntityQuery<CaseResultItem> {
	private static final long serialVersionUID = 7867819205241149388L;

	protected CaseFilters caseFilters;
	private Workspace defaultWorkspace;
	protected List<CaseResultItem> resultList;
	protected String hqlCondition;

	private static final String[] orderValues = {"p.recordNumber, c.caseNumber", "p.gender,p.name", 
		"c.classification", "#{cases.namesOrderBy}", "c.age", "upper(nu.name.name1)", "c.notifAddress.adminUnit.parent.name.name1", 
		"c.notifAddress.adminUnit.name.name1", "c.state", "c.registrationDate", "c.treatmentPeriod.iniDate", "c.validationState"};

	private static final String[] inverseOrderValues = {"p.recordNumber desc, c.caseNumber", "p.gender desc, upper(p.name) desc, upper(p.middleName) desc, upper(p.lastName) desc", 
		"c.classification desc", "#{cases.namesOrderBy}", "c.age desc", "upper(nu.name.name1) desc", "c.notifAddress.adminUnit.parent.name.name1 desc", 
		"c.notifAddress.adminUnit.name.name1 desc", "c.state desc", "c.registrationDate desc", "c.treatmentPeriod.iniDate desc", "c.validationState desc"};

	// static filters
	private static final String[] restrictions = {/*"nu.id = #{caseFilters.tbunit.id}",*/
		/*"p.recordNumber = #{caseFilters.recordNumber}", */ // just search by recordNumber if workspace.displayCaseNumber = RECORD_NUMBER
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
		"nu.healthSystem.id = #{userWorkspace.healthSystem.id}",
		"year(p.birthDate) = #{caseFilters.birthYear}",
	"exists(select t.id from c.tags t where t.id = #{caseFilters.tagid})"};

	private static final String notifCond = "(nu.id = #{caseFilters.tbunitselection.tbunit.id})";
	private static final String treatCond = "c.ownerUnit.id =  #{caseFilters.tbunitselection.tbunit.id}";

	private static final String notifRegCond = "(nu.adminUnit.code like #{caseFilters.tbAdminUnitLike})";
	private static final String treatRegCond = "c.ownerUnit.adminUnit.code like #{caseFilters.tbAdminUnitLike}";
	
	private static final String prescribedMedicineCond = "exists(SELECT cs.id, pm.id " + 
															"FROM " + ETB.getWsClassName(TbCase.class) + " cs " +
															"INNER JOIN cs.prescribedMedicines pm " + 
															"WHERE 	pm.medicine.id = #{caseFilters.prescribedMedicine.id} " + 
															"and c.id = cs.id)";

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.EntityQuery#getResultList()
	 */
	@Override
	public List<CaseResultItem> getResultList() {
		if (resultList == null)
		{
			javax.persistence.Query query = createQuery();
			List<Object[]> lst = query==null ? null : query.getResultList();
			fillResultList(lst);
		}
		return resultList;
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "select p.name, c.age, p.gender, p.recordNumber, c.caseNumber, " + 
		"c.treatmentPeriod.iniDate, c.registrationDate, nu.name.name1, " +
		"loc.name.name1, loc.code, c.id, " +
		"c.treatmentPeriod.endDate, c.state, c.classification, p.middleName, p.lastName, " +
		"c.validationState, c.registrationCode, c.diagnosisType, p.birthDate, c.diagnosisDate, c.outcomeDate " +
		getFromHQL() + " join c.patient p " +
		"join c.notificationUnit nu " +
		"join c.notifAddress.adminUnit loc ".concat(dynamicConditions());
	}


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	public String getCountEjbql() {
		return "select count(*) " + getFromHQL() + 
		" join c.patient p " +
		"join c.notificationUnit nu " + 
		dynamicConditions();
	}


	/**
	 * Return join tables in use in this query
	 * @return
	 */
	public String getFromHQL() {
		return "from " + ETB.getWsClassName(TbCase.class) + " c";
	}


	/**
	 * Generate HQL conditions for filters that cannot be included in the restrictions clause
	 * @return
	 */
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
					addCondition(notifCond);
					break;
				case TREATMENT_UNIT:
					addCondition(treatCond);
					break;
				case BOTH:
					addCondition("(" + treatCond + " or " + notifCond + ")");
				}
			}
			else // region was set ? 
				if (getCaseFilters().getTbunitselection().getAdminUnit() != null) {
					switch (filterUnit) {
					case NOTIFICATION_UNIT:
						addCondition(notifRegCond);
						break;
					case TREATMENT_UNIT:
						addCondition(treatRegCond);
						break;
					case BOTH:
						addCondition("(" + treatRegCond + " or " + notifRegCond + ")");
					}
				}
		}

		if(caseFilters.getPrescribedMedicine() != null && caseFilters.getPrescribedMedicine().getId() != 0){
			addCondition(prescribedMedicineCond);
		}
		
		if(caseFilters.getClassifications().getSelectedItems().size()>0){
			String clas = " c.classification in (";
			for(CaseClassification c : caseFilters.getClassifications().getSelectedItems()){
				clas = clas + c.ordinal() + ",";
			}
			clas = clas.substring(0, clas.length()-1);
			clas = clas + ")";
			addCondition(clas);
		}
		
		mountAdvancedSearchConditions();
		mountSingleSearchConditions();
		
		if (!hqlCondition.isEmpty())
			hqlCondition = " where ".concat(hqlCondition);

		return hqlCondition;
	}


	/**
	 * Generate HQL instructions for advanced search conditions
	 */
	protected void mountAdvancedSearchConditions() {
		CaseFilters caseFilters = getCaseFilters();
		// include filters by date
		Date dtIni = caseFilters.getIniDate();
		Date dtEnd = caseFilters.getEndDate();
		if ((dtIni != null) || (dtEnd != null)) {
			if (caseFilters.isDiagnosisDate())
				generateHQLPeriod(dtIni, dtEnd, "c.diagnosisDate");

			if (caseFilters.isOutcomeDate())
				generateHQLPeriod(dtIni, dtEnd, "c.outcomeDate");

			if (caseFilters.isIniTreatmentDate())
				generateHQLPeriod(dtIni, dtEnd, "c.treatmentPeriod.iniDate");

			if (caseFilters.isRegistrationDate())
				generateHQLPeriod(dtIni, dtEnd, "c.registrationDate");
		}

		if (DisplayCaseNumber.REGISTRATION_CODE.equals(getDefaultWorkspace().getDisplayCaseNumber())) {
			if ((caseFilters.getRecordNumber() != null) && (!caseFilters.getRecordNumber().isEmpty()))
				addCondition("c.registrationCode = #{casesFilter.recordNumber}");
		}
	}


	/**
	 * Generate HQL instructions for single search by patient name 
	 */
	protected void mountSingleSearchConditions() {
		CaseFilters caseFilters = getCaseFilters();
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

		Integer stateIndex = caseFilters.getStateIndex();
		if (stateIndex != null) {
			String cond;
			switch (stateIndex) {
			// CLOSED
			case 100: cond = "c.state > " + Integer.toString( CaseState.ONTREATMENT.ordinal() );
			break;

			// SUSPECT NOT ON TREATMENT
			case 200: cond = "c.state = " + CaseState.WAITING_TREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.SUSPECT.ordinal();
			break;
			
			// SUSPECT NOT ON TREATMENT
			case 300: cond = "c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.SUSPECT.ordinal();
				break;
				
				// CONFIRMED ON TREATMENT
			case 400: cond = "c.state = " + CaseState.ONTREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal();
			break;
			
			// CONFIRMED NOT ON TREATMENT
			case 500: cond = "c.state = " + CaseState.WAITING_TREATMENT.ordinal() + " and c.diagnosisType = " + DiagnosisType.CONFIRMED.ordinal();
			break;

			// WAIT TO START TREATMENT
			case 0: cond = "c.state = " + CaseState.WAITING_TREATMENT.ordinal() + " and c.diagnosisType != " + DiagnosisType.SUSPECT.ordinal();
			break;
			
			// ON TREATMENT
			case 600: cond = "c.state = " + CaseState.ONTREATMENT.ordinal() + 
							 " and c.ownerUnit.id = " + caseFilters.getUnitId();
			break;
			
			// ON TREATMENT - TRANSFERIN
			case 601: cond = "c.state = " + CaseState.ONTREATMENT.ordinal() + 
							 " and c.ownerUnit.id = " + caseFilters.getUnitId() +
							 " and exists(select id from TreatmentHealthUnit t" +
							 	" where t.period.iniDate > c.treatmentPeriod.iniDate and period.endDate = c.treatmentPeriod.endDate and c.state= " + CaseState.ONTREATMENT.ordinal() +
							 	" and transferring=false and tbunit.id = "+ caseFilters.getUnitId() +
							 	" and tbcase.id = c.id)";
			break;
			
			// ON TREATMENT / TRANSFERING - TRANSFEROUT
			case 602: cond = " exists(select id from TreatmentHealthUnit t" +
							 " where t.period.endDate < c.treatmentPeriod.endDate" +
							 " and c.state in (" + CaseState.ONTREATMENT.ordinal() + "," + CaseState.TRANSFERRING.ordinal() + ")" +
							 " and tbunit.id = "+ caseFilters.getUnitId() +
							 " and tbcase.id = c.id)";
			
			break;
			
			// MISSING EXAM CASES
			case 603: cond = " exists(select tc.id " +
					         "from MedicalExamination med " + 
					         "inner join med.tbcase tc " +
							 "inner join tc.ownerUnit as tu " +
							 "inner join tu.adminUnit as a " +
							 "where tu.workspace.id = " + defaultWorkspace.getId().toString() + 
							 " and med.nextAppointment is not null and med.nextAppointment + 15 < current_date " +
							 " and tc.state < " + CaseState.CURED.ordinal() +
							 " and med.date = (select max(m.date) from MedicalExamination m where m.tbcase.id = tc.id)" +
							 " and tu.id = "+ caseFilters.getUnitId() +
							 " and tc.id = c.id)";
			
			break;

			default: cond = "c.state = " + stateIndex.toString();
			break;
			}

			addCondition(cond);
		}
	}


	/**
	 * Convert the number entered by the user and separates patient number and case number
	 * @return
	 */
	protected String generateHQLPatientNumber() {
		String pat = getCaseFilters().getPatient();
		if (pat == null)
			return null;

		// patient code is generated by the system
		if (DisplayCaseNumber.REGISTRATION_CODE.equals(getDefaultWorkspace().getDisplayCaseNumber())) {
			return "c.registrationCode = #{caseFilters.patient}";
		}
		else {
			Integer patNumber = null;
			Integer caseNumber = null;

			String[] vals = pat.split("-");
			try {
				patNumber = Integer.parseInt(vals[0]);
			} catch (Exception e) {
				patNumber = null;
			}
			if (vals.length > 1)
				try {
					caseNumber = Integer.parseInt(vals[1]);
				} catch (Exception e) {
					caseNumber = null;
				}

				if (patNumber == null)
					return null;

				String s = "p.recordNumber = " + patNumber;
				if (caseNumber != null)
					s = "(" + s + ") and (c.caseNumber = " + caseNumber + ")";
				return s;
		}
	}

	/**
	 * Generates HQL condition to a date field filter
	 * @param dtIni - Initial date of the filter
	 * @param dtEnd - Ending date of the filter
	 * @param dateField - Date field name in the HQL query
	 */
	protected void generateHQLPeriod(Date dtIni, Date dtEnd, String dateField) {
		if (dtIni != null)
			addCondition("(" + dateField + ">=#{caseFilters.iniDate})");
		if (dtEnd != null)
			addCondition("(" + dateField + "<=#{caseFilters.endDate})");		
	}


	/**
	 * Includes a condition to the hql instruction
	 * @param hql - the hql instruction
	 * @param condition - the condition to be included
	 */
	protected void addCondition(String condition) {
		if (hqlCondition.isEmpty())
			hqlCondition = condition;
		else hqlCondition = hqlCondition + " and " + condition;
	}

	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	@Override
	public String getOrder() {
		String s = getCaseFilters().isInverseOrder()? inverseOrderValues[getCaseFilters().getOrder()] : orderValues[getCaseFilters().getOrder()];
		s = Expressions.instance().createValueExpression(s).getValue().toString();
		return s;
	}


	@Override
	public Integer getMaxResults() {
		return 50;
	}

	/**
	 * Create the result list based on the result query from the database
	 * @param lst
	 */
	protected void fillResultList(List<Object[]> lst) {
		List<AdministrativeUnit> adminUnits = getCaseFilters().getAuselection().getOptionsLevel1();
		if (adminUnits == null) {
			adminUnits = new ArrayList<AdministrativeUnit>();
			adminUnits.add(getCaseFilters().getAuselection().getUnitLevel1());
		}

		resultList = new ArrayList<CaseResultItem>();
		for (Object[] obj: lst) {
			CaseResultItem item = new CaseResultItem();

			TbCase tbcase = item.getTbcase();
			Patient p = new Patient();
			tbcase.setPatient(p);

			p.setName((String)obj[0]);
			p.setMiddleName((String)obj[14]);
			p.setLastName((String)obj[15]);
			p.setGender((Gender)obj[2]);

			tbcase.setAge((Integer)obj[1]);
			tbcase.setTreatmentPeriod( new Period((Date)obj[5], (Date)obj[11]) ); 
			tbcase.setRegistrationDate((Date)obj[6]);
			Tbunit unit = new Tbunit();
			unit.getName().setName1((String)obj[7]);
			tbcase.setOwnerUnit(unit);
			item.setAdminUnitDisplay((String)obj[8]);
			tbcase.setId((Integer)obj[10]);
			tbcase.setState((CaseState)obj[12]);
			tbcase.setClassification((CaseClassification)obj[13]);
			tbcase.setValidationState((ValidationState)obj[16]);
			p.setBirthDate((Date)obj[19]);
			tbcase.setRegistrationCode((String)obj[17]);
			p.setRecordNumber((Integer)obj[3]);
			tbcase.setCaseNumber((Integer)obj[4]);				
			tbcase.setDiagnosisType((DiagnosisType)obj[18]);
			tbcase.setDiagnosisDate((Date)obj[20]);
			tbcase.setOutcomeDate((Date)obj[21]);

			// search for administrative unit
			AdministrativeUnit adm = null;
			String code = (String)obj[9];
			for (AdministrativeUnit aux: adminUnits) {
				if (aux.isSameOrChildCode(code)) {
					if (!code.equals(aux.getCode()))
						adm = aux;
					break;
				}
			}

			if (adm != null)
				item.setAdminUnitDisplay(adm.getName().getDefaultName() + ", " + item.getAdminUnitDisplay());

			resultList.add(item);
		}
	}


	@Override
	@Transactional
	public boolean isNextExists()
	{
		boolean b = (resultList!=null) && (resultList.size() > getMaxResults());
		return b;
	}

	@Override
	public void refresh() {
		resultList = null;
		hqlCondition = null;

		super.refresh();
	}


	public String getNamesOrderBy() {
		boolean descOrder = getCaseFilters().isInverseOrder();
		switch (getDefaultWorkspace().getPatientNameComposition()) {
		case FULLNAME: 
			return "upper(p.name)" + (descOrder? " desc": "");
		case FIRSTSURNAME: 
			return (descOrder? "upper(p.name) desc, upper(p.middleName) desc": "upper(p.name), upper(p.middleName)");
		case LAST_FIRST_MIDDLENAME: 
			return (descOrder? "upper(p.lastName) desc, upper(p.name) desc, upper(p.middleName) desc": "upper(p.lastName), upper(p.name), upper(p.middleName)");
		case SURNAME_FIRSTNAME: 
			return "upper(p.middleName) desc, upper(p.name) desc";
		}
		// default if FIRST_MIDDLE_LASTNAME

		return descOrder? "upper(p.name) desc, upper(p.middleName) desc, upper(p.lastName) desc": "upper(p.name), upper(p.middleName), upper(p.lastName)";
	}

	/**
	 * Return the default workspace in use
	 * @return
	 */
	public Workspace getDefaultWorkspace() {
		if (defaultWorkspace == null)
			defaultWorkspace = (Workspace)Component.getInstance("defaultWorkspace");
		return defaultWorkspace;
	}


	/**
	 * Return the filters to be applied to the search
	 * @return
	 */
	public CaseFilters getCaseFilters() {
		if (caseFilters == null)
			caseFilters = (CaseFilters)Component.getInstance("caseFilters", true);
		return caseFilters;
	}
}
