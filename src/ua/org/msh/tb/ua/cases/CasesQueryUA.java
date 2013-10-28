package org.msh.tb.ua.cases;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.ETB;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.cases.SearchCriteria;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.ua.entities.enums.TreatmentType;


/**
 * Query about cases. Use instead standard CasesQuery to fix error related
 * to search cases without TBUnit:
 * @author alexey
 */
@Name("casesUA")
@BypassInterceptors
public class CasesQueryUA extends CasesQuery{
		
	private static final long serialVersionUID = -7293313123644540670L;
	private static final String notifCondUA = "(nu.id = #{caseFilters.tbunitselection.tbunit.id})";
	private static final String treatCondUA = "tu.id =  #{caseFilters.tbunitselection.tbunit.id}";
	private static final String notifRegCondUA = "(nu.id in (select id from org.msh.tb.entities.Tbunit tbu where tbu.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String treatRegCondUA = "(tu.id in (select id from org.msh.tb.entities.Tbunit tbu1 where tbu1.adminUnit.code like #{caseFilters.tbAdminUnitAnyLevelLike}))";
	private static final String notifAdrAdmUnitUA="c.notifAddress.adminUnit.code like ";
	private static final String notifAdrAdmUnitRegUA="c.notifAddress.adminUnit.code = ";
	private static final String sourcesCond = "exists(SELECT cs.id, pm.id " + 
													"FROM " + ETB.getWsClassName(TbCase.class) + " cs " +
													"INNER JOIN cs.prescribedMedicines pm " + 
													"WHERE 	pm.source.id = #{caseFiltersUA.source.id} " + 
													"and c.id = cs.id)";
	private static final String hosp = "(select cd.id from CaseDataUA cd " +
			"where ((cd.dischargeDate is null) and (cd.hospitalizationDate is not null) " +
			"or (cd.dischargeDate2 is null) and (cd.hospitalizationDate2 is not null) " +
			"or (cd.dischargeDate3 is null) and (cd.hospitalizationDate3 is not null) " +
			"or (cd.dischargeDate4 is null) and (cd.hospitalizationDate4 is not null) " +
			"or (cd.dischargeDate5 is null) and (cd.hospitalizationDate5 is not null)))";
	
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
		return "select p.name, c.age, p.gender, p.recordNumber, c.caseNumber, " + 
			"c.treatmentPeriod.iniDate, c.diagnosisDate, nu.name.name1, " +
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
		
		CaseFiltersUA cfua = (CaseFiltersUA)App.getComponent("caseFiltersUA");
		if (SearchCriteria.CUSTOM_FILTER.equals(getSearchCriteria())){
			if (cfua.getTag()!=null)
				getCaseFilters().setTagid(cfua.getTag().getId());
		}
		else{
			cfua.setTag(null);
		}
		
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
					if((caseFilters.getStateIndex()==null && caseFilters.getSearchCriteria().equals(SearchCriteria.CASE_TAG))
							|| caseFilters.getStateIndex()!= CaseFilters.TRANSFER_OUT)
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

		if(cfua.getSource() != null && cfua.getSource().getId() != 0){
			addCondition(sourcesCond);
		}
		
		mountAdvancedSearchConditions();
		mountSingleSearchConditions();
		
		if (getStateIndex()!=null){
			if (getStateIndex().equals(CaseFilters.CLOSED))
				addCondition("c.state > " + CaseState.TRANSFERRING.ordinal());
			if (getStateIndex()>=100 && getStateIndex()<200){
				addCondition("(c.diagnosisType = 1 or c.classification = 0 or c.state>1)");
				Integer hsID = null;
				UserWorkspace userWorkspace = (UserWorkspace) App.getComponent("userWorkspace");
				if (userWorkspace.getHealthSystem() != null)
					hsID = userWorkspace.getHealthSystem().getId();
				if (hsID!=null)
					addCondition("c.notificationUnit.healthSystem.id = #{userWorkspace.healthSystem.id}");
			}
			if (getStateIndex()==700)
				addCondition("c.diagnosisType = 0 and c.classification = 1 and c.state = 0");
			if (getStateIndex()==701)
				addCondition("c.diagnosisType = 0 and c.classification = 1 and c.state = 1");
			
		}
		
		if (hqlCondition.contains("c.diagnosisType = 1")){
			hqlCondition = hqlCondition.replace("c.diagnosisType = 1", "(c.diagnosisType = 1 or c.diagnosisType is NULL)");
		}
		
		if (!hqlCondition.isEmpty())
			hqlCondition = " where ".concat(hqlCondition);

		return hqlCondition;
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
	
	@Override
	protected void mountAdvancedSearchConditions() {
		super.mountAdvancedSearchConditions();
		CaseFiltersUA caseFiltersUA = (CaseFiltersUA) Component.getInstance("caseFiltersUA");
		if (InfectionSite.PULMONARY.equals(caseFilters.getInfectionSite()))
			if (caseFiltersUA.getPulmonaryType()!=null)
				addCondition("c.pulmonaryType.id = "+caseFiltersUA.getPulmonaryType().getId());
		if (InfectionSite.EXTRAPULMONARY.equals(caseFilters.getInfectionSite()) || InfectionSite.BOTH.equals(caseFilters.getInfectionSite())){
			if (caseFiltersUA.getExtrapulmonaryType()!=null)
				addCondition("(c.extrapulmonaryType.id = "+caseFiltersUA.getExtrapulmonaryType().getId()+" OR "+"c.extrapulmonaryType2.id = "+caseFiltersUA.getExtrapulmonaryType().getId()+")");
			if (caseFiltersUA.getExtrapulmonaryType2()!=null)
				addCondition("(c.extrapulmonaryType.id = "+caseFiltersUA.getExtrapulmonaryType2().getId()+" OR "+"c.extrapulmonaryType2.id = "+caseFiltersUA.getExtrapulmonaryType2().getId()+")");
		}
		if (TreatmentType.HOSPITALISATION.equals(caseFiltersUA.getTtype()))
				addCondition("c.id in "+hosp);
		else
			if (TreatmentType.OUTPATIENT.equals(caseFiltersUA.getTtype()))
				addCondition("c.id not in "+hosp);
	}
	
}
