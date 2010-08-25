package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.estimating.CaseInfo;
import org.msh.tb.medicines.estimating.MedicineEstimation;
import org.msh.tb.medicines.estimating.MedicineQuantity;



/**
 * Generates report about treatment cost by case
 * @author Ricardo Memoria
 *
 */
@Name("treatmentCostReport")
public class TreatmentCostReport {

	@In(create=true) ReportSelection reportSelection;
	@In(create=true) EntityManager entityManager;
	@In(create=true) MedicineEstimation medicineEstimation;
	@In(create=true) UserSession userSession;

	private List<Medicine> medicines;
	private List<Item> itens;
	private List<MedicineUnitPrice> prices;
	private Item total;
	private boolean executing;

	/**
	 * Cria relatorio
	 */
	public void createReport() {
		executing = true;
		
		restoreMedicineUnitPrice();
		
		medicineEstimation.setDetailedByPatient(true);
		medicineEstimation.setIniDate(reportSelection.getIniDate());
		medicineEstimation.setEndDate(reportSelection.getEndDate());
		medicineEstimation.setSource(reportSelection.getSource());
		medicineEstimation.setTbunit(userSession.getTbunit());
		medicineEstimation.setDetailedByPatient(true);

		
		medicineEstimation.calcEstimatedConsumption();
		
		List<MedicineQuantity> quantities = medicineEstimation.getQuantities();
		if (quantities == null)
			return;
		
		mountMedicineList(quantities);
		
		itens = new ArrayList<Item>();
		
		total = new Item();
		for (int i=0; i<medicines.size(); i++)
			total.prices.add(new Double(0));
		
		// monta relatorio
		for (CaseInfo caseInfo: medicineEstimation.getCases()) {
			/*Integer patientId = (Integer)obj[0];
			String patientName = (String)obj[1];
			CaseState state = (CaseState)obj[2];
			String p = medicineName(obj);
			Double price = (Double)obj[4];
			Date iniDate = (Date)obj[8];
			Date endDate = (Date)obj[9];
			Integer pacNum = (Integer)obj[10];
			Integer caseNum = (Integer)obj[11];
			*/

			TbCase tbcase = caseInfo.getTbcase();
			String caseNumber = tbcase.getRegistrationCode();
			
			Item it = itemByPatient(tbcase.getId(), tbcase.getPatient().getName(), tbcase.getState());
			
			it.setCaseNumber(caseNumber);
			it.setIniTreatmentDate(tbcase.getTreatmentPeriod().getIniDate());
			it.setEndTreatmentDate(tbcase.getTreatmentPeriod().getEndDate());

			for (MedicineQuantity medqtd: caseInfo.getQuantities()) {
				int index = medicines.indexOf(medqtd.getMedicine());
				
				Double unitPrice = 1D;
				Double price = unitPrice * medqtd.getQtyEstimated();
				
				it.getPrices().set(index, price);
				total.getPrices().set(index, total.getPrices().get(index) + price);				
			}
		}
	}


	protected String medicineName(Object[] obj) {
		return (String)obj[3];
//		return (String)obj[3] + " " + (String)obj[5] + (String)obj[6] + " (" + (String)obj[7] + ")";
	}

	protected Item itemByPatient(Integer caseId, String name, CaseState state) {
		for (Item it: itens) {
			if (it.getCaseId().equals(caseId))
				return it;
		}
		
		Item it = new Item();
		it.setPatientName(name);
		it.setCaseId(caseId);
		it.setState(state);
		for (int i=0; i < medicines.size(); i++)
			it.getPrices().add(new Double(0));
		itens.add(it);
		
		return it;
	}



