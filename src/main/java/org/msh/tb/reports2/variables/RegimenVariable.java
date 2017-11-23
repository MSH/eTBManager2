package org.msh.tb.reports2.variables;

import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOption;
import org.msh.tb.entities.Regimen;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class RegimenVariable extends VariableImpl {

	private List<Regimen> regimens;
	
	public RegimenVariable() {
		super("regimen", "Regimen", "tbcase.regimen_id");
	}

	/**
	 * Return the list of regimens available
	 * @return
	 */
	public List<Regimen> getRegimens() {
		if (regimens == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			regimens = em.createQuery("from Regimen where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return regimens;
	}

	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if ((key == null) || (KEY_NULL.equals(key)))
			return Messages.instance().get("regimens.individualized");

		Integer id = (Integer)key;
		
		for (Regimen reg: getRegimens())
			if (reg.getId().equals(id)) {
				return reg.getName();
			}
		
		// if id is not found, return undefined 
		return super.getDisplayText(key);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();
		lst.add(new FilterOption("null", Messages.instance().get("regimens.individualized")));

		for (Regimen reg: getRegimens()) {
			lst.add(new FilterOption(reg.getId(), reg.getName()));
		}
		
		return lst;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
/*	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		if ("null".equals(value))
			 def.addRestriction(getFieldName() + " is null");
		else super.prepareFilterQuery(def, oper, value);
	}
*/
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return convertIntFilter(value);
/*
		if (KEY_NULL.equals(value))
			return null;
		return Integer.parseInt(value);
*/
	}
	
}
