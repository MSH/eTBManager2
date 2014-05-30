package org.msh.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintain a list of {@link ItemSelect} objects used in UI for user selection 
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class ItemSelectList<E> extends ArrayList<ItemSelect<E>>{
	private static final long serialVersionUID = 1320099756559908003L;

	public ItemSelectList(List<E> items) {
		super();
		setItems(items);
	}
	
	public ItemSelectList(E[] items) {
		super();
		setItems(items);
	}

	public void setItems(List<E> items) {
		for (E item: items)
			add(new ItemSelect<E>(item));
	}
	
	public void setItems(E[] items) {
		for (E item: items)
			add(new ItemSelect<E>(item));
	}
	
	/**
	 * Create a list with all selected items
	 * @return
	 */
	public List<E> getSelectedItems() {
		return getItemsByState(true);
	}
	
	/**
	 * Create a list with all unselected items
	 * @return
	 */
	public List<E> getUnselectedItems() {
		return getItemsByState(false);
	}

	public List<E> getItemsByState(boolean selected) {
		List<E> lst = new ArrayList<E>();
		for (ItemSelect<E> itemSel: this) {
			if (selected == itemSel.isSelected())
			{
				lst.add(itemSel.getItem());
			}
		}
		return lst;
	}
	
	/**
	 * Select all items of the list.<br/>
	 * The property selected of all items is set to true
	 */
	public void selectAll() {
		for (ItemSelect<E> itemSel: this) {
			itemSel.setSelected(true);
		}
	}
	
	/**
	 * Unselect all items of the list.<br/>
	 * The property selected of all items is set to false
	 */
	public void unselectAll() {
		for (ItemSelect<E> itemSel: this) {
			itemSel.setSelected(false);
		}
	}
}
