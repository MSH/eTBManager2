package org.msh.tb.ua.medicines;

import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.medicines.movs.StockAdjustmentHome;
import org.msh.tb.medicines.movs.StockAdjustmentHome.StockPositionItem;
import org.msh.tb.ua.utils.MedicineCalculator;

@Name("stockAdjustmentUAHome")
public class StockAdjustmentUAHome {
	@In(create=false) StockAdjustmentHome stockAdjustmentHome;
	
	private int numContainers;
	private double containerPrice;
	private double totalPrice;
	private Batch batch;
	
	
	public int getNumContainers() {
		if (getBatch() == null) return 0;
		if (numContainers == 0)
			numContainers = MedicineCalculator.calculateNumContainers(getBatch().getQuantityContainer(), getBatch().getQuantityReceived());
		return numContainers;
	}
	
	public double getContainerPrice() {
		if (getBatch() == null) return 0F;
		if (containerPrice == 0)
			containerPrice = MedicineCalculator.calculateContPrice(getBatch().getQuantityContainer(), getBatch().getUnitPrice());
		return containerPrice;
	}
	
	public double getTotalPrice() {
		if (getBatch() == null) return 0F;
		if (totalPrice == 0)
			totalPrice = MedicineCalculator.calculateTotalPrice(getBatch().getQuantityReceived(),getBatch().getQuantityContainer(), getBatch().getUnitPrice(),5);
		return totalPrice;
	}
	
	public void setNumContainers(int numContainers) {
		this.numContainers = numContainers;
	}
	
	public void setContainerPrice(double containerPrice) {
		this.containerPrice = containerPrice;
	}
	
	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Batch getBatch() {
		if (stockAdjustmentHome == null) return null;
		batch = stockAdjustmentHome.getBatchQuantity().getBatch();
		return batch;
	}
	
	/**
	 * Call {@link stockAdjustmentHome#getItems()} and recount total quantities
	 * @return
	 */
	public List<StockPositionItem> getItems() {
		//recount summary quantities
		Iterator<StockPositionItem> it = stockAdjustmentHome.getItems().iterator();
		while(it.hasNext()){
			StockPositionItem item = it.next();
			int q = 0;
			for (BatchQuantity b: item.getBatches())
			if (!b.getBatch().isExpired()){
				q+=b.getQuantity();
			}
			item.getStockPosition().setQuantity(q);
		}
		return stockAdjustmentHome.getItems();
	}

	/**
	 * Override {@link stockAdjustmentHome#deleteBatch()} 
	 * If there is last batch in stock by it's medicine, 
	 * 		then delete stockposition by this medicine from database 
	 * @return
	 */
	public String deleteBatch() {
		String resdel = stockAdjustmentHome.deleteBatch();

		StockPositionItem meditem = stockAdjustmentHome.findStockPosition(stockAdjustmentHome.getBatchQuantity().getBatch().getMedicine());
		if (meditem!=null)
			if (meditem.getBatches().size() == 1)
				delMedicineFromList(meditem);
		
		//App.getEntityManager().remove(stockAdjustmentHome.getBatchQuantity().getBatch());
		/*App.getEntityManager().remove(stockAdjustmentHome.getBatchQuantity());
		try{
		App.getEntityManager().flush();}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}*/
		return resdel;
	}

	/**
	 * Delete current stockposition from database
	 * @param meditem
	 */
	private void delMedicineFromList(StockPositionItem meditem) {
		Iterator<StockPositionItem> it = getItems().iterator();
		while (it.hasNext()){
			StockPositionItem item = it.next();
			if (item.equals(meditem)){
				it.remove();
				break;
			}
		}
		//App.getEntityManager().remove(meditem.getStockPosition());
		
	}
	
}
