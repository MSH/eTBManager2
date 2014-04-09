/**
 * 
 */
package org.msh.tb.kh.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.validator.NotNull;
import org.msh.tb.entities.TreatmentMonitoring;
import org.msh.tb.entities.enums.TreatmentDayOption;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

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
