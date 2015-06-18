package org.msh.tb.entities;

import javax.persistence.*;

@Embeddable
public class FieldValueComponent {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FIELDVALUE_ID")
	private FieldValue value;

	@Column(name="Complement", length=100)
	private String complement;


	public FieldValueComponent() {
		super();
	}


	public FieldValueComponent(FieldValue value) {
		super();
		this.value = value;
	}

	@Override
	public String toString() {
        if (value == null) {
            return "";
        }

        String s = value.toString();
        if (complement != null && !complement.isEmpty()) {
            s += " - " + s;
        }
        return s;
	}
	
	/**
	 * Clear the content of the field
	 */
	public void clear() {
		value = null;
		complement = null;
	}
	
	/**
	 * Check if complement is empty
	 * @return true - complement is empty
	 */
	public boolean isComplementEmpty() {
		return ((complement == null) || (complement.isEmpty()));
	}
	
	/**
	 * @return the value
	 */
	public FieldValue getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(FieldValue value) {
		if (value != null)
			complement = null;
		this.value = value;
	}

	/**
	 * @return the complement
	 */
	public String getComplement() {
		return complement;
	}

	/**
	 * @param complement the complement to set
	 */
	public void setComplement(String complement) {
		this.complement = complement;
	}

}
