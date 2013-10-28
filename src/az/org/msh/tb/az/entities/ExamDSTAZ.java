package org.msh.tb.az.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.ExamDST;

@Entity
@DiscriminatorValue("az")
public class ExamDSTAZ extends ExamDST{
	private static final long serialVersionUID = 206101374456340227L;
	
	@Temporal(TemporalType.DATE)
	private Date datePlating;
	@Temporal(TemporalType.DATE)
	private Date dateTestBegin;

	public void setDatePlating(Date datePlating) {
		this.datePlating = datePlating;
	}

	public Date getDatePlating() {
		return datePlating;
	}

	public void setDateTestBegin(Date dateTestBegin) {
		this.dateTestBegin = dateTestBegin;
	}

	public Date getDateTestBegin() {
		return dateTestBegin;
	}
}
