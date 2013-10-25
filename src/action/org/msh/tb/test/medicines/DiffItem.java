/**
 * 
 */
package org.msh.tb.test.medicines;


/**
 * @author Ricardo Memoria
 *
 */
public class DiffItem {

	private Integer unitId;
	private String unitName;
	private Integer sourceId;
	private Integer medicineId;
	private String sourceName;
	private String medicineName;
	private Integer batchId;
	private String batch;
	private int qtyStockPos;
	private int qtyMovements;
	private int qtyBatch;

	public DiffItem(Integer unitId, String unitName, Integer sourceId, String sourceName, 
			Integer medicineId, String medicineName) {
		super();
		this.unitId = unitId;
		this.unitName = unitName;
		this.sourceId = sourceId;
		this.medicineId = medicineId;
		this.sourceName = sourceName;
		this.medicineName = medicineName;
	}
	/**
	 * @return the qtyStockPos
	 */
	public int getQtyStockPos() {
		return qtyStockPos;
	}
	/**
	 * @param qtyStockPos the qtyStockPos to set
	 */
	public void setQtyStockPos(int qtyStockPos) {
		this.qtyStockPos = qtyStockPos;
	}
	/**
	 * @return the qtyMovements
	 */
	public int getQtyMovements() {
		return qtyMovements;
	}
	/**
	 * @param qtyMovements the qtyMovements to set
	 */
	public void setQtyMovements(int qtyMovements) {
		this.qtyMovements = qtyMovements;
	}
	/**
	 * @return the qtyBatch
	 */
	public int getQtyBatch() {
		return qtyBatch;
	}
	/**
	 * @param qtyBatch the qtyBatch to set
	 */
	public void setQtyBatch(int qtyBatch) {
		this.qtyBatch = qtyBatch;
	}
	/**
	 * @return the unitId
	 */
	public Integer getUnitId() {
		return unitId;
	}
	/**
	 * @param unitId the unitId to set
	 */
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}
	/**
	 * @return the unitName
	 */
	public String getUnitName() {
		return unitName;
	}
	/**
	 * @param unitName the unitName to set
	 */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	/**
	 * @return the sourceId
	 */
	public Integer getSourceId() {
		return sourceId;
	}
	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	/**
	 * @return the medicineId
	 */
	public Integer getMedicineId() {
		return medicineId;
	}
	/**
	 * @param medicineId the medicineId to set
	 */
	public void setMedicineId(Integer medicineId) {
		this.medicineId = medicineId;
	}
	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		return sourceName;
	}
	/**
	 * @param sourceName the sourceName to set
	 */
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	/**
	 * @return the medicineName
	 */
	public String getMedicineName() {
		return medicineName;
	}
	/**
	 * @param medicineName the medicineName to set
	 */
	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}
	/**
	 * @return the batchId
	 */
	public Integer getBatchId() {
		return batchId;
	}
	/**
	 * @param batchId the batchId to set
	 */
	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
	}
	/**
	 * @return the batch
	 */
	public String getBatch() {
		return batch;
	}
	/**
	 * @param batch the batch to set
	 */
	public void setBatch(String batch) {
		this.batch = batch;
	}
}
