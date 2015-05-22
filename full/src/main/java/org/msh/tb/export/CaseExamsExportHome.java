package org.msh.tb.export;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.CaseHQLBase;
import org.msh.tb.indicators.core.IndicatorFilters;

import java.util.List;
/**
 * Handle export of cases to an Excel file
 * @author Mauricio Santos
 *
 */
@Name("caseExamExportHome")
public class CaseExamsExportHome extends CaseHQLBase{

	private static final long serialVersionUID = 2222974356769288527L;

	@In(create=true) CaseDataExport caseDataExport;

	private List<Integer> cases;
	
	/**
	 * Create the excel file and send it to the client browser
	 */
	public String download() {
        List<Integer> ids = getCasesId();

        if(ids == null || ids.size()<1){
            return "no-cases-found";
        }

		caseDataExport.download(ids);
        return "download";
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
