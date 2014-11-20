package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.List;


public class StockPosReportItem {

	private Object item;
	private StockPosReportItem parent;
	private int[] quantities;
	private List<StockPosReportItem> children = new ArrayList<StockPosReportItem>();

	/**
	 * Redimensiona o array de quantidade
	 * @param size
	 */
	public void redimArray(int size) {
		quantities = new int[size];
		for (StockPosReportItem item: children) {
			item.redimArray(size);
		}
	}
	
	
	/**
	 * Add an item as a child
	 * @param item
	 * @return
	 */
	public StockPosReportItem addChild(Object item) {
		StockPosReportItem it = new StockPosReportItem();
		it.setItem(item);
		it.redimArray(quantities.length);
		children.add(it);
		it.parent = this;
		
		return it;
	}
	
	/**
	 * Search for a children by its item object
	 * @param item
	 * @return
	 */
	public StockPosReportItem findChild(Object item) {
		for (StockPosReportItem it: children) {
			if (it.getItem() == item)
				return it;
		}
		
		return null;
	}
	
	/**
	 * Adiciona a quantidade no objeto e em todos os pais
	 * @param index
	 * @param quantity
	 */
	public void setQuantity(int index, int quantity) {
		StockPosReportItem it = this;
		while (it != null) {
			it.quantities[index] += quantity;
			it = it.parent;
		}
	}


	public StockPosReportItem getParent() {
		return parent;
	}

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public int[] getQuantities() {
		return quantities;
	}

	public List<StockPosReportItem> getChildren() {
		return children;
	}
	

}
