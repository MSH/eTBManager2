package org.msh.tb.entities;

import org.hibernate.validator.NotNull;
import org.msh.tb.entities.enums.BufferStockMeasure;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="medicineunit")
public class MedicineUnit implements Serializable {
	private static final long serialVersionUID = 688447456891967008L;


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;
	
	@ManyToOne
	@JoinColumn(name="MEDICINE_ID")
	@NotNull
	private Medicine medicine;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SOURCE_ID")
	@NotNull
	private Source source;
	
	/**
	 * Indicate the minimum buffer stock (the unit depends on the measure type)
	 */
	private Integer minBufferStock;

	
	/**
	 * Indicate how the buffer stock is measured, in time (days, months) or in quantity
	 */
	private BufferStockMeasure measure;
	

	private Integer numDaysOrder;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	public Integer getMinBufferStock() {
		return minBufferStock;
	}

	public void setMinBufferStock(Integer minBufferStock) {
		this.minBufferStock = minBufferStock;
	}

	public BufferStockMeasure getMeasure() {
		return measure;
	}

	public void setMeasure(BufferStockMeasure measure) {
		this.measure = measure;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Integer getNumDaysOrder() {
		return numDaysOrder;
	}

	public void setNumDaysOrder(Integer numDaysOrder) {
		this.numDaysOrder = numDaysOrder;
	}
	
	public boolean isOverrideUnitOrder() {
		return numDaysOrder != null;
	}
	
	public void setOverrideUnitOrder(boolean value) {
		if (!value)
			numDaysOrder = null;
	}
}
