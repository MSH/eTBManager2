package org.msh.tb.client.commons;

import com.google.gwt.user.client.ui.Anchor;

/**
 * Extension of the {@link Anchor} widget that point to an object data
 * 
 * @author Ricardo Memoria
 *
 */
public class AnchorData extends Anchor{

	public AnchorData() {
		super();
		// TODO Auto-generated constructor stub
	}


	public AnchorData(String text, Object data) {
		super(text);
		this.data = data;
	}

	private Object data;

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
}
