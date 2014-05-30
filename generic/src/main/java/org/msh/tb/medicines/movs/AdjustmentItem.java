package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.List;

import org.msh.tb.entities.StockPosition;



public class AdjustmentItem {

	private StockPosition stockPosition;
	private Integer quantity;
	private String comment;
	private List<BatchAdjustmentItem> batches = new ArrayList<BatchAdjustmentItem>();


	public List<BatchAdjustmentItem> getBatches() {
		return batches;
	}
	public void setBatches(List<BatchAdjustmentItem> batches) {
		this.batches = batches;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public StockPosition getStockPosition() {
		return stockPosition;
	}
	public void setStockPosition(StockPosition stockPosition) {
		this.stockPosition = stockPosition;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
