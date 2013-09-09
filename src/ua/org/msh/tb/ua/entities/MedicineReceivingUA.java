package org.msh.tb.ua.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.MedicineReceiving;


@Entity
@Table(name="medicinereceiving_ua")
public class MedicineReceivingUA extends MedicineReceiving{
	private static final long serialVersionUID = -5025474492084426057L;
	
	private String consignmentNumber;

	public void setConsignmentNumber(String consignmentNumber) {
		this.consignmentNumber = consignmentNumber;
	}

	public String getConsignmentNumber() {
		return consignmentNumber;
	}
	
/*	
	public void clone(MedicineReceiving mr){
		this.setComments(mr.getComments());
		this.setId(mr.getId());
		this.setMovements(mr.getMovements());
		this.setReceivingDate(mr.getReceivingDate());
		this.setSource(mr.getSource());
		this.setTbunit(mr.getTbunit());
		this.setTotalPrice(mr.getTotalPrice());
	}*/
}