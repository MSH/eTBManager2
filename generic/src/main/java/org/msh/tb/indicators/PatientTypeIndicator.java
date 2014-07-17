package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorFilters;

import java.util.List;
import java.util.Map;

/**
 * Generates patient type indicator<br>
 * Consider all cases waiting for treatment or on treatment during the period set in {@link IndicatorFilters}  
 * @author Ricardo Memoria
 *
 */
@Name("patientTypeIndicator")
public class PatientTypeIndicator extends Indicator{
	private static final long serialVersionUID = 1L;

	@Override
	protected void createIndicators() {
		List<Object[]> lst = generateValuesByField("c.patientType", null);
		
		Map<String, String> messages = getMessages();
		
		for (Object[] vals: lst) {
			PatientType pt = (PatientType)vals[0];
			Long qtd = (Long)vals[1];
			
			String msg;
			
			if (pt != null)
				 msg = messages.get(pt.getKey());
			else msg = messages.get("global.notdef");
			
			addValue(msg, qtd.intValue());
		}
	}

}
