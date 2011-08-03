package org.msh.tb.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class BatchDispensing {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="DISPENSING_ID")
	private MedicineDispensing dispensing;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BATCH_ID")
	@NotNull
	private Batch batch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;

	private int quantity;



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
	
	
}
