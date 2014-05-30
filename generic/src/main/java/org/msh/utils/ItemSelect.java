/*
 * ItemSelect.java
 *
 * Created on 31 de Janeiro de 2007, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.msh.utils;

import java.io.Serializable;

/**
 *
 * @author Ricardo
 */
public class ItemSelect<E> implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 8988824156454330368L;
	private E item;
	private boolean selected;

	public ItemSelect() {
		super();
	}
	
    public ItemSelect(E item) {
		super();
		this.item = item;
	}

	public ItemSelect(E item, boolean selected) {
		super();
		this.item = item;
		this.selected = selected;
	}

    
    public E getItem() {
        return item;
    }

    public void setItem(E item) {
        this.item = item;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof ItemSelect))
			return false;
		
		return ((ItemSelect)obj).getItem().equals(getItem());
	}
    
}
