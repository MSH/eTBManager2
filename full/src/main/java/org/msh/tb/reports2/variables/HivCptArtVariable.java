/**
 * 
 */
package org.msh.tb.reports2.variables;

import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate ART or CPT report based on HIV exam results. The type of report
 * is set in an argument of the constructor
 * @author Ricardo Memoria
 *
 */
public class HivCptArtVariable extends VariableImpl {

	public enum ReportType {CPT_REPORT, ART_REPORT};
	
	private static final Integer VAL_YES = 1;
	private static final Integer VAL_NO = 0;
	
	private ReportType type;
	
	/**
	 * Default constructor, with a unique argument indicating if the variable will generate
	 * indicators about CPT or ART data
	 * @param type type of the report to generate - ART or CPT
	 */
	public HivCptArtVariable(ReportType type) {
		super(type == ReportType.CPT_REPORT ? "hiv_ctp" : "hiv_art", 
				type == ReportType.CPT_REPORT? "cases.examhiv.cpt" : "cases.examhiv.art", 
				null, null);
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.select("dt is not null");
		// add a table in the join
		def.join("(select case_id, max(" + getHivField() + ") dt from examhiv "
				+ "where result=0 group by case_id)", "case_id", "tbcase", "id");
	}

	
	/**
	 * @return
	 */
	protected String getHivField() {
		if (type == ReportType.CPT_REPORT)
			return "startedCPTdate";
		else return "startedARTdate";
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		if (VAL_YES.equals(key))
			return Messages.instance().get("global.yes");
		
		if (VAL_NO.equals(key))
			return Messages.instance().get("global.no");

		// if range is not found, return undefined 
		return super.getDisplayText(key);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
		
		// get the right field
		String field = getHivField();

		// check the restriction
		if (VAL_YES.equals(Integer.parseInt( (String)value ))) 
			 field += " is not null";
		else field += " is null";

		// add the restriction
		def.addRestriction("exists(select * from examhiv where case_id=tbcase.id and result=0 and " + field + ")");
	}

	/** {@inheritDoc}
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();
		lst.add(new FilterOption(0, getDisplayText(0)));
		lst.add(new FilterOption(1, getDisplayText(1)));

		return lst;
	}

	@Override
	public boolean isMultiSelection() {
		return false;
	}
}
