package org.msh.tb.entities;

import java.io.Serializable;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.NotNull;
import org.msh.tb.transactionlog.PropertyLog;



@MappedSuperclass
public class WSObject implements Serializable {
	private static final long serialVersionUID = 179043557345585531L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="WORKSPACE_ID")
	@NotNull
	@PropertyLog(ignore=true)
	private Workspace workspace;

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
