package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class BatchMovement implements Serializable {
	private static final long serialVersionUID = -2778240504031969875L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="BATCH_ID")
	@NotNull
	private Batch batch;
	
	private int quantity;
	
	@ManyToOne
	@JoinColumn(name="MOVEMENT_ID")
	@NotNull
	private Movement movement;

	public int getQtdOperation() {
		return getQuantity() * movement.getType().getOper();
	}
	
	public float getTotalPrice() {
		return (getBatch( )!= null? quantity * batch.getUnitPrice(): 0);
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Batch getBatch() {
		return batch;
	}

	public void setBatch(Batch batch) {
		this.batch = batch;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Movement getMovement() {
		return movement;
	}

	public void setMovement(Movement movement) {
		this.movement = movement;
	}
}
