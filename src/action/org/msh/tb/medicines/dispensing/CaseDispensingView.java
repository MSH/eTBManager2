package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.TbCase;

/**
 * Display information about dispensing for patients 
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingView")
public class CaseDispensingView {

	private List<DispensingInfo> cases;
	private List<DispensingInfo> dispensingDays;
	private boolean allCaseDispensing;
	
	@In EntityManager entityManager;
	@In DispensingSelection dispensingSelection;


	/**
	 * Initialize the month and year with the current month/year
	 */
	public void initMonthYear() {
		dispensingSelection.initialize();
	}

	
	/**
	 * Return list of cases and its dispensing quantity in the period
	 * @return
	 */
	public List<DispensingInfo> getCases() {
		if (cases == null)
			createCases();
		return cases;
	}
	
	
	/**
	 * Create the list of cases and its dispensing information in the month
	 */
	protected void createCases() {
		if (!dispensingSelection.isMonthYearSelected())
			return;
		
		List<MedicineDispensingCase> lst = entityManager.createQuery("from MedicineDispensingCase a " +
				"join fetch a.tbcase join fetch a.dispensing b join fetch a.batch join fetch a.tbcase.patient " +
				"where b.dispensingDate between :dt1 and :dt2 and b.tbunit.id = #{userSession.tbunit.id}")
				.setParameter("dt1", dispensingSelection.getIniDate())
				.setParameter("dt2", dispensingSelection.getEndDate())
				.getResultList();

		cases = new ArrayList<CaseDispensingView.DispensingInfo>();

		// create list of medicine dispensing info
		for (MedicineDispensingCase meddisp: lst) {
			DispensingInfo info = getDispensingInfo(meddisp.getTbcase());
			DispensingRow row = info.getTable().addRow(meddisp.getSource(), meddisp.getBatch());
			row.setQuantity( row.getQuantity() + meddisp.getQuantity() );
		}

		// organize the layout of each table
		for (DispensingInfo info: cases) {
			info.getTable().updateLayout();
		}
	}


	/**
	 * Return dispensing information of a case 
	 * @param tbcase
	 * @return
	 */
	private DispensingInfo getDispensingInfo(TbCase tbcase) {
		for (DispensingInfo info: cases) {
			if (info.getTbcase().getId().equals(tbcase.getId()))
				return info;
		}
		
		DispensingInfo info = new DispensingInfo();
		info.setTbcase(tbcase);
		cases.add(info);
		return info;
	}


	/**
	 * Return dispensing information of case for a given date
	 * @param date
	 * @return
	 */
	private DispensingInfo getDayInfo(Date date) {
		for (DispensingInfo it: dispensingDays) {
			if (it.getDate().equals(date))
				return it;
		}
		
		DispensingInfo info = new DispensingInfo();
		info.setDate(date);
		dispensingDays.add(info);
		return info;
	}

	/**
	 * Return the list of medicine dispensing of a specific case  
	 * @return
	 */
	public List<DispensingInfo> getDispensingDays() {
		if (dispensingDays == null)
			createDispensingDays();
		return dispensingDays;
	}


	/**
	 * Create the list of {@link BatchDispensingTable} instances containing dispensing done by day of a case
	 */
	protected void createDispensingDays() {
		if ((!dispensingSelection.isMonthYearSelected()) && (!allCaseDispensing))
			return;

		String hql = "from MedicineDispensingCase a " +
			"join fetch a.tbcase join fetch a.dispensing b join fetch a.batch join fetch a.tbcase.patient " +
			"where a.tbcase.id = #{caseHome.id}";

		if (!allCaseDispensing)
			hql += " and b.dispensingDate between :dt1 and :dt2";
		hql += " order by b.dispensingDate";
		
		List<MedicineDispensingCase> lst = null;

		// it is to return just dispensing data of the given month and year ?
		if (!allCaseDispensing) 
			lst = entityManager.createQuery(hql)
					.setParameter("dt1", dispensingSelection.getIniDate())
					.setParameter("dt2", dispensingSelection.getEndDate())
					.getResultList();
		else lst = entityManager.createQuery(hql).getResultList();

		dispensingDays = new ArrayList<CaseDispensingView.DispensingInfo>();
		
		// create list of medicine dispensing information
		for (MedicineDispensingCase it: lst) {
			DispensingInfo info = getDayInfo(it.getDispensing().getDispensingDate());
			DispensingRow row = info.getTable().addRow(it.getSource(), it.getBatch());
			row.setQuantity( row.getQuantity() + it.getQuantity() );
		}

		// update layout of the tables
		for (DispensingInfo info: dispensingDays) {
			info.getTable().updateLayout();
		}
	}

	
	/**
	 * Hold information about cases and dispensing done
	 * @author Ricardo Memoria
	 *
	 */
	public class DispensingInfo {
		private TbCase tbcase;
		private Date date;
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
		/**
		 * @return the date
		 */
		public Date getDate() {
			return date;
		}
		/**
		 * @param date the date to set
		 */
		public void setDate(Date date) {
			this.date = date;
		}
	}
	
	/**
	 * @return the allCaseDispensing
	 */
	public boolean isAllCaseDispensing() {
		return allCaseDispensing;
	}


	/**
	 * @param allCaseDispensing the allCaseDispensing to set
	 */
	public void setAllCaseDispensing(boolean allCaseDispensing) {
		this.allCaseDispensing = allCaseDispensing;
	}
}
