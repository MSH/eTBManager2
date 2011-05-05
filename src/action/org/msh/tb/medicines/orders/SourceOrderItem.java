package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.OrderItem;
import org.msh.tb.entities.Source;

/**
 * @author Ricardo
 *
 * Helper class to store order items by medicine source
 */
public class SourceOrderItem {

	public class OrderItemAux {
		private int stockQuantity;
		private OrderItem item;
		private Integer approvedQuantity;
		private boolean unavailable;
		
		public boolean isUnavailable() {
			return unavailable;
		}
		public void setUnavailable(boolean unavailable) {
			this.unavailable = unavailable;
		}
		public Integer getApprovedQuantity() {
			return approvedQuantity;
		}
		public void setApprovedQuantity(Integer approvedQuantity) {
			this.approvedQuantity = approvedQuantity;
		}
		public int getStockQuantity() {
			return stockQuantity;
		}
		public void setStockQuantity(int stockQuantity) {
			this.stockQuantity = stockQuantity;
		}
		public OrderItem getItem() {
			return item;
		}
		public void setItem(OrderItem item) {
			this.item = item;
		}
	}
	
	private Source source;
	private List<OrderItemAux> items = new ArrayList<OrderItemAux>();
	
	/**
	 * Returns order item by medicine
	 * @param m
	 * @return
	 */
	public OrderItemAux itemByMedicine(Medicine m) {
		OrderItemAux aux = findItemByMedicine(m); 
			
		if (aux != null)
			return aux;
		
		OrderItem it = new OrderItem();
		it.setSource(source);
		it.setMedicine(m);

		return addOrderItem(it);
	}
	
	public OrderItemAux findItemByMedicine(Medicine m) {
		for (OrderItemAux it: items) {
			if (it.getItem().getMedicine().equals(m))
				return it;
		}
		return null;
	}
	
	public OrderItemAux addOrderItem(OrderItem it) {
		OrderItemAux aux = new OrderItemAux();
		aux.setItem(it);
		items.add(aux);
		return aux;
	}
	
	public Source getSource() {
		return source;
	}
	
	public List<OrderItemAux> getItems() {
		return items;
	}

	public SourceOrderItem(Source source) {
		super();
		this.source = source;
	}
}
