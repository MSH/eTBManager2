package org.msh.tb.cases;

/**
 * Hold information about the cases of a health unit (report generation in the main page of case management)
 * @author Ricardo Memoria
 *
 */
public class HealthUnitInfo {

	private Integer unitId;
	private String unitName;
	private String adminUnitCode;
	private Long casesNotifs;
	private Long casesOnTreatment;
	private Long casesTransferIn;
	private Long casesTransferOut;
	private Long casesNotOnTreatment;
	private Long medExamMissing;

	
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
	 * @return the casesNotifs
	 */
	public Long getCasesNotifs() {
		return casesNotifs;
	}
	/**
	 * @param casesNotifs the casesNotifs to set
	 */
	public void setCasesNotifs(Long casesNotifs) {
		this.casesNotifs = casesNotifs;
	}
	/**
	 * @return the casesOnTreatment
	 */
	public Long getCasesOnTreatment() {
		return casesOnTreatment;
	}
	/**
	 * @param casesOnTreatment the casesOnTreatment to set
	 */
	public void setCasesOnTreatment(Long casesOnTreatment) {
		this.casesOnTreatment = casesOnTreatment;
	}
	/**
	 * @return the casesTransferIn
	 */
	public Long getCasesTransferIn() {
		return casesTransferIn;
	}
	/**
	 * @param casesTransferIn the casesTransferIn to set
	 */
	public void setCasesTransferIn(Long casesTransferIn) {
		this.casesTransferIn = casesTransferIn;
	}
	/**
	 * @return the casesTransferOut
	 */
	public Long getCasesTransferOut() {
		return casesTransferOut;
	}
	/**
	 * @param casesTransferOut the casesTransferOut to set
	 */
	public void setCasesTransferOut(Long casesTransferOut) {
		this.casesTransferOut = casesTransferOut;
	}
	/**
	 * @return the adminUnitCode
	 */
	public String getAdminUnitCode() {
		return adminUnitCode;
	}
	/**
	 * @param adminUnitCode the adminUnitCode to set
	 */
	public void setAdminUnitCode(String adminUnitCode) {
		this.adminUnitCode = adminUnitCode;
	}
	/**
	 * @return the medExamMissing
	 */
	public Long getMedExamMissing() {
		return medExamMissing;
	}
	/**
	 * @param medExamMissing the medExamMissing to set
	 */
	public void setMedExamMissing(Long medExamMissing) {
		this.medExamMissing = medExamMissing;
	}
	/**
	 * @return the casesNotOnTreatment
	 */
	public Long getCasesNotOnTreatment() {
		return casesNotOnTreatment;
	}
	/**
	 * @param casesNotOnTreatment the casesNotOnTreatment to set
	 */
	public void setCasesNotOnTreatment(Long casesNotOnTreatment) {
		this.casesNotOnTreatment = casesNotOnTreatment;
	}
}
