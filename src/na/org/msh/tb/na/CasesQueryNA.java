package org.msh.tb.na;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CasesQuery;

@Name("casesQueryNA")
public class CasesQueryNA extends CasesQuery {
	private static final long serialVersionUID = -756905077428555077L;

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.CasesQuery#getJoinTablesHQL()
	 */
	@Override
	public String getFromHQL() {
		return "from CaseDataNA na join na.tbcase c";
	}
}
