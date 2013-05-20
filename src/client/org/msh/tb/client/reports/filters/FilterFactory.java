package org.msh.tb.client.reports.filters;

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
	
	private static final FilterFactory instance = new FilterFactory();
	private Map<String, FilterConstructor> filterConstructors = new HashMap<String, FilterConstructor>();
	private FilterConstructor defaultFilterConstructor;

	public FilterFactory() {
		super();
		defaultFilterConstructor = this;
		registerFilter(FILTER_PERIOD, this);
		registerFilter(FILTER_REMOTE_OPTS, this);
	}

	/**
	 * Create an instance of the filter by its type
	 * @param type is the string representation of the filter
	 * @return instance of the {@link FilterWidget} interface
	 */
	public static FilterWidget createFilter(String type) {
		return instance.internalCreateFilter(type);
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
	 * @param filterClass
	 */
	public void registerFilter(String type, FilterConstructor filterConstructor) {
		filterConstructors.put(type, filterConstructor);
	}
	
	/**
	 * Set the default filter type. The default filter type is the one created when
	 * no filter type name is specified
	 * @param filterClass class that implements the {@link FilterWidget} interface
	 */
	public void setDefaultFilter(FilterConstructor filterConstructor) {
		defaultFilterConstructor = filterConstructor;
	}
	
	
	/**
	 * Private method that implements the static method to create a 
	 * new filter widget
	 * @param type
	 * @return
	 */
	private FilterWidget internalCreateFilter(String type) {
		FilterConstructor constructor;
		if (type == null) {
			constructor = defaultFilterConstructor;
		}
		else {
			constructor = filterConstructors.get(type);
		}
		if (constructor == null)
			throwFilterNotFound(type);

		return constructor.create(type);
	}


	/**
	 * Standard method to throw an error when filter type is not found
	 * @param filter
	 */
	private void throwFilterNotFound(String filter) {
		throw new RuntimeException("FilterWidget not found " + filter);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.client.reports.filters.FilterConstructor#create(java.lang.String)
	 */
	@Override
	public FilterWidget create(String ftype) {
		if ((ftype == null) || ("options".equals(ftype)))
			return new OptionsFilter();
		
		if (FILTER_PERIOD.equals(ftype))
			return new PeriodFilter();
		
		if (FILTER_REMOTE_OPTS.equals(ftype))
			return new RemoteOptionsFilter();

		return null;
	}
	
	

}
