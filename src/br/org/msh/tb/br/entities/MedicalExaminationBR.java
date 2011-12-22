package org.msh.tb.br.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@DiscriminatorValue("br")
public class MedicalExaminationBR extends MedicalExamination {
	private static final long serialVersionUID = 7510532907471481516L;

	@Temporal(TemporalType.DATE)
	@PropertyLog(operations={Operation.NEW, Operation.EDIT})
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
