/**
 * 
 */
package org.msh.tb.na.dataanalysis;

import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

/**
 * @author Ricardo Memoria
 *
 */
public class SocialSupportNAVariable extends VariableImpl {

//	private static final int IT_ALLCASES = 0;
	private static final int IT_SOCDISABILITY = 0;
	private static final int IT_FOODPACKAGE = 1;
	private static final int IT_TRANSPASSIST = 2;
	
	public SocialSupportNAVariable() {
		super("socsupNA", "cases.socialsupport", null);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.table("tbcase").join("id", "tbcasena.id");
		def.select("'" + Integer.toString(iteration) + "'");

		switch (iteration) {
		case IT_SOCDISABILITY:
			def.addRestriction("tbcasena.socialDisabilityAwarded = true");
			break;

		case IT_FOODPACKAGE:
			def.addRestriction("tbcasena.foodPackageAwarded = true");
			break;

		case IT_TRANSPASSIST:
			def.addRestriction("tbcasena.transportAssistProvided = true");
			break;
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public int getIteractionCount() {
		return 3;
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getDisplayText(Object key) {
		int iteration = Integer.parseInt(key.toString());
		switch (iteration) {
/*		case IT_ALLCASES:
			return formatMessage("cases");
*/			
		case IT_FOODPACKAGE:
			return formatMessage("TbCaseNA.foodPackageAwarded");
		
		case IT_SOCDISABILITY:
			return formatMessage("TbCaseNA.socialDisabilityAwarded");

		case IT_TRANSPASSIST:
			return formatMessage("TbCaseNA.transportAssistProvided");
		}
		return super.getDisplayText(key);
	}


	/** {@inheritDoc}
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object createKey(Object value) {
		if (!(value instanceof Integer)) {
			return Integer.parseInt( value.toString() );
		}
		
		return super.createKey(value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object[] getDomain() {
		Integer[] vals = new Integer[3];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = i;
		}
		return vals;
	}


}
