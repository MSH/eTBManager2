package org.msh.tb.az.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.tb.entities.ExamCulture;

@Entity
@Table(name="examculture_az")
public class ExamCulture_Az extends ExamCulture{
	private static final long serialVersionUID = 206101374456340227L;
	
	@Temporal(TemporalType.DATE)
	private Date datePlating;

	public void setDatePlating(Date datePlating) {
		this.datePlating = datePlating;
	}

	public Date getDatePlating() {
		return datePlating;
	}
}
