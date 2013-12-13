package org.msh.tb.ua.medicines;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.application.App;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.login.UserSession;
import org.msh.tb.ua.entities.MedicineReceivingUA;
import org.msh.tb.ua.entities.TransferUA;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.DateUtils;
/**
 * Report about batches movements, customized for UA    
 * @author A.M.
 */
@Name("reportMed")
@Scope(ScopeType.CONVERSATION)
public class ReportMedicinesSources {
	private static final long serialVersionUID = -1020072725822707877L;
	
	private Date iniDate;
	private Date endDate;
	private Source source;

	private List<ExtBatchItem> resultList;

	public void clear() {
		resultList = null;
	}
	
	public List<ExtBatchItem> getResultList() {
		if (resultList == null)
		{
			if (iniDate==null || endDate==null || source==null)
				return new ArrayList<ReportMedicinesSources.ExtBatchItem>();
			String hql="select bm.batch"+stockInDateSubQuery(iniDate,false) + stockInDateSubQuery(endDate,true) +
			" from BatchMovement bm " +
			" join bm.movement m " +
			"where bm.movement.id in (" +
				"select m.id " +
				"from Movement m " +
				"where m.tbunit.id=#{userSession.workingTbunit.id}" +
				//getPeriodCondidtion("m.date")+
				(source != null ? " and m.source.id="+source.getId():"")+") " +
			" group by bm.batch order by bm.batch.medicine,bm.batch.batchNumber ";
			List<Object[]> lst = App.getEntityManager().createQuery(hql).getResultList();
			
			resultList = new ArrayList<ReportMedicinesSources.ExtBatchItem>();
			for (Object[] vals:lst){
				ExtBatchItem item = new ExtBatchItem();
				item.setBatch((Batch)vals[0]);
				item.setStockInIniDate((Long)vals[1]);
				item.setStockInEndDate((Long)vals[2]);
				resultList.add(item);
			}
		}
		return resultList;
	}
	
	/*private String stockInIniDateSubQuery() {
		if (iniDate == null)
			return ",0L ";
		return ", (select sum(b.quantity * a.oper) " +
				"from BatchMovement b " +
				"join b.movement a " +
				"where a.tbunit.id=m.tbunit.id " +
					"and a.source.id=m.source.id " +
					"and a.medicine.id=m.medicine.id " +
					"and b.batch.id=bm.batch.id " +
					"and ((a.date < '"+DateUtils.formatDate(iniDate,"yyyy-MM-dd")+"')))";// or (a.date = '"+DateUtils.formatDate(iniDate,"yyyy-MM-dd")+"' and a.recordDate <= m.recordDate)))";
	}*/
	
	private String stockInDateSubQuery(Date d,boolean eq) {
		if (d == null)
			return ",0L ";
		return ", (select sum(b.quantity * a.oper) " +
				"from BatchMovement b " +
				"join b.movement a " +
				"where a.tbunit.id=m.tbunit.id " +
					"and a.source.id=m.source.id " +
					"and a.medicine.id=m.medicine.id " +
					"and b.batch.id=bm.batch.id " +
					"and ((a.date <"+(eq?"=":"")+" '"+DateUtils.formatDate(d,"yyyy-MM-dd")+"')))";// or (a.date = '"+DateUtils.formatDate(iniDate,"yyyy-MM-dd")+"' and a.recordDate <= m.recordDate)))";
	}

	/**
	 * Return period restrictions for not-null period's dates
	 * @param fullFieldName
	 * @return
	 */
	private String getPeriodCondidtion(String fullFieldName) {
		String cond = "";
		if (iniDate!=null)
			cond+=" and "+fullFieldName+">='"+DateUtils.formatDate(iniDate,"yyyy-MM-dd")+"'";
		if (endDate!=null)
			cond+=" and "+fullFieldName+"<='"+DateUtils.formatDate(endDate,"yyyy-MM-dd")+"'";
		return cond;
	}
	
	public boolean isStorehouse() {
		UserSession us = (UserSession) App.getComponent(UserSession.class);
		Tbunit workingUnit = us.getWorkingTbunit();
		if (!workingUnit.isTreatmentHealthUnit() && workingUnit.isMedicineStorage())
			return true;
		return false;
	}
	
	public Date getIniDate() {
		return iniDate;
	}

