package org.msh.tb.client.reports.filters;

import org.msh.tb.client.shared.model.CFilter;

import java.util.HashMap;
import java.util.Map;


/**
 * Singleton filter factory. Create new instances of the filter by
 * its type. New filters can be registered specifying its class and
 * type name
 * 
 * @author Ricardo Memoria
 *
 */
public class FilterFactory implements FilterConstructor{

	public final static String FILTER_PERIOD = "period";
	public final static String FILTER_REMOTE_OPTS = "rem_opts";
	public final static String FILTER_TBUNIT = "tbunit";
    public final static String FILTER_ADMINUNIT = "adminunit";

	
	private static final FilterFactory instance = new FilterFactory();
	private Map<String, FilterConstructor> filterConstructors = new HashMap<String, FilterConstructor>();
	private FilterConstructor defaultFilterConstructor;

	public FilterFactory() {
		super();
		defaultFilterConstructor = this;
		registerFilter(FILTER_PERIOD, this);
		registerFilter(FILTER_REMOTE_OPTS, this);
		registerFilter(FILTER_TBUNIT, this);
        registerFilter(FILTER_ADMINUNIT, this);
	}

	/**
	 * Create an instance of the filter by its type
	 * @param filter is the instance of the {@link CFilter} class
	 * @return instance of the {@link FilterWidget} interface
	 */
	public static FilterWidget createFilter(CFilter filter) {
		return instance.internalCreateFilter(filter);
	}

	
	/**
	 * Return the singleton instance of the {@link FilterFactory} class
	 * @return instance of {@link FilterFactory} class
	 */
	public static FilterFactory instance() {
		return instance;
	}
	
	/**
	 * Register new filters by its name and class type
	 * @param type is the name 
	 * @param filterConstructor
	 */
	public void registerFilter(String type, FilterConstructor filterConstructor) {
		filterConstructors.put(type, filterConstructor);
	}
	
	/**
	 * Set the default filter type. The default filter type is the one created when
	 * no filter type name is specified
	 * @param filterConstructor class that implements the {@link FilterWidget} interface
	 */
	public void setDefaultFilter(FilterConstructor filterConstructor) {
		defaultFilterConstructor = filterConstructor;
	}
	
	
	/**
	 * Private method that implements the static method to create a 
	 * new filter widget
	 * @param filter
	 * @return
	 */
	private FilterWidget internalCreateFilter(CFilter filter) {
		FilterConstructor constructor;
		if (filter.getType() == null) {
			constructor = defaultFilterConstructor;
		}
		else {
			constructor = filterConstructors.get(filter.getType());
		}

		if (constructor == null)
			throwFilterNotFound(filter.getType());

		return constructor.create(filter);
	}


	/**
	 * Standard method to throw an error when filter type is not found
	 * @param filter
	 */
	private void throwFilterNotFound(String filter) {
		throw new RuntimeException("FilterWidget not found " + filter);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.org.msh.reports.filters.FilterConstructor#create(java.lang.String)
	 */
	@Override
	public FilterWidget create(CFilter filter) {
		String ftype = filter.getType();
		if ((ftype == null) || ("options".equals(ftype)))
			return new OptionsFilter(filter.isMultiSels());
		
		if (FILTER_PERIOD.equals(ftype))
			return new PeriodFilter();
		
		if (FILTER_REMOTE_OPTS.equals(ftype)) {
            return new RemoteOptionsFilter(filter.isMultiSels());
        }

		if (FILTER_TBUNIT.equals(ftype)) {
			return new TbunitFilter();
		}

        if (FILTER_ADMINUNIT.equals(ftype)) {
            return new AdminUnitFilter();
        }

		return null;
	}
}
