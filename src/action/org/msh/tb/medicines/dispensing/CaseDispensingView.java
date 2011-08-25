package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.TbCase;
import org.msh.utils.date.DateUtils;

/**
 * Display information about dispensing for patients 
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingView")
public class CaseDispensingView {

	private Integer month;
	private Integer year;
	private List<CaseInfo> cases;
	
	@In EntityManager entityManager;


	/**
	 * Initialize the month and year with the current month/year
	 */
	public void initMonthYear() {
		Calendar c = Calendar.getInstance();
		month = c.get(Calendar.MONTH);
		year = c.get(Calendar.YEAR);
	}

	
	/**
	 * Return list of cases and its dispensing quantity in the period
	 * @return
	 */
	public List<CaseInfo> getCases() {
		if (cases == null)
			createCases();
		return cases;
	}
	
	
	/**
	 * Create the list of cases and its dispensing information in the month
	 */
	protected void createCases() {
		if ((month == null) || (year == null))
			return;
		
		Date dtini = DateUtils.newDate(year, month, 1);
		Date dtend = DateUtils.newDate(year, month, DateUtils.daysInAMonth(year, month));
		
		List<MedicineDispensingCase> lst = entityManager.createQuery("from MedicineDispensingCase a " +
				"join fetch a.tbcase join fetch a.dispensing b join fetch a.batch join fetch a.tbcase.patient " +
				"where b.dispensingDate between :dt1 and :dt2")
				.setParameter("dt1", dtini)
				.setParameter("dt2", dtend)
				.getResultList();

		cases = new ArrayList<CaseDispensingView.CaseInfo>();

		// create list of medicine dispensing info
		for (MedicineDispensingCase meddisp: lst) {
			CaseInfo info = getCaseInfo(meddisp.getTbcase());
			DispensingRow row = info.getTable().addRow(meddisp.getSource(), meddisp.getBatch());
			row.setQuantity( row.getQuantity() + meddisp.getQuantity() );
		}

		// organize the layout of each table
		for (CaseInfo info: cases) {
			info.getTable().updateLayout();
		}
	}


	/**
	 * Return dispensing information of a case 
	 * @param tbcase
	 * @return
	 */
	private CaseInfo getCaseInfo(TbCase tbcase) {
		for (CaseInfo info: cases) {
			if (info.getTbcase().getId().equals(tbcase.getId()))
				return info;
		}
		
		CaseInfo info = new CaseInfo();
		info.setTbcase(tbcase);
		cases.add(info);
		return info;
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
	
	/**
	 * Hold information about cases and dispensing done
	 * @author Ricardo Memoria
	 *
	 */
	public class CaseInfo {
		private TbCase tbcase;
		private BatchDispensingTable table = new BatchDispensingTable();
		/**
		 * @return the tbcase
		 */
		public TbCase getTbcase() {
			return tbcase;
		}
		/**
		 * @param tbcase the tbcase to set
		 */
		public void setTbcase(TbCase tbcase) {
			this.tbcase = tbcase;
		}
		/**
		 * @return the table
		 */
		public BatchDispensingTable getTable() {
			return table;
		}
		/**
		 * @param table the table to set
		 */
		public void setTable(BatchDispensingTable table) {
			this.table = table;
		}
	}
}
