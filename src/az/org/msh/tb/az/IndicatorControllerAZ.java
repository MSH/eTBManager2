package org.msh.tb.az;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.IndicatorController;

@Name("indicatorControllerAZ")
public class IndicatorControllerAZ {
	@In(create=true) IndicatorController indicatorController;
	@In(required=false) Report08AZ report08az;
	
	public void execute() {
		indicatorController.execute();
		if (report08az!=null)
			report08az.clear();
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
}
