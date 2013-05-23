package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
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
	private final Integer KEY_CONFIRMED = 0;
	private final Integer KEY_SUSPECT = 1;
	
	public SuspectConfirmedVariable() {
		super("diagtype", "DiagnosisType", "diagnosisType");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		if (iteration == 0) {
			def.addField("'susp'");
			def.addRestriction("(registrationDate < diagnosisDate or diagnosisDate is null)");
		}
		else {
			def.addField("'conf'");
			def.addRestriction("diagnosisDate is not null");
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if ("susp".equals(values))
			 return KEY_SUSPECT;
		else return KEY_CONFIRMED;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getIteractionCount()
	 */
	@Override
	public int getIteractionCount() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		if (KEY_CONFIRMED.equals(value))
			 def.addRestriction("tbcase.diagnosisDate is not null");
		else def.addRestriction("(registrationDate < diagnosisDate or diagnosisDate is null)");
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
		else return Messages.instance().get("DiagnosisType.CONFIRMED");
	}

}
