package org.msh.tb.bd;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorFilters;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("tBForm10Block6")
public class TBForm10Block6 extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1095628534810163566L;
	private String condition;
	public int flag;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
					flag = 1;
					List<Object[]> lst = createQuery().getResultList();
					flag = 0;
					
					float cntSusM = 0, cntSusF = 0, cntConfM = 0, cntConfF = 0;
					
					for(Object[] val:lst){
						
						String strMicroscopyRes = getMicroscopyResult((Integer) val[0]);
						System.out.println("Microscopy result --->"+strMicroscopyRes);
						if(strMicroscopyRes!=null) {
							if(val[1]==Gender.MALE)
								cntSusM++;
							if(val[1]==Gender.FEMALE)
								cntSusF++;	
						}
						
						if(strMicroscopyRes!=null && strMicroscopyRes.equals("POSITIVE")) {
							if(val[1]==Gender.MALE)
								cntConfM++;
							if(val[1]==Gender.FEMALE)
								cntConfF++;	
						}
					 }
					
					Map<String, String> messages = Messages.instance();
					
					addValue(messages.get("manag.gender.male0"), messages.get("#"), cntSusM);
					addValue(messages.get("manag.gender.female0"), messages.get("#"), cntSusF);
					addValue(messages.get("manag.pulmonary.sum"), messages.get("#"), cntSusM + cntSusF);
					addValue(messages.get("manag.gender.male1"), messages.get("#"), cntConfM);
					addValue(messages.get("manag.gender.female1"), messages.get("#"), cntConfF);
					addValue(messages.get("manag.pulmonary.tot"), messages.get("#"), cntConfM + cntConfF);
		
	}
	
	@Override
	public String getHQLSelect() {
		String strFrom = "";
		if(flag == 1)
			strFrom = "select c.id, c.patient.gender ";
		if(flag == 2)
			strFrom =  " select e.result ";
		return strFrom;
	}
		
	@Override
	protected String getHQLWhere() {
		String strWhere = "";
		if(flag == 1){
		String hql = "where c.notificationUnit.workspace.id = " + getWorkspace().getId().toString();
		
		IndicatorFilters filters = getIndicatorFilters();

		UserWorkspace userWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (userWorkspace.getHealthSystem() != null)
			hql += " and c.notificationUnit.healthSystem.id = " + userWorkspace.getHealthSystem().getId();
//		hql += " and c.notificationUnit.healthSystem.id = #{userWorkspace.tbunit.healthSystem.id}";

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
			
//		if ((isUseDiagnosisTypeFilter()) && (filters.getDiagnosisType() != null)) {
//			hql += " and c.diagnosisType = #{indicatorFilters.diagnosisType}";
//		}

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
		Tbunit unit = filters.getTbunitselection().getTbunit();
		if (unit != null)
			hql += " and c.notificationUnit.id = " + unit.getId().toString();
		
		hql = addCondition(hql, getCaseStateCondition());
		hql = addCondition(hql, getPeriodCondition());
		hql = addCondition(hql, getAdminUnitCondition());
		hql = addCondition(hql, condition);
		hql = addCondition(hql, " ( c.diagnosisDate > c.registrationDate or c.diagnosisDate = null)");
		//return hql;
		strWhere = hql;
		}
	
		if(flag ==2)
			strWhere = super.getHQLWhere();
		
		return strWhere;
	}
	
	private String addCondition(String hql, String condition) {
		if (condition == null)
			return hql;
		
		return hql + " and " + condition;
	}
	
public String getMicroscopyResult(int tbcaseid) {
		
		flag = 2;
		String strMicroscopyResult = "";		
		String condition = "e.dateCollected in (select min(m.dateCollected) from ExamMicroscopy m where m.tbcase.id = " +tbcaseid + ")" ;
		setCondition(condition);
		
		List<Object> lst = new ArrayList<Object>();
		try{
			lst = createQuery().getResultList();
			if(lst.size()==1){
				for(Object val: lst){
				strMicroscopyResult = val.toString();
				}
			}
			if(lst.size()>1){
				List<String> allRes = new ArrayList<String>();
				for(Object val: lst){
					allRes.add(val.toString());
				}
				if(allRes.contains("POSITIVE")||allRes.contains("PLUS")||allRes.contains("PLUS2")||allRes.contains("PLUS3")||allRes.contains("PLUS4"))
					strMicroscopyResult = "POSITIVE";
				else 
					strMicroscopyResult = "NEGATIVE";
			}
		}
		catch(NoResultException e){
			return null;
		}
				return strMicroscopyResult;
	
	}

@Override
protected String getHQLFrom() {
	// TODO Auto-generated method stub
	String strFrom = "";
	if(flag == 1)
	return "from TbCase c ";
	if(flag == 2)
		strFrom =  " from ExamMicroscopy e inner join e.tbcase c ";
	return strFrom;
}
		
}
