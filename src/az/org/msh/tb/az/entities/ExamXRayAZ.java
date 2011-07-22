package org.msh.tb.az.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.FieldValue;

@Entity
@DiscriminatorValue("az")
public class ExamXRayAZ extends ExamXRay {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LOCALIZATION_ID")
	private FieldValue localization;

	/**
	 * @return the localization
	 */
	public FieldValue getLocalization() {
		return localization;
	}

	/**
	 * @param localization the localization to set
	 */
	public void setLocalization(FieldValue localization) {
		this.localization = localization;
	}

}
