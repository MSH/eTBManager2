package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.enums.HIVResult;


/**
 * Generate number of cases by HIV result
 * 
 * @author Ricardo Memoria
 *
 */
public class HivResultVariable extends EnumFieldVariable {

	public HivResultVariable() {
		super("hivres", "HIVResult", null, HIVResult.class);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String tbl = def.getMasterTable().getAlias();

		switch (iteration) {
		case 0: // POSITIVE
			def.addField("'1'");
			def.addRestriction("exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id)");
			break;

		case 1: // NEGATIVE
			def.addField("'2'");
			def.addRestriction("exists (select * from examhiv where examhiv.result=1 and examhiv.case_id=" + tbl + ".id)");
			def.addRestriction("not exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id)");
			break;

		case 2: // NOT DONE
			def.addField("'4'");
			def.addRestriction("not exists (select * from examhiv where examhiv.result in (0, 1) and examhiv.case_id=" + tbl + ".id)");
			break;

		default:
			throw new RuntimeException("Unknown iteration " + iteration);
		}
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
		if (value == null)
			return;

		HIVResult res = (HIVResult)filterValueFromString((String)value);
		String tbl = def.getMasterTable().getAlias();
		
		switch (res) {
		case NEGATIVE:
			def.addRestriction("exists (select * from examhiv where examhiv.result=1 and examhiv.case_id=" + tbl + ".id)");
			def.addRestriction("not exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id");
			break;

		case POSITIVE:
			def.addRestriction("exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id)");
			break;

		case NOTDONE:
			def.addRestriction("not exists (select * from examhiv where examhiv.result not in (0, 1) and examhiv.case_id=" + tbl + ".id");
			break;
			
		case ONGOING:
			def.addRestriction("exists (select * from examhiv where examhiv.result=2 and examhiv.case_id=" + tbl + ".id)");
			def.addRestriction("not exists (select * from examhiv where examhiv.result<>2 and examhiv.case_id=" + tbl + ".id");
			break;

		default:
			break;
		}
		super.prepareFilterQuery(def, oper, value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getIteractionCount()
	 */
	@Override
	public int getIteractionCount() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.EnumFieldVariable#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		Integer val = Integer.parseInt(value.toString()) - 1;
		return super.createKey(val);
	}

}
