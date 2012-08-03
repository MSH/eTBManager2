package org.msh.tb.bd;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.hibernate.util.CalendarComparator;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.indicators.core.Indicator2D;

import sun.util.calendar.CalendarUtils;

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
	public boolean flagMicroscopyNeg = false;
	public boolean flagMicroscopyPos = false;
	public int flagSelectQuery;
	private boolean flag = false;
	@In(create=true) EntityManager entityManager;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		//setNewCasesOnly(true);
		//List<Object[]> lst = generateValuesByOutputSelection("c.patientType = 0");
		
		//Fetching all confirmed cases notified as 'new' and ontreatment
		flagSelectQuery = 1;
		String cond = " c.patientType = 0 ";
		setCondition(cond);
		//List<Object[]> lst = createQuery().getResultList();
		List<TbCase> lst = new ArrayList<TbCase>();
		lst = createQuery().getResultList();
		System.out.println("--------------->"+lst.size());
		

		//System.out.println("List Size--->" +lst.size());
		float cntSmearPosM = 0, cntSmearPosF = 0, cntSmearNegM = 0, cntSmearNegF = 0, cntEPM = 0, cntEPF = 0;
		float cntDiedPosM = 0,cntDiedPosF = 0, cntDiedNegM = 0, cntDiedNegF = 0;
		float cntFailPosM = 0, cntFailPosF = 0, cntFailNegM = 0, cntFailNegF = 0;
		float cntDefaultedPosM = 0, cntDefaultedPosF = 0, cntDefaultedNegM = 0, cntDefaultedNegF = 0; 
		float cntTransOutPosM = 0, cntTransOutPosF = 0, cntTransOutNegM = 0, cntTransOutNegF = 0;
