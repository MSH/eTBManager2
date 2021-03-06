package org.msh.tb.webservices;

import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;

import java.util.Date;

/**
 * Store information about an XPert result send from an external application
 * thought web-services
 * @author Ricardo Memoria
 *
 */
public class XpertData {

	private Integer caseId;
	private String sampleId;
	private Date sampleDateCollected;
	private Date releaseDate;
	private Integer laboratoryId;
	private XpertResult result;
	private XpertRifResult rifResult;
	private String comments;

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
	/**
	 * @return the sampleId
	 */
	public String getSampleId() {
		return sampleId;
	}
	/**
	 * @param sampleId the sampleId to set
	 */
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	/**
	 * @return the releaseDate
	 */
	public Date getReleaseDate() {
		return releaseDate;
	}
	/**
	 * @param releaseDate the releaseDate to set
	 */
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	/**
	 * @return the laboratoryId
	 */
	public Integer getLaboratoryId() {
		return laboratoryId;
	}
	/**
	 * @param laboratoryId the laboratoryId to set
	 */
	public void setLaboratoryId(Integer laboratoryId) {
		this.laboratoryId = laboratoryId;
	}
	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return the sampleDateCollected
	 */
	public Date getSampleDateCollected() {
		return sampleDateCollected;
	}
	/**
	 * @param sampleDateCollected the sampleDateCollected to set
	 */
	public void setSampleDateCollected(Date sampleDateCollected) {
		this.sampleDateCollected = sampleDateCollected;
	}
	/**
	 * @return the result
	 */
	public XpertResult getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(XpertResult result) {
		this.result = result;
	}
	/**
	 * @return the rifResult
	 */
	public XpertRifResult getRifResult() {
		return rifResult;
	}
	/**
	 * @param rifResult the rifResult to set
	 */
	public void setRifResult(XpertRifResult rifResult) {
		this.rifResult = rifResult;
	}
}
