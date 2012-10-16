package org.msh.tb.az.entities;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.msh.tb.entities.ExamMicroscopy;

@Entity
@DiscriminatorValue("az")
public class ExamMicroscopyAZ extends ExamMicroscopy{
	private static final long serialVersionUID = 206101374456340227L;
	
	private Date datePlating;

	public void setDatePlating(Date datePlating) {
		this.datePlating = datePlating;
	}

	public Date getDatePlating() {
		return datePlating;
	}
}
