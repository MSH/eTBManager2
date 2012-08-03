package org.msh.tb.bd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.indicators.core.Indicator2D;

@Name("tBForm10Block6")
public class TBForm10Block6 extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1095628534810163566L;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		
					
					List<TbCase> lst = new ArrayList<TbCase>();
					lst = createQuery().getResultList();
					
					float cntSusM = 0, cntSusF = 0, cntConfM = 0, cntConfF = 0;
					
					for(TbCase val:lst){
						if(val.getDiagnosisType()==DiagnosisType.SUSPECT) {
							if(val.getPatient().getGender()==Gender.MALE)
								cntSusM++;
							if(val.getPatient().getGender()==Gender.FEMALE)
								cntSusF++;			
						}
							
						if(val.getDiagnosisType()==DiagnosisType.CONFIRMED) {
							if(val.getPatient().getGender()==Gender.MALE)
								cntConfM++;
							if(val.getPatient().getGender()==Gender.FEMALE)
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
		return "";
	}
	
}
