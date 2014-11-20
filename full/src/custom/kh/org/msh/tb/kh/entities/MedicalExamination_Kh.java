package org.msh.tb.kh.entities;

import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.enums.DotBy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("kh")
public class MedicalExamination_Kh extends MedicalExamination {

	/**
	 * 
	 */
	private static final long serialVersionUID = -94225315652834888L;

	private DotBy dotDurinContPhase;
	
	private String otherPatRefBySrc;

	public DotBy getDotDurinContPhase() {
		return dotDurinContPhase;
	}	
	
	public void setDotDurinContPhase(DotBy dotDurinContPhase) {
		this.dotDurinContPhase = dotDurinContPhase;
	}
	
	public String getOtherPatRefBySrc() {
		return otherPatRefBySrc;
	}

	public void setOtherPatRefBySrc(String otherPatRefBySrc) {
		this.otherPatRefBySrc = otherPatRefBySrc;
	}
}

