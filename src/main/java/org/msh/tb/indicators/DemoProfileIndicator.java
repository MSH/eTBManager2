package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.Nationality;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorSeries;

import java.util.List;
import java.util.Map;

/**
 * Generate the demographic indicator
 * @author Ricardo Memoria
 *
 */
@Name("demoProfileIndicator")
public class DemoProfileIndicator extends Indicator {
	private static final long serialVersionUID = -5669436052284216230L;

	private IndicatorSeries seriesNationality;
	private IndicatorSeries seriesAge;
	
	@Override
	protected void createIndicators() {
		// create indicators by gender
		setNewCasesOnly(false);

		List<Object[]> lst = generateValuesByField("c.patient.gender", null);
		
		Map<String, String> messages = getMessages();
		
		for (Object[] vals: lst) {
			Gender gen = (Gender)vals[0];
			Long qtd = (Long)vals[1];
			
			addValue(messages.get(gen.getKey()), qtd.intValue());
		}
	}

	
	/**
	 * Create indicator by nationality
	 */
	protected void createIndicatorsNationality() {
		seriesNationality = new IndicatorSeries();
		
		setNewCasesOnly(false);

		List<Object[]> lst = generateValuesByField("c.nationality", null);
		
		Map<String, String> messages = getMessages();
		
		for (Object[] vals: lst) {
			Nationality val = (Nationality)vals[0];
			Long qtd = (Long)vals[1];
			
			if (val == null)
				 seriesNationality.addValue("undefined", qtd.intValue());
			else seriesNationality.addValue(messages.get(val.getKey()), qtd.intValue());
		}
		seriesNationality.sort();
	}


	/**
	 * Create indicator by age range 
	 */
	protected void createIndicatorsAge() {
		seriesAge = new IndicatorSeries();
		seriesAge.sort();
	}
	
	public IndicatorSeries getSeriesNationality() {
		if (seriesNationality == null)
			createIndicatorsNationality();
		return seriesNationality;
	}

	public IndicatorSeries getSeriesAge() {
		if (seriesAge == null)
			createIndicatorsAge();
		return seriesAge;
	}
}
