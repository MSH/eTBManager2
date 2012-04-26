package org.msh.tb.indicators;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;

/**
 * Generates indicator about DST resistances for previously treated patients
 * @author Vani Rao
 *
 */
@Name("dSTPrevTreatFLDIndicator")
public class DSTPrevTreatFLDIndicator extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -13626993693577124L;
	
	private int total;
	private int flag = 0;
	private int countNewPatients = 0;
	private int countPrevTrtPatRelapse = 0;
	private int countPrevTrtPatFail1 = 0;
	private int countPrevTrtReTreat = 0;
	
	

	@In(create=true) List<Substance> substanceList;
	private String strPrevTrtPatRelapse;
	private String strPrevTrtPatFail1;
	private String strPrevTrtReTreat;
	private String tot;
	


	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		setGroupFields(null);
		String[] message = new String[4];
		strPrevTrtPatRelapse = getMessage("manag.ind.dstprofile.prevtreatedRelpase");
		strPrevTrtPatFail1 = getMessage("manag.ind.dstprofile.prevtreatedFail1");
		strPrevTrtReTreat = getMessage("manag.ind.dstprofile.prevtreatedReTreat");
		tot    = getMessage("global.total");

		message[0] = strPrevTrtPatRelapse;
		message[1] = strPrevTrtPatFail1;
		message[2] = strPrevTrtReTreat;
		message[3] = tot;

		List<ResistancePattern> resistancePatterns = new ArrayList<ResistancePattern>();
		Substance E = substanceList.get(0);
		Substance H = substanceList.get(1);
		Substance R = substanceList.get(2);
		Substance S = substanceList.get(3);
		
		ResistancePattern rp0 = new ResistancePattern();
		rp0.setName("Any resistance to H");
		List<Substance> substances0 = new ArrayList<Substance>();
		substances0.add(H);
		rp0.setSubstances(substances0);
		resistancePatterns.add(rp0);
		
		ResistancePattern rp1 = new ResistancePattern();
		rp1.setName("H+R");
		List<Substance> substances1 = new ArrayList<Substance>();
		substances1.add(H);
		substances1.add(R);
		rp1.setSubstances(substances1);
		resistancePatterns.add(rp1);
		
		ResistancePattern rp2 = new ResistancePattern();
		rp2.setName("H+R+E");
		List<Substance> substances2 = new ArrayList<Substance>();
		substances2.add(H);
		substances2.add(R);
		substances2.add(E);
		rp2.setSubstances(substances2);
		resistancePatterns.add(rp2);
		
		ResistancePattern rp3 = new ResistancePattern();
		rp3.setName("H+R+S");
		List<Substance> substances3 = new ArrayList<Substance>();
		substances3.add(H);
		substances3.add(R);
		substances3.add(S);
		rp3.setSubstances(substances3);
		resistancePatterns.add(rp3);
		
		ResistancePattern rp4 = new ResistancePattern();
		rp4.setName("H+R+E+S");
		List<Substance> substances4 = new ArrayList<Substance>();
		substances4.add(H);
		substances4.add(R);
		substances4.add(E);
		substances4.add(S);
		rp4.setSubstances(substances4);			
		resistancePatterns.add(rp4);
		
		IndicatorTable table = getTable();
		table.addColumn(strPrevTrtPatRelapse, null);
		
		TableColumn newPercent = table.addColumn(strPrevTrtPatFail1, null);
		newPercent.setHighlight(true);
		
		table.addColumn(strPrevTrtReTreat, null);
		//TableColumn oldPercent = table.addColumn(strPrevTrtPatFailSLD, null);
		//oldPercent.setHighlight(true);
		
		TableColumn totalCell = table.addColumn(tot, null);
		totalCell.setRowTotal(false);
	

	Float newCalc = null;
	Float oldCalc = null;
	
	String strCondForOnly = " and exam.numResistant = " + "pattern.getSubstances().size()";
	
	for (int i = 0; i < resistancePatterns.size(); i++) {
		countNewPatients = 0;
		countPrevTrtPatRelapse = 0;
		countPrevTrtPatFail1 = 0;
		countPrevTrtReTreat = 0;
		List<TbCase> tbCaseList = new ArrayList<TbCase>();
		ResistancePattern pattern = resistancePatterns.get(i);
		
		if(i==0){
			tbCaseList = addResistancePatternForAny(pattern);
		}
		else{	
		tbCaseList = addResistancePattern(pattern);
		}
		
		if (tbCaseList.size() != 0) {
			for (int k = 0; k < tbCaseList.size(); k++) {
				if (tbCaseList.get(k).getPatientType().getKey()
						.equalsIgnoreCase("PatientType.NEW")|| tbCaseList.get(k).getPatientType().getKey()
						.equalsIgnoreCase("PatientType.TRANSFER_IN")) {
					countNewPatients = ++countNewPatients;	
				}
				
				if (tbCaseList.get(k).getPatientType().getKey().equalsIgnoreCase("PatientType.RELAPSE")){
					countPrevTrtPatRelapse = ++countPrevTrtPatRelapse;
				}
				if (tbCaseList.get(k).getPatientType().getKey().equalsIgnoreCase("PatientType.FAILURE_FT")){
					countPrevTrtPatFail1 = ++countPrevTrtPatFail1;
				}
				if (tbCaseList.get(k).getPatientType().getKey().equalsIgnoreCase("PatientType.FAILURE_RT")){
					countPrevTrtReTreat = ++countPrevTrtReTreat;
				}
			}
		}
		int totalPatients = countPrevTrtPatRelapse + countPrevTrtPatFail1 +countPrevTrtReTreat;
		
		addValue(message[0], pattern.getName(), new Float(countPrevTrtPatRelapse));
		addValue(message[1], pattern.getName(),  new Float(countPrevTrtPatFail1));
		addValue(message[2], pattern.getName(), new Float(countPrevTrtReTreat));
		addValue(message[3], pattern.getName(), new Float(totalPatients));
		
	} //end of for
}

