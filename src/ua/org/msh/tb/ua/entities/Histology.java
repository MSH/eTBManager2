package org.msh.tb.ua.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.CaseData;
import org.msh.tb.ua.entities.enums.HistologyResult;

/**
 * Store information about a histology result
 * @author Ricardo Memoria
 *
 */
@Entity
@Table(name="histology")
public class Histology extends CaseData {

	private HistologyResult result;

	public HistologyResult getResult() {
		return result;
	}

	public void setResult(HistologyResult result) {
		this.result = result;
	}
}
