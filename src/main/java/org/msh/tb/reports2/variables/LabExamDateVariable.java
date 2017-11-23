package org.msh.tb.reports2.variables;


/**
 * Variable that handle number of exam tests by month/year or just year
 * 
 * @author Ricardo Memoria
 *
 */
public class LabExamDateVariable extends CaseItemDateVariable {

	public LabExamDateVariable(String id, String label, String fieldName,
			boolean yearOnly, UnitType unitType) {
		super(id, label, fieldName, yearOnly);
		setUnitType(unitType);
	}

}
