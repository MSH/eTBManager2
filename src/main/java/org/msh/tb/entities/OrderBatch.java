package org.msh.tb.entities;

import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="orderbatch")
public class OrderBatch implements Serializable {
	private static final long serialVersionUID = 374938237338336760L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="ORDERITEM_ID")
	@NotNull
	private OrderItem orderItem;
	
	@ManyToOne
	@JoinColumn(name="BATCH_ID")
	@NotNull
	private Batch batch;

	private int quantity;
	private Integer receivedQuantity;

	@Transient
	public float getTotalPrice() {
		float qtd = (receivedQuantity != null? receivedQuantity: quantity);
		return (batch != null? batch.getUnitPrice() * qtd: 0);
	}
	
	public Integer getReceivedQuantity() {
		return receivedQuantity;
	}
	public void setReceivedQuantity(Integer receivedQuantity) {
		this.receivedQuantity = receivedQuantity;
	}
	public Batch getBatch() {
		return batch;
	}
	public void setBatch(Batch batch) {
		this.batch = batch;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
	}
	
	/**
	 * Return received quantity of containers
	 */
	public int getNumContainersRec(){
		int quantityContainer = batch.getQuantityContainer();
		return (quantityContainer > 0)? (int)Math.ceil((double)receivedQuantity/(double)quantityContainer): 0;
	}
	
	public void setNumContainersRec(int numContainersRec){
		return;
	}
}
