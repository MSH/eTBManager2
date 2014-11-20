package org.msh.tb.cases.treatment;

import org.msh.utils.date.Period;

import java.util.Date;

/**
 * Abstract class that contains information about a prescription period in the patient's treatment
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public class TreatmentPeriod<E> {

	private PrescriptionTable table;
	private E item;

	private int width;
	private int left;
	private Period period;
	

	/**
	 * Return an instance of the {@link Period} class containing the period of the treatment
	 * @return
	 */
	public Period getPeriod() {
		return period;
	}
	
	
	/**
	 * Return the initial date of the period
	 * @return
	 */
	public Date getIniDate() {
		return getPeriod().getIniDate();
	}
	
	/**
	 * Return the end date of the period
	 * @return
	 */
	public Date getEndDate() {
		return getPeriod().getEndDate();
	}


	/**
	 * Expand the end date making sure the period is inside the prescription table period
	 * @param newEndDate
	 */
	public void expandEndDate(Date newEndDate) {
		if (newEndDate.after(table.getPeriod().getEndDate()))
			 getPeriod().setEndDate(table.getPeriod().getEndDate());
		else getPeriod().setEndDate(newEndDate);
	}


	public TreatmentPeriod(PrescriptionTable table, E item, Period p) {
		super();
		this.table = table;
		this.item = item;

		// create a new period and intersects with the treatment period
		this.period = new Period(p);
		this.period.intersect(table.getPeriod());
	}

	public PrescriptionTable getTable() {
		return table;
	}


	public E getItem() {
		return item;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

}
