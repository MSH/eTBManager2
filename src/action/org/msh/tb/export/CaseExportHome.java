package org.msh.tb.export;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.CaseHQLBase;
import org.msh.tb.indicators.core.IndicatorFilters;


/**
 * Handle export of cases to an Excel file
 * @author Mauricio Santos
 *
 */
@Name("caseExportHome")
public class CaseExportHome extends CaseHQLBase {

	private static final long serialVersionUID = -6601234462983625566L;
	
	@In(create=true) CaseDataExport caseDataExport;

	private List<Integer> cases;
	
	/**
	 * Create the excel file and send it to the client browser
	 */
	public void download() {
		caseDataExport.download(getCasesId());
	}

	public List<Integer> getCasesId() {
		if (cases == null)
			createCases();
		return cases;
	}
	
	/**
	 * Create the list of cases based on the filters in the {@link IndicatorFilters} session variable 
	 */
	protected void createCases() {
		setNewCasesOnly(true);
		setHQLSelect("select c.id");
		cases = createQuery().getResultList();
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.indicators.core.CaseHQLBase#getHQLJoin()
	 */
	@Override
	protected String getHQLJoin() {
		return null;
	}

}
