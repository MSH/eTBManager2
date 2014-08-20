package org.msh.tb.laboratories;

/**
 * Enumeration of exams performed by an identified laboratory in the system
 * @author Ricardo Memoria
 *
 */
public enum ExamType {

	MICROSCOPY ("cases.exammicroscopy"), 
	CULTURE ("cases.examculture"), 
	XPERT("cases.examxpert"),
	DST ("cases.examdst"); 
	
	private String key;

	private ExamType(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}

}