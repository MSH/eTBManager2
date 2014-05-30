package org.msh.tb.reports2.variables;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

/**
 * Report variable that calculate number of suspects and confirmed cases
 * 
 * @author Ricardo Memoria
 *
 */
public class SuspectConfirmedVariable extends VariableImpl {

	// keys used in suspect and confirmed cases
	private final Integer KEY_CONFIRMED = 1;
	private final Integer KEY_SUSPECT = 0;
	private final Integer KEY_CONFIRMED_SUSPECT = 2;
	
	public SuspectConfirmedVariable() {
		super("diagtype", "DiagnosisType", "registrationDate < diagnosisDate");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		super.prepareVariableQuery(def, iteration);
/*		if (iteration == 0) {
			def.addField("'susp'");
			def.addRestriction("(registrationDate < diagnosisDate or diagnosisDate is null)");
		}
		else {
			def.addField("'conf'");
			def.addRestriction("diagnosisDate is not null");
		}
*/	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (values == null)
			return KEY_SUSPECT;
		
		if (((Integer)values) == 0)
			return KEY_CONFIRMED;
		
		return KEY_CONFIRMED_SUSPECT;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		if ((value == null) || (KEY_NULL.equals(value)))
			return;

		if (KEY_CONFIRMED.equals(value))
			 def.addRestriction("tbcase.registrationDate >= tbcase.diagnosisDate");
		else
		if (KEY_SUSPECT.equals(value))
			def.addRestriction("tbcase.diagnosisDate is null");
		else
		if (KEY_CONFIRMED_SUSPECT.equals(value))
			def.addRestriction("tbcase.registrationDate < tbcase.diagnosisDate");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return Integer.parseInt(value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (KEY_SUSPECT.equals(key)) 
			 return Messages.instance().get("DiagnosisType.SUSPECT");
		
		if (KEY_CONFIRMED.equals(key))
			return Messages.instance().get("DiagnosisType.CONFIRMED");
		
		if (KEY_CONFIRMED_SUSPECT.equals(key))
			return Messages.instance().get("manag.reportgen.suspconf");
		
		return super.getDisplayText(key);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();
		lst.add(createFilterOption(KEY_SUSPECT));
		lst.add(createFilterOption(KEY_CONFIRMED_SUSPECT));
		lst.add(createFilterOption(KEY_CONFIRMED));
		return lst;
	}

}
