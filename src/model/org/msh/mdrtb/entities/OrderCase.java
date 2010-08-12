package org.msh.mdrtb.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
public class OrderCase implements Serializable {
	private static final long serialVersionUID = 2544354553754037596L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbCase;
	
	@ManyToOne
	@JoinColumn(name="ORDERITEM_ID")
	@NotNull
	private OrderItem item;
	
	private int estimatedQuantity;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TbCase getTbCase() {
		return tbCase;
	}

	public void setTbCase(TbCase tbCase) {
		this.tbCase = tbCase;
	}

	public OrderItem getItem() {
		return item;
	}

	public void setItem(OrderItem item) {
		this.item = item;
	}

	public int getEstimatedQuantity() {
		return estimatedQuantity;
	}

	public void setEstimatedQuantity(int estimatedQuantity) {
		this.estimatedQuantity = estimatedQuantity;
	}
}
