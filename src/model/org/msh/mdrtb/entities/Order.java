package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;
import org.msh.mdrtb.entities.enums.OrderStatus;

@Entity
@Table(name="MedicineOrder")
public class Order implements Serializable {
	private static final long serialVersionUID = 2113860266433650022L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Temporal(TemporalType.DATE)
	@NotNull
	private Date orderDate;

	@Temporal(TemporalType.DATE)
	private Date approvingDate;	
	
	@Temporal(TemporalType.DATE)
	private Date shippingDate;

	@Temporal(TemporalType.DATE)
	private Date receivingDate;

	private OrderStatus status;
	
	private Integer numDays;
	
	@ManyToOne
	@JoinColumn(name="UNIT_FROM_ID")
	@NotNull
	private Tbunit tbunitFrom;

	@ManyToOne
	@JoinColumn(name="UNIT_TO_ID")
	@NotNull
	private Tbunit tbunitTo;

	@Column(length=200)
	private String comments;
	
	@Column(length=200)
	private String cancelReason;
	
	@ManyToOne
	@JoinColumn(name="USER_CREATOR_ID")
	@NotNull
	private User userCreator;
	
	@ManyToOne
	@JoinColumn(name="AUTHORIZER_UNIT_ID")
	private Tbunit authorizer;
	
	@OneToMany(mappedBy="order", cascade={CascadeType.ALL})
	private List<OrderItem> items = new ArrayList<OrderItem>();

	@Column(length=50)
	private String legacyId;

	@Transient
	public float getTotalPrice() {
		float tot = 0;
		for (OrderItem it: items) {
			tot += it.getTotalPrice();
		}
		return tot;
	}

	@Override
	public String toString() {
		if (id != null)
			 return id.toString();
		else return super.toString();
	}

	public Date getShippingDate() {
		return shippingDate;
	}

	public void setShippingDate(Date shippingDate) {
		this.shippingDate = shippingDate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public Date getReceivingDate() {
		return receivingDate;
	}

	public void setReceivingDate(Date receivingDate) {
		this.receivingDate = receivingDate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public Date getApprovingDate() {
		return approvingDate;
	}

	public void setApprovingDate(Date approvingDate) {
		this.approvingDate = approvingDate;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Tbunit getTbunitFrom() {
		return tbunitFrom;
	}

	public void setTbunitFrom(Tbunit tbunitFrom) {
		this.tbunitFrom = tbunitFrom;
	}

	public Tbunit getTbunitTo() {
		return tbunitTo;
	}

	public void setTbunitTo(Tbunit tbunitTo) {
		this.tbunitTo = tbunitTo;
	}

	public Integer getNumDays() {
		return numDays;
	}

	public void setNumDays(Integer numDays) {
		this.numDays = numDays;
	}

	public User getUserCreator() {
		return userCreator;
	}

	public void setUserCreator(User userCreator) {
		this.userCreator = userCreator;
	}

	public Tbunit getAuthorizer() {
		return authorizer;
	}

	public void setAuthorizer(Tbunit authorizer) {
		this.authorizer = authorizer;
	}

	/**
	 * @return the legacyId
	 */
	public String getLegacyId() {
		return legacyId;
	}

	/**
	 * @param legacyId the legacyId to set
	 */
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}


}
