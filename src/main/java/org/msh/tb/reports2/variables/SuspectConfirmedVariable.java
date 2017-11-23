package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorInt;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Report variable that calculate number of suspects and confirmed cases
 * 
 * @author Ricardo Memoria
 *
 */
public class SuspectConfirmedVariable extends VariableImpl {

	// keys used in suspect and confirmed cases
	private final int KEY_CONFIRMED = 1;
	private final int KEY_SUSPECT = 0;
	private final int KEY_CONFIRMED_SUSPECT = 2;
	
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
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		if ((value == null) || (KEY_NULL.equals(value)))
			return;

		String sql = value.mapSqlOR(new ValueIteratorInt() {
            @Override
            public String iterateInt(Integer value, int index) {
                return getSqlRestriction(value);
            }
        });

        def.addRestriction(sql);
	}


    protected String getSqlRestriction(int key) {
        switch (key) {
            case KEY_CONFIRMED: return "tbcase.registrationDate >= tbcase.diagnosisDate";
            case KEY_SUSPECT: return "tbcase.diagnosisDate is null";
            case KEY_CONFIRMED_SUSPECT: return "tbcase.registrationDate < tbcase.diagnosisDate";
            default: throw new RuntimeException("Invalid key " + key);
        }
    }

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
        return convertIntFilter(value);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
        switch ((Integer)key) {
            case KEY_SUSPECT: return Messages.instance().get("DiagnosisType.SUSPECT");
            case KEY_CONFIRMED: return Messages.instance().get("DiagnosisType.CONFIRMED");
            case KEY_CONFIRMED_SUSPECT: return Messages.instance().get("manag.reportgen.suspconf");
        }

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
