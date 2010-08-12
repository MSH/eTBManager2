package org.msh.tb.indicators.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ricardo Memória
 * Contains a series of a 
 */
public class IndicatorSeries implements Comparator<IndicatorItem>{

	private List<IndicatorItem> items = new ArrayList<IndicatorItem>();
	private Integer total;

	public Integer getTotal() {
		if (total == null)
			refreshTotal();
		
		return total;
	}


	
	/**
	 * Search for an item by its key
	 * @param key key of item to search
	 * @return {@link IndicatorItem} instance
	 */
	public IndicatorItem findItemByKey(String key) {
		if (items == null)
			return null;
		for (IndicatorItem item: items)
			if (item.getKey().equals(key))
				return item;
		return null;
	}


	/**
	 * Add a new value to the series or include an existing value, according to the key
	 * @param key display key value of the item
	 * @param value value of the item
	 * @return {@link IndicatorItem} instance
	 */
	public IndicatorItem addValue(String key, int value) {
		IndicatorItem item = findItemByKey(key);

		// add to total
		if (getTotal() != null)
			 total += value;
		else total = value;
		
		if (item == null) {
			item = new IndicatorItem(this);
			item.setKey(key);
			item.setValue(value);

			if (items == null)
				items = new ArrayList<IndicatorItem>();
			items.add(item);
		}
		else item.setValue(item.getValue() + value);
		
		return item;
	}


	protected void refreshTotal() {
		if (items == null)
			return;
		
		total = 0;
		for (IndicatorItem item: items) {
			total += total + item.getValue();
		}
	}
	
	public List<IndicatorItem> getItems() {
		return items;
	}
	
	public void sort() {
		Collections.sort(items, this);		
	}

	public int compare(IndicatorItem item1, IndicatorItem item2) {
		return item1.getKey().compareTo(item2.getKey());
	}
	
	public int getChartHeight() {
		return 80 + (getItems().size() * 16);
	}
}
