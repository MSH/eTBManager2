package org.msh.tb.br;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CasesQuery;

@Name("casesQueryBR")
public class CasesQueryBR extends CasesQuery {
	private static final long serialVersionUID = -756905077428555077L;

	@Override
	public List<String> getStringRestrictions() {
		List<String> lst = super.getStringRestrictions();
		
		List<String> rest = new ArrayList<String>();
		for (String s: lst)
			rest.add(s);
		rest.add("br.resistanceType.value = #{caseFiltersBR.resistanceType}");
		rest.add("br.schemaChangeType.value = #{caseFiltersBR.schemaChangeType}");
		rest.add("br.outcomeRegimenChanged = #{caseFiltersBR.outcomeRegimenChanged}");
		rest.add("br.outcomeResistanceType = #{caseFiltersBR.outcomeResistanceType}");
		
		return rest;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.cases.CasesQuery#getJoinTablesHQL()
	 */
	@Override
	public String getFromHQL() {
		return "from CaseDataBR br join br.tbcase c";
	}
}
