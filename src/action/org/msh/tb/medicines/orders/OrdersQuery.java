package org.msh.tb.medicines.orders;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Order;
import org.msh.mdrtb.entities.enums.OrderStatus;
import org.msh.utils.EntityQuery;




@Name("orders")
public class OrdersQuery extends EntityQuery<Order> {
	private static final long serialVersionUID = 5333458213098681671L;

	private Integer month;
	private Integer year;
	private OrderStatus status;
	
	private static final String[] restrictions = {
			"month(o.orderDate) = #{orders.month} + 1",
			"year(o.orderDate) = #{orders.year}",
			"o.status = #{orders.status}"};
	
	private String condition = " where (o.tbunitTo.id = #{userSession.tbunit.id} or " +
			"o.tbunitFrom.id = #{userSession.tbunit.id} or " +
			"o.tbunitTo.authorizerUnit.id = #{userSession.tbunit.id})";
			
	
	@Override
	public String getEjbql() {
		return "from Order o join fetch o.tbunitFrom".concat(condition);
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Order o".concat(condition);
	}

	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	@Override
	public Integer getMaxResults() {
		return 30;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();

		if (s == null)
			 return "o.orderDate desc";
		else return s;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
}
