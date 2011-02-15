package org.msh.tb.br;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.PatientType;
import org.msh.tb.cases.CaseFilters;

@Name("caseFiltersBR")
@Scope(ScopeType.SESSION)
public class CaseFiltersBR {
	
	@In(create=true) CaseFilters caseFilters;

	private FieldValue resistanceType;
	
	private FieldValue schemaChangeType;

	private FieldValue outcomeRegimenChanged;

	private FieldValue outcomeResistanceType;

	
	@Observer("cases-clear-filters")
	public void clear() {
		resistanceType = null;
		schemaChangeType = null;
		outcomeRegimenChanged = null;
		outcomeResistanceType = null;
	}

	/**
	 * @return the resistanceType
	 */
	public FieldValue getResistanceType() {
		return (caseFilters.getClassification() == CaseClassification.DRTB? resistanceType: null);
	}

	/**
	 * @param resistanceType the resistanceType to set
	 */
	public void setResistanceType(FieldValue resistanceType) {
		this.resistanceType = resistanceType;
	}

	/**
	 * @return the schemaChangeType
	 */
	public FieldValue getSchemaChangeType() {
		return (caseFilters.getPatientType() == PatientType.SCHEMA_CHANGED? schemaChangeType : null); 
	}

	/**
	 * @param schemaChangeType the schemaChangeType to set
	 */
	public void setSchemaChangeType(FieldValue schemaChangeType) {
		this.schemaChangeType = schemaChangeType;
	}

	/**
	 * @return the outcomeRegimenChanged
	 */
	public FieldValue getOutcomeRegimenChanged() {
		return (caseFilters.getCaseState() == CaseState.REGIMEN_CHANGED? outcomeRegimenChanged : null);
	}

	/**
	 * @param outcomeRegimenChanged the outcomeRegimenChanged to set
	 */
	public void setOutcomeRegimenChanged(FieldValue outcomeRegimenChanged) {
		this.outcomeRegimenChanged = outcomeRegimenChanged;
	}

	/**
	 * @return the outcomeResistanceType
	 */
	public FieldValue getOutcomeResistanceType() {
		return (caseFilters.getCaseState() == CaseState.MDR_CASE? outcomeResistanceType : null);
	}

	/**
	 * @param outcomeResistanceType the outcomeResistanceType to set
	 */
	public void setOutcomeResistanceType(FieldValue outcomeResistanceType) {
		this.outcomeResistanceType = outcomeResistanceType;
	}
}
