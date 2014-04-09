package org.msh.tb.kh;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.msh.tb.ETB;
import org.msh.tb.application.App;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.Period;

// 02.04.2012 Change some from public to to protected Alexey Kurasov
/**
 * Return the result search of cases based on the filters defined in {@link CaseFilters} 
 * @author Ricardo Memoria
 *
 */
@Name("caseskh")
@BypassInterceptors
public class CasesQueryKH extends CasesQuery {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1112816180483259390L;
	
	@Override
	public String getOrder() {
		String s = super.getInverseordervalues()[9];
		s = Expressions.instance().createValueExpression(s).getValue().toString();
		return s;
	}
			
}
