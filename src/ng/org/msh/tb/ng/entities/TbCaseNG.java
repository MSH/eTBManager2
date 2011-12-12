package org.msh.tb.ng.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.msh.tb.entities.TbCase;
import org.msh.tb.log.FieldLog;

@Entity
@Table(name="tbcaseng")
public class TbCaseNG extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227321155884824528L;
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	@FieldLog(ignore=true)
	private List<CaseDispensing_Ng> dispng = new ArrayList<CaseDispensing_Ng>();

	public List<CaseDispensing_Ng> getDispng() {
		return dispng;
	}

	public void setDispng(List<CaseDispensing_Ng> dispng) {
		this.dispng = dispng;
	}
	
	


	
	



}
