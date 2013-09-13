package org.msh.tb.ua.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@DiscriminatorValue("ua")
public class MedicalExaminationUA extends MedicalExamination {
	private static final long serialVersionUID = -23120770338083661L;

	@Temporal(TemporalType.DATE)
	@PropertyLog(operations={Operation.NEW, Operation.DELETE}, messageKey="cases.details.date")
	private Date nextAppointment;


	/**
	 * @return the nextAppointment
	 */
	public Date getNextAppointment() {
		return nextAppointment;
	}

	/**
	 * @param nextAppointment the nextAppointment to set
	 */
	public void setNextAppointment(Date nextAppointment) {
		this.nextAppointment = nextAppointment;
	}
}
