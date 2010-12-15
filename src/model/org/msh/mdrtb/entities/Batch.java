package org.msh.mdrtb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.msh.mdrtb.entities.enums.Container;

@Entity
public class Batch implements Serializable {
	private static final long serialVersionUID = -7099327398266493703L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@NotNull
	@Temporal(TemporalType.DATE)
	private Date expiryDate;

	@Column(length=80)
	private String brandName;
	
	@Column(length=30)
	@NotNull
	private String batchNumber;
	
	@Column(length=80)
	private String manufacturer;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="RECEIVINGITEM_ID")
	private MedicineReceivingItem medicineReceivingItem;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	private Container container;
	private int quantityReceived;
	private int quantityContainer;
	private float unitPrice;


	/**
	 * Check if batch is 
	 * @return
	 */
	public boolean isExpired() {
		return (expiryDate != null) && (expiryDate.before(new Date()));
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Batch))
			return false;
		
		return (((id==null) || (((Batch)obj).getId() != null)? false: ((Batch)obj).id.equals(id)));
	}
	
	/**
	 * Copy data from another batch. 
	 * @param b
	 */
/*	public void copyFromBatch(Batch b) {
		batchNumber = b.getBatchNumber();
		brandName = b.getBrandName();
		container = b.getContainer();
		expiryDate = b.getExpiryDate();
		manufacturer = b.getManufacturer();
		medicine = b.getMedicine();
		quantityReceived = b.getQuantityReceived();
		quantityContainer = b.getQuantityContainer();
		unitPrice = b.getUnitPrice();
	}
*/
	
	/**
	 * Returns the number of containers based on the quantity and the quantity per box
	 * @return
	 */
	public int getNumContainers() {
		return (quantityContainer > 0)? quantityReceived / quantityContainer: 0;
	}
	
	public void setNumContainers(int value) {
		quantityContainer = (value != 0? quantityReceived/value: 0);
	}
	
	public Integer getQuantityContainer() {
		return quantityContainer;
	}

	public void setQuantityContainer(Integer quantityContainer) {
		this.quantityContainer = quantityContainer;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public float getUnitPrice() {
		return unitPrice;
	}
	
	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public MedicineReceivingItem getMedicineReceivingItem() {
		return medicineReceivingItem;
	}

	public void setMedicineReceivingItem(MedicineReceivingItem medicineReceivingItem) {
		this.medicineReceivingItem = medicineReceivingItem;
	}

	public float getTotalPrice() {
		return unitPrice * quantityReceived;
	}

	public void setTotalPrice(float totalPrice) {
		if (quantityReceived != 0)
			unitPrice = totalPrice / quantityReceived;
		else
		if (unitPrice != 0)
			quantityReceived = Math.round(totalPrice / unitPrice);
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	/**
	 * @return the expiryDate
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}

	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	/**
	 * @param quantityReceived the quantityReceived to set
	 */
	public void setQuantityReceived(int quantityReceived) {
		this.quantityReceived = quantityReceived;
	}

	/**
	 * @return the quantityReceived
	 */
	public int getQuantityReceived() {
		return quantityReceived;
	}

	/**
	 * @param unitPrice the unitPrice to set
	 */
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
}
