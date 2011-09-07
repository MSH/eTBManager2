package org.msh.tb.medicines;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.SourceMedicineTree.MedicineNode;
import org.msh.tb.medicines.SourceMedicineTree.SourceNode;
import org.msh.tb.medicines.movs.StockPositionList;
import org.msh.utils.date.DateUtils;

@Name("inventoryReport")
public class InventoryReport {

	@In EntityManager entityManager;
	@In(create=true) StockPositionList stockPositionList;

	private SourceMedicineTree<BatchQuantity> root;
	
	/**
	 * Return list of sources
	 * @return list of {@link SourceNode} objects
	 */
	public List getSources() {
		if (root == null)
			createSources();
		return root.getSources();
	}


	/**
	 * Create the list of sources, i.e, the inventory report
	 */
	private void createSources() {
		root = new SourceMedicineTree<BatchQuantity>();

		UserSession userSession = (UserSession)Component.getInstance("userSession", true);
		
		List<StockPosition> lst = stockPositionList.generate(userSession.getTbunit(), null);

		// mount medicine quantity
		for (StockPosition sp: lst) {
			MedicineNode medNode = root.addMedicine(sp.getSource(), sp.getMedicine());
			MedicineInfo info = new MedicineInfo(medNode, sp);
			calcAMC(info);
			medNode.setItem(info);
		}
		
		// mount batches quantity
		List<BatchQuantity> batches = stockPositionList.getBatchAvailable(userSession.getTbunit(), null);
		for (BatchQuantity bq: batches) {
			root.addItem(bq.getSource(), bq.getBatch().getMedicine(), bq);
		}
		
		loadLastMovement(userSession.getTbunit());
	}

	
	protected void calcAMC(MedicineInfo info) {
		StockPosition sp = info.getStockPosition();
		Integer amc = sp.getAmc();
		if ((amc == null) || (amc == 0))
			return;
		
		float val = (float)sp.getQuantity() / (float)amc;
		int days = Math.round(val * 30);
		
		Date dt = DateUtils.incDays(sp.getLastMovement(), days);
		info.setStockOutDate(dt);
		info.setNextOrderDate(DateUtils.incMonths(dt, -1));
	}


	/**
	 * Load information of when the medicine was last issued
	 */
	protected void loadLastMovement(Tbunit unit) {
		String hql = "select max(m.date), m.source.id, m.medicine.id " +
			"from Movement m " +
			"where m.tbunit.id = :unit " +
			"group by m.source.id, m.medicine.id";
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("unit", unit.getId())
			.getResultList();

		for (Object vals[]: lst) {
			Date dt = (Date)vals[0];
			Integer sourceId = (Integer)vals[1];
			Integer medId = (Integer)vals[2];

			MedicineNode medNode = root.findNodeByMedicineId(sourceId, medId);
			if (medNode != null) {
				MedicineInfo info = (MedicineInfo)medNode.getItem();
				if (info != null)
					info.setLastMovement(dt);
			}
		}
	}

	
	/**
	 * @author Ricardo Memoria
	 *
	 */
	public class MedicineInfo {
		private StockPosition stockPosition;
		private MedicineNode node;
		private Date lastMovement;
		private Date stockOutDate;
		private Date nextOrderDate;
		
		public MedicineInfo(MedicineNode node, StockPosition stockPosition) {
			this.stockPosition = stockPosition;
			this.node = node;
		}

		public int getQuantity() {
			return stockPosition.getQuantity();
		}
		
		public float getUnitPrice() {
			return stockPosition.getUnitPrice();
		}
		
		public float getTotalPrice() {
			return stockPosition.getTotalPrice();
		}
		
		/**
		 * Generate a unique row id for the medicine
		 * @return
		 */
		public String getRowId() {
			return stockPosition.getSource().getId().toString() + "_" + stockPosition.getMedicine().getId().toString();
		}
		
		public Date getNextBatchExpire() {
			Date dt = null;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if ((dt == null) || (dt.after(bq.getBatch().getExpiryDate())))
					dt = bq.getBatch().getExpiryDate();
			}
			return dt;
		}
		
		/**
		 * @return the stockPosition
		 */
		public StockPosition getStockPosition() {
			return stockPosition;
		}
		/**
		 * @param stockPosition the stockPosition to set
		 */
		public void setStockPosition(StockPosition stockPosition) {
			this.stockPosition = stockPosition;
		}
		/**
		 * @return the node
		 */
		public MedicineNode getNode() {
			return node;
		}
		/**
		 * @param node the node to set
		 */
		public void setNode(MedicineNode node) {
			this.node = node;
		}
		/**
		 * @return the lastMovement
		 */
		public Date getLastMovement() {
			return lastMovement;
		}
		/**
		 * @param lastMovement the lastMovement to set
		 */
		public void setLastMovement(Date lastMovement) {
			this.lastMovement = lastMovement;
		}

		/**
		 * @return the stockOutDate
		 */
		public Date getStockOutDate() {
			return stockOutDate;
		}

		/**
		 * @param stockOutDate the stockOutDate to set
		 */
		public void setStockOutDate(Date stockOutDate) {
			this.stockOutDate = stockOutDate;
		}

		/**
		 * @return the nextOrderDate
		 */
		public Date getNextOrderDate() {
			return nextOrderDate;
		}

		/**
		 * @param nextOrderDate the nextOrderDate to set
		 */
		public void setNextOrderDate(Date nextOrderDate) {
			this.nextOrderDate = nextOrderDate;
		}
	}
}
