package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Entity
public class MedicineReceivingItem implements Serializable {
	private static final long serialVersionUID = 3933172296754337929L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne
	@JoinColumn(name="RECEIVING_ID")
	@NotNull
	private MedicineReceiving medicineReceiving;
	
	@ManyToOne
	@JoinColumn(name="MOVEMENT_ID")
	private Movement movement;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="RECEIVINGITEM_ID")
	private List<Batch> batches = new ArrayList<Batch>();

	@Transient
	public float getTotalPrice() {
		float total = 0;
		for (Batch b: batches) {
			total += b.getTotalPrice();
		}
		
		return total;
	}
	
	@Transient
	public int getTotalQtdBatches() {
		int total = 0;
		for (Batch b: batches) {
			total += b.getQuantityReceived();
		}
		
		return total;
	}
	
	@Transient
	public float getUnitPrice() {
		int total = getTotalQtdBatches();
		if (total == 0)
			return 0;
		else return getTotalPrice() / getTotalQtdBatches();
	}
	
	public List<Batch> getBatches() {
		return batches;
	}

	public void setBatches(List<Batch> batches) {
		this.batches = batches;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Movement getMovement() {
		return movement;
	}

	public void setMovement(Movement movement) {
		this.movement = movement;
	}

	public MedicineReceiving getMedicineReceiving() {
		return medicineReceiving;
	}

	public void setMedicineReceiving(MedicineReceiving medicineReceiving) {
		this.medicineReceiving = medicineReceiving;
	}
}
