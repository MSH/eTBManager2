/**
 * @author MSANTOS
 * Class responsible to allocate the informations of a respective row on the 
 * Quarterly Stock Position. 
 */
package org.msh.tb.bd;

import org.msh.tb.entities.Medicine;

public class QSPMedicineRow {
 
	private Medicine medicine;
	private Long openingBalance;
	private Long receivedFromCS;
	private Long positiveAdjust;
	private Long negativeAdjust;
	private Long dispensed;
	private Long expired;
	private Long outOfStockDays;
	private Long dispensedForAmcCalc;
	private Long outOfStockForAmcCalc;
	private boolean highlight;
	private Long unitQtdForAmcCalc;
	private Long transferedOutQtd;
	private Long daysQtdInPeriod;
	
	/**
	 * Calculates the closingBalance according to the values of the others parameters.
	 * @return
	 */
	public Long getClosingBalance(){
		return getOpeningBalance() + getReceivedFromCS()+ getPositiveAdjust() - getNegativeAdjust() - getDispensed() - getExpired();
	}
	
	/**
	 * Creates an instance of QSPMedicineRow setting the medicine
	 * @param medicine
	 */
	public QSPMedicineRow(Medicine medicine){
		this.medicine = medicine;
	}
	
	public boolean isEditableRow(){
		if((getOpeningBalance() + getReceivedFromCS() + getPositiveAdjust()) <= 0)
			return false;
		
		return true;
	}
	
	/**
	 * @return the medicine
	 */
	public Medicine getMedicine() {
		return medicine;
	}

	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(Medicine medicine) {
		this.medicine = medicine;
	}

	/**
	 * @return the openingBalance
	 */
	public Long getOpeningBalance() {
		if(openingBalance == null) openingBalance = new Long (0);
		return openingBalance;
	}

	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(Long openingBalance) {
		this.openingBalance = openingBalance;
	}

	/**
	 * @return the receivedFromCS
	 */
	public Long getReceivedFromCS() {
		if(receivedFromCS == null) receivedFromCS = new Long (0);
		return receivedFromCS;
	}

	/**
	 * @param receivedFromCS the receivedFromCS to set
	 */
	public void setReceivedFromCS(Long receivedFromCS) {
		this.receivedFromCS = receivedFromCS;
	}

	/**
	 * @return the positiveAdjust
	 */
	public Long getPositiveAdjust() {
		if(positiveAdjust == null) positiveAdjust = new Long (0);
		return positiveAdjust;
	}

	/**
	 * @param positiveAdjust the positiveAdjust to set
	 */
	public void setPositiveAdjust(Long positiveAdjust) {
		this.positiveAdjust = positiveAdjust;
	}

	/**
	 * @return the negativeAdjust
	 */
	public Long getNegativeAdjust() {
		if(negativeAdjust == null) negativeAdjust = new Long (0);
		if(negativeAdjust < 0) negativeAdjust = negativeAdjust * -1;
		return negativeAdjust;
	}

	/**
	 * @param negativeAdjust the negativeAdjust to set
	 */
	public void setNegativeAdjust(Long negativeAdjust) {
		this.negativeAdjust = negativeAdjust;
	}

	/**
	 * @return the dispensed
	 */
	public Long getDispensed() {
		if(dispensed == null) dispensed = new Long (0);
		if(dispensed < 0) dispensed = dispensed * -1;
		return dispensed;
	}

	/**
	 * @param dispensed the dispensed to set
	 */
	public void setDispensed(Long dispensed) {
		this.dispensed = dispensed;
	}

	/**
	 * @return the expired
	 */
	public Long getExpired() {
		if(expired == null) expired = new Long (0);
		if(expired < 0) expired = expired * -1;
		return expired;
	}

	/**
	 * @param expired the expired to set
	 */
	public void setExpired(Long expired) {
		this.expired = expired;
	}

	/**
	 * @return the outOfStockDays
	 */
	public Long getOutOfStockDays() {
		if(outOfStockDays == null) outOfStockDays = new Long (0);
		return outOfStockDays;
	}

	/**
	 * @param outOfStockDays the outOfStockDays to set
	 */
	public void setOutOfStockDays(Long outOfStockDays) {
		this.outOfStockDays = outOfStockDays;
	}
	/**
	 * @return the dispensedForAmcCalc
	 */
	public Long getDispensedForAmcCalc() {
		if(dispensedForAmcCalc == null) dispensedForAmcCalc = new Long (0);
		if(dispensedForAmcCalc < 0) dispensedForAmcCalc = dispensedForAmcCalc * -1;
		
		if(getUnitQtdForAmcCalc() == 0)
			return new Long(0);
		
		return dispensedForAmcCalc/getUnitQtdForAmcCalc();
	}
	/**
	 * @param dispensedForAmcCalc the dispensedForAmcCalc to set
	 */
	public void setDispensedForAmcCalc(Long dispensedForAmcCalc) {
		this.dispensedForAmcCalc = dispensedForAmcCalc;
	}
	/**
	 * @return the outOfStockForAmcCalc
	 */
	public Long getOutOfStockForAmcCalc() {
		if(outOfStockForAmcCalc == null) outOfStockForAmcCalc = new Long (0);
		if(outOfStockForAmcCalc < 0) outOfStockForAmcCalc = outOfStockForAmcCalc * -1;
		
		if(getUnitQtdForAmcCalc() == 0)
			return new Long(0);
		
		return outOfStockForAmcCalc;
	}
	/**
	 * @param outOfStockForAmcCalc the outOfStockForAmcCalc to set
	 */
	public void setOutOfStockForAmcCalc(Long outOfStockForAmcCalc) {
		this.outOfStockForAmcCalc = outOfStockForAmcCalc;
	}

