package org.msh.tb.kh;


import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.msh.tb.cases.CasesQuery;

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
