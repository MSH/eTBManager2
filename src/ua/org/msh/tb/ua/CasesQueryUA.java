package org.msh.tb.ua;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.cases.FilterHealthUnit;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserView;


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
			"c.validationState, c.registrationCode, c.diagnosisType, p.birthDate " +
			getFromHQL() + " join c.patient p " +
		 	   "left outer join c.notificationUnit nu " +
		 	   "left outer join c.treatmentUnit tu " +
		 	   "join c.notifAddress.adminUnit loc ".concat(dynamicConditions());
	}
	@Override
	public String getCountEjbql() {
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
	
	
}
