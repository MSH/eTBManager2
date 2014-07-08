package org.msh.tb.medicines.movs;

import org.msh.tb.entities.BatchMovement;
import org.msh.tb.entities.Movement;

public class MovementItem {
	private Movement movement;
	private BatchMovement batchMovement;
	private int stockQuantity;
    private int batchQuantity;


    public int getBatchQuantity() {
        return batchQuantity;
    }

    public void setBatchQuantity(int batchQuantity) {
        this.batchQuantity = batchQuantity;
    }

    /**
	 * @return the movement
	 */
	public Movement getMovement() {
		return (batchMovement != null? batchMovement.getMovement(): movement);
	}
	/**
	 * @param movement the movement to set
	 */
	public void setMovement(Movement movement) {
		this.movement = movement;
	}
	/**
	 * @return the stockQuantity
	 */
	public int getStockQuantity() {
		return stockQuantity;
	}
	/**
	 * @param stockQuantity the stockQuantity to set
	 */
	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
	/**
	 * @return the batchMovement
	 */
	public BatchMovement getBatchMovement() {
		return batchMovement;
	}
	/**
	 * @param batchMovement the batchMovement to set
	 */
	public void setBatchMovement(BatchMovement batchMovement) {
		this.batchMovement = batchMovement;
	}
}
