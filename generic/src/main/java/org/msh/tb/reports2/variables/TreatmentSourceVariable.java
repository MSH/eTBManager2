/**
 * 
 */
package org.msh.tb.reports2.variables;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.Source;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

/**
 * Variable to handle the medicine source used in prescribed medicines
 * 
 * @author Ricardo Memoria
 *
 */
public class TreatmentSourceVariable extends VariableImpl {

	private List<Source> sources;
	
	/**
	 * Default constructor
	 */
	public TreatmentSourceVariable() {
		super("source", "Source", "prescribedmedicine.source_id");
	}
	
	/**
	 * Return the list of sources
	 * @return list of {@link Source} objects
	 */
	public List<Source> getSources() {
		if (sources == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			sources = em.createQuery("from Source where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return sources;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		// add the join with prescribed medicine
		def.table("tbcase").join("id", "prescribedmedicine.case_id");
		// restrict the number of prescribed medicine records with a unique source
		def.addRestriction("prescribedmedicine.id = (select min(pm1.id) from prescribedmedicine pm1 where pm1.source_id = prescribedmedicine.source_id " +
				"and pm1.case_id = " + def.getMasterTable().getAlias() + ".id )");
		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		def.addRestriction("exists(select * from prescribedmedicine pm1 where pm1.source_id = :sourceid " +
				"and pm1.case_id =" + def.getMasterTable().getAlias() + ".id)");
		Integer id = Integer.parseInt(value.toString());
		def.addParameter("sourceid", id);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);

		for (Source source: getSources()) {
			if (source.getId().equals(key))
				return source.getName().toString();
		}
		
		return super.getDisplayText(key);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		return value;
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
		for (Source source: getSources()) {
			lst.add(new FilterOption(source.getId(), source.toString()));
		}
		return lst;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}
}
