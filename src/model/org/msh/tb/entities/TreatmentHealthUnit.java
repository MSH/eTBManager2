package org.msh.tb.entities;

 import java.io.Serializable;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.msh.utils.date.Period;


@Entity
@Table(name="treatmenthealthunit")
public class TreatmentHealthUnit implements Serializable {
	private static final long serialVersionUID = -7878679577509206518L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ID")
	@NotNull
	private TbCase tbcase;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UNIT_ID")
	@NotNull
	private Tbunit tbunit;

	@Embedded
	private Period period = new Period();
	
	
	/**
	 * Indicate if case is being transferred to this health unit
	 */
	private boolean transferring;

	
	public boolean isTransferring() {
		return transferring;
	}


	public void setTransferring(boolean transferring) {
		this.transferring = transferring;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TbCase getTbcase() {
		return tbcase;
	}

	public void setTbCase(TbCase tbcase) {
		this.tbcase = tbcase;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}

	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}


	public Period getPeriod() {
		return period;
	}


	public void setPeriod(Period period) {
		this.period = period;
	}
}
