package org.msh.tb.laboratories;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Component that generates a home report of the total quantities of samples 
 * @author Ricardo Memoria
 *
 */
@Name("sampleHomeReport")
public class SampleHomeReport {

	private List<Item> items;
	private List<String> titles;
	private Integer month;
	private Integer year;
	
	private Date iniDate;
	private Date endDate;
    private UserWorkspace userWorkspace;
	
	@In EntityManager entityManager;
	
	/**
	 * Create the items of the report
	 */
	protected void createItems() {
		items = new ArrayList<Item>();
		initializePeriod();
		for (ExamType type: ExamType.values())
			addExamData(type);
	}
	
	
	/**
	 * Check if month and year were informed, otherwise, initialize them with the current date
	 */
	protected void initializePeriod() {
		Date dt = DateUtils.getDate();
		if (month == null)
			month = DateUtils.monthOf(dt);
		
		if (year == null)
			year = DateUtils.yearOf(dt);
	
//		iniDate = DateUtils.newDate(2000, 1, 1);
		iniDate = DateUtils.newDate(year, month, 1);
		endDate = DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month));
	}

	
	/**
	 * Return the list of status titles
	 * @return
	 */
	public List<String> getStatusTitles() {
		if (titles == null) {
			titles = new ArrayList<String>();
			for (ExamStatus st: ExamStatus.values())
				titles.add(Messages.instance().get(st.getKey()));
		}
		
		return titles;
	}


	/**
	 * Add a new exam to the report
	 * @param type
	 */
	protected void addExamData(ExamType type) {
        if (userWorkspace == null) {
            userWorkspace = UserSession.getUserWorkspace();
        }

		String tableName;

		switch (type) {
		case CULTURE: 
			tableName = "examculture";
			break;
		case MICROSCOPY: 
			tableName = "exammicroscopy";
			break;
		case DST: 
			tableName = "examdst";
			break;
		case XPERT:
			tableName = "examxpert";
			break;
		default: throw new IllegalArgumentException("Exam type is null or not supported");
		}

		String sql = "select a.status, count(*) " + 
				"from " + tableName + " a " + 
				"inner join examrequest b on b.id = a.request_id " +
				"where ((a.status <> :val1) or (a.status = :val2 and a.datecollected between :dt1 and :dt2)) and a.laboratory_id = :labid " +
				"group by a.status";
		

		List<Object[]> lst = entityManager.createNativeQuery(sql)
			.setParameter("dt1", iniDate)
			.setParameter("dt2", endDate)
			.setParameter("val1", ExamStatus.PERFORMED.ordinal())
			.setParameter("val2", ExamStatus.PERFORMED.ordinal())
			.setParameter("labid", userWorkspace.getLaboratory().getId())
			.getResultList();
		
		Item item = new Item(type);
		items.add(item);
		for (Object[] vals: lst) {
			if (vals[0] != null) {
				int index = (Integer)vals[0];
				ExamStatus status = ExamStatus.values()[index];
				Integer count = ((BigInteger)vals[1]).intValue();
				item.addQuantity(status, count);
			}
		}
	}


	
	/**
	 * Move report to the previous month
	 */
	public void previousMonth() {
		if ((month == null) || (year == null))
			return;
		
		month--;
		if (month < 0) {
			month = 11;
			year--;
		}
		items = null;
	}


	/**
	 * Move report to the next month
	 */
	public void nextMonth() {
		if ((month == null) || (year == null))
			return;
		
		month++;
		if (month >= 12) {
			year++;
			month=0;
		}
		items = null;
	}

	
	/**
	 * Return the items that compound the report
	 * @return
	 */
	public List<Item> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	
	public class Item {
		private ExamType type;
		private Integer[] quantities;

		public Item(ExamType type) {
			super();
			this.type = type;
			this.quantities = new Integer[ExamStatus.values().length];
		}

		/**
		 * @return the type
		 */
		public ExamType getType() {
			return type;
		}
		/**
		 * @param type the type to set
		 */
		public void setType(ExamType type) {
			this.type = type;
		}
		
		/**
		 * Add a value to the quantity
		 * @param status
		 * @param count
		 */
		public void addQuantity(ExamStatus status, int count) {
			if (status == null)
				return;
			Integer qtd = quantities[status.ordinal()];
			if (qtd == null)
				qtd = 0;
			quantities[status.ordinal()] = count + qtd;
		}
		
		/**
		 * Return the quantities in an array list
		 * @return
		 */
		public List<Integer> getQuantities() {
			return Arrays.asList(quantities);
		}
		
		/**
		 * @param status
		 * @return
		 */
		public Integer getQuantity(ExamStatus status) {
			return quantities[status.ordinal()];
		}
	}

	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}
}
