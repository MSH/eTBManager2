package org.msh.tb.forecasting;

import org.msh.mdrtb.entities.ForecastingBatch;

/**
 * Store information about a month result of a batch
 * @author Ricardo Memoria
 *
 */
public class BatchMonthInfo {

	private ForecastingBatch batch;
	
	private int quantityConsumption;

	/**
	 * @return the batch
	 */
	public ForecastingBatch getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(ForecastingBatch batch) {
		this.batch = batch;
	}

	/**
	 * @return the quantityConsumption
	 */
	public int getQuantityConsumption() {
		return quantityConsumption;
	}

	/**
	 * @param quantityConsumption the quantityConsumption to set
	 */
	public void setQuantityConsumption(int quantityConsumption) {
		this.quantityConsumption = quantityConsumption;
	}
}
