package org.msh.tb.bd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;


import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.indicators.core.Indicator2D;


/**
 * Generate indicator for Pulmonary TB patients for Bangladesh - TB 12 form
 * @author Vani Rao
 *
 */
@Name("pulmonaryTBNewIndicator")
public class PulmonaryTBNewIndicator extends Indicator2D{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -351439981021805081L;
	private int flagSelectQuery = 0;
	@In(create=true) EntityManager entityManager;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
		flagSelectQuery = 1;
		String cond3 = " c.id = e.tbcase.id and c.patientType = 0 group by c.id";
		setCondition(cond3);
		setOrderByFields("e.tbcase.id, e.dateCollected");
		List<Object[]> lst3 = createQuery().getResultList();
		flagSelectQuery = 0;
		
		float cntSmearPosM = 0, cntSmearPosF = 0, cntSmearNegM = 0, cntSmearNegF = 0;
		float cntDiedPosM = 0,cntDiedPosF = 0, cntDiedNegM = 0, cntDiedNegF = 0;
		float cntFailPosM = 0, cntFailPosF = 0, cntFailNegM = 0, cntFailNegF = 0;
		float cntDefaultedPosM = 0, cntDefaultedPosF = 0, cntDefaultedNegM = 0, cntDefaultedNegF = 0; 
		float cntTransOutPosM = 0, cntTransOutPosF = 0, cntTransOutNegM = 0, cntTransOutNegF = 0;
		float cntNovEvalPosM = 0, cntNovEvalPosF = 0, cntNovEvalNegM = 0, cntNovEvalNegF = 0;
		float cntSmearPosMNegM = 0, cntSmearPosMNegF = 0, cntSmearNegMNegM = 0, cntSmearNegMNegF = 0;
		float cntSmearPosMPosM = 0, cntSmearPosMPosF = 0, cntSmearNegMPosM = 0, cntSmearNegMPosF = 0;
		Date dtIniTreat = null, dtOutcome = null;
			
	
	for(Object[] val:lst3){	
					dtIniTreat = (Date)val[1];
					Date dt2AfterIni = null, dt3AfterIni = null;		 
						try {
							dt2AfterIni = addMonthsToDate(dtIniTreat,2);
							dt3AfterIni = addMonthsToDate(dtIniTreat,3);
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				
				/*
				 * Checking for 1st reported Microscopy result to be Positive
				 */
			if(val[4] == MicroscopyResult.PLUS || val[4] == MicroscopyResult.PLUS2 || val[4] == MicroscopyResult.PLUS3 || val[4] == MicroscopyResult.PLUS4) {
				if(val[2] == Gender.MALE){
					cntSmearPosM++;
					/*
					 * Fetches Microscopy results for sample collected within 2-3 months of start treatment
					 */
					String mres = getMicroscopyRes((Integer)val[0], dtIniTreat);
					
					/*
					 *  Checking for all cases with microscopy results still NOT closed
					 */
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && ((CaseState)val[5]).ordinal()<3)
						cntSmearPosMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && ((CaseState)val[5]).ordinal()<3)
						cntSmearPosMPosM++;
					/*
					 * Not evaluated cases - no results for sample collected 2-3 months after start treatment							
					 */
					if(mres=="")
						cntNovEvalPosM++;
					
					/*
					 * for cases with an outcome within 2-3 months of start treatment
					 */
					if(val[6]!= null) {
								dtOutcome =  (Date)val[6];
						if(dtOutcome.after(dt2AfterIni) && dtOutcome.before(dt3AfterIni)) {
									if(val[5]== CaseState.DIED)
											cntDiedPosM++;
									if(val[5] == CaseState.FAILED)
											cntFailPosM++;
									if(val[5] == CaseState.DEFAULTED)
												cntDefaultedPosM++;
									if(val[5] == CaseState.TRANSFERRED_OUT)
												cntTransOutPosM++;
										}
								}			
				}
						
					
				if(val[2] == Gender.FEMALE){
					cntSmearPosF++;
					String mres = getMicroscopyRes((Integer)val[0], dtIniTreat);
					
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && ((CaseState)val[5]).ordinal()<3)
						cntSmearPosMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && ((CaseState)val[5]).ordinal()<3)
						cntSmearPosMPosF++;
					
