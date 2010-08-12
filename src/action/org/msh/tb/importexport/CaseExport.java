package org.msh.tb.importexport;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.indicators.core.CaseHQLBase;
import org.msh.tb.indicators.core.IndicatorFilters;


/**
 * Handle export of cases to an Excel file
 * @author Ricardo Memoria
 *
 */
@Name("caseExport")
@BypassInterceptors
public class CaseExport extends CaseHQLBase {
	private static final long serialVersionUID = 7234788990898793962L;

	private List<TbCase> cases;

	
	/**
	 * Execute the download of the excel file
	 * @return page representing excel file
	 */
	public String download() {
		return "/export/cases.xhtml";
	}

	
	/**
	 * Return the list of cases based on the filters in {@link IndicatorFilters} session variable
	 * @return list of objects {@link TbCase}
	 */
	public List<TbCase> getCases() {
		if (cases == null)
			createCases();
		return cases;
	}


	/**
	 * Create the list of cases based on the filters in the {@link IndicatorFilters} session variable 
	 */
	protected void createCases() {
		setNewCasesOnly(true);
		cases = createQuery().getResultList();
	}

}
