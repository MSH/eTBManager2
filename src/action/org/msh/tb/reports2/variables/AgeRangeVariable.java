package org.msh.tb.reports2.variables;


import org.jboss.seam.Component;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.entities.AgeRange;
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
}
