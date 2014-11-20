package org.msh.tb.na;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.CaseFilters;

@Name("caseFiltersNA")
@Scope(ScopeType.SESSION)
public class CaseFiltersNA {
	
	@In(create=true) CaseFilters caseFilters;

}
