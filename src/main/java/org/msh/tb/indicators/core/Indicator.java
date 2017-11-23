package org.msh.tb.indicators.core;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.msh.tb.AgeRangeHome;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class supporting TB/MDR-TB indicator generation
 * <br/>
 * A base class must be created and override the abstract method {@link #createIndicators()}
 * @author Ricardo Memoria
 *
 */
public abstract class Indicator extends CaseHQLBase {
	private static final long serialVersionUID = -7709791400340789164L;

	private boolean executing;
	private int indexPos;
	private boolean sortRows = true;
	private IndicatorTable table;
	private boolean showPerc;
	
	
	private IndicatorSeries series;
	@In(create=true) EntityManager entityManager;
	/**
	 * Abstract method called to generate the indicators
	 * To include values in the indicator list use {@link #addValue(String, int)}
	 */
	protected abstract void createIndicators();

	
	/**
	 * Generate indicator by the property {@link #getOutputSelections()}
	 * @param condition HQL condition to be included in the HQL instruction
	 */
	protected void generateIndicatorByOutputSelection(String condition) {
		createItems(generateValuesByOutputSelection(condition));
	}


	
	/**
	 * Query the database to return indicators based on the filters and grouped by the outputSelection property 
	 * @param condition HQL condition to be included in the query
	 * @return List of object values
	 */
	protected List<Object[]> generateValuesByOutputSelection(String condition) {
		setOutputSelected(true);
		setCondition(condition);
		String hql = createHQLByOutputSelection();
		return getQueryResult(hql);
	}

	
	/**
	 * Query the database to return the number of cases according to the condition
	 * @param condition - Condition to be applied in the system
	 * @return number of cases
	 */
	protected int calcNumberOfCases(String condition) {
		setOutputSelected(false);
		setGroupFields(null);
		setCondition(condition);
		return ((Long)createQuery().getSingleResult()).intValue();
	}


	/**
	 * Query the database based to return an indicator based on the filters and grouped by a field (or fields).
	 * Filters are set in <i>indicatorFilters.region</i>
	 * Also consider properties <i>newCasesOnly</i> and <i>classification</i>
	 * @param fields HQL fields to group result (comma separated)
	 * @param condition HQL condition to be included in the query
	 * @return List of object values
	 */
	protected List<Object[]> generateValuesByField(String fields, String condition) {
		setGroupFields(fields);
		setCondition(condition);
		String hql = createHQL();
		return getQueryResult(hql);
	}

	/**
	 * Return the query result based on the HQL instruction.
	 * The HQL must have the parameters <i>classification</i> and <i>casestatus</i> (CaseState.ONTREATMENT) 
	 * @param hql
	 * @return
	 */
	protected List<Object[]> getQueryResult(String hql) {
		return createQuery().getResultList();
	}


	/**
	 * Return the display text associated with the enumeration. Considering that the enum implements the String getKey() method 
	 * @param value Enumeration object
	 * @return display text
	 */
	protected String getEnumDisplayText(Enum value) {
		try {
			Class params[] = null;
			Method met = value.getClass().getMethod("getKey", params);
			Object args[] = null;
			String res = (String)met.invoke(value, args);
			return getMessage(res);
		} catch (Exception e) {
			return value.toString();
		}
	}

	
	/**
	 * Return a specific message by its key according to the selected language
	 * @param key the message key
	 * @return message according to the given key
	 */
	protected String getMessage(String key) {
		return getMessages().get(key);
	}


	
	/**
	 * Include a new value in the indicator list of values
	 * @param label the label of the value to be displayed
	 * @param value indicator value
	 * @return instance of the {@link IndicatorItem} class representing the value
	 */
	protected IndicatorItem addValue(String label, int value) {
		return series.addValue(label, value);
	}



	/**
	 * Automatically generate the list of indicators based on a list of object values.
	 * The first item of the object values must be the label and the second item is the value
	 * @param lst list of indicator values
	 */
	protected void createItems(List<Object[]> lst) {
		if ((isOutputSelected()) && (getIndicatorFilters().getOutputSelection() == OutputSelection.AGERANGE)) {
			lst = groupValuesByAreRange(lst,0, 1);
		}

		for (Object[] vals: lst) {
			int val = ((Long)vals[1]).intValue();
			if (val > 0)
				addValue(translateKey(vals[0]), val);
		}
	}

	
	/**
	 * Return the age range home component with the list of age ranges in use by the system
	 * @return
	 */
	protected AgeRangeHome getAgeRangeHome() {
		return (AgeRangeHome)Component.getInstance("ageRangeHome", true);
	}

	
	/**
	 * Group result values by age range. The result is another list with the same structure but
	 * the position of the age (ageIndex) replaced and grouped by an {@link AgeRange} object 
	 * @param lst - List of objects with the ageIndex position of the array grouped by age range
	 * @param ageIndex - Position of the array where age information is stored
	 * @return List of Object[] with the ageIndex position grouped by age range
	 */
	protected List<Object[]> groupValuesByAreRange(List<Object[]> lst, int ageIndex, int keyCount) {
		AgeRangeHome ageRangeHome = getAgeRangeHome();
		
		List<Object[]> retlst = new ArrayList<Object[]>();
		
		for (Object[] vals: lst) {
			long value = (Long)vals[vals.length - 1];
			int age = (Integer)vals[ageIndex];
			
			AgeRange range = ageRangeHome.findRange(age);
			Object key;
			if (range != null)
				 key = range;
			else key = getMessage("global.notdef");
			
			// search for record
			Object[] keys = new Object[keyCount];
			for (int i = 0; i < keyCount; i++) {
				if (i == ageIndex)
					 keys[i] = key;
				else keys[i] = vals[i];
			}
			Object[] newvalues = findRecord(retlst, keys);

			// age range was found ?
			if (newvalues == null) {
				// rebuild new values
				newvalues = new Object[vals.length];
				int index = 0;
				while (index < vals.length - 1) {
					newvalues[index] = vals[index];
					index++;
				}
				
				newvalues[ageIndex] = key;
				newvalues[vals.length - 1] = 0L;
				retlst.add(newvalues);
			}
			
			// increment age counter
			newvalues[vals.length - 1] = ((Long)newvalues[vals.length - 1]) + value;
		}

		indexPos = ageIndex;
		sortRows = false;
		
		// sort new list by age range
		Collections.sort(retlst, new Comparator<Object[]>() {
			public int compare(Object[] vals1, Object[] vals2) {
				Object o1 = vals1[indexPos];
				Object o2 = vals2[indexPos];
				
				if (o1 == o2)
					return 0;
				if (o1 instanceof String)
					return -1;
				if (o2 instanceof String)
					return 1;

				return ((AgeRange)o1).getIniAge() < ((AgeRange)o2).getIniAge()? -1: 1;
			}
		});
		
		return retlst;
	}


	/**
	 * Search for a specific record (array of objects) in a list, search for the first n elements of the array 
	 * indicated by the keyCount parameter
	 * @param lst - list of records (array of objects)
	 * @param keyCount - the number of keys in the array, starting from the 0-index item
	 * @return item in the list that matches the search
	 */
	protected Object[] findRecord(List<Object[]> lst, Object[] keys) {
		for (Object[] vals: lst) {
			boolean match = true;
			for (int i = 0; i < keys.length; i++) {
				if (keys[i] != vals[i]) {
					match = false;
					break;
				}
			}
			
			if (match)
				return vals;
		}
		return null;
	}

	/**
	 * Translate the key to a string 
	 * @param key Object to be translated
	 * @return String representation of the key
	 */
	protected String translateKey(Object key) {
		if (key == null) {
			if ((isOutputSelected()) && (getIndicatorFilters().getOutputSelection() == OutputSelection.REGIMENS))
				 return getMessage("regimens.individualized");
			else return getMessage("global.notdef");
		}
		else {
			if (key instanceof Enum)
				 return getEnumDisplayText((Enum)key);
			else
			if ((isOutputSelected()) && (getIndicatorFilters().getOutputSelection() == OutputSelection.ADMINUNIT))
				return getAdminUnitDisplayText((String)key);
			else return key.toString();
		}
	}


	/**
	 * Return the display text of the administrative unit according to its code
	 * @param code of the administrative unit
	 * @return name of the administrative unit
	 */
	protected String getAdminUnitDisplayText(String code) {
		if (code == null)
			return "-";
		List<AdministrativeUnit> lst = null;
		AdminUnitSelection sel = getIndicatorFilters().getTbunitselection().getAuselection();
		AdministrativeUnit adm = sel.getSelectedUnit();
		if (adm == null)
			lst = sel.getOptionsLevel1();
		else {
			switch (adm.getLevel()) {
			case 1:
				lst = sel.getOptionsLevel2();
				break;
			case 2:
				lst = sel.getOptionsLevel3();
				break;
			case 3:
				lst = sel.getOptionsLevel4();
				break;
			case 4:
				lst = sel.getOptionsLevel5();
			}
		}
		
		if (lst == null)
			return "-";
		for (AdministrativeUnit aux: lst) {
			if (aux.isSameOrChildCode(code)) {
				return aux.getName().toString();
			}
		}
		return "-";
	}


	/**
	 * Notify the indicator is being generated
	 */
	public void execute() {
		executing = true;
	}


	/**
	 * Checks if the indicator is being generated
	 * @return <b>true</b> if the indicator is being created, otherwise returns <b>false</b>
	 */
	public boolean isExecuting() {
		return executing;
	}


	/**
	 * Create the indicator series of values
	 */
	private void createSeries() {
		setConsolidated(true);
		series = new IndicatorSeries();
		createIndicators();

		if (sortRows)
			series.sort();
	}


	/**
	 * Returns the series list containing the indicator
	 * @return {@link IndicatorSeries} instance
	 */
	public IndicatorSeries getSeries() {
		if (series==null)
			createSeries();
		return series;
	}


	/**
	 * @param sortRows the sortRows to set
	 */
	public void setSortRows(boolean sortRows) {
		this.sortRows = sortRows;
	}


	/**
	 * @return the sortRows
	 */
	public boolean isSortRows() {
		return sortRows;
	}

	
	/**
	 * Check if the indicator must display a total row of the table
	 * @return true if it has a total row
	 */
	public boolean isHasTotal() {
		return true;
	}
	
	/**
	 * Check if the indicator must display the values in percent
	 * @return true if the value has to be shown in percent
	 */
	public boolean isShowPerc() {
		return showPerc;
	}

	/**
	 * @param showPerc the sortRows to set
	 */
	public void setShowPerc(boolean showPerc) {
		this.showPerc = showPerc;
	}
	
	
	/**
	 * Open the indicator, if not done yet
	 */
	public void open() {
		if (series != null)
			return;
		createSeries();
	}
	
	
	/**
	 * Return the table containing the data
	 * @return {@link IndicatorTable} instance
	 */
	public IndicatorTable getTable() {
		if (table == null)
			createTable();
		return table;
	}
	
	/**
	 * Create the table and fill it with the indicator values
	 */
	protected void createTable() {
		setConsolidated(true);
		table = new IndicatorTable();
		createIndicators();
	}
	
	/**
	 * Add a value to the table
	 * @param columnTitle title of the column
	 * @param rowTitle title of the row
	 * @param value value to be set
	 * @return {@link TableCell} instance containing the cell data
	 */
	public TableCell addValue(String columnTitle, String rowTitle, Float value) {
		return table.addValue(columnTitle, rowTitle, value);
	}
}
