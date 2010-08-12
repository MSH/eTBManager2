package org.msh.mdrtb.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

/**
 * Store information about a medicine dispensed during a period
 * @author Ricardo Memoria
 *
 */
@Entity
public class MedicineDispensingItem {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="DISPENSING_ID")
	private MedicineDispensing dispensing;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MOVEMENT_ID")
	@NotNull
	private Movement movement;

	@OneToMany(mappedBy="item", cascade={CascadeType.ALL})
	private List<MedicineDispensingBatch> batches = new ArrayList<MedicineDispensingBatch>();

	/**
	 * Used to store temporary information, for example, a validation error message to be displayed at the page
	 */
	@Transient
	private Object data;
	
	public int getQuantity(){
		int total = 0;
		for (MedicineDispensingBatch batch: batches) {
			total += batch.getQuantity();
		}
		return total;
	}

	
	public double getTotalPrice() {
		double total = 0;
		for (MedicineDispensingBatch batch: batches) {
			total += batch.getBatch().getUnitPrice() * batch.getQuantity();
		}
		return total;
	}
	

	/**
	 * Return the unit price
	 * @return
	 */
	public double getUnitPrice() {
		double totalPrice = 0;
		double totalQtd = 0;
		for (MedicineDispensingBatch batch: batches) {
			totalQtd += batch.getQuantity();
			totalPrice += batch.getBatch().getUnitPrice();
		}
		
		return (totalQtd == 0? 0: totalPrice/totalQtd);
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
	 * @return the medicine
	 */
	public Medicine getMedicine() {
		return medicine;
	}

	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the batches
	 */
	public List<MedicineDispensingBatch> getBatches() {
		return batches;
	}

	/**
	 * @param batches the batches to set
	 */
	public void setBatches(List<MedicineDispensingBatch> batches) {
		this.batches = batches;
	}

	/**
	 * @return the dispensing
	 */
	public MedicineDispensing getDispensing() {
		return dispensing;
	}

	/**
	 * @param dispensing the dispensing to set
	 */
	public void setDispensing(MedicineDispensing dispensing) {
		this.dispensing = dispensing;
	}

	/**
	 * @return the movement
	 */
	public Movement getMovement() {
		return movement;
	}

	/**
	 * @param movement the movement to set
	 */
	public void setMovement(Movement movement) {
		this.movement = movement;
	}


	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}


	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

}
