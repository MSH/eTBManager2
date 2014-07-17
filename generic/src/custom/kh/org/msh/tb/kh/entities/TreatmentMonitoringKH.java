/**
 * 
 */
package org.msh.tb.kh.entities;

import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Store information about patient weight in addition to medicine in-take. 
 * 
 * @author Utkarsh Srivastava
 *
 */
@Entity
@Table(name="treatmentmonitoringKH")
public class TreatmentMonitoringKH extends TreatmentMonitoring {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2564765080157840436L;

	@PropertyLog(operations={Operation.NEW})
	private int weight;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}


	
}
