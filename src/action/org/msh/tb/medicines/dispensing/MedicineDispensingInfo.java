package org.msh.tb.medicines.dispensing;

import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;


public class MedicineDispensingInfo {

	private Integer sourceId;
	private Integer medicineId;
	
	private int qtdReserved;
	private int qtdMovements;
	private int qtdDispensed;
	private float unitPrice;

	// this properties are just filled in case of validation errors
	private Source source;
	private Medicine medicine;


	public int getQtdAvailable() {
		return qtdReserved + qtdMovements;
	}
	
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public Integer getMedicineId() {
		return medicineId;
	}
	public void setMedicineId(Integer medicineId) {
		this.medicineId = medicineId;
	}
	public int getQtdReserved() {
		return qtdReserved;
	}
	public void setQtdReserved(int qtdReserved) {
		this.qtdReserved = qtdReserved;
	}
	public int getQtdMovements() {
		return qtdMovements;
	}
	public void setQtdMovements(int qtdMovements) {
		this.qtdMovements = qtdMovements;
	}
	public int getQtdDispensed() {
		return qtdDispensed;
	}
	public void setQtdDispensed(int qtdDispensed) {
		this.qtdDispensed = qtdDispensed;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Medicine getMedicine() {
		return medicine;
	}

	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}
}
