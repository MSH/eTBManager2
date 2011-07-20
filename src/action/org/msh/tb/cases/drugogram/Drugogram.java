package org.msh.tb.cases.drugogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DstResult;
import org.msh.utils.date.DateUtils;


/**
 * Display the drugogram of a case, i.e, a resume of the case displaying in a single table the
 * treatment time line, the list of medicines prescribed, microscopy results, culture results and DST results
 * @author Ricardo Memoria
 *
 */
@Name("drugogram")
public class Drugogram {

	@In(required=true) CaseHome caseHome;
	@In EntityManager entityManager;
	@In(create=true) ExamCultureHome examCultureHome;
	@In(create=true) ExamMicroscopyHome examMicroscopyHome;
	@In(create=true) ExamDSTHome examDSTHome;
	
	private TbCase tbcase;

	private List<DrugogramItem> items;
	private int numExams;

	private List<String> examLabels;
	private List<MedicineColumn> medicines;
	
	private boolean includeXRay;
	private boolean includeMedicalExamination;
	
	private List<MedicineComponent> medicineComponents;


	/**
	 * Return the rows of the drugogram report
	 * @return
	 */
	public List<DrugogramItem> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	
	/**
	 * Create the drugogram report
	 */
	protected void createItems() {
		items = new ArrayList<DrugogramItem>();
		tbcase = caseHome.getInstance();
		createExamResults();
		createRegimenTable();
		createDSTResults();
		
		if (includeXRay)
			addXRayResults();
		
		if (includeMedicalExamination)
			addMedicalExaminations();

		// sort items
		Collections.sort(items, new Comparator<DrugogramItem>() {
			public int compare(DrugogramItem o1, DrugogramItem o2) {
				if (o1.getMonthTreatment() < o2.getMonthTreatment())
					return -1;
				else
				if (o1.getMonthTreatment() > o2.getMonthTreatment())
					return 1;
				else return 0;
			}
		});
		
		// adjust number of exam results per row
		for (DrugogramItem item: items) {
			while (item.getResults().size() < numExams)
				item.newResult();
		}
	}

	
	/**
	 * Create the list of exam results
	 */
	protected void createExamResults() {
		DrugogramItem item;
		
		numExams = 0;
		
		for (ExamCulture examCulture: examCultureHome.getAllResults()) {
			if (examCulture != null) {
				Date dt = examCulture.getDateCollected();
				item = findItemByDate(dt);
				item.getFreeResultCulture().setExamCulture(examCulture);
				
				// calculate the number of exams by row
				if (item.getResults().size() > numExams)
					numExams = item.getResults().size();
				item.checkDateCollected(dt);
				if (item.getSpecimenId() == null)
					item.setSpecimenId(examCulture.getSampleNumber());
			}
		}
		
		for (ExamMicroscopy examMicroscopy: examMicroscopyHome.getAllResults()) {
			if (examMicroscopy != null) {
				Date dt = examMicroscopy.getDateCollected();
				item = findItemByDate(dt);
				item.getFreeResultMicroscopy().setExamMicroscopy(examMicroscopy);

				if (item.getResults().size() > numExams)
					numExams = item.getResults().size();
				item.checkDateCollected(dt);
				if (item.getSpecimenId() == null)
					item.setSpecimenId(examMicroscopy.getSampleNumber());
			}
		}
	}


	/**
	 * Fill the drugogram with DST results
	 */
	protected void createDSTResults() {
		List<Object[]> lst = entityManager.createQuery("select res.result, res.substance, exam.dateCollected " +
				"from ExamDSTResult res " +
				"join res.exam exam " +
				"where exam.tbcase.id = :id")
				.setParameter("id", tbcase.getId())
				.getResultList();
		
		for (Object[] vals: lst) {
			Date dt = (Date)vals[2];
			DstResult res = (DstResult)vals[0];
			Substance sub = (Substance)vals[1];
			
			DrugogramItem item = findItemByDate(dt);
			SubstanceItem subitem = item.findSubstance(sub.getAbbrevName().toString());
			subitem.setSubstance(sub);
			subitem.setDstResult(res);
		}
	}


	/**
	 * Include XRay exam results 
	 */
	protected void addXRayResults() {
		List<ExamXRay> lst = entityManager.createQuery("from ExamXRay exa " +
				"join fetch exa.presentation " +
				"where exa.tbcase.id = :id " +
				"order by exa.date")
				.setParameter("id", tbcase.getId())
				.getResultList();
		
		for (ExamXRay exam: lst) {
			DrugogramItem item = findItemByDate(exam.getDate());
			item.setExamXRay(exam);
			if (item.getDateCollected() == null)
				item.setDateCollected(exam.getDate());
		}
	}

	
	/**
	 * Include medical examinations results
	 */
	protected void addMedicalExaminations() {
		List<MedicalExamination> lst = entityManager.createQuery("from MedicalExamination med " +
				"where med.tbcase.id = :id order by med.date")
				.setParameter("id", tbcase.getId())
				.getResultList();
		
		for (MedicalExamination med: lst) {
			DrugogramItem item = findItemByDate(med.getDate());
			item.setMedicalExamination(med);
			if (item.getDateCollected() == null)
				item.setDateCollected(med.getDate());
		}
	}

