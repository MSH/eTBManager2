package org.msh.tb.vi.entities;

import org.msh.tb.entities.TbCase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="tbcasevi")
public class TbCaseVI extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2635662744844808855L;

	@Column(length=50)
	private String nationalIDNumber;

	public String getNationalIDNumber() {
		return nationalIDNumber;
	}

	public void setNationalIDNumber(String nationalIDNumber) {
		this.nationalIDNumber = nationalIDNumber;
	}
	
	
	
	
	
	
	
}
