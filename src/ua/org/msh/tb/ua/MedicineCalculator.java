package org.msh.tb.ua;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.msh.tb.entities.Batch;
/**
 * The class is responsible for all high-precision math operations with real numbers in the Medicine-module. 
 * For the calculation uses class {@link java.math.BigDecimal}
 * @author A.M.
 */
public class MedicineCalculator {
	/**
	 * Precision of real value, with which it is saved in database 
	 */
	private static int scaleForSave = 6;
	
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
		if (!cP.equals(0))
			uPrice = cP.divide(qC,scaleForSave,RoundingMode.HALF_UP);
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
		cPrice = cPrice.setScale(5, RoundingMode.HALF_UP);
		return cPrice.doubleValue();
	}
	
	/**
	 * Calculate total price by batch
	 * @param qRec - quantity units, which received in batch 
	 * @param qCont - quantity units in container
	 * @param uPrice - price of unit
	 * @return total price by batch
	 */
	public static double calculateTotalPrice(int qRec, int qCont, double uPrice){
		BigDecimal cP = new BigDecimal(calculateContPrice(qCont, uPrice));
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
	public static double calculateTotalPrice(List<Batch> lst){
		BigDecimal res = new BigDecimal(0);
		for (Batch b:lst){
			BigDecimal tp = new BigDecimal(calculateTotalPrice(b.getQuantityReceived(), b.getQuantityContainer(), b.getUnitPrice()));
			res = res.add(tp);
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
			nC = qR.divide(qC);
		return nC.intValue();
	}
}
