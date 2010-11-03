package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
 * Store data about the medicines ordered in an order
 * @author Ricardo Memoria
 *
 */
@Entity
public class OrderItem implements Serializable {
	private static final long serialVersionUID = -7765596999719386981L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@ManyToOne
	@JoinColumn(name="ORDER_ID")
	@NotNull
	private Order order;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MOVEMENT_IN_ID")
	private Movement MovementIn;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MOVEMENT_OUT_ID")
	private Movement MovementOut;
	
	private int estimatedQuantity;
	private int requestedQuantity;
	private Integer approvedQuantity;
	private Integer shippedQuantity;
	private Integer receivedQuantity;
	
	private Integer numPatients;
	
	@Column(length=200)
	private String comment;

	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ORDERITEM_ID")
	private List<OrderBatch> batches = new ArrayList<OrderBatch>();

	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ORDERITEM_ID")
	private List<OrderCase> cases = new ArrayList<OrderCase>();

	/**
	 * Used internally to check if item is validated
	 */
	@Transient
	private Object data;

	@Transient
	public float getTotalPrice() {
		float total = 0;
		for (OrderBatch b: batches) {
			total += b.getBatch().getUnitPrice() * b.getQuantity();
		}
		
		return total;
	}
	
	@Transient
	public float getUnitPrice() {
		float total = 0;
		float qtd = 0;
		for (OrderBatch b: batches) {
			int aux = (b.getReceivedQuantity() != null? b.getReceivedQuantity(): b.getQuantity()); 
			qtd += aux;
			total += b.getBatch().getUnitPrice() * aux;
		}
		
		return (qtd > 0? total/qtd: 0);
	}
	
	public void addCase(TbCase c, int estimatedQtd) {
		for (OrderCase oc: cases) {
			if (oc.getTbcase().equals(c))
				return;
		}

		OrderCase oc = new OrderCase();
		oc.setTbcase(c);
		oc.setItem(this);
		oc.setEstimatedQuantity(estimatedQtd);
		
		cases.add(oc);
		numPatients = cases.size();
	}
	
	public Integer getApprovedQuantity() {
		return approvedQuantity;
	}
	public void setApprovedQuantity(Integer approvedQuantity) {
		this.approvedQuantity = approvedQuantity;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Medicine getMedicine() {
		return medicine;
	}
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}
	public int getRequestedQuantity() {
		return requestedQuantity;
	}
	public void setRequestedQuantity(int requestedQuantity) {
		this.requestedQuantity = requestedQuantity;
	}
	public Integer getReceivedQuantity() {
		return receivedQuantity;
	}
	public void setReceivedQuantity(Integer receivedQuantity) {
		this.receivedQuantity = receivedQuantity;
	}
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public Movement getMovementIn() {
		return MovementIn;
	}
	public void setMovementIn(Movement movementIn) {
		MovementIn = movementIn;
	}
	public Movement getMovementOut() {
		return MovementOut;
	}
	public void setMovementOut(Movement movementOut) {
		MovementOut = movementOut;
	}
	public List<OrderBatch> getBatches() {
		return batches;
	}
	public void setBatches(List<OrderBatch> batches) {
		this.batches = batches;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public int getEstimatedQuantity() {
		return estimatedQuantity;
	}
	public void setEstimatedQuantity(int estimatedQuantity) {
		this.estimatedQuantity = estimatedQuantity;
	}
	public Integer getShippedQuantity() {
		return shippedQuantity;
	}
	public void setShippedQuantity(Integer shippedQuantity) {
		this.shippedQuantity = shippedQuantity;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getNumPatients() {
		return numPatients;
	}

	public void setNumPatients(Integer numPatients) {
		this.numPatients = numPatients;
	}

	public List<OrderCase> getCases() {
		return cases;
	}

	public void setCases(List<OrderCase> cases) {
		this.cases = cases;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
