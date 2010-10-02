package org.msh.tb.cases.drugogram;

import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamMicroscopy;

public class ExamResult {

	private ExamCulture examCulture;
	private ExamMicroscopy examMicroscopy;


	/**
	 * Check if there is any result
	 * @return true if there is any result, otherwise return false
	 */
	public boolean isHasResults() {
		return ((examCulture != null) || (examMicroscopy != null));
	}
	/**
	 * @return the examCulture
	 */
	public ExamCulture getExamCulture() {
		return examCulture;
	}
	/**
	 * @param examCulture the examCulture to set
	 */
	public void setExamCulture(ExamCulture examCulture) {
		this.examCulture = examCulture;
	}
	/**
	 * @return the examMicroscopy
	 */
	public ExamMicroscopy getExamMicroscopy() {
		return examMicroscopy;
	}
	/**
	 * @param examMicroscopy the examMicroscopy to set
	 */
	public void setExamMicroscopy(ExamMicroscopy examMicroscopy) {
		this.examMicroscopy = examMicroscopy;
	}
}
