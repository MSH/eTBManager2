package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineDispensing;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.medicines.movs.StockPositionList;


@Name("caseDispensingUIHome")
public class CaseDispensingUIHome extends AbstractDispensigUIHome {

	@In EntityManager entityManager;
	@In(create=true) CaseHome caseHome;

	private List<CaseInfo> cases;


	/**
	 * Return the list of cases under treatment in the selected unit
	 * @return
	 */
	public List<CaseInfo> getCases() {
		if (cases == null)
			createCaseList();
		return cases;
	}


	/**
	 * Create the list of cases under treatment in the selected unit
	 */
	protected void createCaseList() {
		// load list of cases
		String hql = "from TbCase c join fetch c.patient p " +
				"left join fetch c.regimen " +
				"where c.treatmentUnit.id = #{userSession.tbunit.id} " +
				"and c.state = :st";
		
		List<TbCase> casesOnTreat = entityManager.createQuery(hql)
			.setParameter("st", CaseState.ONTREATMENT)
			.getResultList();
		
		cases = new ArrayList<CaseDispensingUIHome.CaseInfo>();
		for (TbCase c: casesOnTreat) {
			CaseInfo info = new CaseInfo();
			info.setTbcase(c);
			cases.add(info);
		}
		
		// load list of last dispensing registration
		hql = "select max(b.dispensingDate), a.tbcase.id " +
				"from MedicineDispensingCase a " +
				"join a.dispensing b " + 
				"where a.tbcase.state = :st and a.tbcase.treatmentUnit.id = #{userSession.tbunit.id} " +
				"group by a.tbcase.id";
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("st", CaseState.ONTREATMENT)
			.getResultList();
		
		for (Object[] vals: lst) {
			Date lastDisp = (Date)vals[0];
			Integer caseId = (Integer)vals[1];
	
			CaseInfo info = findCaseInfoByCaseId(caseId);
			info.setLastDispensing(lastDisp);
		}
		
		Collections.sort(cases, new Comparator<CaseInfo>() {
			@Override
			public int compare(CaseInfo c1, CaseInfo c2) {
				return c1.getPatient().getFullName().compareTo(c2.getPatient().getFullName());
			}
		});
	}

	
	/**
	 * Search for information about a case by its id
	 * @param id
	 * @return
	 */
	private CaseInfo findCaseInfoByCaseId(Integer id) {
		for (CaseInfo ci: cases) {
			if (ci.getTbcase().getId().equals(id))
				return ci;
		}
		return null;
	}

	
	/**
	 * Prepare a new dispensing to be registered
	 */
	@Override
	protected void loadSources(List<SourceItem> sources) {
		Tbunit unit = getUnit();

		TbCase tbcase = caseHome.getInstance();
		
		// return list of medicines prescribed
		List<PrescribedMedicine> lst = entityManager.createQuery("from PrescribedMedicine pm " +
				"join fetch pm.medicine m join fetch pm.source s " +
				"where pm.tbcase.id = :id")
				.setParameter("id", tbcase.getId())
				.getResultList();

		sources = new ArrayList<SourceItem>();
		
		// return the list of batches available
		StockPositionList stockPosList = (StockPositionList)Component.getInstance("stockPositionList", true);
		List<BatchQuantity> batches = stockPosList.getBatchAvailable(unit, null);

		for (BatchQuantity bq: batches) {
			Source source = bq.getSource();
			Medicine med = bq.getBatch().getMedicine();
			
			// is medicine in batch prescribed to the patient ?
			if (isMedicinePrescribed(lst, source, med))
				addSourceRow(bq);
		}

		// check if it's a dispensing being edited, and include the quantity dispensed previously
		if (getDispensingHome().isManaged()) {
			MedicineDispensing medDisp = getDispensingHome().getMedicineDispensing();
			for (MedicineDispensingCase mdc: medDisp.getPatients()) {
				if (mdc.getTbcase().equals(tbcase)) {
					DispensingRow row = addSourceRow(mdc.getSource(), mdc.getBatch());
					row.setDispensingQuantity(mdc.getQuantity());
				}
			}
		}
	}


	/**
	 * Check if medicine is in the list of prescribed medicines
	 * @param lst
	 * @param med
	 * @return
	 */
	private boolean isMedicinePrescribed(List<PrescribedMedicine> lst, Source source, Medicine med) {
		for (PrescribedMedicine pm: lst) {
			if ((pm.getMedicine().equals(med)) && (pm.getSource().equals(source))) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Store temporary information about a case dispensing
	 * @author Ricardo Memoria
	 *
	 */
	public class CaseInfo {
		private TbCase tbcase;
		private Date lastDispensing;
		
		public Patient getPatient() {
			return tbcase.getPatient();
		}
		
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
		 * @return the lastDispensing
		 */
		public Date getLastDispensing() {
			return lastDispensing;
		}
		/**
		 * @param lastDispensing the lastDispensing to set
		 */
		public void setLastDispensing(Date lastDispensing) {
			this.lastDispensing = lastDispensing;
		}
	}

	
	/**
	 * @author Ricardo Memoria
	 *
	 */
}
