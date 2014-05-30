package org.msh.tb.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;
import org.msh.tb.transactionlog.PropertyLog;

@Entity
@Table(name="sequenceinfo")
public class SequenceInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Column(name="seq_name", length=50)
	@NotNull
	private String sequence; 
	
	private int number;

	/**
	 * The workspace of this entity
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="WORKSPACE_ID")
	@NotNull
	@PropertyLog(ignore=true)
	private Workspace workspace;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
