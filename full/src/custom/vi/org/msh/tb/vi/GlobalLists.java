package org.msh.tb.vi;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.PatientType;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

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
	
	private static final MtbDetected mtbDetected[] = {
		MtbDetected.YES,
		MtbDetected.NO,
		MtbDetected.ERROR
	};
	
	private static final DstResult dstResultsForMtbYes[] = {
		DstResult.NOTDONE,
		DstResult.RESISTANT,
		DstResult.NOTRESISTANT,
		DstResult.INTERMEDIATE,
		DstResult.CONTAMINATED
	};
	
	private static final DstResult dstResultsForMtbNo[] = {
		DstResult.ERROR
	};
	
	/**
	 * Return options of {@link MtbDetected}
	 * @return
	 */
	public MtbDetected[] getMtbDetectedOptions() {
		return mtbDetected;
	}
		
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
	
	public DstResult[] getDstResultsForMtbYes() {
		return dstResultsForMtbYes;
	}
	
	public DstResult[] getDstResultsForMtbNo() {
		return dstResultsForMtbNo;
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

	@Factory("patientTypesALL_vi")
	public PatientType[] getPatientTypesALL() {
		return patientTypes;
	}

	@Factory("patientTypesTB_vi")
	public PatientType[] getPatientTypesTB() {
		return patientTypes;
	}

	@Factory("patientTypesDRTB_vi")
	public PatientType[] getPatientTypesDRTB() {
		return patientTypes;
	}

}
