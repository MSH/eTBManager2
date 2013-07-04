package org.msh.tb.ua;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;

/**
 * Seam component for call methods of {@link MedicineCalculator} from JSF-pages
 * */
@Name("medCalcCont")
public class MedicineCalculatorController {
	
	public double calculateTotalPrice(List<Batch> lst) {
		return MedicineCalculator.calculateTotalPrice(lst);
	}
	
	public int calculateNumContainers(int qCont, int qRec) {
		return MedicineCalculator.calculateNumContainers(qCont, qRec);
	}
	
	public double calculateContPrice(int qCont, double uPrice) {
		return MedicineCalculator.calculateContPrice(qCont, uPrice);
	}
	
	public double calculateTotalPrice(int qRec, int qCont, double uPrice) {
		return MedicineCalculator.calculateTotalPrice(qRec, qCont, uPrice);
	}
}
