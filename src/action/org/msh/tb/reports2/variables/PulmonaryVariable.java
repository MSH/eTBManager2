package org.msh.tb.reports2.variables;

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
	public void prepareVariableQuery(SQLDefs def) {
		super.prepareVariableQuery(def);
		def.addRestriction("tbcase.infectionSite in (" + InfectionSite.BOTH.ordinal() + "," + InfectionSite.PULMONARY.ordinal() + ")");
	}

}
