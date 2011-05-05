package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class PrevTBTreatment implements Serializable {
	private static final long serialVersionUID = -4070705919226815216L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@Column(name="TREATMENT_MONTH")
	private Integer month;
	
	@Column(name="TREATMENT_YEAR")
	private int year;
	
	@NotNull
	private PrevTBTreatmentOutcome outcome;

	@ManyToMany
	@JoinTable(name="RES_PREVTBTREATMENT", 
			joinColumns={@JoinColumn(name="PREVTBTREATMENT_ID")},
			inverseJoinColumns={@JoinColumn(name="SUBSTANCE_ID")})
	private List<Substance> substances = new ArrayList<Substance>();



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public PrevTBTreatmentOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(PrevTBTreatmentOutcome outcome) {
		this.outcome = outcome;
	}

	public List<Substance> getSubstances() {
		return substances;
	}

	public void setSubstances(List<Substance> substances) {
		this.substances = substances;
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}
}
