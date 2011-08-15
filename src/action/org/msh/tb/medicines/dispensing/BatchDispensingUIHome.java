package org.msh.tb.medicines.dispensing;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.SourceMedicineTree;
import org.msh.tb.medicines.SourceMedicineTree.MedicineNode;
import org.msh.tb.medicines.movs.StockPositionList;
import org.msh.utils.date.LocaleDateConverter;

@Name("batchDispensingUIHome")
public class BatchDispensingUIHome {

	@In EntityManager entityManager;
	@In(create=true) StockPositionList stockPositionList;
	@In(create=true) UserSession userSession;

	private Date dispensingDate;
	private SourceMedicineTree<BatchItem> sourceTree;


	/**
	 * Return list of sources
	 * @return
	 */
	public List getSources() {
		if (sourceTree == null)
			createSources();
		
		return sourceTree.getSources();
	}


	/**
	 * Save dispensing
	 * @return
	 */
	public String saveDispensing() {
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
		
		sourceTree.traverse(new SourceMedicineTree.ItemTraversing<BatchDispensingUIHome.BatchItem>() {
			public void traverse(Source source, Medicine medicine, BatchItem item) {
				Integer qtd = item.getDispensingQuantity();
				if ((qtd != null) && (qtd > 0))
					dispHome.addDispensing(item.getBatchQuantity().getBatch(), source, item.getDispensingQuantity());
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
	 * Create list of sources and its medicines and batches, ready for editing
	 */
	private void createSources() {
		Tbunit unit = userSession.getTbunit();
		List<BatchQuantity> lst = stockPositionList.getBatchAvailable(unit, null);

		sourceTree = new SourceMedicineTree<BatchItem>();
		
		for (BatchQuantity item: lst) {
			if (item.getQuantity() > 0) {
				MedicineNode medNode = sourceTree.addMedicine(item.getSource(), item.getBatch().getMedicine());
				MedicineItem medItem = new MedicineItem(medNode);
				medNode.setItem(medItem);
				
				BatchItem aux = new BatchItem();
				aux.setBatchQuantity(item);
				sourceTree.addItem(item.getSource(), item.getBatch().getMedicine(), aux);
			}
		}
	}


	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineItem {
		private MedicineNode node;
		private String errorMessage;
		
		public MedicineItem(MedicineNode node) {
			this.node = node;
		}

		public int getQuantity() {
			int tot = 0;
			for (Object aux: node.getBatches()) {
				BatchItem item = (BatchItem)aux;
				tot += item.getBatchQuantity().getQuantity();
			}
			return tot;
		}

		public int getDispensingQuantity() {
			int tot = 0;
			for (Object aux: node.getBatches()) {
				BatchItem item = (BatchItem)aux;
				if (item.getDispensingQuantity() != null)
					tot += item.getDispensingQuantity();
			}
			return tot;
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
		private BatchQuantity batchQuantity;
		private Integer dispensingQuantity;
		/**
		 * @return the batchQuantity
		 */
		public BatchQuantity getBatchQuantity() {
			return batchQuantity;
		}
		/**
		 * @param batchQuantity the batchQuantity to set
		 */
		public void setBatchQuantity(BatchQuantity batchQuantity) {
			this.batchQuantity = batchQuantity;
		}
		/**
		 * @return the dispensingQuantity
		 */
		public Integer getDispensingQuantity() {
			return dispensingQuantity;
		}
		/**
		 * @param dispensingQuantity the dispensingQuantity to set
		 */
		public void setDispensingQuantity(Integer dispensingQuantity) {
			this.dispensingQuantity = dispensingQuantity;
		}
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
