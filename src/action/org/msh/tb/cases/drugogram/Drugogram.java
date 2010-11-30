package org.msh.tb.cases.drugogram;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamMicroscopy;
import org.msh.mdrtb.entities.ExamXRay;
import org.msh.mdrtb.entities.LocalizedNameComp;
import org.msh.mdrtb.entities.MedicalExamination;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.MedicineComponent;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.utils.date.DateUtils;


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
	private List<String> medicines;
	
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
		List<Object[]> lst = entityManager.createQuery("select res.result, res.substance.abbrevName, exam.dateCollected " +
				"from ExamDSTResult res " +
				"join res.exam exam " +
				"where exam.tbcase.id = :id")
				.setParameter("id", tbcase.getId())
				.getResultList();
		
		for (Object[] vals: lst) {
			Date dt = (Date)vals[2];
			DstResult res = (DstResult)vals[0];
			String subname = ((LocalizedNameComp)vals[1]).toString();
			
			DrugogramItem item = findItemByDate(dt);
			SubstanceItem subitem = item.findSubstance(subname);
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
				
//				item.setMonthTreatment(tbcase.getMonthTreatment(dt));
				
				Medicine med = pm.getMedicine();
				List<String> subs = getSubstancesAbbrevName(med);
				for (String subName: subs) {
					SubstanceItem sub = item.findSubstance(subName);
					sub.setPrescribed(true);						
				}
				dt = DateUtils.incMonths(dt, 1);
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
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		
		for (DrugogramItem item: items) {
			if ((item.getMonth() == month) && (item.getYear() == year)) {
				return item;
			}
		}
		
		DrugogramItem item = new DrugogramItem();
		item.setDrugogram(this);
		item.setMonth(month);
		item.setYear(year);
		item.setMonthTreatment(tbcase.getMonthTreatment(date));
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


	public List<String> getMedicines() {
		if (medicines == null) {
			medicines = new ArrayList<String>();
			for (DrugogramItem item: getItems()) {
				for (SubstanceItem sub: item.getSubstances()) {
					if (!medicines.contains(sub.getMedicine()))
						medicines.add(sub.getMedicine());
				}
			}
			Collections.sort(medicines);
		}
		return medicines;
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
}
