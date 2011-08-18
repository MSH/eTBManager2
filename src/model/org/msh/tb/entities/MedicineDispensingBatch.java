package org.msh.tb.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;

/**
 * Store information about a batch dispensed
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="medicinedispensingbatch")
public class MedicineDispensingBatch {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DISPENSINGITEM_ID")
	private MedicineDispensingItem item;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BATCH_ID")
	@NotNull
	private Batch batch;
	
	private int quantity;

	
	public double getTotalPrice() {
		return (batch != null? batch.getUnitPrice() * quantity: 0);
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
	 * @return the batch
	 */
	public Batch getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the item
	 */
	public MedicineDispensingItem getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(MedicineDispensingItem item) {
		this.item = item;
	}
}
