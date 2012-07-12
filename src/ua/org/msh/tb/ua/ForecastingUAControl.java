package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.forecasting.ForecastingHome;
import org.msh.tb.forecasting.ForecastingView;

/**
 * Controller to provide UA specific for forecasting
 * Uses international forecasting components
 * 
 * @author alexey
 *
 */
@Name("forecastingUAControl")
public class ForecastingUAControl {
	@In(create=true) ForecastingView forecastingView;
	@In(create=true) ForecastingHome forecastingHome;

	public List<String> getMonths(){
		return forecastingView.getCasesRegimenTable().getMonths();
	}
}
