package org.msh.tb.az.cases;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.CaseSeverityMark;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.misc.FieldsQuery;

@Name("caseSeverityMarksHome")
public class CaseSeverityMarksHome {

	@In EntityManager entityManager;
	@In CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<CaseSeverityMark> severityMarks;
	private boolean editing;
	
	
	
	/**
	 * Return list of symptoms from the case
	 * @return {@link List} of {@link CaseSeverityMark} objects
	 */
	public List<CaseSeverityMark> getSeverityMarks() {
		if (severityMarks == null)
			createSymptomsList();
		return severityMarks;
	}
	
	
	/**
	 * Save the case symptoms
	 */
	public void save() {
		if (severityMarks == null)
			return;
		
		if (!editing)
			throw new RuntimeException("SymptomsHome not in edit mode");
		
		for (CaseSeverityMark sym: severityMarks) {
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
		severityMarks = entityManager
			.createQuery("from CaseSeverityMark s where s.tbcase.id = #{caseHome.instance.id} order by s.severityMark.name.name1")
			.getResultList();

//		if (editing) {
			List<FieldValue> fields = fieldsQuery.getSeverityMarks();
			for (FieldValue field: fields) {
				CaseSeverityMark aux = findCaseSeverityMark(field);
				if (aux == null) {
					aux = new CaseSeverityMark();
					aux.setSeverityMark(field);
					aux.setTbcase((TbCaseAZ)caseHome.getInstance());
					severityMarks.add(aux);
				}
				else aux.setChecked(true);
//			}
			
			// sort the items
			Collections.sort(severityMarks, new Comparator<CaseSeverityMark>() {
				public int compare(CaseSeverityMark o1, CaseSeverityMark o2) {
					return o1.getSeverityMark().getName().toString().compareTo(o2.getSeverityMark().getName().toString());
				}
			});
		}
	}
	

	/**
	 * Search for a case symptom by its symptom
	 * @param fieldValue
	 * @return {@link CaseSeverityMark} instance
	 */
	private CaseSeverityMark findCaseSeverityMark(FieldValue fieldValue) {
		for (CaseSeverityMark aux: severityMarks) {
			if (aux.getSeverityMark().equals(fieldValue)) {
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
		severityMarks = null;
	}
}
