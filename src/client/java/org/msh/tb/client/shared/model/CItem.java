package org.msh.tb.client.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Standard item storing value/text to be shared between server and client
 * 
 * @author Ricardo Memoria
 *
 */
public class CItem implements IsSerializable {

	private String value;
	private String label;

	public CItem(String value, String label) {
		super();
		this.value = value;
		this.label = label;
	}
	
	public CItem() {
		super();
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
