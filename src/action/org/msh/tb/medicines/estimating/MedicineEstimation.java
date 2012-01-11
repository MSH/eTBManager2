package org.msh.tb.medicines.estimating;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.Period;

@Name("medicineEstimation")
public class MedicineEstimation {

	private Date iniDate;
	private Date endDate;
	private Source source;
	private Tbunit tbunit;
	private boolean detailedByPatient;
	
	private List<MedicineQuantity> quantities;
	private List<Medicine> medicines;
	private List<CaseInfo> cases;
	
	@In EntityManager entityManager;
	
	
	/**
	 * Calculate estimated consumption
	 */
	public void calcEstimatedConsumption() {
		if ((iniDate == null) || (endDate == null) || (tbunit == null))
			return;
		
		if (quantities != null)
			return;
		
		quantities = new ArrayList<MedicineQuantity>();
		loadPrescriptions();

		for (CaseInfo caseInfo: cases) {
			for (PrescribedMedicine presc: caseInfo.getPrescriptions()) {
				int qtd = presc.calcEstimatedDispensing(new Period(iniDate, endDate));
				MedicineQuantity mq = findMedicineQuantity(quantities, presc.getMedicine().getId());
				mq.setQtyEstimated(mq.getQtyEstimated() + qtd);

				if (detailedByPatient) {
					mq = findMedicineQuantity(caseInfo.getQuantities(), presc.getMedicine().getId());
					mq.setQtyEstimated(mq.getQtyEstimated() + qtd);
				}
			}
		}
	}
	
	protected CaseInfo findCase(int caseId) {
		for (CaseInfo caseInfo: cases) {
			if (caseInfo.getTbcase().getId().equals(caseId))
				return caseInfo;
		}
		return null;
	}
	
	protected MedicineQuantity findMedicineQuantity(List<MedicineQuantity> meds, Integer medicineId) {
		for (MedicineQuantity med: meds) {
			if (med.getMedicine().getId().equals(medicineId))
				return med;
		}
		
		MedicineQuantity mq = new MedicineQuantity();
		mq.setMedicine( getMedicineById(medicineId));
		meds.add(mq);
		
		return mq;
	}


	/**
	 * Load prescription information 
	 */
	protected void loadPrescriptions() {
		if (cases != null)
			return;
		
		medicines = entityManager
			.createQuery("from Medicine m where m.workspace.id = #{defaultWorkspace.id}")
			.getResultList();
		
		String hql = "select presc.medicine.id, presc.doseUnit, presc.frequency, presc.period.iniDate, presc.period.endDate, " +
			"c.id, c.registrationCode, c.treatmentPeriod.iniDate, p.id, p.name, p.middleName, p.lastName, " +
			"c.state, c.treatmentPeriod.endDate " +
			"from PrescribedMedicine presc " +
			"join presc.tbcase c " +
			"join c.patient p " +
			"where presc.period.iniDate <= :dtend and presc.period.endDate >= :dtini and p.workspace.id = #{defaultWorkspace.id}";
		if (tbunit != null) 
			hql += " and c.treatmentUnit.id  = " + tbunit.getId().toString();
		if (source != null)
			hql += " and presc.source.id = " + source.getId().toString();
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("dtini", iniDate)
			.setParameter("dtend", endDate)
			.getResultList();

		cases = new ArrayList<CaseInfo>();
		
		for (Object[] vals: lst) {
			Integer caseId = (Integer)vals[5];
			CaseInfo caseInfo = findCase(caseId);
			if (caseInfo == null) {
				caseInfo = new CaseInfo();
				caseInfo.getTbcase().setId(caseId);
				cases.add(caseInfo);
			}

			PrescribedMedicine pm = new PrescribedMedicine();
			caseInfo.getPrescriptions().add(pm);
			pm.setMedicine(getMedicineById((Integer)vals[0]));
			pm.setDoseUnit((Integer)vals[1]);
			pm.setFrequency((Integer)vals[2]);
			pm.setPeriod(new Period((Date)vals[3], (Date)vals[4]));

			TbCase c = caseInfo.getTbcase();
			c.setRegistrationCode((String)vals[6]);
			c.getTreatmentPeriod().setIniDate((Date)vals[7]);
			c.getTreatmentPeriod().setEndDate((Date)vals[13]);
			
			Patient p = caseInfo.getTbcase().getPatient(); 
			p.setId((Integer)vals[8]);
			p.setName((String)vals[9]);
			p.setMiddleName((String)vals[10]);
			p.setLastName((String)vals[11]);
			c.setState((CaseState)vals[12]);
			
			cases.add(caseInfo);
		}
	}


	private Medicine getMedicineById(int id) {
		for (Medicine med: medicines) {
			if (med.getId().equals(id))
				return med;
		}
		return null;
	}
	
	/**
	 * @return the iniDate
	 */
	public Date getIniDate() {
		return iniDate;
	}
	/**
	 * @param iniDate the iniDate to set
	 */
	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}
	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}
	/**
	 * @return the tbunit
	 */
	public Tbunit getTbunit() {
		return tbunit;
	}
	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}

	/**
	 * @return the detailedByPatient
	 */
	public boolean isDetailedByPatient() {
		return detailedByPatient;
	}

	/**
	 * @param detailedByPatient the detailedByPatient to set
	 */
	public void setDetailedByPatient(boolean detailedByPatient) {
		this.detailedByPatient = detailedByPatient;
	}

	/**
	 * @return the quantities
	 */
	public List<MedicineQuantity> getQuantities() {
		return quantities;
	}

	/**
	 * @return the cases
	 */
	public List<CaseInfo> getCases() {
		return cases;
	}

}
