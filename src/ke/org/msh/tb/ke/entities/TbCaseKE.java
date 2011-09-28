package org.msh.tb.ke.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.msh.tb.entities.TbCase;
import org.msh.tb.log.FieldLog;

@Entity
@Table(name="tbcaseke")
public class TbCaseKE extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227321155884824528L;
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	@FieldLog(ignore=true)
	private List<CaseDispensing_Ke> dispke = new ArrayList<CaseDispensing_Ke>();

	public List<CaseDispensing_Ke> getDispke() {
		return dispke;
	}

	public void setDispke(List<CaseDispensing_Ke> dispke) {
		this.dispke = dispke;
	}
	
	


	
	



}
