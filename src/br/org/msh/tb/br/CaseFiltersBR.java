package org.msh.tb.br;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.CaseFilters;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.PatientType;

@Name("caseFiltersBR")
@Scope(ScopeType.SESSION)
public class CaseFiltersBR {
	
	@In(create=true) CaseFilters caseFilters;

	private DrugResistanceType drugResistanceType;
	
	private FieldValue schemaChangeType;

	private FieldValue outcomeRegimenChanged;

	private DrugResistanceType outcomeResistanceType;

	
	public void clear() {
		drugResistanceType = null;
		schemaChangeType = null;
		outcomeRegimenChanged = null;
		outcomeResistanceType = null;
	}

	/**
	 * @return the drugResistanceType
	 */
	public DrugResistanceType getDrugResistanceType() {
		return this.drugResistanceType;
	}

	/**
	 * @param drugResistanceType the drugResistanceType to set
	 */
	public void setDrugResistanceType(DrugResistanceType drugResistanceType) {
		this.drugResistanceType = drugResistanceType;
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
	public DrugResistanceType getOutcomeResistanceType() {
		return (caseFilters.getCaseState() == CaseState.MDR_CASE? outcomeResistanceType : null);
	}

	/**
	 * @param outcomeResistanceType the outcomeResistanceType to set
	 */
	public void setOutcomeResistanceType(DrugResistanceType outcomeResistanceType) {
		this.outcomeResistanceType = outcomeResistanceType;
	}
}
