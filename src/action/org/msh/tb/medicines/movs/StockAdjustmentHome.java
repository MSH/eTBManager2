package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.StockPosition;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.utils.date.DateUtils;


@Name("stockAdjustmentHome")
@Scope(ScopeType.CONVERSATION)
public class StockAdjustmentHome extends Controller {
	private static final long serialVersionUID = 5670008276682104291L;

	private Source source;
	private List<AdjustmentItem> items;
	private AdjustmentItem item;
	
	@In(create=true) EntityManager entityManager;
	@In(create=true) MovementHome movementHome;
	@In(create=true) UserSession userSession;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) FacesMessages facesMessages;

	/**
	 * Initialize the items for adjustment (used when source or tbunit chage)
	 */
	public void initialize() {
		items = null;
	}

	/**
	 * Cria os itens para ajuste
	 */
	private void createItems() {
		items = new ArrayList<AdjustmentItem>();
		Tbunit tbunit = userSession.getTbunit();
		
		if ((source == null) || (tbunit == null)) {
			return;
		}
		
		String hql = "from StockPosition sp join fetch sp.medicine " +
			"where sp.date = (select max(aux.date) from StockPosition aux " +
			"where aux.source.id = sp.source.id and aux.tbunit.id = sp.tbunit.id " +
			"and aux.medicine.id = sp.medicine.id) " +
			"and sp.source.id = :sourceid and sp.tbunit.id = :unitid " +
			"order by sp.medicine.genericName";

		List<StockPosition> lst = entityManager.createQuery(hql)
			.setParameter("sourceid", source.getId())
			.setParameter("unitid", tbunit.getId())
			.getResultList();
		
		for (StockPosition sp: lst) {
			AdjustmentItem it = new AdjustmentItem();
			if (!tbunit.isBatchControl())
				it.setQuantity(sp.getQuantity());
			it.setStockPosition(sp);
			items.add(it);
		}
	}
	
	/**
	 * Execute the adjustment
	 * @return
	 */
	@Transactional
	public String execute() {
		for (AdjustmentItem item: items) {
			if ((item.getQuantity() != null) &&
				(item.getQuantity() != item.getStockPosition().getQuantity()) &&
				(item.getComment().isEmpty()))
			{
				facesMessages.addFromResourceBundle("medicines.movs.commentreq");
				return "error";
			}
		}
		
		MovementType type = MovementType.ADJUSTMENT;
		
		movementHome.initMovementRecording();
		for (AdjustmentItem item: items) {			
			if ((item.getQuantity() != null) && (item.getQuantity() != item.getStockPosition().getQuantity())) {

				// create batch map to be saved with the movement
				Map<Batch, Integer> batches = new HashMap<Batch, Integer>();
				for (BatchAdjustmentItem it: item.getBatches())
					batches.put(it.getBatchQtd().getBatch(), it.getQuantity() - it.getBatchQtd().getQuantity());
	
				movementHome.prepareNewMovement(DateUtils.getDate(), userSession.getTbunit(), source, item.getStockPosition().getMedicine(),
						type, batches, item.getComment());
			}
		}
		movementHome.savePreparedMovements();
		
		entityManager.flush();
		
		return "success";
	}


	/**
	 * Select batches with quantity modified of the selected item
	 */
	public void selectBatches() {
		Map<BatchQuantity, Integer> sels = batchSelection.getSelectedBatchesQtds();
		
		// check if there is a batch with zero quantity or quantity over the remaining quantity
		for (BatchQuantity b: sels.keySet()) {
			Integer val = sels.get(b);
			if ((val == null) || (val == 0)) 
				return;
		}

		item.getBatches().clear();
		
		int qtd = 0;
		for (BatchQuantity b: sels.keySet()) {
			BatchAdjustmentItem tb = new BatchAdjustmentItem();
			tb.setBatchQtd(b);
			tb.setQuantity(sels.get(b));
			
			qtd += tb.getQuantity() - b.getQuantity();
			
			item.getBatches().add(tb);
		}

		item.setQuantity(qtd + item.getStockPosition().getQuantity());
		
		// free memory space
		batchSelection.clear();
		item = null;
	}

	
	/**
	 * Initialize the batch selection 
	 */
	public void initializeBatchSelection(AdjustmentItem item) {
		this.item = item;
		batchSelection.clear();
		batchSelection.setTbunit(userSession.getTbunit());
		batchSelection.setMedicine(item.getStockPosition().getMedicine());
		batchSelection.setSource(source);
		batchSelection.setAllowQtdOverStock(true);

		batchSelection.setSelectedBatches(getBatchesMap(item, true));		
	}

	private Map<Batch, Integer> getBatchesMap(AdjustmentItem it, boolean shpippedQtd) {
		Map<Batch, Integer> sels = new HashMap<Batch, Integer>();
		for (BatchAdjustmentItem b: it.getBatches()) {
		 	 sels.put(b.getBatchQtd().getBatch(), b.getQuantity());
		}
		return sels;
	}
	
	public List<AdjustmentItem> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}

	public AdjustmentItem getItem() {
		return item;
	}
	
	public void setSourceId(Integer id) {
		if (id == null)
			 source = null;
		else source = entityManager.find(Source.class, id); 
	}
	
	public Integer getSourceId() {
		if (source == null)
			 return null;
		else return source.getId();
	}
}
