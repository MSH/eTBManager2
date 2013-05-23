package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.TbField;

public class ExtrapulmonarVariable extends FieldValueVariable {

	public ExtrapulmonarVariable() {
		super("extrapulmonar", "TbField.EXTRAPULMONARY_TYPES", "tbcase.extrapulmonary_id", TbField.EXTRAPULMONARY_TYPES);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (values == null)
			return null;

		Object[] ids = (Object[])values;

		if (ids[1] == null)
			 return ids[0];
		else return ids;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		super.prepareVariableQuery(def, iteration);
		def.addField("tbcase.extrapulmonary2_id");
		def.addRestriction("tbcase.infectionSite in (" + InfectionSite.BOTH.ordinal() + "," + InfectionSite.EXTRAPULMONARY.ordinal() + ")");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		def.addRestriction("(tbcase.extrapulmonary_id = " + value + " or tbcase.extrapulmonary2_id = " + value + ")");
	}

}
