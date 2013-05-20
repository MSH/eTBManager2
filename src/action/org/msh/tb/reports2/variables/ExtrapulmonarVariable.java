package org.msh.tb.reports2.variables;

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

}
