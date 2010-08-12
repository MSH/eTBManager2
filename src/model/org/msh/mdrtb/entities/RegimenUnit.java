package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class RegimenUnit implements Serializable {
	private static final long serialVersionUID = -1577379478375533729L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="REGIMEN_ID")
	@NotNull
	private Regimen regimen;
	
	@ManyToOne
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;
	
	private int numTreatments;


	public int getNumTreatments() {
		return numTreatments;
	}

	public void setNumTreatments(int numTreatments) {
		this.numTreatments = numTreatments;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Regimen getRegimen() {
		return regimen;
	}

	public void setRegimen(Regimen regimen) {
		this.regimen = regimen;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}
}
