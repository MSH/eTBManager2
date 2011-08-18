package org.msh.tb.entities;

import java.io.Serializable;

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
 * Store information about an available quantity of medicines from one specific batch in a TB unit
 * @author Ricardo Memória
 *
 */
@Entity
@Table(name = "batchquantity")
public class BatchQuantity implements Serializable {
	private static final long serialVersionUID = -5948846203987021296L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BATCH_ID")
	@NotNull
	private Batch batch;

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
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}
	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
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
	
	public float getTotalPrice() {
		Batch batch = getBatch();
		return (batch != null? batch.getUnitPrice() * quantity: 0);
	}
}
