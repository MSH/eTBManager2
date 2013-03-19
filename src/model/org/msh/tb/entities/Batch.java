package org.msh.tb.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.Container;
import org.msh.utils.date.DateUtils;

@Entity
@Table(name = "batch")
public class Batch implements Serializable {
	private static final long serialVersionUID = -7099327398266493703L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@NotNull
	@Temporal(TemporalType.DATE)
	private Date expiryDate;
	
	@Column(length=30)
	@NotNull
	private String batchNumber;
	
	@Column(length=80)
	private String manufacturer;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	private Container container;
	private int quantityReceived;
	private int quantityContainer;
	private float unitPrice;
	
	private String brandName;
	private String registCardNumber;
	
	@Temporal(TemporalType.DATE)
	private Date registCardBeginDate;
	
	@Temporal(TemporalType.DATE)
	private Date registCardEndDate;


	/**
	 * Check if batch is 
	 * @return
	 */
	public boolean isExpired() {
		return (expiryDate != null) && (expiryDate.before(DateUtils.getDate()));
	}

	@Override
	public String toString() {
		return (batchNumber != null? batchNumber: super.toString());
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Batch))
			return false;
		
		return (((id==null) || (((Batch)obj).getId() == null)? false: ((Batch)obj).getId().equals(id)));
	}
	
	/**
	 * Copy data from another batch. 
	 * @param b
	 */
/*	public void copyFromBatch(Batch b) {
		batchNumber = b.getBatchNumber();
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
		return (quantityContainer > 0)? (int)Math.ceil((double)quantityReceived/(double)quantityContainer): 0;
	}
	
	public void setNumContainers(int value) {
		if (quantityContainer==0)
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

	/**
	 * @return the brandName
	 */
	public String getBrandName() {
		return brandName;
	}

	/**
	 * @param brandName the brandName to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	/**
	 * @return the registCardNumber
	 */
	public String getRegistCardNumber() {
		return registCardNumber;
	}

	/**
	 * @param registCardNumber the registCardNumber to set
	 */
	public void setRegistCardNumber(String registCardNumber) {
		this.registCardNumber = registCardNumber;
	}

	/**
	 * @return the registCardBeginDate
	 */
	public Date getRegistCardBeginDate() {
		return registCardBeginDate;
	}

	/**
	 * @param registCardBeginDate the registCardBeginDate to set
	 */
	public void setRegistCardBeginDate(Date registCardBeginDate) {
		this.registCardBeginDate = registCardBeginDate;
	}

	/**
	 * @return the registCardEndDate
	 */
	public Date getRegistCardEndDate() {
		return registCardEndDate;
	}

	/**
	 * @param registCardEndDate the registCardEndDate to set
	 */
	public void setRegistCardEndDate(Date registCardEndDate) {
		this.registCardEndDate = registCardEndDate;
	}
	
	
}
