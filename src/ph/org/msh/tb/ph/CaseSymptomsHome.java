package org.msh.tb.ph;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.misc.FieldsQuery;
import org.msh.tb.ph.entities.CaseSymptom;

@Name("caseSymptomsHome")
public class CaseSymptomsHome {

	@In EntityManager entityManager;
	@In CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<CaseSymptom> caseSymptoms;
	private boolean editing;
	
	
	
	/**
	 * Return list of symptoms from the case
	 * @return {@link List} of {@link CaseSymptom} objects
	 */
	public List<CaseSymptom> getCaseSymptoms() {
		if (caseSymptoms == null)
			createSymptomsList();
		return caseSymptoms;
	}
	
	
	/**
	 * Save the case symptoms
	 */
	public void save() {
		if (caseSymptoms == null)
			return;
		
		if (!editing)
			throw new RuntimeException("SymptomsHome not in edit mode");
		
		for (CaseSymptom sym: caseSymptoms) {
			if (!sym.isChecked()) {
				if (entityManager.contains(sym))
					entityManager.remove(sym);
			}
			else entityManager.persist(sym);
		}
	}
	

	/**
	 * Create synptoms list
	 */
	protected void createSymptomsList() {
		caseSymptoms = entityManager
			.createQuery("from CaseSymptom s where s.tbcase.id = #{caseHome.instance.id} order by s.symptom.name.name1")
			.getResultList();

//		if (editing) {
			List<FieldValue> fields = fieldsQuery.getSymptoms();
			for (FieldValue field: fields) {
				CaseSymptom aux = findCaseSymptom(field);
				if (aux == null) {
					aux = new CaseSymptom();
					aux.setSymptom(field);
					aux.setTbcase(caseHome.getInstance());
					caseSymptoms.add(aux);
				}
				else aux.setChecked(true);
//			}
			
			// sort the items
			Collections.sort(caseSymptoms, new Comparator<CaseSymptom>() {
				public int compare(CaseSymptom o1, CaseSymptom o2) {
					return o1.getSymptom().getName().toString().compareTo(o2.getSymptom().getName().toString());
				}
			});
		}
	}
	

	/**
	 * Search for a case symptom by its symptom
	 * @param fieldValue
	 * @return {@link CaseSymptom} instance
	 */
	private CaseSymptom findCaseSymptom(FieldValue fieldValue) {
		for (CaseSymptom aux: caseSymptoms) {
			if (aux.getSymptom().equals(fieldValue)) {
				return aux;
			}
		}
		return null;
	}

	/**
	 * @return the editing
	 */
	public boolean isEditing() {
		return editing;
	}

	/**
	 * @param editing the editing to set
	 */
	public void setEditing(boolean editing) {
		if (this.editing == editing)
			return;
		
		this.editing = editing;
		caseSymptoms = null;
	}
}
