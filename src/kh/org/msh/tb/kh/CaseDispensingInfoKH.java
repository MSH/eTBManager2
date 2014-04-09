package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.msh.tb.cases.dispensing.CaseDispensingInfo;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.kh.entities.TreatmentMonitoringKH;
import org.msh.utils.date.DateUtils;


/**
 * Extends the CaseDospensingInfo method for cambodia workspace. 
 * @author Utkarsh Srivastava
 *
 */
public class CaseDispensingInfoKH extends CaseDispensingInfo{

	private TreatmentMonitoringKH treatmentMonitoring;


	public CaseDispensingInfoKH(TreatmentMonitoringKH treatmentMonitoring) {
		super((TreatmentMonitoring) treatmentMonitoring);
		this.treatmentMonitoring = treatmentMonitoring;
		mountDispensingDays();
	}
	


	public TreatmentMonitoringKH getTreatmentMonitoring() {
		return treatmentMonitoring;
	}



	public void setTreatmentMonitoring(TreatmentMonitoringKH treatmentMonitoring) {
		this.treatmentMonitoring = treatmentMonitoring;
	}

}
