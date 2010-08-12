package org.msh.tb.cases;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Substance;

/**
 * Component of a medicine, 
 *  example: Isoniazid is a common component of medicines
 * @author Ricardo Memoria
 *
 */
@Entity
public class MedicineComponent implements Serializable{
	private static final long serialVersionUID = -195735659908845780L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="SUBSTANCE_ID")
	private Substance substance;

	private Integer strength;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	private Medicine medicine;


	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Substance getSubstance() {
		return substance;
	}

	public void setSubstance(Substance substance) {
		this.substance = substance;
	}

	public Integer getStrength() {
		return strength;
	}

	public void setStrength(Integer strength) {
		this.strength = strength;
	}
		
}