	public void setIniDate(Date iniDate) {
		this.iniDate = iniDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public class ExtBatchItem{
		private Batch batch;
		/**
		 * Quantity of this batch in first day of period
		 */
		private Long stockInIniDate;
		private Long stockInEndDate;
		
		private Long sumReceivings;
		private Long sumTransFromUnits;
		private Long sumTransFromTreatUnits;
		private Long sumTransFromNotTreatUnits;
		
		/**
		 * Check necessity displaying row
		 * @return
		 */
		public boolean isRender() {
			if (isStorehouse() && (stockInIniDate == null || stockInIniDate == 0) && getSumTransFromUnits()==null && getSumReceivings()==null)
				return false;
			if (!isStorehouse() && (stockInIniDate == null || stockInIniDate == 0) && getSumTransFromNotTreatUnits()==null && getSumTransFromTreatUnits()==null)
				return false;
			return true;
		}
		
		/**
		 * Return the list of numbers and dates transfer orders, with which received current batch.
		 * In case of several transfer orders return all
		 * @return
		 */
		public String getOrderNumber() {
			String res="";
			String hql = "from TransferUA t " +
					"where t.id in (" +
						"select ti.transfer.id " +
						"from TransferItem ti " +
						"where ti.id in (" +
							"select tb.transferItem.id " +
							"from TransferBatch tb " +
							"where tb.batch.id="+batch.getId()+"))" +
						" and t.unitTo.id=#{userSession.workingTbunit.id}"+
						" and t.status = 1" +
						"order by t.receivingDate";
			List<Object> lst = App.getEntityManager().createQuery(hql).getResultList();
			if (lst.size()!=0)
			for (Object o:lst){
				TransferUA t = (TransferUA)o;
				if (t.getOrderNumber()!=null && !"".equals(t.getOrderNumber()))
					res += "¹"+t.getOrderNumber();
				if (t.getOrderDate()!=null)
					res += " "+App.getMessage("from")+" "+DateUtils.formatDate(t.getOrderDate(), App.getMessage("locale.datePattern"))+"; ";
				
			}
			return res;
		}
		
		/**
		 * Return the list of numbers and dates invoices of medicine receivings, with which received current batch.
		 * In case of several transfer orders return all
		 * @return
		 */
		public String getInvoiceNumber() {
			String res="";
			String hql = "select mr " +
						"from MedicineReceivingUA mr " +
						"join mr.movements m join m.batches bm "+
						"where bm.batch.id = " +batch.getId()+
						" and mr.tbunit.id=#{userSession.workingTbunit.id} "+
						"order by mr.receivingDate";
			List<Object> lst = App.getEntityManager().createQuery(hql).getResultList();
			if (lst.size()!=0)
			for (Object o:lst){
				MedicineReceivingUA mr = (MedicineReceivingUA)o;
				if (mr.getConsignmentNumber()!=null && !"".equals(mr.getConsignmentNumber()))
					res += "¹"+mr.getConsignmentNumber();
				if (mr.getReceivingDate()!=null)
					res += " "+App.getMessage("from")+" "+DateUtils.formatDate(mr.getReceivingDate(), App.getMessage("locale.datePattern"))+"; ";
				
			}
			return res;
		}
		
		/**
		 * Return total quantity of medicine from current batch, which 
		 * received in this period from manufacturer
		 * @return
		 */
		public Long getSumReceivings() {
			if (sumReceivings==null){
				String hql = "select sum(bm.quantity) from MedicineReceivingUA mr " +
							"join mr.movements m join m.batches bm "+
							"where bm.batch.id = " +batch.getId()+
							" and mr.tbunit.id=#{userSession.workingTbunit.id} "+
							getPeriodCondidtion("mr.receivingDate");
				List<Object> lst = App.getEntityManager().createQuery(hql).getResultList();
				if (lst.size()!=0){
					if (lst.get(0)!=null) sumReceivings =(Long)lst.get(0);
				}			
			}
			return sumReceivings;
		}
		
		public Long getSumTransFromUnits() {
			if (sumTransFromUnits == null)
				sumTransFromUnits = getSumTransFromUnit(null);
			return sumTransFromUnits;
		}

		public Long getSumTransFromTreatUnits() {
			if (sumTransFromTreatUnits == null)
				sumTransFromTreatUnits = getSumTransFromUnit(false);
			return sumTransFromTreatUnits;
		}
		
		public Long getSumTransFromNotTreatUnits() {
			if (sumTransFromNotTreatUnits == null)
				sumTransFromNotTreatUnits = getSumTransFromUnit(true);
			return sumTransFromNotTreatUnits;
		}	
		/**
		 * Return total quantity of medicine from current batch, which 
		 * received in this period from other tb-units 
		 * @param main - if true, than consider only not health tb-units; false - only health tb-units
		 * @return
		 */
		private Long getSumTransFromUnit(Boolean main) {
			Long res = null;
			String hql = "select sum(bm.quantity) " +
					"from BatchMovement bm " +
					"where bm.batch.id="+batch.getId() +
						getPeriodCondidtion("bm.movement.date")+
						" and bm.movement.id in (" +
							"select tb.transferItem.movementIn.id " +
							"from TransferBatch tb " +
							"where tb.batch.id="+batch.getId() +
								" and tb.transferItem.transfer.unitTo.id=#{userSession.workingTbunit.id}" +
								(main!=null?" and tb.transferItem.transfer.unitFrom.treatmentHealthUnit="+(main?0:1):"")+
								" and tb.transferItem.transfer.status = 1)";
			List<Object> lst = App.getEntityManager().createQuery(hql).getResultList();
			if (lst.size()!=0){
				if (lst.get(0)!=null) res =(Long)lst.get(0);
			}			
			return res;
		}
		
		/**
		 * Return total dispensing of current batch in this period in working unit
		 * @return
		 */
		public Long getDispensing() {
			Long res = null;
			String hql = "select sum(bm.quantity) " +
					"from MedicineDispensing md " +
					"join md.movements m " +
					"join m.batches bm " +
					"where bm.batch.id="+batch.getId()+
					" and m.tbunit.id=#{userSession.workingTbunit.id} "+
					getPeriodCondidtion("md.dispensingDate");
			List<Object> lst = App.getEntityManager().createQuery(hql).getResultList();
			if (lst.size()!=0){
				if (lst.get(0)!=null) res =(Long)lst.get(0);
			}
			return res;
		}
		
		public ExtBatchItem(){
			super();
		}

		public void setBatch(Batch batch) {
			this.batch = batch;
		}

		public Batch getBatch() {
			return batch;
		}

		public void setStockInIniDate(Long stockInIniDate) {
			this.stockInIniDate = stockInIniDate;
		}

		public Long getStockInIniDate() {
			return stockInIniDate;
		}

		public void setStockInEndDate(Long stockInEndDate) {
			this.stockInEndDate = stockInEndDate;
		}

		public Long getStockInEndDate() {
			return stockInEndDate;
		}
	}
}
