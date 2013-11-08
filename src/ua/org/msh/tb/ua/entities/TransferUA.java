package org.msh.tb.ua.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.MedicineReceiving;
import org.msh.tb.entities.Transfer;


@Entity
@Table(name="transfer_ua")
public class TransferUA extends Transfer{
	private static final long serialVersionUID = 4684897945702341635L;

	private String orderNumber;
	@Temporal(TemporalType.DATE) private Date orderDate;
	
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

}