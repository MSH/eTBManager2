package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DispensingMovementDetail {

	public class DispensingPatientDetail{
		private Integer caseId;
		private String patientName;
		private int quantity;
		private String batchNumber;
		private String manufacturerName;
		/**
		 * @return the patientName
		 */
		public String getPatientName() {
			return patientName;
		}
		/**
		 * @param patientName the patientName to set
		 */
		public void setPatientName(String patientName) {
			this.patientName = patientName;
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
		 * @return the batchNumber
		 */
		public String getBatchNumber() {
			return batchNumber;
		}
		/**
		 * @param batchNumber the batchNumber to set
		 */
		public void setBatchNumber(String batchNumber) {
			this.batchNumber = batchNumber;
		}
		/**
		 * @return the manufacturerName
		 */
		public String getManufacturerName() {
			return manufacturerName;
		}
		/**
		 * @param manufacturerName the manufacturerName to set
		 */
		public void setManufacturerName(String manufacturerName) {
			this.manufacturerName = manufacturerName;
		}
		/**
		 * @return the caseId
		 */
		public Integer getCaseId() {
			return caseId;
		}
		/**
		 * @param caseId the caseId to set
		 */
		public void setCaseId(Integer caseId) {
			this.caseId = caseId;
		}
		
	}
	
	private Date movementDate;
	private String medicineName;
	private String dosageForm;
	private String SourceName;
	private List<DispensingPatientDetail> patientDetails;
	private Integer totalQuantity;
	
	/**
	 * @return the movementDate
	 */
	public Date getMovementDate() {
		return movementDate;
	}
	/**
	 * @param movementDate the movementDate to set
	 */
	public void setMovementDate(Date movementDate) {
		this.movementDate = movementDate;
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
	 * @return the sourceName
	 */
	public String getSourceName() {
		return SourceName;
	}
	/**
	 * @param sourceName the sourceName to set
	 */
	public void setSourceName(String sourceName) {
		SourceName = sourceName;
	}
	/**
	 * @return the patientDetails
	 */
	public List<DispensingPatientDetail> getPatientDetails() {
		return patientDetails;
	}
	/**
	 * @param patientDetails the patientDetails to set
	 */
	public void setPatientDetails(List<DispensingPatientDetail> patientDetails) {
		this.patientDetails = patientDetails;
	}
	/**
	 * @return the dosageForm
	 */
	public String getDosageForm() {
		return dosageForm;
	}
	/**
	 * @param dosageForm the dosageForm to set
	 */
	public void setDosageForm(String dosageForm) {
		this.dosageForm = dosageForm;
	}
	
	public DispensingPatientDetail addDispensingPatientDetail() {
		if(patientDetails == null)
			patientDetails = new ArrayList<DispensingMovementDetail.DispensingPatientDetail>();
		
		DispensingPatientDetail aux = new DispensingPatientDetail();
		patientDetails.add(aux);
		return aux;
	}
	/**
	 * @return the totalQuantity
	 */
	public Integer getTotalQuantity() {
		return totalQuantity;
	}
	/**
	 * @param totalQuantity the totalQuantity to set
	 */
	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}	
}
