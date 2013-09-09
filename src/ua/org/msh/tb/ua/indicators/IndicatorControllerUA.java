package org.msh.tb.ua.indicators;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.indicators.core.IndicatorController;
import org.msh.tb.indicators.core.IndicatorFilters;

@Name("indicatorControllerUA")
public class IndicatorControllerUA {
	@In(create=true) IndicatorController indicatorController;
	@In(required=false) ReportTB07 reportTB07;
	@In(required=false) ReportTB07mrtb reportTB07mrtb;
	@In(required=false) ReportTB08 reportTB08;
	@In(required=false) ReportTB10 reportTB10;
	@In(required=false) ReportTB11 reportTB11;
	
	public void execute() {
		indicatorController.execute();
		if (reportTB07!=null)
			reportTB07.clear();
		if (reportTB07mrtb!=null)
			reportTB07mrtb.clear();
		if (reportTB08!=null)
			reportTB08.clear();
		if (reportTB10!=null)
			reportTB10.clear();
		if (reportTB11!=null)
			reportTB11.clear();
	}
	
	/**
	 * @return the executing
	 */
	public boolean isExecuting() {
		return indicatorController.isExecuting();
	}

	/**
	 * @param executing the executing to set
	 */
	public void setExecuting(boolean executing) {
		indicatorController.setExecuting(executing);
	}
	
	/**
	 * Set only useDiagnosisDate to true as default in filters for reports
	 */
	public void setUseDiagDateDefaultTrue(){
		IndicatorFilters filters = (IndicatorFilters)App.getComponent("indicatorFilters");
		filters.setUseIniTreatmentDate(false);
		filters.setUseRegistrationDate(false);
		filters.setUseDiagnosisDate(true);
	}
}
