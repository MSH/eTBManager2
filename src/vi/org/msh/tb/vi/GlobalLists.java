package org.msh.tb.vi;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.PatientType;

@Name("globalLists_vi")
public class GlobalLists {

	private static final CultureResult cultureResults[] = {
		CultureResult.NOTDONE,
		CultureResult.NEGATIVE,
		CultureResult.POSITIVE,
		CultureResult.PLUS,
		CultureResult.PLUS2,
		CultureResult.PLUS3,
		CultureResult.PLUS4,
		CultureResult.CONTAMINATED
	};


	private static final PatientType patientTypes[] = {
		PatientType.NEW,
		PatientType.TRANSFER_IN,
		PatientType.RELAPSE_CATI,
		PatientType.RELAPSE_CATII,
		PatientType.FAILURE_CATI_2ND,
		PatientType.FAILURE_CATI_3RD,
		PatientType.FAILURE_CATII_2ND,
		PatientType.FAILURE_CATII_3RD,
		PatientType.CUSTOM1,
		PatientType.CUSTOM2,
		PatientType.CUSTOM3,
		PatientType.CUSTOM4,
		PatientType.CUSTOM5,
		PatientType.CUSTOM6,
		PatientType.CUSTOM7,
		PatientType.CUSTOM8,
		PatientType.CUSTOM9,
		PatientType.CUSTOM10,
		PatientType.CUSTOM11,
		PatientType.CUSTOM12,
		PatientType.CUSTOM13,
		PatientType.CUSTOM14,
		PatientType.CUSTOM15,
		PatientType.OTHER
	};

	
	/**
	 * Return the results of culture customized to Vietnam
	 * @return
	 */
	public CultureResult[] getCultureResults() {
		return cultureResults;
	}
	
	public PatientType[] getPatientTypes() {
		return patientTypes;
	}
	
	/**
	 * Return the number of AFBs in a microscopy exam customized to Vietnam
	 * @return
	 */
	public List<SelectItem> getNumberOfAFBs() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		SelectItem item = new SelectItem();
		item.setLabel("-");
		lst.add(item);
		
		for (int i = 1; i <= 19; i++) {
			item = new SelectItem();
			item.setLabel(Integer.toString(i));
			item.setValue(i);
			lst.add(item);
		}
		
		return lst;
	}
}
