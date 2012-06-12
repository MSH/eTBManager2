package org.msh.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.Query;

/**
 * Extend SEAM EntityQuery class including more functionalities like paging, restrictions by string, etc
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class EntityQuery<E> extends org.jboss.seam.framework.EntityQuery<E> {
	private static final long serialVersionUID = -8321685531579895731L;

	private static final Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);

	private List<SelectItem> pageOptions;
	
	public EntityQuery() {
		super();
		List<String> restrictions = getStringRestrictions();
		if (restrictions != null)
			setRestrictionExpressionStrings(restrictions);
	}

	protected List<String> getStringRestrictions() {
		return null;
	}
	
	@Override
	protected Query createCountQuery() {
      parseEjbql();

      evaluateAllParameters();

      joinTransaction();
	  
      String hql = getCountEjbqlWithRestrictions();
      javax.persistence.Query query = getEntityManager().createQuery( hql );
      return query;
	}

	protected String getCountEjbqlWithRestrictions() {
		String hql = getCountEjbql();

		StringBuilder builder = new StringBuilder().append(hql);

		boolean bWhere = WHERE_PATTERN.matcher(builder).find();
		
		for (int i = 0; i < getRestrictions().size(); i++) {
			Object parameterValue = getRestrictionParameters().get(i)
					.getValue();
			if (isRestrictionParameterSet(parameterValue)) {
				if (bWhere) {
					builder.append(" and ");
				} else {
					builder.append(" where ");
					bWhere = true;
				}
				builder.append(getRestrictions().get(i).getExpressionString());
			}
		}
		return builder.toString();
	}
	
	public int getFirstItemPage() {
		Integer num = getFirstResult();
		if (num == null)
			 return 1;
		else return getFirstResult() + 1;
	}
	
	public int getLastItemPage() {
		Integer firstRes = getFirstResult();
		Integer maxRes = getMaxResults();
		if (firstRes == null)
			firstRes = 0;
		if (maxRes == null)
			return 0;
		int num = firstRes + maxRes; 
		return (num > getResultCount()? getResultCount().intValue(): num);
	}


	/**
	 * Return the current page of the result set
	 * @return
	 */
	public Integer getCurrentPage() {
		Integer maxresults = getMaxResults();
		if (maxresults == null)
			return null;
		Integer firstResult = getFirstResult();
		return (firstResult != null? Math.round((float)firstResult / (float)maxresults) + 1: 1);
	}


	/**
	 * Changes the current page of the result set
	 * @param page
	 */
	public void setCurrentPage(Integer page) {
		Integer maxresults = getMaxResults();
		if (maxresults != null)
			setFirstResult(maxresults * (page - 1));
	}


	/**
	 * Return the max page allowed
	 * @return
	 */
	public Integer getMaxPage() {
		Integer maxresults = getMaxResults();
		if (maxresults == null)
			return null;
		
		Long recordcount = getResultCount();
		if (recordcount < maxresults)
			 return 1;
		else {
			return ((int)Math.floor((float)recordcount / (float)maxresults)) + 1;
		}
	}


	/**
	 * Check if page is the last one
	 * @return
	 */
	public boolean isLastPage() {
		Integer curPage = getCurrentPage();
		if (curPage == null)
			return true;
		
		return (curPage.equals( getMaxPage()) );
	}

	/**
	 * Return the result list just in the render phase
	 * @return
	 */
	public List<E> getResultListRender() {
		if (!FacesContext.getCurrentInstance().getRenderResponse())
			 return null;
		else return getResultList();
	}

	
	public List<SelectItem> getPageOptions() {
		if (pageOptions != null)
			return pageOptions;
		
		Integer currentPage = getCurrentPage();
		if (currentPage == null)
			return null;

		Integer maxpage = getMaxPage();
		if (maxpage == null)
			return null;
		
		int min = currentPage - 5;
		int max = currentPage + 5;
		
		if (min < 1)
			min = 1;

		if (max - min < 10)
			max = min + 10;
		
		if (min == 1)
			max = 10;

		if (max > maxpage)
			max = maxpage;
		
		pageOptions = new ArrayList<SelectItem>();
		for (int i = min; i <= max; i++) {
			pageOptions.add(new SelectItem(i, Integer.toString(i)));
		}
		
		return pageOptions;
	}
}
