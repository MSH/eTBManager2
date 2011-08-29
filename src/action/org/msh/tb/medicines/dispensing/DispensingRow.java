package org.msh.tb.medicines.dispensing;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;

/**
 * Defines a row of a {@link BatchDispensingTable} table. Each row contains information about a batch
 * and its quantity dispensed.
 * <p/>
 * The row also includes information about medicine source {@link Source}, {@link Medicine} of the batch,
 * the instance of {@link Batch} and the quantity dispensed of the batch and its total by medicine.
 * <p/>
 * Rows are sorted by source and medicine, and the quantity dispensed by medicine is just informed in the first
 * row in the group of batches with same medicine. Each row contains a property <i>isSpanned()</i>, which is used
 * during rendering of the table to identify the first row of a group of row with same medicine, and if 
 * isSpanned() returns true (i.e, the first row of a group of batches with same medicine), the property
 * <i>rowSpan()</i> indicate the number of rows in the group
 *    
 * @author Ricardo Memoria
 *
 */
public class DispensingRow {

	private BatchDispensingTable table;
	private Batch batch;
	private Source source;
	private int quantity;
	private int totalQuantity;
	private int rowSpan;
	
	// support for editing of dispensing quantity
	private Integer dispensingQuantity;
	private String errorMessage;

	public DispensingRow(BatchDispensingTable table, Source source, Batch batch) {
		super();
		this.table = table;
		this.source = source;
		this.batch = batch;
	}
	
	/**
	 * Increment the quantity by qtd
	 * @param qtd
	 */
	public void addQuantity(int qtd) {
		quantity += qtd;
	}
	
	public int getTotalQuantity() {
		return totalQuantity;
	}
	
	public void setTotalQuantity(int totalQtd) {
		this.totalQuantity = totalQtd;
	}
	
	public Medicine getMedicine() {
		return (batch != null? batch.getMedicine(): null);
	}
	
	public BatchDispensingTable getTable() {
		return table;
	}

	public Batch getBatch() {
		return batch;
	}

	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * @param rowSpan the rowSpan to set
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
	
	public boolean isSpanned() {
		return rowSpan == -1;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the dispensingQuantity
	 */
	public Integer getDispensingQuantity() {
		return dispensingQuantity;
	}

	/**
	 * @param dispensingQuantity the dispensingQuantity to set
	 */
	public void setDispensingQuantity(Integer dispensingQuantity) {
		this.dispensingQuantity = dispensingQuantity;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
