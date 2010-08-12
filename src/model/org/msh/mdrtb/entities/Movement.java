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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.msh.mdrtb.entities.enums.MovementType;

/**
 * Stores information about a medicine transaction
 * @author Ricardo Memoria
 *
 */
@Entity
public class Movement implements Serializable {
	private static final long serialVersionUID = 3238105346061740436L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Temporal(TemporalType.DATE)
	@Column(name="mov_date")
	@NotNull
	private Date date;
	
	private int quantity;
	private int oper;
	
	private float unitPrice;

	private MovementType type;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	@ManyToOne
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;
	
	private int stockQuantity;
	
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date recordDate;

	@Length(max=250)
	private String comment;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="movement")
	private List<BatchMovement> batches = new ArrayList<BatchMovement>();

	
	/**
	 * Returns the total price of the transaction
	 * @return float number representing the total price
	 */
	public float getTotalPrice() {
		return quantity * unitPrice;
	}
	
	/**
	 * Returns the signed quantity of medicine to be added to the stock position  
	 * @return signed quantity of medicine for the transaction
	 */
	public int getQtdOperation() {
		return oper * quantity;
	}

	/**
	 * Returns the remark of the transaction
	 * @return String containing the remarks of the transaction
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Changes the remark of the transaction
	 * @param comment - the new remark of the transaction
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Returns the date of the transaction
	 * @return instance of the Date class
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Changes the date of the transaction
	 * @param date to be changed
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Returns the unique id of the transaction
	 * @return Integer instance representing the id of the transaction
	 */
	public Integer getId() {
		return id;
	}

	
	/**
	 * Changes the id of the transaction
	 * @param id to be changed
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Returns the medicine of the transaction
	 * @return instance of the Medicine class
	 */
	public Medicine getMedicine() {
		return medicine;
	}

	/**
	 * Changes the medicine of the transaction
	 * @param medicine - new medicine of the transaction 
	 */
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	/**
	 * Returns the quantity of medicine of the transaction
	 * @return quantity of medicine
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Changes the quantity of the transaction
	 * @param quantity - the new quantity
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Returns the source of the transaction
	 * @return instance of the Source class 
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Changes the source of the transaction
	 * @param source - the new source of the transaction
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Returns the TB unit of the transaction
	 * @return instance of Tbunit class
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}

	/**
	 * Changes the TB unit of the transaction
	 * @param tbunit - the new TB Unit
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	/**
	 * Returns the type of the transaction
	 * @return MovementType instance
	 */
	public MovementType getType() {
		return type;
	}

	/**
	 * Changes the type of the transaction
	 * @param type - new type of movement
	 */
	public void setType(MovementType type) {
		this.type = type;
	}

	/**
	 * Returns the date and time that the transaction was generated
	 * @return Date instance
	 */
	public Date getRecordDate() {
		return recordDate;
	}

	/**
	 * Changes the date and time the record was generated
	 * @param recordDate - the new record date
	 */
	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	/**
	 * Returns the operation of the transaction
	 * @return +1 if the transaction is including medicine into the stock, -1 if the transaction is removing medicine from the stock 
	 */
	public int getOper() {
		return oper;
	}

	/**
	 * Changes the operation of the transaction
	 * @param oper - the operation of the transaction (+1 or -1)
	 */
	public void setOper(int oper) {
		this.oper = oper;
	}

	/**
	 * Returns the batches movements involved in this transaction
	 * @return the list of instances of BachMovement class 
	 */
	public List<BatchMovement> getBatches() {
		return batches;
	}

	/**
	 * Changes the batches involved in the transaction
	 * @param batches - the list of BachMovement objects of the transaction
	 */
	public void setBatches(List<BatchMovement> batches) {
		this.batches = batches;
	}

	/**
	 * Returns the unit price of the transaction
	 * @return the unit price
	 */
	public float getUnitPrice() {
		return unitPrice;
	}

	/**
	 * Changes the unit price of the medicine for this transaction
	 * @param unitPrice - unit price of the medicine
	 */
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	/**
	 * Returns the stock quantity of the medicine after the execution of the transaction 
	 * @return
	 */
	public int getStockQuantity() {
		return stockQuantity;
	}

	/**
	 * Changes the stock quantity of the medicine
	 * @param stockQuantity - quantity of medicine
	 */
	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
}
