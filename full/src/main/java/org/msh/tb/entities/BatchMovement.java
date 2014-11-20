package org.msh.tb.entities;

import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "batchmovement")
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

    private int availableQuantity;

    @ManyToOne
    @JoinColumn(name="MOVEMENT_ID")
    @NotNull
    private Movement movement;


    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

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
