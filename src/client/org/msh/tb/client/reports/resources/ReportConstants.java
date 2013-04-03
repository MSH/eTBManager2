package org.msh.tb.client.reports.resources;

import com.google.gwt.i18n.client.Constants;

public interface ReportConstants extends Constants {

	@Key("manag.reportgen.totalcases")
	String numberOfCases();
	
	@Key("manag.reportgen.totalcasesby")
	String numberOfCasesBy();
	
	@Key("manag.reportgen.novars")
	String noVariableDefined();
	
	@Key("form.filters")
	String filters();
	
	@Key("cases.details.noresultfound")
	String noResultFound();
	
	@Key("manag.reportgen")
	String title();
	
	@Key("global.colvariable")
	String columns();
	
	@Key("global.rowvariable")
	String rows();
	
	@Key("manag.reportgen.addfilters")
	String addFilters();
	
	@Key("form.generate")
	String generate();
}
