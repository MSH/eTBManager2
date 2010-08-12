package org.msh.utils;

import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.Query;

public class EntityQuery<E> extends org.jboss.seam.framework.EntityQuery<E> {
	private static final long serialVersionUID = -8321685531579895731L;

	private static final Pattern WHERE_PATTERN = Pattern.compile("\\s(where)\\s", Pattern.CASE_INSENSITIVE);

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
}