//		float cntCuredPosM = 0, cntCuredPosF = 0, cntCuredNegM = 0, cntCuredNegF = 0;
//		float cntTreatedPosM = 0, cntTreatedNegM = 0, cntTreatedPosF = 0, cntTreatedNegF = 0;
//		float cntEPCuredM = 0, cntEPTreatedM = 0, cntEPDiedM = 0, cntEPFailM = 0, cntEPDefaultedM = 0, cntEPTransOutM = 0;
//		float cntEPCuredF = 0, cntEPTreatedF = 0, cntEPDiedF = 0, cntEPFailF = 0, cntEPDefaultedF = 0, cntEPTransOutF = 0;
		float cntSmearPosMNegM = 0, cntSmearPosMNegF = 0, cntSmearNegMNegM = 0, cntSmearNegMNegF = 0, cntSmearPosMPosM = 0, cntSmearPosMPosF = 0, cntSmearNegMPosM = 0, cntSmearNegMPosF = 0;
			
		for(TbCase val:lst){
			
			if(val.getPulmonaryType() != null){
				if(isSmearPos(val.getPulmonaryType().getShortName().toString())){
					if(val.getPatient().getGender() == Gender.MALE){
					cntSmearPosM++;
//						Date today = new Date(); 
//						int monthOfTreat = val.getMonthTreatment(today);
//						System.out.println("Month of Treatemnt-------->" +monthOfTreat);
						val.getTreatmentPeriod().getIniDate();
						
						String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
						System.out.println("Microscopy Result ----->" +mres);
						
						// Checking for all cases with microscopy results still NOT closed
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
							cntSmearPosMNegM++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
								|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
								|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
							cntSmearPosMPosM++;
												
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
						
						// Checking if the case was closed within 2-3 months of start treatment
						if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						
						if(val.getState() == CaseState.DIED)
							cntDiedPosM++;
						if(val.getState() == CaseState.FAILED)
							cntFailPosM++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefaultedPosM++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTransOutPosM++;
						}
						
					}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntSmearPosF++;
//					MicroscopyResult mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
//					if(mres == MicroscopyResult.NEGATIVE)
//						cntSmearPosMNegF++;
//					if(mres == MicroscopyResult.POSITIVE || mres == MicroscopyResult.PLUS || mres == MicroscopyResult.PLUS2 || mres == MicroscopyResult.PLUS3 || mres == MicroscopyResult.PLUS4)
//						cntSmearPosMPosF++;
					val.getTreatmentPeriod().getIniDate();
					
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntSmearPosMNegF++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntSmearPosMPosF++;
					
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
						if(val.getState() == CaseState.DIED)
							cntDiedPosF++;
						if(val.getState() == CaseState.FAILED)
							cntFailPosF++;
						if(val.getState() == CaseState.DEFAULTED)
							cntDefaultedPosF++;
						if(val.getState() == CaseState.TRANSFERRED_OUT)
							cntTransOutPosF++;
						}
					}
			}
			
			if(isSmearNeg(val.getPulmonaryType().getShortName().toString())){
				if(val.getPatient().getGender() == Gender.MALE){
					cntSmearNegM++;
					val.getTreatmentPeriod().getIniDate();
					
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
					if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntSmearNegMNegM++;
					if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntSmearNegMPosM++;
											
					Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
					Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
					// Checking if the case was closed within 2-3 months of start treatment
					if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
					if(val.getState() == CaseState.DIED)
						cntDiedNegM++;
					if(val.getState() == CaseState.FAILED)
						cntFailNegM++;
					if(val.getState() == CaseState.DEFAULTED)
						cntDefaultedNegM++;
					if(val.getState() == CaseState.TRANSFERRED_OUT)
						cntTransOutNegM++;
					}
				}
				if(val.getPatient().getGender() == Gender.FEMALE){
					cntSmearNegF++;
					val.getTreatmentPeriod().getIniDate();
					
					String mres = getMicroscopyResult(val.getId(), val.getTreatmentPeriod().getIniDate());
					System.out.println("Microscopy Result ----->" +mres);
					
					// Checking for all cases with microscopy results still NOT closed
						if(mres!= null && mres.equalsIgnoreCase("NEGATIVE") && val.getState().ordinal()<3)
						cntSmearNegMNegF++;
						if(mres!= null && (mres.equalsIgnoreCase("POSITIVE") || mres.equalsIgnoreCase("PLUS") 
							|| mres.equalsIgnoreCase("PLUS2") || mres.equalsIgnoreCase("PLUS3") 
							|| mres.equalsIgnoreCase("PLUS4"))  && val.getState().ordinal()<3)
						cntSmearNegMPosF++;
											
						Date dt2AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),2);
						Date dt3AfterIni = addMonthsToDate(val.getTreatmentPeriod().getIniDate(),3);
					
						// Checking if the case was closed within 2-3 months of start treatment
						if(val.getOutcomeDate()!= null && val.getOutcomeDate().after(dt2AfterIni) && val.getOutcomeDate().before(dt3AfterIni)) {
							if(val.getState() == CaseState.DIED)
								cntDiedNegF++;
							if(val.getState() == CaseState.FAILED)
								cntFailNegF++;
							if(val.getState() == CaseState.DEFAULTED)
								cntDefaultedNegF++;
							if(val.getState() == CaseState.TRANSFERRED_OUT)
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
			
		float totPosM = cntSmearPosMNegM + cntSmearPosMPosM + cntDiedPosM + cntFailPosM + cntDefaultedPosM + cntTransOutPosM;
		float totNegM = cntSmearNegMNegM + cntSmearNegMPosM + cntDiedNegM + cntFailNegM + cntDefaultedNegM + cntTransOutNegM;
		float totPosF = cntSmearPosMNegF + cntSmearPosMPosF + cntDiedPosF + cntFailPosF + cntDefaultedPosF + cntTransOutPosF;
		float totNegF = cntSmearNegMNegF + cntSmearNegMPosF + cntDiedNegF + cntFailNegF + cntDefaultedNegF + cntTransOutNegF;
		
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearpositive"), totPosM);
		addValue(messages.get("manag.gender.male7"), messages.get("manag.pulmonary.smearnegative"), totNegM);
		
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearpositive"), totPosF);
		addValue(messages.get("manag.gender.female7"), messages.get("manag.pulmonary.smearnegative"), totNegF);
				
		float totPos = totPosM + totPosF;
		float totNeg = totNegM + totNegF;
		
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearpositive"), totPos);
		addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearnegative"), totNeg);
	}
	
	@Override
	public String getHQLSelect() {
		String strSel = "";
		if(flagSelectQuery == 2)
			strSel = "select e.result, e.dateCollected";
		return strSel;
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		String strFrom = "";
		if(flagSelectQuery == 1)
			strFrom = "from TbCase c";
		if(flagSelectQuery ==2)
			strFrom =  " from ExamMicroscopy e inner join e.tbcase c ";
		return strFrom;
		
	}

	public boolean isSmearPos(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(+)")){
			flag = true;
		}
		return flag;		
	}
	
	public boolean isSmearNeg(String strSmear){
		flag = false;
		if(strSmear.equalsIgnoreCase("Smear(-)")){
			flag = true;
		}
		return flag;		
	}
	
	public String getMicroscopyResult(int tbcaseid, Date ini) {
		
		flagSelectQuery = 2;
		String strMicroscopyResult = "";		
		String condition = "e.dateCollected in (select max(m.dateCollected) from ExamMicroscopy m where m.tbcase.id = " +tbcaseid + ")" ;
		setCondition(condition);
//		Object[] o = null;
//		try{
//		o = (Object[]) createQuery().getSingleResult();
//		
//		System.out.println("Result ------> " +o[0]);
//		System.out.println("date Collected ------> " +o[1]);
//		Date dtCollected = (Date)o[1];
//		Date dt2Months = addMonthsToDate(ini, 2);
//		Date dt3Months = addMonthsToDate(ini, 3);
//			if(dtCollected.after(dt2Months) && dtCollected.before(dt3Months))
//				strMicroscopyResult = o[0].toString();
//			else 
//				strMicroscopyResult = "";
//		}
//		catch (NoResultException e){		
//			return null;
//			}
		
		/*
		 * Fix for above when and if there is no one single result for a given date
		 */
		
		List<Object[]> lst = new ArrayList<Object[]>();
		try{
			lst = createQuery().getResultList();
			if(lst.size()==1){
				for(Object[] val: lst){
				strMicroscopyResult = val[0].toString();
				}
			}
			if(lst.size()>1){
				List<String> allRes = new ArrayList<String>();
				for(Object[] val: lst){
					allRes.add(val[0].toString());
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
	
	public Date addMonthsToDate(Date date, int addMon){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, addMon);
		Date dFwd = c.getTime();
		return dFwd;		
	}
}
