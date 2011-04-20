package org.msh.tb.na;

import java.sql.Date;
import java.util.Calendar;

import org.msh.utils.date.DateUtils;

public class DateTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("date == "+DateUtils.FormatDateTime("MM", Calendar.getInstance().getTime()));

	}

}
