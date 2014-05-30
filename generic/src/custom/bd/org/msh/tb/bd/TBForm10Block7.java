package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;

@Name("tBForm10Block7")
public class TBForm10Block7 extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2014368874909253845L;
	@In(create=true) EntityManager entityManager;
	private int flag = 0;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub

		List<Object[]> lst = new ArrayList<Object[]>();
		flag = 1;
		String cond = "exists(select a.id from ExamHIV a where a.tbcase.id = c.id) and exists(select m.id from ExamMicroscopy m where m.tbcase.id = c.id)";
		setCondition(cond);
		lst = createQuery().getResultList();
		flag=0;
		
		float cntNewHIVTestedSmearPosM = 0, cntNewHIVTestedSmearPosF = 0, cntNewHIVTestedSmearNegM = 0, cntNewHIVTestedSmearNegF = 0;
		float cntNewHIVTestedEPM = 0, cntNewHIVTestedEPF = 0, cntNewHIVPosEPM = 0,cntNewHIVPosEPF = 0;
		float cntReHIVTestedM = 0, cntReHIVTestedF = 0, cntReHIVPosM = 0, cntReHIVPosF = 0;
		float cntNewHIVPosSmearPosM = 0, cntNewHIVPosSmearPosF = 0, cntNewHIVPosSmearNegM = 0, cntNewHIVPosSmearNegF = 0;
		float cntOtherHIVTestedM = 0, cntOtherHIVTestedF = 0, cntOtherHIVPosM = 0, cntOtherHIVPosF = 0;
		
		for(Object[] val:lst){
		
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val[0]);
			
			int tbcaseid = (Integer) val[0];
			
			String hqlMicroscopy = "from ExamMicroscopy m where m.dateRelease in (select max(m.dateRelease) from ExamMicroscopy where m.tbcase.id = " +tbcaseid + ")" + "and m.tbcase.id = " +tbcaseid;
			ArrayList<ExamMicroscopy> examMicroscopy = (ArrayList<ExamMicroscopy>) entityManager.createQuery(hqlMicroscopy).getResultList();
			
			String hqlHIV = "from ExamHIV h where h.date in (select max(h.date) from ExamHIV where h.tbcase.id = " +tbcaseid + ")" + "and h.tbcase.id = " +tbcaseid;
			ArrayList<ExamHIV> examHIV = (ArrayList<ExamHIV>) entityManager.createQuery(hqlHIV).getResultList();
			
			if(!examMicroscopy.isEmpty() && !examHIV.isEmpty() && tbcase.getPatientType()==PatientType.NEW) {
				if(examMicroscopy.get(0).getResult().isPositive() && tbcase.getPatient().getGender()==Gender.MALE){
					cntNewHIVTestedSmearPosM++;
					if(examHIV.get(0).getResult() == HIVResult.POSITIVE)
						cntNewHIVPosSmearPosM++;
					}
				if(examMicroscopy.get(0).getResult().isNegative() && tbcase.getPatient().getGender()==Gender.MALE){
					cntNewHIVTestedSmearNegM++;
					if(examHIV.get(0).getResult() == HIVResult.POSITIVE)
						cntNewHIVPosSmearNegM++;
					}
				if(examMicroscopy.get(0).getResult().isPositive() && tbcase.getPatient().getGender()==Gender.FEMALE){
					cntNewHIVTestedSmearPosF++;
					if(examHIV.get(0).getResult() == HIVResult.POSITIVE)
						cntNewHIVPosSmearPosF++;
					}
				if(examMicroscopy.get(0).getResult().isNegative() && tbcase.getPatient().getGender()==Gender.FEMALE){
					cntNewHIVTestedSmearNegF++;
					if(examHIV.get(0).getResult() == HIVResult.POSITIVE)
						cntNewHIVPosSmearNegF++;
					}
			  	}
		    }
		
		flag = 2;
		String cond2 = " c.id = e.tbcase.id group by c.id ";
		setCondition(cond2);
		setOrderByFields("e.tbcase.id, e.date");
		List<Object[]> lst2 = createQuery().getResultList();
		flag = 0;
		
		for(Object[] obj:lst2) {
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, obj[0]);
			if(tbcase.getPatientType()==PatientType.AFTER_DEFAULT||tbcase.getPatientType()==PatientType.FAILURE||tbcase.getPatientType()==PatientType.FAILURE_FT||tbcase.getPatientType()==PatientType.FAILURE_RT){
				if(tbcase.getPatient().getGender()==Gender.MALE){
					cntReHIVTestedM++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntReHIVPosM++;
				}
				if(tbcase.getPatient().getGender()==Gender.FEMALE)
					cntReHIVTestedF++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntReHIVPosF++;		
			}
			
			if(tbcase.getInfectionSite()==InfectionSite.EXTRAPULMONARY){
				if(tbcase.getPatient().getGender()==Gender.MALE){
					cntNewHIVTestedEPM++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntNewHIVPosEPM++;
				}
				if(tbcase.getPatient().getGender()==Gender.FEMALE){
					cntNewHIVTestedEPF++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntNewHIVPosEPF++;
				}				
			}
			
			if(tbcase.getPatientType()==PatientType.OTHER){
				if(tbcase.getPatient().getGender()==Gender.MALE){
					cntOtherHIVTestedM++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntOtherHIVPosM++;
				}
				if(tbcase.getPatient().getGender()==Gender.FEMALE){
					cntOtherHIVTestedF++;
					if((HIVResult)obj[2]==HIVResult.POSITIVE)
						cntOtherHIVPosF++;
				}
			}
		}
		
		Map<String, String> messages = Messages.instance();
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVTestedSmearPosM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVTestedSmearNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVTestedM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.ep"), cntNewHIVTestedEPM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.tbform10.block7.others"), cntOtherHIVTestedM);
		
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVTestedSmearPosF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVTestedSmearNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVTestedF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.ep"), cntNewHIVTestedEPF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.tbform10.block7.others"), cntOtherHIVTestedF);
		
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVPosSmearPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVPosSmearNegM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.ep"), cntNewHIVPosEPM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.tbform10.block7.others"), cntOtherHIVPosM);
		
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.newsmearpositive"), cntNewHIVPosSmearPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.newsmearnegative"), cntNewHIVPosSmearNegF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.retreatcases"), cntReHIVPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.ep"), cntNewHIVPosEPF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.tbform10.block7.others"), cntOtherHIVPosF);
		
		
	}
	
	@Override
	public String getHQLSelect() {
		String sqlsel = null;
		if(flag == 1)
			sqlsel = "select c.id, c.patient.gender";
		if(flag == 2)
			sqlsel = "select c.id, c.patient.gender, e.result";
		return sqlsel;
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		String hqlfrom = null;
		 if(flag==1)
			 hqlfrom = super.getHQLFrom();
		 if(flag==2)
		hqlfrom = "from ExamHIV e inner join e.tbcase c ";
		 return hqlfrom;
	}
	

}
