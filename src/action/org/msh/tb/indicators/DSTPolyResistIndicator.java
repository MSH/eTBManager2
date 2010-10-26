package org.msh.tb.indicators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ResistancePattern;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;

/**
 * Generates indicator about DST resistances
 * @author Utkarsh Srivastava
 *
 */
@Name("polyResistanceIndicator")
public class DSTPolyResistIndicator extends Indicator2D {



	/**
	 * 
	 */
	private static final long serialVersionUID = -7184080485375502553L;
	@In(create=true) List<Substance> substanceList;
	private String newp;
	private String newPer;
	private String oldp;
	private String oldPer;
	private String total;
	private String medicine;

	
	
	@Override
	protected void createIndicators() {
		setGroupFields(null);
		
		String[] message = new String[5];
		newp 		= getMessage("manag.ind.dstprofile.nevertreated");
		newPer 		= getMessage("manag.ind.dstprofile.prevtreated");
		oldp 		= getMessage("manag.ind.dstprofile.nevertreatedpercent");
		oldPer 		= getMessage("manag.ind.dstprofile.prevtreatedpercent");
		total 		= getMessage("global.total");
		medicine 	= "";
		
		message[0] = newp;
		message[1] = oldp;
		message[2] = newPer;
		message[3] = oldPer;
		message[4] = total;
		
		List<ResistancePattern> resistancePatterns = new ArrayList<ResistancePattern>();
		Substance E = substanceList.get(0);
		Substance H = substanceList.get(1);
		Substance R = substanceList.get(2);
		Substance S = substanceList.get(3);
				
		ResistancePattern rp1 = new ResistancePattern();
		rp1.setName("H+E");
		List<Substance> substances1 = new ArrayList<Substance>();
		substances1.add(H);
		substances1.add(E);
		rp1.setSubstances(substances1);
		resistancePatterns.add(rp1);
		
		
		ResistancePattern rp2 = new ResistancePattern();
		rp2.setName("H+S");
		List<Substance> substances2 = new ArrayList<Substance>();
		substances2.add(H);
		substances2.add(S);
		rp2.setSubstances(substances2);
		resistancePatterns.add(rp2);
		
		ResistancePattern rp3 = new ResistancePattern();
		rp3.setName("H+E+S");
		List<Substance> substances3 = new ArrayList<Substance>();
		substances3.add(H);
		substances3.add(E);
		substances3.add(S);
		rp3.setSubstances(substances3);
		resistancePatterns.add(rp3);
		
		ResistancePattern rp4 = new ResistancePattern();
		rp4.setName("R+E");
		List<Substance> substances4 = new ArrayList<Substance>();
		substances4.add(R);
		substances4.add(E);
		rp4.setSubstances(substances4);			
		resistancePatterns.add(rp4);
		
		ResistancePattern rp5 = new ResistancePattern();
		rp5.setName("R+S");
		List<Substance> substances5 = new ArrayList<Substance>();
		substances5.add(R);
		substances5.add(S);
		rp5.setSubstances(substances5);			
		resistancePatterns.add(rp5);
		
		ResistancePattern rp6 = new ResistancePattern();
		rp6.setName("R+E+S");
		List<Substance> substances6 = new ArrayList<Substance>();
		substances6.add(R);
		substances6.add(E);
		substances6.add(S);
		rp6.setSubstances(substances6);			
		resistancePatterns.add(rp6);
		
		ResistancePattern rp7 = new ResistancePattern();
		rp7.setName("E+S");
		List<Substance> substances7 = new ArrayList<Substance>();
		substances7.add(E);
		substances7.add(S);
		rp7.setSubstances(substances7);			
		resistancePatterns.add(rp7);
		
		
		IndicatorTable table = getTable();
		table.addColumn(newp, null);
		table.addColumn(oldp, null);

		TableColumn newPercent = table.addColumn(newPer, null);
		newPercent.setHighlight(true);
		
		TableColumn oldPercent = table.addColumn(oldPer, null);
		oldPercent.setHighlight(true);
		
		TableColumn totalCell = table.addColumn(total, null);
		totalCell.setRowTotal(false);
		
		
		Float newCalc = null;
		Float oldCalc = null;
		for (int i = 0; i < resistancePatterns.size(); i++) {
			ResistancePattern pattern = resistancePatterns.get(i);
			Long newValue = addResistancePattern(pattern, false);
			Long oldValue = addResistancePattern(pattern, true);
			Long total = newValue + oldValue;
			
			if(total != 0){
				newCalc = (newValue.floatValue()/total * 100);
				oldCalc = (oldValue.floatValue()/total * 100);
			}else{
				newCalc = new Float(0);
				oldCalc = new Float(0);
			}

			addValue(message[0], pattern.getName(), newValue.floatValue());
			addValue(message[1], pattern.getName(), oldValue.floatValue());
			addValue(message[2], pattern.getName(), newCalc);
			addValue(message[3], pattern.getName(), oldCalc);
			addValue(message[4], pattern.getName(), total.floatValue());
		}
		
	}
	
	/**
	 * Mounts resistance pattern of a set of medicines
	 * @param substances
	 */
	protected Long addResistancePattern(ResistancePattern pattern, boolean other) {
		if (pattern.getSubstances().size() == 0)
			return new Long(0);
		String s = "";
		for (Substance sub: pattern.getSubstances()) {
			if (!s.isEmpty()) {
				s = s + ",";
			}
			s = s + sub.getId().toString();
		}
		
		String cond;
		if(!other)
		// rule: check if the first DST exam of the case and check if it's according to the resistance pattern 
			cond = "(select count(*) from ExamDSTResult res " +
				"join res.exam exam " +
				"where exam.tbcase.id = c.id and res.substance.id in (" + s +
				") and res.result = " + DstResult.RESISTANT.ordinal() + 
				" and exam.numResistant = " + pattern.getSubstances().size() + 
				" and c.patientType = " + PatientType.NEW.ordinal() +
				" and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux " +
				"where aux.tbcase.id = c.id)) = " + pattern.getSubstances().size();
		else
		// rule: check if the first DST exam of the case and check if it's according to the resistance pattern 
			cond = "(select count(*) from ExamDSTResult res " +
				"join res.exam exam " +
				"where exam.tbcase.id = c.id and res.substance.id in (" + s +
				") and res.result = " + DstResult.RESISTANT.ordinal() + 
				" and exam.numResistant = " + pattern.getSubstances().size() + 
				" and c.patientType != " + PatientType.NEW.ordinal() +
				" and exam.dateCollected = (select min(aux.dateCollected) from ExamDST aux " +
				"where aux.tbcase.id = c.id)) = " + pattern.getSubstances().size();		
		setCondition(cond);
		Long val = (Long)createQuery().getSingleResult();
//		addValue(pattern.getName(), val.intValue());
		return val;
	}
	
}
