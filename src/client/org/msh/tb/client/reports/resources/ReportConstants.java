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
	
	@Key("global.total")
	String total();
	
	@Key("meds.movs.to")
	String until();
	
	@Key("cases.nonumber")
	String unnumbered();
	
	@Key("cases")
	String casesTitle();
	
	@Key("form.result")
	String resulting();
	
	@Key("global.wait")
	String waitMessage();
	
	@Key("form.of")
	String of();
	
	@Key("form.navnext")
	String navegNext();
	
	@Key("form.navprevious")
	String navegPrevious();
	
	@Key("FixedPeriod.LAST_3MONTHS")
	String fixedPeriodLAST_3MONTHS();

	@Key("FixedPeriod.LAST_6MONTHS")
	String fixedPeriodLAST_6MONTHS();

	@Key("FixedPeriod.LAST_12MONTHS")
	String fixedPeriodLAST_12MONTHS();

	@Key("FixedPeriod.PREVIOUS_QUARTER")
	String fixedPeriodPREVIOUS_QUARTER();

	@Key("FixedPeriod.PREVIOUS_YEAR")
	String fixedPeriodPREVIOUS_YEAR();
}
