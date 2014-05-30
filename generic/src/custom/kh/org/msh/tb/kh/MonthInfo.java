package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.msh.utils.date.DateUtils;



/**
 * Stores information about the treatment in a specific month of the treatment 
 * @author Ricardo Lima
 *
 */
public class MonthInfo extends org.msh.tb.cases.treatment.MonthInfo{

	private int weight;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	
}
