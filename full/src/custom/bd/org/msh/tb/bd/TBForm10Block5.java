package org.msh.tb.bd;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.enums.ReferredTo;
import org.msh.tb.indicators.core.Indicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("tBForm10Block5")
public class TBForm10Block5 extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7179702701143322617L;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
		String cond = " me.patientRefTo is not null "		
;

		setCondition(cond);
		
		List<Object> lst = new ArrayList<Object>();
		lst = createQuery().getResultList();
		
		float cntPP = 0, cntNONPP = 0, cntGFS = 0, cntSS = 0, cntVD = 0, cntCV = 0, cntGov = 0, cntPriv = 0, cntTBPat = 0, cntOther = 0;
		
		for(Object val:lst){
			
			if(val.toString().equalsIgnoreCase(ReferredTo.PP.toString()))
				cntPP++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.GFS.toString()))
				cntGFS++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.NON_PP.toString()))
				cntNONPP++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.SS.toString())) 
				cntSS++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.VD.toString())) 
				cntVD++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.CV.toString())) 
				cntCV++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.GOV.toString())) 
				cntGov++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.PRIVATE_HOSP.toString())) 
				cntPriv++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.TB_PATIENT.toString())) 
				cntTBPat++;
			
			if(val.toString().equalsIgnoreCase(ReferredTo.OTHER.toString())) 
				cntOther++;
		}
		
		float fzero = 0;
		
		Map<String, String> messages = Messages.instance();
	
		
		addValue(messages.get("manag.form10.block5.pp"), messages.get("#"), cntPP);
		addValue(messages.get("manag.form10.block5.nonpp"), messages.get("#"), cntNONPP);
		addValue(messages.get("manag.form10.block5.gfs"), messages.get("#"), cntGFS);
		addValue(messages.get("manag.form10.block5.ss"), messages.get("#"), cntSS);
		addValue(messages.get("manag.form10.block5.vd"), messages.get("#"), cntVD);
		addValue(messages.get("manag.form10.block5.cv"), messages.get("#"), cntCV);
		addValue(messages.get("manag.form10.block5.gov"), messages.get("#"), cntGov);
		addValue(messages.get("manag.form10.block5.priv"), messages.get("#"), cntPriv);
		addValue(messages.get("manag.form10.block5.tb"), messages.get("#"), cntTBPat);
		addValue(messages.get("manag.form10.block5.other"), messages.get("#"), cntOther);
		float cntTot = cntPP + cntNONPP + cntGFS + cntSS + cntVD + cntCV + cntGov + cntPriv + cntTBPat + cntOther;
		addValue(messages.get("manag.pulmonary.tot"), messages.get("#"), cntTot);
		
	}
	@Override
	public String getHQLSelect() {
		return "select me.patientRefTo";
	}
	
	@Override
	protected String getHQLFrom() {
		// TODO Auto-generated method stub
		return " from MedicalExamination me inner join me.tbcase c ";
		
	}
}
