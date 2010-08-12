package org.msh.tb.cases.drugogram;

import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamSputumSmear;

public class ExamResult {

	private ExamCulture examCulture;
	private ExamSputumSmear examSputum;


	/**
	 * Check if there is any result
	 * @return true if there is any result, otherwise return false
	 */
	public boolean isHasResults() {
		return ((examCulture != null) || (examSputum != null));
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
	 * @return the examSputum
	 */
	public ExamSputumSmear getExamSputum() {
		return examSputum;
	}
	/**
	 * @param examSputum the examSputum to set
	 */
	public void setExamSputum(ExamSputumSmear examSputum) {
		this.examSputum = examSputum;
	}
}
