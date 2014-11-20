package org.msh.tb.vi;

import org.msh.tb.cases.dispensing.CaseDispensingInfo;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.vi.entities.TreatmentMonitoringVI;


/**
 * Extends the CaseDospensingInfo method for cambodia workspace. 
 * @author Utkarsh Srivastava
 *
 */
public class CaseDispensingInfoVI extends CaseDispensingInfo{

	private TreatmentMonitoringVI treatmentMonitoring;


	public CaseDispensingInfoVI(TreatmentMonitoringVI treatmentMonitoring) {
		super((TreatmentMonitoring) treatmentMonitoring);
		this.treatmentMonitoring = treatmentMonitoring;
		mountDispensingDays();
	}
	


	public TreatmentMonitoringVI getTreatmentMonitoring() {
		return treatmentMonitoring;
	}



	public void setTreatmentMonitoring(TreatmentMonitoringVI treatmentMonitoring) {
		this.treatmentMonitoring = treatmentMonitoring;
	}

}
