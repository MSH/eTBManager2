package org.msh.mdrtb.entities;

import javax.persistence.Entity;

import org.msh.mdrtb.entities.enums.HistologyResult;

/**
 * Store information about a histology result
 * @author Ricardo Memoria
 *
 */
@Entity
public class Histology extends CaseData {

	private HistologyResult result;

	public HistologyResult getResult() {
		return result;
	}

	public void setResult(HistologyResult result) {
		this.result = result;
	}
}
