package org.msh.utils;


public class RowGroupingItem {
	private Object item;
	private int rowSpan;

	public boolean isSpaned() {
		return rowSpan >= 1;
	}
	public Object getItem() {
		return item;
	}
	public void setItem(Object item) {
		this.item = item;
	}
	public int getRowSpan() {
		return rowSpan;
	}
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
}
