package org.msh.tb.entities;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.YesNoType;

/**
 * Holds information about a side effect of a TB case
 * @author Ricardo Lima
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("gen")
@Table(name="casesideeffect")
public class CaseSideEffect {


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="SIDEEFFECT_ID")
	@NotNull
	private FieldValue sideEffect;
	
	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	private String medicines;
	
	@Column(name="SE_MONTH")
	private int month;
	
	private YesNoType resolved;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSTANCE2_ID")
	private Substance substance2;

	
	/**
	 * @return the resolved
	 */
	public YesNoType getResolved() {
		return resolved;
	}

	/**
	 * @param resolved the resolved to set
	 */
	public void setResolved(YesNoType resolved) {
		this.resolved = resolved;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the sideEffect
	 */
	public FieldValue getSideEffect() {
		return sideEffect;
	}

	/**
	 * @param sideEffect the sideEffect to set
	 */
	public void setSideEffect(FieldValue sideEffect) {
		this.sideEffect = sideEffect;
	}

	/**
	 * @return the medicines
	 */
	public String getMedicines() {
		return medicines;
	}

	/**
	 * @param medicines the medicines to set
	 */
	public void setMedicines(String medicines) {
		this.medicines = medicines;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}

	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	/**
	 * @return the substance
	 */
	public Substance getSubstance() {
		return substance;
	}

	/**
	 * @param substance the substance to set
	 */
	public void setSubstance(Substance substance) {
		this.substance = substance;
	}

	/**
	 * @return the substance2
	 */
	public Substance getSubstance2() {
		return substance2;
	}

	/**
	 * @param substance2 the substance2 to set
	 */
	public void setSubstance2(Substance substance2) {
		this.substance2 = substance2;
	}
}
