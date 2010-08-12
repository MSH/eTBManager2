package org.msh.tb.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


/**
 * Support batch selection by the user 
 * @author Ricardo Memoria
 * 
 */
@Name("batchSelection")
@Scope(ScopeType.CONVERSATION)
public class BatchSelection {

	@In(create=true) EntityManager entityManager;
	@In(create=true) Map<String, String> messages;
	
	public class BatchItem {
		private Batch batch;
		private BatchQuantity batchQuantity;
		private Integer quantity;

		public Integer getQuantity() {
			return quantity;
		}
		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}
		/**
		 * @param batchQuantity the batchQuantity to set
		 */
		public void setBatchQuantity(BatchQuantity batchQuantity) {
			this.batchQuantity = batchQuantity;
		}
		/**
		 * @return the batchQuantity
		 */
		public BatchQuantity getBatchQuantity() {
			return batchQuantity;
		}
		/**
		 * @return the batch
		 */
		public Batch getBatch() {
			return batch;
		}
		/**
		 * @param batch the batch to set
		 */
		public void setBatch(Batch batch) {
			this.batch = batch;
		}
	}
	
	private Medicine medicine;
	private Source source;
	private Tbunit tbunit;
	private List<BatchQuantity> batches;
	private String condition;
	private List<ItemSelect<BatchItem>> items;
	private Map<Batch, Integer> selectedBatches;
	private boolean allowQtdOverStock;


	/**
	 * Create the batch list from the batches available in the database.
	 * The unit, source and medicine are required.
	 * Optionally a condition for batches filtering may be specified 
	 */
	protected void createBatchList() {
		if ((tbunit == null) || (source == null) || (medicine == null))
			return;
		
		String hql;
		if ((condition != null) && (!condition.isEmpty()))
			hql = " and ".concat(condition);
		else hql = "";
		
		batches = entityManager.createQuery("from BatchQuantity b join fetch b.batch " +
				"where b.tbunit.id = :unitid and b.batch.expiryDate >= :dt" +
				hql +
				" and b.source.id = :sourceid " + 
				"and b.batch.medicine.id = :medid " +
				"order by b.batch.expiryDate")
				.setParameter("unitid", tbunit.getId())
				.setParameter("sourceid", source.getId())
				.setParameter("medid", medicine.getId())
				.setParameter("dt", new Date())
				.getResultList();
	}


	/**
	 * Return the list of selected batches and its entered quantities
	 * @return Map of Batch and Integer objects
	 */
	public Map<Batch, Integer> getSelectedBatches() {
		if (items == null)
			return selectedBatches;
		
		List<BatchItem> lst = ItemSelectHelper.createItemsList(getItems(), true);
		
		selectedBatches = new HashMap<Batch, Integer>();
		
		for (BatchItem it: lst) {
			selectedBatches.put(it.getBatchQuantity().getBatch(), it.getQuantity());
		}
		return selectedBatches;
	}

	
	/**
	 * Return the list of selected batches from the unit and its entered quantities
	 * @return Map of BatchQuantity and Integer objects
	 */
	public Map<BatchQuantity, Integer> getSelectedBatchesQtds() {
		List<BatchItem> lst = ItemSelectHelper.createItemsList(getItems(), true);
		
		Map<BatchQuantity, Integer> batches = new HashMap<BatchQuantity, Integer>();
		
		for (BatchItem it: lst) {
			batches.put(it.getBatchQuantity(), it.getQuantity());
		}
		return batches;
	}


	/**
	 * Set the batches and quantities already selected
	 * @param lst
	 */
	public void setSelectedBatches(Map<Batch, Integer> lst) {
		selectedBatches = lst;
	}

	
	/**
	 * Create items list to be selected by the user 
	 */
	protected void createItems() {
		items = new ArrayList<ItemSelect<BatchItem>>();

		List<BatchQuantity> lst = getBatches();
		if (lst == null)
			return;
		
		for (BatchQuantity b: lst) {
			BatchItem item = new BatchItem();
			item.setBatchQuantity(b);
			item.setBatch(b.getBatch());
			
			ItemSelect it = new ItemSelect();
			it.setItem(item);
			
			if (selectedBatches != null) {
				if (selectedBatches.containsKey(b.getBatch())) {
					item.setQuantity(selectedBatches.get(b.getBatch()));
					it.setSelected(true);
				}
				else {
					it.setSelected(false);
					item.setQuantity(b.getQuantity());
				}
			}
			
			items.add(it);
		}
	}

	public void validateQuantity(FacesContext context, UIComponent editComp, Object value) {
		Number val = (Number) value;

		BatchQuantity b = (BatchQuantity)((UIParameter)editComp.getChildren().get(0)).getValue();

		if ((!allowQtdOverStock) && (val.intValue() > b.getQuantity())) {
			((UIInput)editComp).setValid(false);

			FacesMessage message = new FacesMessage(messages.get("medicines.transfer.batchqtty"));
			context.addMessage(editComp.getClientId(context), message);
		}
	}

	/**
	 * clear the batch list in memory
	 */
	public void clear() {
		source = null;
		medicine = null;
		tbunit = null;
		batches = null;
		items = null;
		selectedBatches = null;
	}
	
	public List<ItemSelect<BatchItem>> getItems() {
		if (items == null)
			createItems();
		return items;
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	public Medicine getMedicine() {
		return medicine;
	}
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
		batches = null;
	}
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
		batches = null;
	}
	public List<BatchQuantity> getBatches() {
		if (batches == null)
			createBatchList();
		return batches;
	}
	public void setBatches(List<BatchQuantity> batches) {
		this.batches = batches;
	}


	public Tbunit getTbunit() {
		return tbunit;
	}


	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}


	public void setAllowQtdOverStock(boolean allowQtdOverStock) {
		this.allowQtdOverStock = allowQtdOverStock;
	}


	public boolean isAllowQtdOverStock() {
		return allowQtdOverStock;
	}
	
	public int getQtdOverStockValue() {
		if (allowQtdOverStock)
			return 1;
		else return 0;
	}
}
