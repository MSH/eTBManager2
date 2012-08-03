package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.indicators.core.Indicator2D;

@Name("tBForm10Block8")
public class TbForm10Block8 extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8000152138975904527L;
	@In(create=true) EntityManager entityManager;
	public int flagSelectQuery;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		flagSelectQuery = 1;
		//fetching all HIV Positive patients
		String cond = "e.result = " +HIVResult.POSITIVE.ordinal();
		setCondition(cond);
		List<Object> lst = new ArrayList<Object>();
		lst = createQuery().getResultList();
		System.out.println("List size ----->" +lst.size());
		
		float cntHIVPosM = 0, cntHIVPosF = 0, cntHIVPosMPosM = 0, cntHIVPosMPosF = 0;
		
		for(Object val:lst){
			TbCase tbcase = new TbCase();
			tbcase = entityManager.find(TbCase.class, val);
			if(tbcase.getPatient().getGender()==Gender.MALE)
				cntHIVPosM++;
			if(tbcase.getPatient().getGender()==Gender.FEMALE)
				cntHIVPosF++;
			
			String strMicroscopyRes = getMicroscopyResult(tbcase.getId());
			
				if(strMicroscopyRes != null && (strMicroscopyRes.equalsIgnoreCase("POSITIVE") || strMicroscopyRes.equalsIgnoreCase("PLUS") 
								|| strMicroscopyRes.equalsIgnoreCase("PLUS2") || strMicroscopyRes.equalsIgnoreCase("PLUS3") 
								|| strMicroscopyRes.equalsIgnoreCase("PLUS4")) ){
				
				if(tbcase.getPatient().getGender()==Gender.MALE)
					cntHIVPosMPosM++;
				if(tbcase.getPatient().getGender()==Gender.FEMALE)
					cntHIVPosMPosF++;	
				}	
			}
		Map<String, String> messages = Messages.instance();
		addValue(messages.get("manag.gender.male1"), messages.get("#"), cntHIVPosM);
		addValue(messages.get("manag.gender.female1"), messages.get("#"), cntHIVPosF);
		
		addValue(messages.get("manag.gender.male2"), messages.get("#"), cntHIVPosMPosM);
		addValue(messages.get("manag.gender.female2"), messages.get("#"), cntHIVPosMPosF);

		
	}
	
	@Override
	public String getHQLSelect() {
		//return "select e.result, c.patient.gender, c.patientType, c.pulmonaryType.shortName, c.extrapulmonaryType.shortName, c.extrapulmonaryType2.shortName";
		String strFrom = "";
		if(flagSelectQuery == 1)
			strFrom =  " select c.id ";
		if(flagSelectQuery == 2)
			strFrom =  " select e.result ";
		return strFrom;
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		String strFrom = "";
		if(flagSelectQuery == 1)
		return "from ExamHIV e inner join e.tbcase c ";
		if(flagSelectQuery == 2)
			strFrom =  " from ExamMicroscopy e inner join e.tbcase c ";
		return strFrom;
	}
public String getMicroscopyResult(int tbcaseid) {
		
		flagSelectQuery = 2;
		String strMicroscopyResult = "";		
		String condition = "e.dateCollected in (select min(m.dateCollected) from ExamMicroscopy m where m.tbcase.id = " +tbcaseid + ")" ;
		setCondition(condition);
//		Object o = null;
//		try{
//		o = createQuery().getSingleResult();
//		
//		strMicroscopyResult = o.toString();		
//		}
//		catch (NoResultException e){		
//			return null;
//			}
//				return strMicroscopyResult;
		
		/*
		 * Fix for above when and if there is more than one result for a given date
		 */
		
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

}
