package org.msh.tb.ua;

import org.msh.tb.cases.HealthUnitInfo;
/**
 * Add UA specific counters to the health unit information
 * @author alexey
 *
 */
public class HealthUnitInfoUA extends HealthUnitInfo {
	private Long casesNoCured;
	private Long casesNeedClosed;
	private Long casesOther;
	private Long casesHere;
	/**
	 * @return the casesNoCured
	 */
	public Long getCasesNoCured() {
		return casesNoCured;
	}
	/**
	 * @param casesNoCured the casesNoCured to set
	 */
	public void setCasesNoCured(Long casesNoCured) {
		this.casesNoCured = casesNoCured;
	}
	/**
	 * @return the casesNeedClosed
	 */
	public Long getCasesNeedClosed() {
		return casesNeedClosed;
	}
	/**
	 * @param casesNeedClosed the casesNeedClosed to set
	 */
	public void setCasesNeedClosed(Long casesNeedClosed) {
		this.casesNeedClosed = casesNeedClosed;
	}
	/**
	 * @return the casesOther
	 */
	public Long getCasesOther() {
		return casesOther;
	}
	/**
	 * @param casesOther the casesOther to set
	 */
	public void setCasesOther(Long casesOther) {
		this.casesOther = casesOther;
	}
	/**
	 * @return the casesHere
	 */
	public Long getCasesHere() {
		return casesHere;
	}
	/**
	 * @param casesHere the casesHere to set
	 */
	public void setCasesHere(Long casesHere) {
		this.casesHere = casesHere;
	}
	

	
	
}
