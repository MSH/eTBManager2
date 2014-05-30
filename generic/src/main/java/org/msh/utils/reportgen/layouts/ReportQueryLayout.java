package org.msh.utils.reportgen.layouts;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.msh.utils.reportgen.ReportData;
import org.msh.utils.reportgen.ReportQuery;
import org.msh.utils.reportgen.Variable;

/**
 * Single report query layout generator displaying all values in a table. Used for testing purposes 
 * @author Ricardo Memoria
 *
 */
public class ReportQueryLayout implements HTMLLayoutGenerator {

	private ReportQuery reportQuery; 
	private String styleClass;
	
	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.layouts.HTMLLayoutGenerator#addQuery(org.msh.utils.reportgen.ReportQuery)
	 */
	@Override
	public void addQuery(ReportQuery reportQuery) {
		if (this.reportQuery != null)
			throw new RuntimeException("Report query has already been set for this layout");
		this.reportQuery = reportQuery;
	}

	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.layouts.HTMLLayoutGenerator#generateHtml()
	 */
	@Override
	public String generateHtml() {
		if (reportQuery == null)
			throw new RuntimeException("ReportQuery not specified for layout generator");

		List<ReportData> values = reportQuery.getResultList();

		StringBuffer html = new StringBuffer(values.size() * 10);

		String s = styleClass != null? "class=" + styleClass: "";
		html.append("<table " + s + "><tr>");
		
		List<Variable> variables = reportQuery.getVariables();

		for (Variable var: reportQuery.getVariables()) { 
			html.append( "<th>" + var.getTitle() + "</th>" );
		}
		
		html.append( "<th>TOTAL</th></tr>" );
		
		DecimalFormat format = new DecimalFormat("#,###,##0");

		for (ReportData data: values) {
			html.append("<tr>");
			Object val[] = data.getValues();
			for (int i = 0; i < val.length; i++) {
				Variable var = variables.get(i);
				s = var.getValueDisplayText(val[i]);
				s = StringEscapeUtils.escapeHtml(s);
				html.append("<td>" + s + "</td>");
			}
			
			html.append("<td>" + format.format(data.getTotal()) + "</td></tr>");
		}
		
		html.append("</table>");
		
		return html.toString();
	}



	/* (non-Javadoc)
	 * @see org.msh.utils.reportgen.layouts.HTMLLayoutGenerator#clear()
	 */
	@Override
	public void clear() {
		reportQuery = null;
	}

	/**
	 * @return the styleClass
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * @param styleClass the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

}
