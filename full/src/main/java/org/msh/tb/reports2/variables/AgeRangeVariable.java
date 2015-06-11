package org.msh.tb.reports2.variables;


import org.jboss.seam.Component;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorInt;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import java.util.ArrayList;
import java.util.List;

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
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
		String sql = value.mapSqlOR(new ValueIteratorInt() {
            @Override
            public String iterateInt(Integer value, int index) {
                return hqlRestriction(value);
            }
        });

        def.addRestriction(sql);
    }


    /**
     * Create the restriction by age range
     * @param id
     * @return
     */
	private String hqlRestriction(Integer id) {
		// search for age range
		AgeRangeHome home = getAgeRangeHome();
		AgeRange range = home.findRangeById(id);
		if (range == null)
			throw new IllegalArgumentException("Age range not found: " + id);

		return "tbcase.age between " + range.getIniAge() + " and " + range.getEndAge();
	}

}
