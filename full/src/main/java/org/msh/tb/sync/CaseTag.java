/**
 * 
 */
package org.msh.tb.sync;

/**
 * @author Ricardo Memoria
 *
 */
public class CaseTag {

	private int caseId;
	private int tagId;

	public CaseTag(int caseId, int tagId) {
		super();
		this.caseId = caseId;
		this.tagId = tagId;
	}
	
	/**
	 * @return the caseId
	 */
	public int getCaseId() {
		return caseId;
	}
	/**
	 * @param caseId the caseId to set
	 */
	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}
	/**
	 * @return the tagId
	 */
	public int getTagId() {
		return tagId;
	}
	/**
	 * @param tagId the tagId to set
	 */
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
}
