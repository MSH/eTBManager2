package org.msh.tb.test.dbgen;

import org.msh.mdrtb.entities.AgeRange;

/**
 * Store information about are range generation preferences
 * @author Ricardo Memoria
 *
 */
public class AgeRangeInfo implements BeanSerialize {

	private AgeRange range;
	private RangeValue weightRange = new RangeValue();
	private RangeValue heigthRange = new RangeValue();


	/**
	 * @return the range
	 */
	public AgeRange getRange() {
		return range;
	}
	/**
	 * @param range the range to set
	 */
	public void setRange(AgeRange range) {
		this.range = range;
	}
	/**
	 * @return the weightRange
	 */
	public RangeValue getWeightRange() {
		return weightRange;
	}
	/**
	 * @param weightRange the weightRange to set
	 */
	public void setWeightRange(RangeValue weightRange) {
		this.weightRange = weightRange;
	}
	/**
	 * @return the heigthRange
	 */
	public RangeValue getHeigthRange() {
		return heigthRange;
	}
	/**
	 * @param heigthRange the heigthRange to set
	 */
	public void setHeigthRange(RangeValue heigthRange) {
		this.heigthRange = heigthRange;
	}
}