					if(mres=="")
						cntNovEvalPosF++;
					
					
					// Checking if the case was closed within 2-3 months of start treatment
					if(val[6]!= null) {
							dtOutcome =  (Date)val[6];
							if(dtOutcome.after(dt2AfterIni) && dtOutcome.before(dt3AfterIni)) {
						if(val[5] == CaseState.DIED)
							cntDiedPosF++;
						if(val[5] == CaseState.FAILED)
							cntFailPosF++;
						if(val[5] == CaseState.DEFAULTED)
							cntDefaultedPosF++;
						if(val[5] == CaseState.TRANSFERRED_OUT)
							cntTransOutPosF++;		
							}
						}
					
					}
				
				}
			
			
			//Checking for 1st reported Microscopy result to be Negative
			if(val[4] == MicroscopyResult.NEGATIVE){
				if(val[2] == Gender.MALE){
					cntSmearNegM++;
					String mres = getMicroscopyRes((Integer)val[0], dtIniTreat);
					
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && ((CaseState)val[5]).ordinal()<3)
						cntSmearNegMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && ((CaseState)val[5]).ordinal()<3)
						cntSmearNegMPosM++;
					if(mres=="")
						cntNovEvalNegM++;
										
					
					// Checking if the case was closed within 2-3 months of start treatment
					if(val[6]!= null) {
						dtOutcome =  (Date)val[6];
						if(dtOutcome.after(dt2AfterIni) && dtOutcome.before(dt3AfterIni)) {
					
							if(val[5]== CaseState.DIED)
								cntDiedNegM++;
							if(val[5] == CaseState.FAILED)
								cntFailNegM++;
							if(val[5] == CaseState.DEFAULTED)
								cntDefaultedNegM++;
							if(val[5] == CaseState.TRANSFERRED_OUT)
								cntTransOutNegM++;
							}
						}
					
					}
				if(val[2]== Gender.FEMALE){
					cntSmearNegF++;
					String mres = getMicroscopyRes((Integer)val[0], dtIniTreat);
					
					// Checking for all cases with microscopy results still NOT closed
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && ((CaseState)val[5]).ordinal()<3)
						cntSmearNegMNegF++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && ((CaseState)val[5]).ordinal()<3)
						cntSmearNegMPosF++;
					if(mres=="")
						cntNovEvalNegF++;
										
						// Checking if the case was closed within 2-3 months of start treatment
						if(val[6]!= null) {
							dtOutcome =  (Date)val[6];
							if(dtOutcome.after(dt2AfterIni) && dtOutcome.before(dt3AfterIni)) {
						
								if(val[5] == CaseState.DIED)
									cntDiedNegF++;
								if(val[5] == CaseState.FAILED)
									cntFailNegF++;
								if(val[5] == CaseState.DEFAULTED)
									cntDefaultedNegF++;
								if(val[5] == CaseState.TRANSFERRED_OUT)
									cntTransOutNegF++;
						}
					}		
				} 
			}	
		} 
		
		Map<String, String> messages = Messages.instance();
		
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosM);
		addValue(messages.get("manag.gender.male0"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegM);
	
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosF);
		addValue(messages.get("manag.gender.female0"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegF);
		
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosM + cntSmearPosF);
		addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegM + cntSmearNegF);
		
		
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosMNegM);
		addValue(messages.get("manag.gender.male1"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegMNegM);
		
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosMNegF);
		addValue(messages.get("manag.gender.female1"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegMNegF);
		
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosMPosM);
		addValue(messages.get("manag.gender.male2"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegMPosM);
		
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.smearpositive"), cntSmearPosMPosF);
		addValue(messages.get("manag.gender.female2"), messages.get("manag.pulmonary.smearnegative"), cntSmearNegMPosF);
		
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.smearpositive"), cntDiedPosM);
		addValue(messages.get("manag.gender.male3"), messages.get("manag.pulmonary.smearnegative"), cntDiedNegM);
		
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.smearpositive"), cntDiedPosF);
		addValue(messages.get("manag.gender.female3"), messages.get("manag.pulmonary.smearnegative"), cntDiedNegF);
			
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.smearpositive"), cntFailPosM);
		addValue(messages.get("manag.gender.male4"), messages.get("manag.pulmonary.smearnegative"), cntFailNegM);
		
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.smearpositive"), cntFailPosF);
		addValue(messages.get("manag.gender.female4"), messages.get("manag.pulmonary.smearnegative"), cntFailNegF);
		
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.smearpositive"), cntDefaultedPosM);
		addValue(messages.get("manag.gender.male5"), messages.get("manag.pulmonary.smearnegative"), cntDefaultedNegM);
		
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.smearpositive"), cntDefaultedPosF);
		addValue(messages.get("manag.gender.female5"), messages.get("manag.pulmonary.smearnegative"), cntDefaultedNegF);
		
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.smearpositive"), cntTransOutPosM);
		addValue(messages.get("manag.gender.male6"), messages.get("manag.pulmonary.smearnegative"), cntTransOutNegM);
		
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.smearpositive"), cntTransOutPosF);
		addValue(messages.get("manag.gender.female6"), messages.get("manag.pulmonary.smearnegative"), cntTransOutNegF);
		
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearpositive"), cntNovEvalPosM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearnegative"), cntNovEvalNegM);
		
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearpositive"), cntNovEvalPosF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearnegative"), cntNovEvalNegF);
			
		float totPosM = cntSmearPosMNegM + cntSmearPosMPosM + cntDiedPosM + cntFailPosM + cntDefaultedPosM + cntTransOutPosM + cntNovEvalPosM;
		float totNegM = cntSmearNegMNegM + cntSmearNegMPosM + cntDiedNegM + cntFailNegM + cntDefaultedNegM + cntTransOutNegM + cntNovEvalNegM;
		float totPosF = cntSmearPosMNegF + cntSmearPosMPosF + cntDiedPosF + cntFailPosF + cntDefaultedPosF + cntTransOutPosF + cntNovEvalPosF;
		float totNegF = cntSmearNegMNegF + cntSmearNegMPosF + cntDiedNegF + cntFailNegF + cntDefaultedNegF + cntTransOutNegF + cntNovEvalNegF;
		
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.smearpositive"), totPosM);
		addValue(messages.get("manag.gender.male8"), messages.get("manag.pulmonary.smearnegative"), totNegM);
		
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.smearpositive"), totPosF);
		addValue(messages.get("manag.gender.female8"), messages.get("manag.pulmonary.smearnegative"), totNegF);
				
		float totPos = totPosM + totPosF;
		float totNeg = totNegM + totNegF;
		
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearpositive"), totPos);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearnegative"), totNeg);
	}
	
	@Override
	public String getHQLSelect() {
		String strSel = "";
		if(flagSelectQuery == 1)
			strSel = "select c.id, c.treatmentPeriod.iniDate, c.patient.gender, e.dateCollected, e.result, c.state, c.outcomeDate";
		if(flagSelectQuery == 2)
			strSel = "select e.result, e.dateCollected";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		String strFrom = "";
		if(flagSelectQuery == 1)
			//strFrom = "from TbCase c";
			strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		if(flagSelectQuery ==2)
			strFrom =  " from ExamMicroscopy e inner join e.tbcase c ";
		if(flagSelectQuery ==3)
			strFrom =  " from ExamMicroscopy e join e.tbcase c ";
		return strFrom;
		
	}
	
	public String getMicroscopyRes(int tbcaseid, Date dtIniTreat){
		flagSelectQuery = 2;	
		String strMicroscopyResult = "" ;
		Date st;
		Date end;
			try {
				 st = addMonthsToDate(dtIniTreat, 2);
				 end = addMonthsToDate(dtIniTreat, 3);
			 							 
					String hql = " select e.result, e.dateCollected from ExamMicroscopy e inner join e.tbcase c where " +
					" c.validationState = #{indicatorFilters.validationState} and c.treatmentPeriod.iniDate between #{indicatorFilters.iniDate} and #{indicatorFilters.endDate} " +
					" and  e.tbcase.id = " +tbcaseid +
					" and e.dateCollected between :dt1 and :dt2 ";
		
			List<Object[]> lst = new ArrayList<Object[]>();
	
				lst = entityManager.createQuery(hql)
				.setParameter("dt1", st)
				.setParameter("dt2", end)
				.getResultList();
				
				if(lst.size()==1){
					for(Object[] val: lst){
					strMicroscopyResult = val[0].toString();
					}
				}
				//If more than one result exists between 2-3 months
				if(lst.size()>1){
					List<String> allRes = new ArrayList<String>();
					String dtColl = "";
					List<Date> dtCollList = new ArrayList<Date>();
					for(Object[] val: lst){
						allRes.add(val[0].toString());
						dtCollList.add((Date)val[1]);	
					}
					List<Date> dupdtCollList = new ArrayList<Date>();
					dupdtCollList = dtCollList;
					Collections.sort(dupdtCollList);
					Date dtmax = dupdtCollList.get(dupdtCollList.size()-1);
					int index = dtCollList.indexOf(dtmax);
					strMicroscopyResult = allRes.get(index);
				}
			}
			catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(NoResultException e){
				return null;
			} 
			finally{
				flagSelectQuery = 0;
			}
					return strMicroscopyResult;
		
	}
	
	public Date addMonthsToDate(Date date, int addMon) throws ParseException{		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, addMon);
		Date dFwd = c.getTime();
		return dFwd;	
	}
		
}