	/**
	 * Create the table of regimen
	 */
	protected void createRegimenTable() {
		createMedicineComponents();

		for (PrescribedMedicine pm: tbcase.getPrescribedMedicines()) {
			Date dt = pm.getPeriod().getIniDate();
			Date dtEnd = pm.getPeriod().getEndDate();
			
			while (dt.before(dtEnd)) {
				DrugogramItem item = findItemByDate(dt);
				
				Medicine med = pm.getMedicine();
				
				// medicine has substances assigned to
				if (med.getComponents().size() > 0) {
					for (MedicineComponent medcomp: med.getComponents()) {
						SubstanceItem sub = item.findSubstance(medcomp.getSubstance().getAbbrevName().toString());
						sub.setPrescribed(true);
						sub.setSubstance(medcomp.getSubstance());
					}
				}
				else {
					item.findSubstance(med.getAbbrevName()).setPrescribed(true);
				}
/*				List<String> subs = getSubstancesAbbrevName(med);
				for (String subName: subs) {
					SubstanceItem sub = item.findSubstance(subName);
					sub.setPrescribed(true);						
				}
*/				dt = DateUtils.incMonths(dt, 1);
			}
		}
	}
	
	
	/**
	 * Search for substances that compound a given medicine
	 * @param med - medicine to list the substances
	 * @return list of abbreviated name of the substances
	 */
	protected List<String> getSubstancesAbbrevName(Medicine med) {
		List<String> subs = new ArrayList<String>();
		
		for (MedicineComponent comp: medicineComponents) {
			if (comp.getMedicine().equals(med)) {
				subs.add(comp.getSubstance().getAbbrevName().toString());
			}
		}
		
		if (subs.size() == 0)
			subs.add(med.getAbbrevName());

		return subs;
	}
	
	/**
	 * Create list of medicine components of the medicines prescribed to the case
	 */
	protected void createMedicineComponents() {
		if (medicineComponents != null)
			return;
		
		String hql = "from MedicineComponent mc " +
				"join fetch mc.substance s " +
				"where mc.medicine.id in (select pm.medicine.id " +
				"from PrescribedMedicine pm " +
				"where pm.tbcase.id = :id)";
		
		medicineComponents = entityManager.createQuery(hql)
			.setParameter("id", tbcase.getId())
			.getResultList();
	}


	/**
	 * Find a item of the drugogram by the date
	 * @param date
	 * @return instance of the {@link DrugogramItem} class
	 */
	protected DrugogramItem findItemByDate(Date date) {
		int monthTreatment = tbcase.getMonthTreatment(date);
		for (DrugogramItem item: items) {
			if (item.getMonthTreatment() == monthTreatment) {
				return item;
			}
		}
		
		DrugogramItem item = new DrugogramItem();
		item.setDrugogram(this);
		item.setMonthTreatment(monthTreatment);
		items.add(item);
		
		return item;
	}

	
	
	/**
	 * Return the maximum number of exams per row
	 * @return
	 */
	public int getNumExams() {
		return numExams;
	}
	
	
	public List<String> getExamLabels() {
		if ((examLabels == null) || (examLabels.size() < numExams)) {
			examLabels = new ArrayList<String>();
			// called to create the items
			getItems();
			Map<String, String> messages = Messages.instance();
			String tit = messages.get("cases.details.report1.tit");
			for (int i = 1; i <= numExams; i++) {
				String s = tit + " " + Integer.toString(i);
				examLabels.add(s);
			}
		}
		return examLabels;
	}


	public List<MedicineColumn> getMedicines() {
		if (medicines == null) {
			medicines = new ArrayList<MedicineColumn>();
			for (DrugogramItem item: getItems()) {
				for (SubstanceItem sub: item.getSubstances()) {
					if (!isSubstanceInList(medicines, sub.getMedicine()))
						medicines.add(new MedicineColumn(sub.getMedicine(), sub.getSubstance()));
				}
			}
			
			// sort list of substances
			Collections.sort(medicines, new Comparator<MedicineColumn>() {
				public int compare(MedicineColumn c1, MedicineColumn c2) {
					Substance s1 = c1.getSubstance();
					Substance s2 = c2.getSubstance();
					Integer order1 = (s1 != null && s1.getPrevTreatmentOrder() != null ? s1.getPrevTreatmentOrder(): 0);
					Integer order2 = (s2 != null && s2.getPrevTreatmentOrder() != null ? s2.getPrevTreatmentOrder(): 0);

					return order1.compareTo(order2);
				}
			});
		}
		return medicines;
	}
	
	
	private boolean isSubstanceInList(List<MedicineColumn> lst, String subName) {
		for (MedicineColumn col: lst) {
			if (col.getMedicine().equals(subName)) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Return the TB case of the drugogram
	 * @return {@link TbCase} instance
	 */
	public TbCase getTbcase() {
		return tbcase;
	}
	
	public boolean isIncludeXRay() {
		return includeXRay;
	}


	public void setIncludeXRay(boolean includeXRay) {
		this.includeXRay = includeXRay;
	}


	public boolean isIncludeMedicalExamination() {
		return includeMedicalExamination;
	}


	public void setIncludeMedicalExamination(boolean includeMedicalExamination) {
		this.includeMedicalExamination = includeMedicalExamination;
	}
	

	public class MedicineColumn {
		public MedicineColumn(String medicine, Substance substance) {
			super();
			this.medicine = medicine;
			this.substance = substance;
		}

		@Override
		public String toString() {
			return medicine;
		}
		
		private String medicine;
		private Substance substance;
		
		/**
		 * @return the medicine
		 */
		public String getMedicine() {
			return medicine;
		}
		/**
		 * @param medicine the medicine to set
		 */
		public void setMedicine(String medicine) {
			this.medicine = medicine;
		}
		/**
		 * @return the substance
		 */
		public Substance getSubstance() {
			return substance;
		}
		/**
		 * @param substance the substance to set
		 */
		public void setSubstance(Substance substance) {
			this.substance = substance;
		}
	}
}
