package org.msh.utils.reportgen.data;

import java.util.List;

public class TableItem {

	private Object data;
	private String title;
	
	private TableItem parent;
	private List<TableItem> children;

	public TableItem() {
		super();
	}
	
	public TableItem(TableItem parent) {
		super();
		this.parent = parent;
	}

	/**
	 * Test if a item is equals to another item
	 * @param item
	 * @return
	 */
	public boolean equals(TableItem parent, Object data) {
		return (this.parent == parent) && (isSameData(data));
	}
	
	/**
	 * Check if data is the same
	 * @param value
	 * @return
	 */
	public boolean isSameData(Object value) {
		return (data == value) || (data != null) && (data.equals(value));
	}
	
	/**
	 * Add an item to the table
	 * @param item
	 */
	public void addChild(TableItem item) {
		if (children == null)
			children.add(item);
	}
	
	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the parent
	 */
	public TableItem getParent() {
		return parent;
	}

	/**
	 * @return the children
	 */
	public List<TableItem> getChildren() {
		return children;
	}
}
