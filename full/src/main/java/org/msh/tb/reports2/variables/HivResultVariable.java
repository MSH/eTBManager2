package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorEnum;
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
		HIVResult res;
		switch (iteration) {
		case 0: res = HIVResult.POSITIVE;
		break;
		
		case 1: res = HIVResult.NEGATIVE;
		break;
		
		case 2: res = HIVResult.NOTDONE;
		break;
		
		default:
			throw new IllegalArgumentException("Unexpected iteration " + iteration);
		}

		addCommonRestrictions(def, res);
		
		def.select("'" + Integer.toString(res.ordinal() + 1) + "'");
	}

	
	/**
	 * Add common restrictions to the variable and filter
	 * @param def
	 * @param res
	 */
	protected void addCommonRestrictions(SQLDefs def, HIVResult res) {
		String tbl = def.getMasterTable().getAlias();

        String sql = sqlRestriction(def, res);
        if (sql != null) {
            def.addRestriction(sql);
        }
	}


    /**
     * generate sql restriction to be applied in the filter
     * @param def Object containing information about the query to be generated
     * @param res the kind of result to be restricted
     * @return SQL restriction
     */
	protected String sqlRestriction(SQLDefs def, HIVResult res) {
        String tbl = def.getMasterTable().getAlias();
		switch (res) {
			case POSITIVE: // POSITIVE
				return "exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id)";

			case NEGATIVE: // NEGATIVE
				return "exists (select * from examhiv where examhiv.result=1 and examhiv.case_id=" + tbl + ".id) and " +
                        "not exists (select * from examhiv where examhiv.result=0 and examhiv.case_id=" + tbl + ".id)";

			case NOTDONE: // NOT DONE
				return "not exists (select * from examhiv where examhiv.result in (0, 1) and examhiv.case_id=" + tbl + ".id)";

			case ONGOING:
                return null;
		}
        throw new RuntimeException("Not expected value " + res);
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(final SQLDefs def, FilterOperation oper, ValueHandler value) {
        value.mapSqlOR(new ValueIteratorEnum<HIVResult>() {
            @Override
            public String iterateEnum(HIVResult value, int index) {
                return sqlRestriction(def, value);
            }
        });
/*
		if (value == null)
			return;

        if (value.getClass().isArray()) {
            Enum[] vals = (Enum[])value;
            String s = "";
            for (Enum res: vals) {
                if (!s.isEmpty()) {
                    s += " or ";
                }
                String restric = sqlRestriction(def, (HIVResult)res);
                if (restric != null) {
                    s += "(" + restric + ")";
                }
            }
            def.addRestriction("(" + s + ")");
        }
        else {
            addCommonRestrictions(def, (HIVResult)value);
        }
*/

//		super.prepareFilterQuery(def, oper, value);
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
