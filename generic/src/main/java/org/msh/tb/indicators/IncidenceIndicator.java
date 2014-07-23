package org.msh.tb.indicators;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorFilters;

import javax.persistence.EntityManager;

/**
 * Generates the TB/MDR-TB incidence indicator <br>
 * The incidence indicator considers just new cases diagnosed in the period set in the {@link IndicatorFilters} 
 * @author Ricardo Mem�ria
 *
 */
@Name("incidenceIndicator")
public class IncidenceIndicator extends Indicator {
	private static final long serialVersionUID = -3029326408511512325L;

	@In(create=true) EntityManager entityManager;
	private String condition;


	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);
		
		generateIndicatorByOutputSelection(null);
	}
	
	@Override
	protected String getHQLWhere() {
		String hql = "where c.notificationUnit.workspace.id = " + getWorkspace().getId().toString();
		
		IndicatorFilters filters = getIndicatorFilters();

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace.getHealthSystem() != null)
			hql += " and c.notificationUnit.healthSystem.id = " + userWorkspace.getHealthSystem().getId();
//		hql += " and c.notificationUnit.healthSystem.id = #{userWorkspace.tbunit.healthSystem.id}";

		if (filters.getHealthSystem() != null)
			hql += " and c.notificationUnit.healthSystem.id = " + filters.getHealthSystem().getId();
		
		if (getClassification() != null)
			hql += " and c.classification = :classification";

		// include filter by gender
		if (filters.getGender() != null)
			hql += " and c.patient.gender = #{indicatorFilters.gender}";
		
		// include filter by regimen
		if (filters.getRegimen() != null)
			hql += " and c.regimen.id = #{indicatorFilters.regimen.id}";
		
		// include filter by patient type
		if (filters.getPatientType() != null)
			hql += " and c.patientType = #{indicatorFilters.patientType}";
		
		// include filter by patient first treatment type
		if(filters.getPatTypFirstTreat() !=null && (filters.getPatTypFirstTreat() == PatientType.NEW ||filters.getPatTypFirstTreat() == PatientType.TRANSFER_IN))
			hql += " and c.patientType = #{indicatorFilters.patTypFirstTreat}";
		
		// include filter by patient re-treatment type
		if(filters.getPatTypFirstTreat() !=null && (filters.getPatTypFirstTreat() == PatientType.ALL_RETREATMENT)){
			if(filters.getPatTypReTreat() != null  )
			hql += " and c.patientType = #{indicatorFilters.patTypReTreat}";	
			else{
				hql += " and" + "(" + "c.patientType = 1" +
						" or c.patientType = 2" +
						" or c.patientType = 3" +
						" or c.patientType = 4" + ")" ;
			}
		}
		// include filter by source
		if (filters.getSource() != null)
			hql += " and exists(select pm.id from PrescribedMedicine pm where pm.source.id = #{indicatorFilters.source.id} and pm.tbcase.id = c.id)";

		// include filter by infectionSite
		String s = getHQLInfectionSite();
		if (s != null)
			hql += " and " + s;
		
		if (filters.getAgeRange() != null) {
			hql += " and c.age between #{indicatorFilters.ageRange.iniAge} and #{indicatorFilters.ageRange.endAge}";
		}
		
		if (filters.getDrugResistanceType() != null) {
			hql += " and c.drugResistanceType = #{indicatorFilters.drugResistanceType}";
		}
		
		if ((isUseDiagnosisTypeFilter()) && (filters.getDiagnosisType() != null)) {
			hql += " and c.diagnosisType = #{indicatorFilters.diagnosisType}";
		}

		// add filters by culture
		s = getHQLCultureCondition();
		if (s != null)
			hql += " and " + s;

		// add filters by HIV result
		s = getHQLHivResultCondition();
		if (s != null)
			hql += " and " + s;
		
		// add filters by microscopy
		s = getHQLMicroscopyCondition();
		if (s != null)
			hql += " and " + s;
		
		s = getHQLValidationState();
		if (s != null)
			hql += " and " + s;

        // include filter by unit
        Tbunit unit = filters.getTbunitselection().getSelected();
        if (unit != null)
            hql += " and c.notificationUnit.id = " + unit.getId().toString();

		hql = addCondition(hql, getCaseStateCondition());
		hql = addCondition(hql, getPeriodCondition());
		hql = addCondition(hql, getAdminUnitCondition());
		hql = addCondition(hql, condition);
		return hql;
	
	}
	
	private String addCondition(String hql, String condition) {
		if (condition == null)
			return hql;
		
		return hql + " and " + condition;
	}
	
	public String getHQLWherePublic(){
		return getHQLWhere();
	}
	
}
