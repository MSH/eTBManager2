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
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineDispensingCase;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.SourceMedicineTree;
import org.msh.tb.medicines.SourceMedicineTree.MedicineNode;
import org.msh.tb.medicines.dispensing.BatchDispensingUIHome.MedicineItem;
import org.msh.tb.medicines.movs.StockPositionList;
import org.msh.utils.date.LocaleDateConverter;


@Name("caseDispensingUIHome")
public class CaseDispensingUIHome {

	@In EntityManager entityManager;
	@In(create=true) UserSession userSession;
	@In(create=true) CaseHome caseHome;

	private List<CaseInfo> cases;
	private SourceMedicineTree<BatchItem> sourceTree;
	private Date dispensingDate;


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
				"join fetch c.regimen " +
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
	 * Register the dispensing quantity entered in the {@link SourceMedicineTree} sources property
	 * @return
	 */
	public String saveDispensing() {
		final TbCase tbcase = caseHome.getInstance();
		
		final DispensingHome dispHome = (DispensingHome)Component.getInstance("dispensingHome");

		Tbunit unit = entityManager.find(Tbunit.class, userSession.getTbunit().getId());
		
		if (unit.getMedManStartDate() == null)
			throw new RuntimeException("Unit not in medicine management control");

		FacesMessages facesMessages = FacesMessages.instance();
		
		if (dispensingDate.before( unit.getMedManStartDate() )) {
			facesMessages.addToControlFromResourceBundle("edtdate", "meds.movs.datebefore", LocaleDateConverter.getDisplayDate( unit.getMedManStartDate() ));
			return "error";
		}
		
		dispHome.initialize(userSession.getTbunit());
		dispHome.setDispensingDate(dispensingDate);
		
		sourceTree.traverse(new SourceMedicineTree.ItemTraversing<BatchItem>() {
			public void traverse(Source source, Medicine medicine, BatchItem item) {
				int qtd = item.getDispensing().getQuantity();
				if (qtd > 0)
					dispHome.addPatientDispensing(tbcase, item.getDispensing().getBatch(), source, qtd);
			}
		});

		// error during saving
		if (!dispHome.saveDispensing()) {
			// handle error messages
			dispHome.traverseErrors(new DispensingHome.ErrorTraverser() {
				public void traverse(Source source, Medicine medicine, String errorMessage) {
					MedicineNode node = sourceTree.findMedicineNode(source, medicine);
					if (node != null) {
						((MedicineItem)node.getItem()).setErrorMessage(errorMessage);
					}
				}
			});
			return "error";
		}
		
		return "persisted";
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
	private void createSources() {
		Tbunit unit = userSession.getTbunit();

		TbCase tbcase = caseHome.getInstance();
		
		// return list of medicines prescribed
		List<PrescribedMedicine> lst = entityManager.createQuery("from PrescribedMedicine pm " +
				"join fetch pm.medicine m join fetch pm.source s " +
				"where pm.tbcase.id = :id")
				.setParameter("id", tbcase.getId())
				.getResultList();

		sourceTree = new SourceMedicineTree<BatchItem>();
		
		// return the list of batches available
		StockPositionList stockPosList = (StockPositionList)Component.getInstance("stockPositionList", true);
		List<BatchQuantity> batches = stockPosList.getBatchAvailable(unit, null);

		for (BatchQuantity bq: batches) {
			Source source = bq.getSource();
			Medicine med = bq.getBatch().getMedicine();
			
			// is medicine in batch prescribed to the patient ?
			if (isMedicinePrescribed(lst, source, med)) {
				// initialize medicine
				MedicineNode node = sourceTree.addMedicine(source, med);
				if (node.getItem() == null)
					node.setItem(new MedicineInfo(node));
				
				// initialize batch
				MedicineDispensingCase item = new MedicineDispensingCase();
				BatchItem info = new BatchItem();
				info.setDispensing(item);
				info.setAvailableQuantity(bq.getQuantity());
				item.setBatch(bq.getBatch());
				item.setSource(bq.getSource());
				sourceTree.addItem(bq.getSource(), bq.getBatch().getMedicine(), info);
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
	public class MedicineInfo {
		private MedicineNode node;
		private String errorMessage;
		
		public MedicineInfo(MedicineNode node) {
			this.node = node;
		}
		
		/**
		 * Return the available quantity of the medicine
		 * @return
		 */
		public int getAvailableQuantity() {
			int qtd = 0;
			for (Object obj: node.getBatches()) {
				BatchItem info = (BatchItem)obj;
				qtd += info.getAvailableQuantity();
			}
			return qtd;
		}
		
		/**
		 * Return the quantity dispensed
		 * @return
		 */
		public int getDispensingQuantity() {
			int qtd = 0;
			for (Object obj: node.getBatches()) {
				BatchItem info = (BatchItem)obj;
				qtd += info.getDispensing().getQuantity();
			}
			return qtd;
		}

		/**
		 * @return the errorMessage
		 */
		public String getErrorMessage() {
			return errorMessage;
		}

		/**
		 * @param errorMessage the errorMessage to set
		 */
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}
	
	
	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class BatchItem {
		private MedicineDispensingCase dispensing;
		private Integer availableQuantity;

		/**
		 * @return the dispensing
		 */
		public MedicineDispensingCase getDispensing() {
			return dispensing;
		}
		/**
		 * @param dispensing the dispensing to set
		 */
		public void setDispensing(MedicineDispensingCase dispensing) {
			this.dispensing = dispensing;
		}
		/**
		 * @return the availableQuantity
		 */
		public Integer getAvailableQuantity() {
			return availableQuantity;
		}
		/**
		 * @param availableQuantity the availableQuantity to set
		 */
		public void setAvailableQuantity(Integer availableQuantity) {
			this.availableQuantity = availableQuantity;
		}
	}

	/**
	 * @return the sources
	 */
	public List getSources() {
		if (sourceTree == null)
			createSources();
		return sourceTree.getSources();
	}


	/**
	 * @return the dispensingDate
	 */
	public Date getDispensingDate() {
		return dispensingDate;
	}


	/**
	 * @param dispensingDate the dispensingDate to set
	 */
	public void setDispensingDate(Date dispensingDate) {
		this.dispensingDate = dispensingDate;
	}
}
