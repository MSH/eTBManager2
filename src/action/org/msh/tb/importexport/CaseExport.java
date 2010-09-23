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

	/**
	 * Group of information to be exported
	 * @author Ricardo Memoria
	 *
	 */
	public enum ExportContent {
		CASEDATA,
		REGIMENS,
		EXAMS,
		MEDEXAM;
		
		public String getKey() {
			return getClass().getSimpleName().concat("." + name());
		}
	}
	
	/**
	 * Selected group of information to be exported
	 */
	private ExportContent exportContent = ExportContent.CASEDATA;

	
	/**
	 * List of cases exported
	 */
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


	public ExportContent getExportContent() {
		return exportContent;
	}


	public void setExportContent(ExportContent exportContent) {
		this.exportContent = exportContent;
	}

	public ExportContent[] getContents() {
		return ExportContent.values();
	}


	@Override
	protected String getHQLJoin() {
		return super.getHQLJoin().concat(" join fetch c.notificationUnit nu " +
				"join fetch nu.adminUnit " +
				"join fetch c.pulmonaryType");
	}
}