	/**
	 * Create a list of medicine unit prices by month, to be used by the report
	 */
	private void restoreMedicineUnitPrice() {
		Source source = reportSelection.getSource();

		String hql = "select item.medicine.id, month(disp.iniDate), avg(b.batch.unitPrice) " +
					"from MedicineDispensingBatch b " + 
					"inner join b.item item " + 
					"inner join item.dispensing disp " +
					"where disp.iniDate >= :dtini and disp.iniDate <= :dtend " +
					(source != null? " and item.source.id = " + source.getId().toString(): "") +
					" group by item.medicine.id, month(disp.iniDate)";

		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("dtini", reportSelection.getIniDate())
			.setParameter("dtend", reportSelection.getEndDate())
			.getResultList();

		prices = new ArrayList<MedicineUnitPrice>();
		for (Object[] vals: lst) {
			MedicineUnitPrice item = new MedicineUnitPrice();
			item.setMedicineId((Integer)vals[0]);
			item.setMonth((Integer)vals[1]);
			item.setUnitPrice((Double)vals[2]);
			
			prices.add(item);
		}
	}

/*	private Double getMedicineUnitPrice(int medicineId, int month) {
		for (MedicineUnitPrice aux: prices) {
			if ((aux.getMedicineId() == medicineId) && (aux.getMonth() == month))
				return aux.getUnitPrice();
		}
		return 0D;
	}
*/	
	/**
	 * Monta a lista de produtos
	 * @param lst
	 */
	private void mountMedicineList(List<MedicineQuantity> lst) {
		medicines = new ArrayList<Medicine>();
		for (MedicineQuantity mq: lst) {
			medicines.add(mq.getMedicine());
		}
	}

	public Item getTotal() {
		if (itens == null)
			createReport();
		return total;
	}
	
	public List<Item> getItens() {
		if (itens == null)
			createReport();
		return itens;
	}
	
	public List<Medicine> getMedicines() {
		if (medicines == null)
			createReport();
		return medicines;
	}

	public String getWidths() {
		String s = "1.2";
		
		for (int i = 0; i < getMedicines().size() + 1; i++) {
			s += " .6";
		}
		
		return s;
	}

	public boolean isExecuting() {
		return executing;
	}
	

	public class Item {
		private Integer caseId;
		private String patientName;
		private CaseState state;
		private Date iniTreatmentDate;
		private Date endTreatmentDate;
		private String caseNumber;
		/**
		 * @return the iniTreatmentDate
		 */
		public Date getIniTreatmentDate() {
			return iniTreatmentDate;
		}
		/**
		 * @param iniTreatmentDate the iniTreatmentDate to set
		 */
		public void setIniTreatmentDate(Date iniTreatmentDate) {
			this.iniTreatmentDate = iniTreatmentDate;
		}
		/**
		 * @return the endTreatmentDate
		 */
		public Date getEndTreatmentDate() {
			return endTreatmentDate;
		}
		/**
		 * @param endTreatmentDate the endTreatmentDate to set
		 */
		public void setEndTreatmentDate(Date endTreatmentDate) {
			this.endTreatmentDate = endTreatmentDate;
		}
		/**
		 * @return the caseNumber
		 */
		public String getCaseNumber() {
			return caseNumber;
		}
		/**
		 * @param caseNumber the caseNumber to set
		 */
		public void setCaseNumber(String caseNumber) {
			this.caseNumber = caseNumber;
		}
		private List<Double> prices = new ArrayList<Double>();
		public String getPatientName() {
			return patientName;
		}
		public void setPatientName(String patient) {
			this.patientName = patient;
		}
		public List<Double> getPrices() {
			return prices;
		}
		public void setPrices(List<Double> prices) {
			this.prices = prices;
		}
		public double getTotal() {
			double total = 0;
			for (double val: prices) {
				total += val;
			}
			return total;
		}
		/**
		 * @param state the state to set
		 */
		public void setState(CaseState state) {
			this.state = state;
		}
		/**
		 * @return the state
		 */
		public CaseState getState() {
			return state;
		}
		/**
		 * @param caseId the caseId to set
		 */
		public void setCaseId(Integer caseId) {
			this.caseId = caseId;
		}
		/**
		 * @return the caseId
		 */
		public Integer getCaseId() {
			return caseId;
		}
	}

	
	/**
	 * Store information about 
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineUnitPrice {
		private int medicineId;
		private Double unitPrice;
		private int month;
		
		/**
		 * @return the medicineId
		 */
		public int getMedicineId() {
			return medicineId;
		}
		/**
		 * @param medicineId the medicineId to set
		 */
		public void setMedicineId(int medicineId) {
			this.medicineId = medicineId;
		}
		/**
		 * @return the unitPrice
		 */
		public Double getUnitPrice() {
			return unitPrice;
		}
		/**
		 * @param unitPrice the unitPrice to set
		 */
		public void setUnitPrice(Double unitPrice) {
			this.unitPrice = unitPrice;
		}
		/**
		 * @return the month
		 */
		public int getMonth() {
			return month;
		}
		/**
		 * @param month the month to set
		 */
		public void setMonth(int month) {
			this.month = month;
		}
	}
}
