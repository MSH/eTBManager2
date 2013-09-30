package org.msh.tb.ua.medicines;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.OrderBatch;
import org.msh.tb.entities.Regimen;
import org.msh.tb.medicines.dispensing.DispensingRow;
import org.msh.tb.medicines.orders.OrderHome;
import org.msh.tb.medicines.orders.OrderShippingHome;
import org.msh.tb.medicines.orders.SourceOrderItem;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;
import org.msh.tb.ua.utils.MedicineCalculator;
import org.msh.tb.ua.utils.MedicineFunctions;

/**
 * Seam component for call static methods and other methods for medicine unit
 * @author A.M.
 * */
@Name("medCalcCont")
public class MedicineController {
	
	public double calculateTotalPrice(List lst) {
		if (lst==null) 
			return 0F;
		return MedicineCalculator.calculateTotalPrice(lst,3);
	}
	
	public double calculateTotalPriceReceived(List lst) {
		if (lst==null) 
			return 0F;
		return MedicineCalculator.calculateTotalPriceReceived(lst,3);
	}

	public int calculateNumContainers(int qCont, int qRec) {
		return MedicineCalculator.calculateNumContainers(qCont, qRec);
	}
	
	public double calculateContPrice(int qCont, double uPrice) {
		return MedicineCalculator.calculateContPrice(qCont, uPrice);
	}
	
	public double calculateTotalPrice(int qRec, int qCont, double uPrice) {
		return MedicineCalculator.calculateTotalPrice(qRec, qCont, uPrice,3);
	}
	
	public double calculateUnitPriceAvg(double totPrice, int quantity){
		return MedicineCalculator.calculateUnitPriceAvg(totPrice, quantity);
	}
	
	/**
	 * Override {@link OrderShippingHome#initialize()}
	 */
	public void initializeOrderShippingHome() {
		OrderShippingHome osh = (OrderShippingHome) App.getComponent("orderShippingHome");
		osh.initialize();
		OrderHome orderHome = (OrderHome) App.getComponent("orderHome");
		
		for (SourceOrderItem s: orderHome.getSources()) 
			for (OrderItemAux item:s.getItems()){
				item.getItem().setShippedQuantity(0);
				for (OrderBatch ob:item.getItem().getBatches()){
					int q = ob.getBatch().getQuantityContainer()*MedicineCalculator.calculateNumContainers(ob.getBatch().getQuantityContainer(), ob.getQuantity());
					ob.setQuantity(q);
					item.getItem().setShippedQuantity(item.getItem().getShippedQuantity()+q);
				}
				
			}

	}
	/**
	 * Return true if in list of DispensingRows exist row, which contains medicine, necessary in current regimen
	 * @param row
	 * @param reg - regiment of current tbcase
	 * @return
	 */
	public boolean containsInRegimen(List<DispensingRow> rows) {
		CaseHome ch = (CaseHome) App.getComponent("caseHome");
		Regimen reg = ch.getTbCase().getRegimen();
		for (DispensingRow row:rows){
			if (containsInRegimen(row, reg))
				return true;
		}
		return false;
	}
	
	/**
	 * Return true if current DispensingRow contains medicine, necessary in current regimen
	 * @param row
	 * @param reg - regiment of current tbcase
	 * @return
	 */
	public boolean containsInRegimen(DispensingRow row, Regimen reg) {
		if (row==null) return false;
		for (MedicineRegimen mr:reg.getMedicines())
			if (mr.getMedicine().getId().intValue() == row.getBatch().getMedicine().getId().intValue() && mr.getDefaultSource().getId() == row.getSource().getId())
				return true;
		return false;
	}
	
	public boolean isExpiringBatch(Object o){
		return MedicineFunctions.isExpiringBatch(o);
	}
	
	public static boolean isExpiringRegistCard(Object o){
		return MedicineFunctions.isExpiringRegistCard(o);
	}
}
