package org.msh.tb.reports2.variables;


import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

public class AgeRangeVariable extends VariableImpl {

	private AgeRangeHome ageRangeHome;
	
	public AgeRangeVariable() {
		super("ageRange", "AgeRange", "tbcase.age");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object values) {
		if (values == null)
			return null;

		// get the age
		int age = (Integer)values;

		// find the range
		AgeRange ageRange = getAgeRangeHome().findRange(age);
		
		if (ageRange == null)
			return null;

		return ageRange.getId();
	}
	

	/**
	 * Return the class that handles are ranges
	 * @return
	 */
	protected AgeRangeHome getAgeRangeHome() {
		if (ageRangeHome == null)
			ageRangeHome = (AgeRangeHome)Component.getInstance("ageRangeHome", true);
		return ageRangeHome;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);

		for (AgeRange ageRange: getAgeRangeHome().getItems()) {
			// found the range ?
			if (ageRange.getId().equals(key))
				return ageRange.toString();
		}

		// if range is not found, return undefined 
		return super.getDisplayText(null);
	}

	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions()
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		AgeRangeHome home = getAgeRangeHome();
		
		List<FilterOption> lst = new ArrayList<FilterOption>();
		for (AgeRange ar: home.getItems()) {
			lst.add(new FilterOption(ar.getId(), ar.toString()));
		}
		
		return lst;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues(Object val1, Object val2) {
		AgeRange range1 = getAgeRangeHome().findRangeById((Integer)val1);
		AgeRange range2 = getAgeRangeHome().findRangeById((Integer)val2);
		
		if ((range1 != null) && (range2 != null))
			return ((Integer)range1.getIniAge()).compareTo(range1.getIniAge());
		
		return super.compareValues(val1, val2);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper,
			Object value) {
		if ((value == null) || (KEY_NULL.equals(value))) {
			def.addRestriction("tbcase.age is null");
			return;
		}

		// search for age range
		AgeRangeHome home = getAgeRangeHome();
		AgeRange range = home.findRangeById((Integer)value);
		if (range == null)
			throw new IllegalArgumentException("Age range not found: " + value);

		// apply restriction
		def.addRestriction("tbcase.age between :age1 and :age2");
		def.addParameter("age1", range.getIniAge());
		def.addParameter("age2", range.getEndAge());
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#filterValueFromString(java.lang.String)
	 */
	@Override
	public Object filterValueFromString(String value) {
		if ((KEY_NULL.equals(value)) || (value == null))
			return null;

		return Integer.parseInt(value);
	}

}
