package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.RegimenPhase;

@Entity
public class MedicineRegimen implements Serializable {
	private static final long serialVersionUID = 442884632590945592L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	private Integer defaultDoseUnit;
	
	private Integer defaultFrequency;
	
	private RegimenPhase phase;
	
	private Integer monthsTreatment;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source defaultSource;


	public Source getDefaultSource() {
		return defaultSource;
	}

	public void setDefaultSource(Source defaultSource) {
		this.defaultSource = defaultSource;
	}

	public RegimenPhase getPhase() {
		return phase;
	}

	public void setPhase(RegimenPhase phase) {
		this.phase = phase;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public Integer getDefaultDoseUnit() {
		return defaultDoseUnit;
	}

	public void setDefaultDoseUnit(Integer defaultDoseUnit) {
		this.defaultDoseUnit = defaultDoseUnit;
	}

	public Integer getDefaultFrequency() {
		return defaultFrequency;
	}

	public void setDefaultFrequency(Integer defaultFrequency) {
		this.defaultFrequency = defaultFrequency;
	}

	/**
	 * @return the monthsTreatment
	 */
	public Integer getMonthsTreatment() {
		return monthsTreatment;
	}

	/**
	 * @param monthsTreatment the monthsTreatment to set
	 */
	public void setMonthsTreatment(Integer monthsTreatment) {
		this.monthsTreatment = monthsTreatment;
	}

}
