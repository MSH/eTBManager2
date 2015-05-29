package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.TbField;

/**
 * A variable that handles all types of TB case pulmonary
 *  
 * @author Ricardo Memoria
 *
 */
public class PulmonaryVariable extends FieldValueVariable {

	public PulmonaryVariable() {
		super("pulmonary", "TbField.PULMONARY_TYPES", "tbcase.pulmonary_id", TbField.PULMONARY_TYPES);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		super.prepareVariableQuery(def, iteration);
		def.addRestriction("tbcase.infectionSite in (" + InfectionSite.BOTH.ordinal() + "," + InfectionSite.PULMONARY.ordinal() + ")");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		super.prepareFilterQuery(def, oper, value);
		def.addRestriction("tbcase.infectionSite in (" + InfectionSite.BOTH.ordinal() + "," + InfectionSite.PULMONARY.ordinal() + ")");
	}

}
