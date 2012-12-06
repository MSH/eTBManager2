package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.MedicineDispensing;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.login.UserSession;

/**
 * Display information about dispensing for patients 
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingView")
public class CaseDispensingView {

	private List<DispensingInfo> cases;
	private List<DispensingInfo> dispensingDays;
	private List<DispensingInfo> casesSearch;
	private boolean allCaseDispensing;
	
	private Integer caseId;
	private Integer dispensingId;
	private String patientNameCondition;
	
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
	 * Delete the dispensing of a specific case
	 * @param dispInfo
	 */
	public String deleteDispensing() {
		MedicineDispensing medDisp = entityManager.find(MedicineDispensing.class, dispensingId);
		TbCase tbcase = entityManager.find(TbCase.class, caseId);

		DispensingHome dispensingHome = (DispensingHome)Component.getInstance("dispensingHome", true);
		
		dispensingHome.deleteCaseDispensing(medDisp, tbcase);
		
		return "removed";
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
		boolean considerDate = (dispensingSelection.isMonthYearSelected());
		
		String hql = "from MedicineDispensingCase a " +
			"join fetch a.tbcase join fetch a.dispensing b join fetch a.batch join fetch a.tbcase.patient " +
			"where a.tbcase.id = #{caseHome.id}";

		if (considerDate)
			hql += " and b.dispensingDate between :dt1 and :dt2";
		hql += " order by b.dispensingDate";
		
		List<MedicineDispensingCase> lst = null;

		// it is to return just dispensing data of the given month and year ?
		if (considerDate) 
			lst = entityManager.createQuery(hql)
					.setParameter("dt1", dispensingSelection.getIniDate())
					.setParameter("dt2", dispensingSelection.getEndDate())
					.getResultList();
		else lst = entityManager.createQuery(hql).getResultList();

		dispensingDays = new ArrayList<CaseDispensingView.DispensingInfo>();
		
		// create list of medicine dispensing information
		for (MedicineDispensingCase it: lst) {
			DispensingInfo info = getDayInfo(it.getDispensing().getDispensingDate());
			info.setId(it.getDispensing().getId());
			DispensingRow row = info.getTable().addRow(it.getSource(), it.getBatch());
			row.setQuantity( row.getQuantity() + it.getQuantity() );
		}

		// update layout of the tables
		for (DispensingInfo info: dispensingDays) {
			info.getTable().updateLayout();
		}
	}
	
	/**
	 * Returns a list of all cases that has a dispensing in the context unit.
	 * @author Mauricio Santos
	 *
	 */
	public void updateCasesSearch(){
		Integer patientNumberCond = null;
		List<Object[]> lst;
		UserSession s = (UserSession) Component.getInstance("userSession");
		
		try{
			patientNumberCond = new Integer(patientNameCondition);
		}catch (Exception e){
			patientNumberCond = null;
		}
		
		String sql = "select distinct c.id, p.recordnumber, p.patient_name, c.registrationcode, c.casenumber " +
						"from medicinedispensingcase mdc " +
						"inner join medicinedispensing md on md.id = mdc.dispensing_id " +
						"inner join tbcase c on mdc.CASE_ID=c.id " +
						"inner join patient p on p.id = c.patient_id " +
						"where md.unit_id = :unitId ";
		
		if(patientNameCondition != null && patientNameCondition != "" && !patientNameCondition.isEmpty()){
			sql += "and (p.patient_name like :patientNameCond ";
			if(patientNumberCond != null){
				sql += "or p.recordnumber = :patientNumberCond) order by p.patient_name";
				lst = entityManager.createNativeQuery(sql)
						.setParameter("patientNameCond", getPatientNameCondition())
						.setParameter("patientNumberCond", patientNumberCond)
						.setParameter("unitId", s.getTbunit().getId())
						.getResultList();
			}else{
				sql += ") order by p.patient_name";
				lst = entityManager.createNativeQuery(sql)
						.setParameter("patientNameCond", getPatientNameCondition())
						.setParameter("unitId", s.getTbunit().getId())
						.getResultList();
			}	
		}else{
			sql += "order by p.patient_name";
			lst = entityManager.createNativeQuery(sql)
					.setParameter("unitId", s.getTbunit().getId())
					.getResultList();
		}
		
		fillCaseSearchList(lst);
	}
	
	public void fillCaseSearchList(List<Object[]> lst){
		casesSearch = new ArrayList<CaseDispensingView.DispensingInfo>();
		for(Object[] o : lst){
			DispensingInfo d = new DispensingInfo();
			d.setTbcase(new TbCase());
			d.getTbcase().setId((Integer) o[0]);
			d.getTbcase().setPatient(new Patient());
			d.getTbcase().getPatient().setRecordNumber((Integer)o[1]);
			d.getTbcase().getPatient().setName((String) o[2]);
			d.getTbcase().setRegistrationCode((String) o[3]);
			d.getTbcase().setCaseNumber((Integer) o[4]);
			
			casesSearch.add(d);
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
		private Integer id;
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
		/**
		 * @return the id
		 */
		public Integer getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(Integer id) {
			this.id = id;
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


	/**
	 * @return the caseId
	 */
	public Integer getCaseId() {
		return caseId;
	}


	/**
	 * @param caseId the caseId to set
	 */
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}


	/**
	 * @return the dispensingId
	 */
	public Integer getDispensingId() {
		return dispensingId;
	}


	/**
	 * @param dispensingId the dispensingId to set
	 */
	public void setDispensingId(Integer dispensingId) {
		this.dispensingId = dispensingId;
	}


	/**
	 * @return the patientNameCondition
	 */
	public String getPatientNameCondition() {
		if (patientNameCondition == null)
			return patientNameCondition;
		return '%'+patientNameCondition+'%';
	}


	/**
	 * @param patientNameCondition the patientNameCondition to set
	 */
	public void setPatientNameCondition(String patientNameCondition) {
		this.patientNameCondition = patientNameCondition;
	}


	/**
	 * @return the casesSearch
	 */
	public List<DispensingInfo> getCasesSearch() {
		return casesSearch;
	}


	/**
	 * @param casesSearch the casesSearch to set
	 */
	public void setCasesSearch(List<DispensingInfo> casesSearch) {
		this.casesSearch = casesSearch;
	}

}