/**
 * Mounts resistance pattern of a set of medicines
 * @param substances
 */
protected List<TbCase> addResistancePattern(ResistancePattern pattern) {
	if (pattern.getSubstances().size() == 0){
		List<TbCase> tbCaseListEmpty = new ArrayList<TbCase>();
		return tbCaseListEmpty;
	}
	String s = "";
	for (Substance sub: pattern.getSubstances()) {
		if (!s.isEmpty()) {
			s = s + ",";
		}
		s = s + sub.getId().toString();
	}
	
	String cond;
	cond = "(select count(*) from ExamDSTResult res " +
	"join res.exam exam " +
	"where exam.tbcase.id = c.id and res.substance.id in (" + s +
	") and res.result = " + DstResult.RESISTANT.ordinal() + 
	" and exam.numResistant = " + pattern.getSubstances().size() + 
	" and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux " +
	"where aux.tbcase.id = c.id)) = " + pattern.getSubstances().size();
	
	setCondition(cond);
	List<TbCase> tbCaseListRes = new ArrayList<TbCase>();
	tbCaseListRes = createQuery().getResultList();
	return tbCaseListRes;
}

/**
 * Mounts resistance pattern of a set of medicines
 * @param substances
 */
protected List<TbCase> addResistancePatternForAny(ResistancePattern pattern) {
	if (pattern.getSubstances().size() == 0){
		List<TbCase> tbCaseListEmpty = new ArrayList<TbCase>();
		return tbCaseListEmpty;
	}
	String s = "";
	for (Substance sub: pattern.getSubstances()) {
		if (!s.isEmpty()) {
			s = s + ",";
		}
		s = s + sub.getId().toString();
	}
	String cond;
	cond = "(select count(*) from ExamDSTResult res " +
	"join res.exam exam " +
	"where exam.tbcase.id = c.id and res.substance.id in (" + s +
	") and res.result = " + DstResult.RESISTANT.ordinal() + 
	" and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux " +
	"where aux.tbcase.id = c.id)) = " + pattern.getSubstances().size();
	
	setCondition(cond);
	List<TbCase> tbCaseListRes = new ArrayList<TbCase>();
	tbCaseListRes = createQuery().getResultList();
	return tbCaseListRes;
}

@Override
protected String getHQLSelect() {
	// TODO Auto-generated method stub
	//return super.getHQLSelect();
	return "";
}
}

