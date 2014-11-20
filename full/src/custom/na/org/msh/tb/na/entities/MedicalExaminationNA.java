package org.msh.tb.na.entities;

import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.na.entities.enums.DotOptions;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("na")
public class MedicalExaminationNA extends MedicalExamination {
	private static final long serialVersionUID = 7510532907471481516L;

	private DotOptions dotIntOptions;	
	
	private DotOptions dotContOptions;
	
	@Column(length=100)
	private String dotContProvName;

	public String getDotContProvName() {
		return dotContProvName;
	}

	public void setDotContProvName(String dotContProvName) {
		this.dotContProvName = dotContProvName;
	}

	public DotOptions getDotIntOptions() {
		return dotIntOptions;
	}

	public void setDotIntOptions(DotOptions dotIntOptions) {
		this.dotIntOptions = dotIntOptions;
	}

	public DotOptions getDotContOptions() {
		return dotContOptions;
	}

	public void setDotContOptions(DotOptions dotContOptions) {
		this.dotContOptions = dotContOptions;
	}	
	
	
}
