package org.msh.tb.indicators.core;

import org.jboss.seam.annotations.Name;

/**
 * Control the execution of the indicators in the page 
 * @author Ricardo Memoria
 *
 */
@Name("indicatorController")
public class IndicatorController {

	private boolean executing;

	public void execute() {
		executing = true;
	}
	
	/**
	 * @return the executing
	 */
	public boolean isExecuting() {
		return executing;
	}

	/**
	 * @param executing the executing to set
	 */
	public void setExecuting(boolean executing) {
		this.executing = executing;
	}
	
}
