package org.msh.utils;

import java.util.ArrayList;
import java.util.List;

public class RowGrouping {

	static public List<RowGroupingItem> createRows(List items, RowGroupingComparator comparator) {
		List<RowGroupingItem> rows = new ArrayList<RowGroupingItem>();
		
		if (items == null)
			return null;

		RowGroupingItem ant = null;
		for (Object item: items) {
			RowGroupingItem it = new RowGroupingItem();
			it.setItem(item);
			it.setRowSpan(1);
			rows.add(it);
			
			if ((ant == null) || (!comparator.compare(ant.getItem(), item)))
				 ant = it;
			else { 
				ant.setRowSpan(ant.getRowSpan() + 1);
				it.setRowSpan(0);
			}
		}
		
		return rows;
	}
}
