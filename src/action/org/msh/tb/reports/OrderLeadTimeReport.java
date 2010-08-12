package org.msh.tb.reports;

import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.utils.date.DateUtils;


@Name("orderLeadTimeReport")
public class OrderLeadTimeReport {

	@In(create=true) ReportSelection reportSelection;
	@In(create=true) EntityManager entityManager;

	/**
	 * @author Ricardo
	 * Class to store order event info
	 */
	public class OrderEvent {
		private int numOrders;
		private int numDays;

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
				 return numDays / numOrders;
			else return 0;
		}
		public int getNumOrders() {
			return numOrders;
		}
		public void setNumOrders(int numOrders) {
			this.numOrders = numOrders;
		}
	}

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

		String s;
		if (reportSelection.getTbunit() != null)
			 s = " and d.tbunitTo.id = " + reportSelection.getTbunit().getId();
		else s = "";
		
		String hql = "select d.orderDate, d.approvingDate, d.shippingDate, d.receivingDate " + 
					"from Order d " +
					"where d.approvingDate is not null" + s +
					" and d.orderDate between :dt1 and :dt2 " +
					"and d.tbunitFrom.workspace.id = #{defaultWorkspace.id}";
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
			authorizing.addOrder(DateUtils.daysBetween(orderDate, approvingDate));
			
			if (shippingDate != null)
				shipping.addOrder(DateUtils.daysBetween(approvingDate, shippingDate));
			
			if (receivingDate != null)
				receiving.addOrder(DateUtils.daysBetween(shippingDate, receivingDate));
		}
	}

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
}