	/**
	 * @return the unitQtdForAmcCalc
	 */
	public Long getUnitQtdForAmcCalc() {
		if(unitQtdForAmcCalc == null) unitQtdForAmcCalc = new Long (0);
		if(unitQtdForAmcCalc < 0) unitQtdForAmcCalc = unitQtdForAmcCalc * -1;
		return unitQtdForAmcCalc;
	}

	/**
	 * @param unitQtdForAmcCalc the unitQtdForAmcCalc to set
	 */
	public void setUnitQtdForAmcCalc(Long unitQtdForAmcCalc) {
		this.unitQtdForAmcCalc = unitQtdForAmcCalc;
	}

	/**
	 * @return the highlight
	 */
	public boolean isHighlight() {
		return highlight;
	}

	/**
	 * @param highlight the highlight to set
	 */
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	/**
	 * @return the transferedOutQtd
	 */
	public Long getTransferedOutQtd() {
		if(transferedOutQtd == null) transferedOutQtd = new Long (0);
		if(transferedOutQtd < 0) transferedOutQtd = transferedOutQtd * -1;
		return transferedOutQtd;
	}

	/**
	 * @param transferedOutQtd the transferedOutQtd to set
	 */
	public void setTransferedOutQtd(Long transferedOutQtd) {
		this.transferedOutQtd = transferedOutQtd;
	}

	/**
	 * @return the amc calculated based on a Quarter information
	 */
	public Long getAmc() {
		Long amc = new Long(0);

		if(getDispensedForAmcCalc() == 0 || getOutOfStockAverage() >= 90 || getUnitQtdForAmcCalc() == 0)
			return new Long(0);

		double disp = getDispensedForAmcCalc();
		double days = 90 - getOutOfStockAverage();
		double result = disp / days;

		result = result * 30;

		amc = (new Double(result)).longValue();


		if(amc == null) amc = new Long (0);
		if(amc < 0) amc = amc * -1;

		return amc;
	}

	/**
	 * @return the amc based on a ini date and end date Information
	 */
	public Long getAmc2() {
		Long amc = new Long(0);

		if(getDispensed() == 0 || getDaysQtdInPeriod() == 0)
			return amc;

		double disp = getDispensed();
		double days = getDaysQtdInPeriod();
		double result = disp / days;

		result = result * 30;

		amc = (new Double(result)).longValue();


		if(amc == null) amc = new Long (0);
		if(amc < 0) amc = amc * -1;

		return amc;
	}

	/**
	 * @return the Estimated Months Of Stock rounded
	 */
	public Double getEstimatedMonthsOfStock() {
		Double months = new Double(0);
		Double amc = new Double(getAmc());
		Double closBal = new Double(getClosingBalance());

		if(amc == 0)
			return null;

		//Calculates the amount of months
		months = closBal / amc;
		months = roundRule(months);

		return months;
	}

	/**
	 * @return the Estimated Months Of Stock rounded
	 */
	public Double getEstimatedMonthsOfStock2() {
		Double months;
		Double amc = new Double(getAmc2());
		Double closBal = new Double(getClosingBalance());

		if(amc == 0)
			return null;

		//Calculates the amount of months
		months = closBal / amc;
		months = roundRule(months);

		return months;
	}
	
	public Double getOutOfStockAverage() {
		Double out = new Double(getOutOfStockForAmcCalc());
		Double unitsQtd = new Double(getUnitQtdForAmcCalc());
		
		if(unitsQtd == 0)
			return new Double(0);
	
		out = outOfStockForAmcCalc/unitsQtd;
		out = roundRule(out);
		
		return out;
	}
	
	private Double roundRule(Double number){
		//Round it down according to the rule. Ex. If 1,1 to 1,4 the result is 1, if 1,6 to minus than 1 the result is 1,5.
		String afterPoint = number.toString();
		String beforePoint = number.toString();
		
		if(afterPoint.indexOf(".") > -1){
			afterPoint = afterPoint.substring(afterPoint.indexOf("."));
			
			if(afterPoint.equalsIgnoreCase(".0"))
				return number;
			
			afterPoint = "0"+afterPoint;
			beforePoint = beforePoint.substring(0, beforePoint.indexOf("."));
			
			//round after point value
			Double afterPointD = Double.parseDouble(afterPoint);
			if(afterPointD == 0.5)
				number = Double.parseDouble(beforePoint) + afterPointD;
			if(afterPointD > 0 && afterPointD < 0.5)
				number = Double.parseDouble(beforePoint);
			if(afterPointD > 0.5 && afterPointD < 1)
				number = Double.parseDouble(beforePoint) + 0.5;
				
		}
		
		return number;
	}

	public Long getDaysQtdInPeriod() {
		return daysQtdInPeriod;
	}

	public void setDaysQtdInPeriod(Long daysQtdInPeriod) {
		this.daysQtdInPeriod = daysQtdInPeriod;
	}
}
