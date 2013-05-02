package org.msh.tb.medicines;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
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
	private Date nextOrderDate;

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
	 * Check if the given batch is in attention period for expiration.
	 */
	public boolean isExpiringBatch(Object o){
		
		Batch b = null;
		BatchQuantity bq = null;
		if(o instanceof Batch)
			b = (Batch) o;
		else if (o instanceof BatchQuantity){
			bq = (BatchQuantity) o;
			b = bq.getBatch();
		}
		
		if(b != null)
			if(b.getExpiryDate()!=null){
/*			Calendar now  = Calendar.getInstance();
			Calendar batchExpiringDate = Calendar.getInstance();
			batchExpiringDate.setTime(b.getExpiryDate());
			
			long diff = batchExpiringDate.getTimeInMillis() - now.getTimeInMillis();
			diff = diff / (24*60*60*1000);
			double diffInDouble = diff;
			double diffInMonths = diffInDouble / 30.0;
*/
			// calculate the number of months between two dates
			int diffInMonths = DateUtils.monthsBetween(b.getExpiryDate(), new Date());
		
			Workspace workspace = UserSession.getWorkspace();
			if(workspace.getMonthsToAlertExpiredMedicines() != null) {
				if(diffInMonths < 0 || diffInMonths > workspace.getMonthsToAlertExpiredMedicines())
					return false;
				else
					return true;
			}
			
			if (workspace.getMonthsToAlertExpiredMedicines() != null) {
				if(diffInMonths < 0 || diffInMonths > workspace.getMonthsToAlertExpiredMedicines())
					return false;
				else
					return true;
			}
		}
		
		return false;
	}

	/**
	 * Check if the registration card is in attention period for expiration.
	 */
	public boolean isExpiringRegistCard(Object o){
		
		Batch b = null;
		BatchQuantity bq = null;
		if(o instanceof Batch)
			b = (Batch) o;
		else if (o instanceof BatchQuantity){
			bq = (BatchQuantity) o;
			b = bq.getBatch();
		}
		
		if(b != null)
			if(b.getRegistCardEndDate()!=null){
/*			Calendar now  = Calendar.getInstance();
			Calendar batchExpiringDate = Calendar.getInstance();
			batchExpiringDate.setTime(b.getExpiryDate());
			
			long diff = batchExpiringDate.getTimeInMillis() - now.getTimeInMillis();
			diff = diff / (24*60*60*1000);
			double diffInDouble = diff;
			double diffInMonths = diffInDouble / 30.0;
*/
			// calculate the number of months between two dates
			int diffInMonths = DateUtils.monthsBetween(b.getRegistCardEndDate(), new Date());
		
			if (diffInMonths<2)
				return true;
		}
		
		return false;
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
			medNode.setItem(info);
		}

		// mount batches quantity
		List<BatchQuantity> batches = stockPositionList.getBatchAvailable(userSession.getTbunit(), null);
		for (BatchQuantity bq: batches) {
			root.addItem(bq.getSource(), bq.getBatch().getMedicine(), bq);

			// check if batch has expired
			if (bq.getBatch().isExpired()) {
				MedicineNode node = root.findMedicineNode(bq.getSource(), bq.getBatch().getMedicine());
				if ((node != null) && (node.getItem() != null))  //AK maybe null
					((MedicineInfo)node.getItem()).setHasBatchExpired(true);
			}
			
			// check if batch is up to expire
			if (isExpiringBatch(bq.getBatch())) {
				MedicineNode node = root.findMedicineNode(bq.getSource(), bq.getBatch().getMedicine());
				if ((node != null) && (node.getItem() != null))  //AK maybe null
					((MedicineInfo)node.getItem()).setHasBatchExpiring(true);
			}
			
		}

		loadLastMovement(userSession.getTbunit());

		// calculate stock on hand
		for (SourceNode node: root.getSources()) {
			for (Object obj: node.getMedicines()) {
				MedicineNode medNode = (MedicineNode)obj;
				calcAMC(medNode);
			}
		}
	}


	/**
	 * Calculate stock on hand
	 * @param info
	 */
	protected void calcAMC(MedicineNode node) {
		MedicineInfo info = (MedicineInfo)node.getItem();
		if (info==null) return; //AK may be null
		StockPosition sp = info.getStockPosition();
		Integer amc = sp.getAmc();
		if ((amc == null) || (amc == 0))
			return;

		int qtd = info.getQuantity();

		if (qtd == 0) {
			info.setStockOutDate(null);
			if (amc > 0)
				nextOrderDate = DateUtils.getDate();
			return;
		}

		float val = (float)qtd / (float)amc;
		int days = Math.round(val * 30);

		Date dt = DateUtils.incDays(sp.getLastMovement(), days);

		Date dtExpire = info.getLastBatchExpire();

		if ((dtExpire != null) && (dt.after(dtExpire)))
			dt = dtExpire;

		info.setStockOutDate(dt);
		info.setNextOrderDate(DateUtils.incMonths(dt, -1));

		if ((nextOrderDate == null) || (nextOrderDate.after( info.getNextOrderDate()))) {
			nextOrderDate = info.getNextOrderDate();
		}
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
		private boolean hasBatchExpired;
		private boolean hasBatchExpiring;

		public MedicineInfo(MedicineNode node, StockPosition stockPosition) {
			this.stockPosition = stockPosition;
			this.node = node;
		}

		public int getQuantity() {
			int tot = 0;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if (!bq.getBatch().isExpired())
					tot += bq.getQuantity();
			}
			return tot; //stockPosition.getQuantity();
		}

		public float getUnitPrice() {
			return stockPosition.getUnitPrice();
		}

		public double getTotalPrice() {
			double tot = 0;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if (!bq.getBatch().isExpired())
					tot += bq.getTotalPrice();
			}
			return tot; //stockPosition.getQuantity();
		}

		public Date getNextBatchExpire() {
			Date dt = null;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if (!bq.getBatch().isExpired()) {
					if ((dt == null) || ((dt.after(bq.getBatch().getExpiryDate()))))
						dt = bq.getBatch().getExpiryDate();
				}
			}
			return dt;
		}
		
		public Date getNextRegistCardExpire() {
			Date dt = null;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if (!bq.getBatch().isRegistCardExpired()) {
					if ((dt == null) || ((dt.after(bq.getBatch().getRegistCardEndDate()))))
						dt = bq.getBatch().getRegistCardEndDate();
				}
			}
			return dt;
		}

		public Date getLastBatchExpire() {
			Date dt = null;
			for (Object obj: node.getBatches()) {
				BatchQuantity bq = (BatchQuantity)obj;
				if (!bq.getBatch().isExpired()) {
					if ((dt == null) || ((dt.before(bq.getBatch().getExpiryDate()))))
						dt = bq.getBatch().getExpiryDate();
				}
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

		/**
		 * @return the hasBatchExpired
		 */
		public boolean isHasBatchExpired() {
			return hasBatchExpired;
		}

		/**
		 * @param hasBatchExpired the hasBatchExpired to set
		 */
		public void setHasBatchExpired(boolean hasBatchExpired) {
			this.hasBatchExpired = hasBatchExpired;
		}

		/**
		 * @return the hasBatchExpiring
		 */
		public boolean isHasBatchExpiring() {
			return hasBatchExpiring;
		}

		/**
		 * @param hasBatchExpiring the hasBatchExpiring to set
		 */
		public void setHasBatchExpiring(boolean hasBatchExpiring) {
			this.hasBatchExpiring = hasBatchExpiring;
		}
		
		/**
		 * Compares today+30days with sotckoutdate, if sotckoutdate 
		 * is before today+30 it's time to alert the user.
		 */
		public boolean almostStockedOut(){
			Date d = DateUtils.incDays(DateUtils.getDate(), 30);
			if(stockOutDate != null){
				return stockOutDate.before(d);
			}else
				return false;
		}

		public void setHasRegistCardExpired(boolean hasRegistCardExpired) {
			return;
		}

		public boolean isHasRegistCardExpired() {
			for (Object o: node.getBatches()){
				BatchQuantity b = (BatchQuantity)o;
				if (b.getBatch().getRegistCardEndDate()!=null){
					if (b.getBatch().isRegistCardExpired())
						return true;
				}
			}
			return false;
		}

		public void setHasRegistCardExpiring(boolean hasRegistCardExpiring) {
			return;
		}

		public boolean isHasRegistCardExpiring() {
			for (Object o: node.getBatches()){
				BatchQuantity b = (BatchQuantity)o;
				if (b.getBatch().getRegistCardEndDate()!=null){
					Date today = new Date();
					int diff = DateUtils.daysBetween(today, b.getBatch().getRegistCardEndDate());
					if (diff<=30)
						return true;
				}
			}
			return false;
		}
		
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
