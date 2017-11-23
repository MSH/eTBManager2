package org.msh.tb.reports;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;


@Name("orderLeadTimeReport")
@Scope(ScopeType.CONVERSATION)
public class OrderLeadTimeReport {

	@In(create=true) ReportSelection reportSelection;
	@In(create=true) EntityManager entityManager;
	
	private TBUnitSelection unitSelection;

	private OrderEvent authorizing;
	private OrderEvent shipping;
	private OrderEvent receiving;
	
	private boolean executing;
	

	/**
	 * Generate the report
	 */
	public void generate() {
		executing = true;
		
		authorizing = new OrderEvent();
		shipping = new OrderEvent();
		receiving = new OrderEvent();

		String s = "";
		if (unitSelection != null) {
			if (unitSelection.getSelected() != null)
				s = " and d.unitTo.id = " + unitSelection.getSelected().getId();
			else
			if (unitSelection.getAdminUnit() != null)
				s = " and d.unitTo.adminUnit.code like '" + unitSelection.getAdminUnit().getCode() + "%'";
		}

		String hql = "select d.orderDate, d.approvingDate, d.shippingDate, d.receivingDate " + 
					"from Order d " +
					"where d.orderDate between :dt1 and :dt2 " + s +
					"and d.unitFrom.workspace.id = #{defaultWorkspace.id}";
		List<Object[]> orders = entityManager.createQuery(hql)
				.setParameter("dt1", reportSelection.getIniDate())
				.setParameter("dt2", reportSelection.getDayAfterEndDate())
				.getResultList();

		// monta o resultado
		for (Object[] ord: orders) {
			Date orderDate = (Date)ord[0];
			Date approvingDate = (Date)ord[1];
			Date shippingDate = (Date)ord[2];
			Date receivingDate = (Date)ord[3];
			
			//calculate authorizing
			if (approvingDate != null) {
				authorizing.addOrder(DateUtils.daysBetween(orderDate, approvingDate));
				
				if (shippingDate != null)
					shipping.addOrder(DateUtils.daysBetween(approvingDate, shippingDate));
			}
			else {
				if (shippingDate != null)
					shipping.addOrder(DateUtils.daysBetween(orderDate, shippingDate));
			}
			
			if (receivingDate != null)
				receiving.addOrder(DateUtils.daysBetween(shippingDate, receivingDate));
		}
		
		calculateAverageDays();
	}

	
	/**
	 * Calculate the number of average days and percentage
	 */
	protected void calculateAverageDays() {
		float tot = authorizing.getAverageDays() + shipping.getAverageDays() + receiving.getAverageDays();

		if (tot > 0) {
			authorizing.setPercentage( (float)authorizing.getAverageDays() /  tot * 100);
			shipping.setPercentage( (float)shipping.getAverageDays() /  tot * 100);
			receiving.setPercentage( (float)receiving.getAverageDays() /  tot * 100);
		}
	}


	/**
	 * Return information about authorizing
	 * @return
	 */
	public OrderEvent getAuthorizing() {
		if (authorizing == null)
			generate();
		
		return authorizing;
	}

	public OrderEvent getShipping() {
		if (shipping == null)
			generate();
		
		return shipping;
	}

	public OrderEvent getReceiving() {
		if (receiving == null)
			generate();
		
		return receiving;
	}

	/**
	 * @return the executing
	 */
	public boolean isExecuting() {
		return executing;
	}

	/**
	 * @return the unitSelection
	 */
	public TBUnitSelection getUnitSelection() {
		if (unitSelection == null)
			unitSelection = new TBUnitSelection("unitid", true, TBUnitType.MEDICINE_SUPPLIERS);
		return unitSelection;
	}


	/**
	 * @author Ricardo
	 * Class to store order event info
	 */
	public class OrderEvent {
		private int numOrders;
		private int numDays;
		private float percentage;

		public void addOrder(long numDays) {
			this.numDays += numDays;
			numOrders++;
		}
		public int getNumDays() {
			return numDays;
		}
		public void setNumDays(int numDays) {
			this.numDays = numDays;
		}
		public int getAverageDays() {
			if (numOrders > 0)
				 return Math.round( (float)numDays / (float)numOrders );
			else return 0;
		}
		public int getNumOrders() {
			return numOrders;
		}
		public void setNumOrders(int numOrders) {
			this.numOrders = numOrders;
		}
		/**
		 * @return the percentage
		 */
		public float getPercentage() {
			return percentage;
		}
		/**
		 * @param percentage the percentage to set
		 */
		public void setPercentage(float percentage) {
			this.percentage = percentage;
		}
	}

}
