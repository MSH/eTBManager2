package org.msh.tb.ua.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.TransferBatch;
import org.msh.tb.medicines.MedicineManStartHome.BatchInfo;
/**
 * The class is responsible for all high-precision math operations with real numbers in the Medicine-module. 
 * For the calculation uses class {@link java.math.BigDecimal}
 * @author A.M.
 */
public class MedicineCalculator {
	/**
	 * Precision of real value, with which it is saved in database 
	 */
	private static int scaleForSave = 10;
	
	/**
	 * Calculate unit price using by price of container and quantity units in container
	 * @param contPrice
	 * @param quantCont
	 * @return  unit price
	 */
	public static float calculateUnitPrice(double contPrice, int quantCont) {
		BigDecimal cP = new BigDecimal(contPrice);
		BigDecimal qC = new BigDecimal(quantCont);
		BigDecimal uPrice = new BigDecimal(0);
		if (!cP.equals(0) && qC.intValue()!=0)
			uPrice = cP.divide(qC,scaleForSave,RoundingMode.HALF_UP);
		return uPrice.floatValue();
	}
	
	/**
	 * Calculate average unit price using total price and total quantity units
	 * @param totPrice
	 * @param quantity
	 * @return  unit price average
	 */
	public static float calculateUnitPriceAvg(double totPrice, int quantity) {
		BigDecimal tP = new BigDecimal(totPrice);
		BigDecimal q = new BigDecimal(quantity);
		BigDecimal uPrice = new BigDecimal(0);
		if (!tP.equals(0) && q.intValue()!=0)
			uPrice = tP.divide(q,scaleForSave,RoundingMode.HALF_UP);
		return uPrice.floatValue();
	}
	
	/**
	 * Calculate container price using by quantity units in container and price of unit
	 * @param qCont
	 * @param uPrice
	 * @return container price
	 */
	public static double calculateContPrice(int qCont, double uPrice){
		BigDecimal qC = new BigDecimal(qCont);
		BigDecimal uP = new BigDecimal(uPrice);
		uP = uP.setScale(scaleForSave,RoundingMode.HALF_UP);
		BigDecimal cPrice = uP.multiply(qC);
		cPrice = cPrice.setScale(3, RoundingMode.HALF_UP);
		/*for (int i = 0; i < 6; i++) {
			BigDecimal cPriceRound = cPrice.setScale(i, RoundingMode.HALF_UP);
			BigDecimal d = cPriceRound.subtract(cPrice).abs();
			BigDecimal eps = new BigDecimal("0.00001");
			if (d.compareTo(eps)<=0){
				cPrice = cPrice.setScale(i-1, RoundingMode.HALF_UP);
				break; 
			}
		}*/
		return cPrice.doubleValue();
	}
	
	/**
	 * Calculate container price for batch
	 * @param b
	 * @return container price
	 */
	public static double calculateContPrice(Batch b){
		return calculateContPrice(b.getQuantityContainer(), b.getUnitPrice());
	}
	
	/**
	 * Calculate total price by batch
	 * @param qRec - quantity units, which received in batch 
	 * @param qCont - quantity units in container
	 * @param uPrice - price of unit
	 * @return total price by batch
	 */
	public static double calculateTotalPrice(int qRec, int qCont, double uPrice, int scaleCPrice){
		BigDecimal cP = new BigDecimal(calculateContPrice(qCont, uPrice));
		cP = cP.setScale(scaleCPrice, RoundingMode.HALF_UP);
		BigDecimal nC = new BigDecimal(calculateNumContainers(qCont, qRec));
		//BigDecimal qR = new BigDecimal(qRec);
		//BigDecimal uP = new BigDecimal(uPrice);
		BigDecimal tPrice = cP.multiply(nC);
		return tPrice.doubleValue();
	}
	
	/**
	 * Calculate total price by list of batches
	 * @param lst - list if bathes
	 * @return total price by list of batches
	 */
	public static double calculateTotalPrice(List lst, int scaleCPrice){
		BigDecimal res = new BigDecimal(0);
		if (!lst.isEmpty()){
			if (lst.get(0) instanceof Batch)
				for (Batch b:(List<Batch>)lst){
					BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantityReceived(), b.getQuantityContainer(), b.getUnitPrice(),scaleCPrice));
					tp = tp.setScale(2,RoundingMode.HALF_UP);
					res = res.add(tp);
				}
			if (lst.get(0) instanceof BatchInfo)
				for (BatchInfo b:(List<BatchInfo>)lst){
					BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantity(), b.getBatch().getQuantityContainer(), b.getBatch().getUnitPrice(),scaleCPrice));
					tp = tp.setScale(2,RoundingMode.HALF_UP);
					res = res.add(tp);
				}
			if (lst.get(0) instanceof TransferBatch)
				for (TransferBatch b:(List<TransferBatch>)lst){
					BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantity(), b.getBatch().getQuantityContainer(), b.getBatch().getUnitPrice(),scaleCPrice));
					tp = tp.setScale(2,RoundingMode.HALF_UP);
					res = res.add(tp);
				}
			if (lst.get(0) instanceof BatchQuantity)
				for (BatchQuantity b:(List<BatchQuantity>)lst){
					BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantity(), b.getBatch().getQuantityContainer(), b.getBatch().getUnitPrice(),scaleCPrice));
					tp = tp.setScale(2,RoundingMode.HALF_UP);
					res = res.add(tp);
				}
			if (lst.get(0) instanceof Movement)
				for (Movement mov:(List<Movement>)lst)
					for (BatchMovement bm:mov.getBatches()){
						BigDecimal tp = new BigDecimal(calculateTotalPrice(bm.getBatch().getQuantityReceived(), bm.getBatch().getQuantityContainer(), bm.getBatch().getUnitPrice(),scaleCPrice));
						tp = tp.setScale(2,RoundingMode.HALF_UP);
						res = res.add(tp);
				}
			if (lst.get(0) instanceof BatchMovement)
				for (BatchMovement bm:(List<BatchMovement>)lst){
					BigDecimal tp = new BigDecimal(calculateTotalPrice(bm.getBatch().getQuantityReceived(), bm.getBatch().getQuantityContainer(), bm.getBatch().getUnitPrice(),scaleCPrice));
					tp = tp.setScale(2,RoundingMode.HALF_UP);
					res = res.add(tp);
				}
			
		}
		return res.doubleValue();
	}
	
	/**
	 * Calculate total price by list of batches
	 * @param lst - list if bathes
	 * @return total price by list of batches
	 */
	public static double calculateTotalPriceReceived(List lst, int scaleCPrice){
		BigDecimal res = new BigDecimal(0);
		if (!lst.isEmpty()){
			if (lst.get(0) instanceof TransferBatch)
				for (TransferBatch b:(List<TransferBatch>)lst){
					if (b.getQuantityReceived() != null){
						BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantityReceived(), b.getBatch().getQuantityContainer(), b.getBatch().getUnitPrice(),scaleCPrice));
						res = res.add(tp);
					}
				}
		}
		return res.doubleValue();
	}
	
	
	/**
	 * Calculate numbers of containers in batch
	 * @param qCont - quantity units in container
	 * @param qRec - quantity units, which received in batch 
	 * @return numbers of containers
	 */
	public static int calculateNumContainers(int qCont, int qRec){
		BigDecimal qC = new BigDecimal(qCont);
		BigDecimal qR = new BigDecimal(qRec);
		BigDecimal nC = new BigDecimal(0);
		if (qC.intValue()!=0)
			nC = qR.divide(qC,RoundingMode.CEILING);
		return nC.intValue();
	}
}
