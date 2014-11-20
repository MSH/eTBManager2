package org.msh.tb.kh;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * Generate indicator about delay in MDR-TB diagnosis and start treatment
 * @author Vani Rao
 *
 */
@Name("delayStartTreatIndicator")
public class DelayStartTreatIndicator extends Indicator2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3795390748763613756L;
	@In(create=true) EntityManager entityManager;
	private List<TbCase>  tbcases; 
	private ExamDST examDST;
	
	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		Map<String, String> messages = Messages.instance();

		// calculate number of cases with Resistant DST Results
		String cond = " exists (select res.exam.id from ExamDSTResult res " +
					   " join res.exam exam " +
					   " where exam.tbcase.id = c.id and res.result = " + DstResult.RESISTANT.ordinal() + ")" + " group by c.id"
			;
//		String cond = " exists (select res.exam.id from ExamDSTResult res " +
//		   " join res.exam exam " +
//		   " where exam.tbcase.id = c.id and res.result = " + DstResult.RESISTANT.ordinal() + ")" + "group by c.id"	
//;				
			setCondition(cond);
			List<Object[]> lst = createQuery().getResultList();
			List<Integer> delayStartTreatArr = new ArrayList<Integer>();
			List<Integer> delayDiagtArr = new ArrayList<Integer>();
		
			for (Object[] val: lst) {
				Date regDt = (Date)val[1];
				Date startTrtDt = (Date)val[2];
				Date dstResDt = (Date)val[3];
				int daysStart, daysDiag;
				
				//Calculate Delay in MDR-TB start treatment - lag between Start treatment dt and DST Result date(resistant) for a confirmed validated case
				if(startTrtDt!=null && dstResDt!=null && startTrtDt.after(dstResDt)){
					daysStart = DateUtils.daysBetween(startTrtDt, dstResDt);
					delayStartTreatArr.add(daysStart);
				}
				
				//Calculate Delay in MDR-TB diagnosis - lag between registration dt and DST Result date(resistant) for a confirmed validated case
				if(startTrtDt == null && dstResDt!=null){
					daysDiag = DateUtils.daysBetween(regDt, dstResDt);
					delayDiagtArr.add(daysDiag);
				}
			}	
			
			
			addValue(messages.get("manag.delay.num"), messages.get("manag.delay.diag"), new Float(delayDiagtArr.size()));
			addValue(messages.get("manag.delay.num"), messages.get("manag.delay.start"), new Float (delayStartTreatArr.size()));
			
			float meanDelayDiag = calcMean(delayDiagtArr);
			float meanStartTreat = calcMean(delayStartTreatArr);
			addValue(messages.get("manag.delay.mean"), messages.get("manag.delay.diag"), meanDelayDiag);
			addValue(messages.get("manag.delay.mean"), messages.get("manag.delay.start"), meanStartTreat);
			
			int minDelayDiag = getMin(delayDiagtArr);
			int minStartTreat = getMin(delayStartTreatArr);
			addValue(messages.get("manag.delay.min"), messages.get("manag.delay.diag"), new Float (minDelayDiag));
			addValue(messages.get("manag.delay.min"), messages.get("manag.delay.start"), new Float (minStartTreat));
			
			int maxDelayDiag = getMax(delayDiagtArr);
			int maxStartTreat = getMax(delayStartTreatArr);
			addValue(messages.get("manag.delay.max"), messages.get("manag.delay.diag"), new Float (maxDelayDiag));
			addValue(messages.get("manag.delay.max"), messages.get("manag.delay.start"), new Float (maxStartTreat));
		
	
	}
	
	@Override
	protected String getHQLSelect() {
		// TODO Auto-generated method stub
		return "select c.id, c.registrationDate, c.treatmentPeriod.iniDate, dstres.exam.dateRelease";
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		return " from ExamDSTResult dstres inner join dstres.exam.tbcase c ";
		
	}
	
public List<Object[]> getDSTCases(String condition){
		
		setOutputSelected(false);
		setGroupFields(null);
		setCondition(condition);	
		return createQuery().getResultList();
	}
	
	public float calcMean(List<Integer> iarr){
		if(iarr.size()==0)
			return 0;
		else {
			int i=0;
			int tot = 0;
			for(i=0; i<iarr.size(); i++){
				tot = tot + iarr.get(i);
			}
			return tot/iarr.size();
			}
	}
	
	public int getMin(List<Integer> iarr){
		if(iarr.size()==0)
			return 0;
		else {
			int minVal = iarr.get(0);
			for (int i=1; i<iarr.size(); i++){
				if(iarr.get(i) < minVal){
					minVal = iarr.get(i);
				}
			}
		return minVal;
		}
	}
	
	public int getMax(List<Integer> iarr){
		if(iarr.size()==0)
			return 0;
		else {
		int maxVal = iarr.get(0);
		for (int i=1; i<iarr.size(); i++){
			if(iarr.get(i) > maxVal){
				maxVal = iarr.get(i);
			}
		}
		return maxVal;
		}
	}

}
