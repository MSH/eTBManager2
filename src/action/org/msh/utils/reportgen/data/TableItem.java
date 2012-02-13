package org.msh.utils.reportgen.data;

import java.util.ArrayList;
import java.util.List;

import org.msh.utils.reportgen.Variable;

public class TableItem implements Comparable<TableItem> {

	private Object data;
	private Variable variable;
	private String title;
	
	private TableItem parent;
	private List<TableItem> children;

	public TableItem() {
		super();
	}
	
	public TableItem(TableItem parent) {
		super();
		this.parent = parent;
		if (parent != null)
			parent.addChild(this);
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
			children = new ArrayList<TableItem>();
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
		if (title != null)
			return title;
		return (variable == null? null: variable.getValueDisplayText(data));
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

	@Override
	public int compareTo(TableItem tbl) {
		if ((parent == null) && (tbl.parent != null))
			return -1;

		if ((parent != null) && (tbl.parent == null))
			return 1;

		int comp;
		if ((parent != null) && (tbl.parent != null))
			 comp = parent.compareTo(tbl.parent);
		else comp = 0;

		if (comp == 0)
			comp = variable.compareValues(data, tbl.getData());

		return comp;
	}

	/**
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * @param variable the variable to set
	 */
	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
