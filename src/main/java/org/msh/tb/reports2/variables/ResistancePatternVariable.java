package org.msh.tb.reports2.variables;

import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle user defined resistance patterns in an indicator variable
 *  
 * @author Ricardo Memoria
 *
 */
public class ResistancePatternVariable extends VariableImpl {

	private List<ResistancePattern> patterns;
	
	/**
	 * If true, the variable will select the resistance patterns from diagnosis, 
	 * otherwise will select the resisance patterns of the whole treatment
	 */
	private boolean diagnosis;
	
	public ResistancePatternVariable(String id, boolean diagnosis) {
		super(id, "manag.ind.resist", "caseresistancepattern.resistpattern_id");
		this.diagnosis = diagnosis;
	}

	
	/**
	 * Return the available list of resistance patterns
	 * @return a list of {@link ResistancePattern} instances
	 */
	public List<ResistancePattern> getPatterns() {
		if (patterns == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			patterns = em.createQuery("from ResistancePattern where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		
		return patterns;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs, int)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		// condition for the side effect join
		def.table("tbcase").join("id", "caseresistancepattern.case_id");
		def.addRestriction("caseresistancepattern.diagnosis = :" + getId());
		def.addParameter(getId(), diagnosis);
		super.prepareVariableQuery(def, iteration);
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if ((key == null) || (KEY_NULL.equals(key)))
			return super.getDisplayText(key);

		Integer id = (Integer)key;
		
		for (ResistancePattern reg: getPatterns())
			if (reg.getId().equals(id)) {
				return reg.getName();
			}
		
		// if id is not found, return undefined 
		return super.getDisplayText(key);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();

		for (ResistancePattern item: getPatterns()) {
			lst.add(new FilterOption(item.getId(), item.getName()));
		}
		
		return lst;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.variables.FieldValueVariable#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		def.table("tbcase").join("id", "caseresistancepattern.case_id");
		def.addRestriction("caseresistancepattern.diagnosis = :" + getId());
		def.addParameter(getId(), diagnosis);
		super.prepareFilterQuery(def, oper, value);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		return convertIntFilter(value);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getLabel()
	 */
	@Override
	public String getLabel() {
		String s = super.getLabel();
		if (diagnosis)
			s += " (" + Messages.instance().get("global.atdiag") + ")";
		else s += " (" + Messages.instance().get("global.current") + ")";
		return s;
	}
	

}
