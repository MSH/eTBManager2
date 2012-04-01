package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.cases.CaseResultItem;
import org.msh.tb.cases.CasesQuery;
import org.msh.tb.cases.FilterHealthUnit;


/**
 * Query about cases. Use instead standard CasesQuery to fix error related
 * to search cases without TBUnit:
 * <ul>
 * <li>User have region access restriction
 * <li>User try simple search by name
 * <li>System will not return cases with cure not assignment
 * </ul>
 * @author alexey
 */
@Name("casesUA")
@BypassInterceptors
public class CasesQueryUA {
	/**
	 * Comparator for treeset result item
	 * @author alexey
	 *
	 */
	public class CaseResultItemComparator implements Comparator<CaseResultItem>{

		@Override
		public int compare(CaseResultItem arg0, CaseResultItem arg1) {
			if (arg0.getCaseId() == arg1.getCaseId()) return 0;
			int res = arg0.getCaseState().compareTo(arg1.getCaseState());
			if (res==0){
				int res1 = arg0.getPatientName().compareTo(arg1.getPatientName());
				if (res1 == 0){
					int res2 = arg0.getNotificationDate().compareTo(arg1.getNotificationDate());
					if (res2 == 0) return -1;
					else return res2;
				}else return res1;
			}else return res;
		}
	}
	/**
	 * old style query
	 */
	private CasesQuery cases=null;
	/**
	 * valid result list
	 */
	private List<CaseResultItem> results= new ArrayList<CaseResultItem>();
	

	/**
	 * Get search result depends of HealthUnit filters - as simple or as union of two queries
	 */
	public List<CaseResultItem> getResultList() {
		if (getResults().size() == 0){
			CasesQuery oldQuery = getCasesQuery();
			CaseFilters filters = oldQuery.getCaseFilters();
			FilterHealthUnit filterUnit = filters.getFilterHealthUnit();
			if (filterUnit != null){
				if (filters.getTbunitselection().getAdminUnit() != null){
					if (filterUnit == FilterHealthUnit.BOTH){
						setResults(unionResult(filters));
					}else setResults(oldQuery.getResultList());
				}else setResults(oldQuery.getResultList());
			}else setResults(oldQuery.getResultList());
		}
		return getResults();
	}
	/**
	 * need two queries, then union results
	 * @param filters current users filters
	 * @return result of query
	 */
	private List<CaseResultItem> unionResult(CaseFilters filters) {
		// place for result - must be unique
		Set<CaseResultItem> resultSet = new TreeSet<CaseResultItem>(new CaseResultItemComparator());
		//change filterUnit to NOTIFICATION_UNIT
		filters.setFilterHealthUnit(FilterHealthUnit.NOTIFICATION_UNIT);
		//perform first query
		List<CaseResultItem> res = getCasesQuery().getResultList();
		addToresultSet(resultSet, res);
		// TODO change filterUnit to TREATMENT_UNIT
		filters.setFilterHealthUnit(FilterHealthUnit.TREATMENT_UNIT);
		getCasesQuery().refresh();
		res = getCasesQuery().getResultList();
		addToresultSet(resultSet, res);
		// return right filter
		filters.setFilterHealthUnit(FilterHealthUnit.BOTH); //return filters
		// TODO convert resultset to resultlist
		addToResultList(resultSet);
		return getResults();
	}
	/**
	 * Get result from temporary set to resultList
	 * @param resultSet
	 */
	private void addToResultList(Set<CaseResultItem> resultSet) {
		Iterator<CaseResultItem> it = resultSet.iterator();
		getResults().clear();
		while (it.hasNext()){
			getResults().add(it.next());
		}
		
	}
	/**
	 * Add all result items from the result list to the result set
	 * @param resultSet 
	 * @param res
	 */
	private void addToresultSet(Set<CaseResultItem> resultSet,
			List<CaseResultItem> res) {
		if (res != null){
			Iterator<CaseResultItem> it = res.iterator();
			while(it.hasNext()){
				resultSet.add(it.next());
			}
		}

	}
	/**
	 * implement refresh for old caseQuery, called from SEAM
	 */
	public void refresh(){
		getResults().clear();
		getCasesQuery().refresh();
	}
	/**
	 * get or inst old CasesQuery
	 * @return
	 */
	public CasesQuery getCasesQuery(){
		if (cases == null){
			cases = (CasesQuery) Component.getInstance(CasesQuery.class);
		}
		return cases;
	}
	public List<CaseResultItem> getResults() {
		return results;
	}
	public void setResults(List<CaseResultItem> results) {
		this.results = results;
	}
	/**
	 * Provide size of result, similar to JBOSS SEAM EntityQuery
	 * @return
	 */
	public Long getResultCount(){
		getResultList();
		return new Long(getResults().size());
	}
	/**
	 * Provide pages count, similar to old func
	 * @return
	 */
	public Long getPageCount(){
		//TODO calc real
		return new Long(1);
	}
}
