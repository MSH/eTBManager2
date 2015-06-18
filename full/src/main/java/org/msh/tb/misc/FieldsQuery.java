package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for handle lists of field values for a drop down menu in a TB form
 * @author Ricardo Memoria
 *
 */
@Name("fieldsQuery")
public class FieldsQuery {

	@In(create=true) EntityManager entityManager;
	
	private TbField field;
	private Map<TbField, List<FieldValue>> lists = new HashMap<TbField, List<FieldValue>>();
	private String order;
	
	/**
	 * Return available values according to the <i>field</i> property   
	 * @return list of objects FieldValue
	 */
	public List<FieldValue> getValues() {
		if (field == null)
			return null;
		
		List<FieldValue> values = lists.get(field);
		if (values != null)
			return values;
		
		values = entityManager
			.createQuery("from FieldValue f where f.field = #{fieldsQuery.field} " +
					"and f.workspace.id = #{defaultWorkspace.id} order by f.displayOrder, f.name.name1")
			.getResultList();
		
		lists.put(field, values);
		return values;
	}
	
	public List<FieldValue> getValuesByField(TbField field) {
		setField(field);
		return getValues();
	}
	
	/**
	 * @param tbfield the field to set
	 */
	public void setField(TbField tbfield) {
		this.field = tbfield;
	}

	/**
	 * @return the field
	 */
	public TbField getField() {
		return field;
	}


	/**
	 * Erase all lists forcing them to be loaded again next time it is read
	 */
	public void refreshLists() {
		if (field != null) {
			lists.clear();
//			lists.remove(field);
		}
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(String order) {
		this.order = order;
	}

	/**
	 * @return the order
	 */
	public String getOrder() {
		return order;
	}

	/**
	 * Returns list of detection values
	 * @return
	 */
	public List<FieldValue> getDetections() {
		field = TbField.TBDETECTION;
		return getValues();
	}
	

	/**
	 * Returns list of position values
	 * @return
	 */
	public List<FieldValue> getPositions() {
		field = TbField.POSITION;
		return getValues();
	}

	/**
	 * Returns list of diagnosis values
	 * @return
	 */
	public List<FieldValue> getDiagnosis() {
		field = TbField.DIAG_CONFIRMATION;
		return getValues();
	}

	/**
	 * Returns list of side effects values
	 * @return
	 */
	public List<FieldValue> getSideEffects() {
		field = TbField.SIDEEFFECT;
		return getValues();
	}

	public List<FieldValue> getSideEffectsTb() {
		field = TbField.SIDEEFECT_TB;
		return getValues();
	}

	/**
	 * Returns list of comorbidities
	 * @return
	 */
	public List<FieldValue> getComorbidities() {
		field = TbField.COMORBIDITY;
		return getValues();
	}

	/**
	 * Returns list of contact types
	 * @return
	 */
	public List<FieldValue> getContactTypes() {
		field = TbField.CONTACTTYPE;
		return getValues();
	}

	/**
	 * Returns list of contact conducts
	 * @return
	 */
	public List<FieldValue> getContactConducts() {
		field = TbField.CONTACTCONDUCT;
		return getValues();
	}

	/**
	 * Returns list of physical exams
	 * @return
	 */
	public List<FieldValue> getPhysicalExams() {
		field = TbField.PHYSICAL_EXAMS;
		return getValues();
	}
	
	public List<FieldValue> getDSTMethods() {
		field = TbField.DST_METHOD;
		return getValues();
	}
	
	public List<FieldValue> getCultureMethods() {
		field = TbField.CULTURE_METHOD;
		return getValues();
	}
	
	public List<FieldValue> getBiopsyMethods() {
		field = TbField.BIOPSY_METHOD;
		return getValues();
	}		
	
	public List<FieldValue> getSmearMethods() {
		field = TbField.SMEAR_METHOD;
		return getValues();
	}
	
	public List<FieldValue> getSymptoms() {
		field = TbField.SYMPTOMS;
		return getValues();
	}
	
	public List<FieldValue> getXRayPresentations() {
		field = TbField.XRAYPRESENTATION;
		return getValues();
	}
	
	public List<FieldValue> getPulmonaryTypes() {
		field = TbField.PULMONARY_TYPES;
		return getValues();		
	}
	
	public List<FieldValue> getExtrapulmonaryTypes() {
		field = TbField.EXTRAPULMONARY_TYPES;
		return getValues();		
	}
	
	public List<FieldValue> getSkinColors() {
		field = TbField.SKINCOLOR;
		return getValues();
	}
	
	public List<FieldValue> getPregnancePeriod() {
		field = TbField.PREGNANCE_PERIOD;
		return getValues();
	}
	
	public List<FieldValue> getEducationalDegree() {
		field = TbField.EDUCATIONAL_DEGREE;
		return getValues();
	}
	
	public List<FieldValue> getContagPlace() {
		field = TbField.CONTAG_PLACE;
		return getValues();
	}
	
	public List<FieldValue> getSchemaTypes() {
		field = TbField.SCHEMA_TYPES;
		return getValues();
	}
	
	//public List<FieldValue> getResistanceTypes() {
	//	field = TbField.RESISTANCE_TYPES;
	//	return getValues();
	//}
	
	public List<FieldValue> getMicobacterioses() {
		field = TbField.MICOBACTERIOSE;
		return getValues();
	}
	
	public List<FieldValue> getRegistrationCategories() {
		field = TbField.REGISTRATION_CATEGORY;
		return getValues();
	}
	
	public List<FieldValue> getMolecularBiologyMethods() {
		field = TbField.MOLECULARBIOLOGY_METHOD;
		return getValues();
	}
	
	public List<FieldValue> getArtRegimens() {
		field = TbField.ART_REGIMEN;
		return getValues();
	}
	
	public List<FieldValue> getMaritalStatus() {
		field = TbField.MARITAL_STATUS;
		return getValues();
	}
	
	public List<FieldValue> getSeverityMarks() {
		field = TbField.SEVERITY_MARKS;
		return getValues();
	}
	
	public List<FieldValue> getXRayContacts() {
		field = TbField.XRAY_CONTACT;
		return getValues();
	}
	
	
	public List<FieldValue> getXRayLocalizations() {
		field = TbField.XRAY_LOCALIZATION;
		return getValues();
	}
	
	public List<FieldValue> getAnothertbs() {
		field = TbField.ANOTHERTB;
		return getValues();
	}
	
	public List<FieldValue> getRiskGroup() {
		field = TbField.RISK_GROUP;
		return getValues();
	}
	
	public List<FieldValue> getAdjustmentType() {
		field = TbField.ADJUSTMENT;
		return getValues();
	}
	
	public List<FieldValue> getPosAdjustmentType() {
		field = TbField.ADJUSTMENT;
		if(UserSession.getWorkspace().getExpiredMedicineAdjustmentType() == null)
			return getValues();
		
		ArrayList<FieldValue> lst = (ArrayList<FieldValue>) getValues();
		ArrayList<FieldValue> ret = new ArrayList<FieldValue>();
		
		for(FieldValue field : lst){
			if(field.getId() != UserSession.getWorkspace().getExpiredMedicineAdjustmentType().getId())
				ret.add(field);
		}
		
		return ret;
	}
	
	public List<FieldValue> getSuspectTypes() {
		field = TbField.SUSPECT_TYPE;
		return getValues();
	}
	
	public List<FieldValue> getTreatOutcomeILTB() {
		field = TbField.TREATMENT_OUTCOME_ILTB;
		return getValues();
	}
	
	public List<FieldValue> getIdentification() {
		field = TbField.IDENTIFICATION;
		return getValues();
	}
	
	public List<FieldValue> getSuspectCriteria() {
		field = TbField.SUSPECT_CRITERIA;
		return getValues();
	}
	
	public List<FieldValue> getCauseOfChange() {
		field = TbField.CAUSE_OF_CHANGE;
		return getValues();
	}
	
	public List<FieldValue> getManufacturersMed() {
		field = TbField.MANUFACTURER;
		return getValues();
	}

    /**
     * Return list of dot types
     * @return
     */
    public List<FieldValue> getDotTypes() {
        field = TbField.MEDEXAM_DOTTYPE;
        return getValues();
    }

    /**
     * Return list of referred to types
     * @return
     */
    public List<FieldValue> getRefToTypes() {
        field = TbField.MEDEXAM_REFTOTYPE;
        return getValues();
    }

	/**
	 * Return list of sources of referral
	 * @return
	 */
	public List<FieldValue> getSourcesReferral() {
		field = TbField.SOURCE_REFERRAL;
		return getValues();
	}

	/**
	 * Return list of options for patient occupation
	 * @return
	 */
	public List<FieldValue> getOccupations() {
		field = TbField.OCCUPATION;
		return getValues();
	}

	public List<FieldValue> getReasosXpertTest() {
		field = TbField.REASON_XPERT_EXAM;
		return getValues();
	}

	public List<FieldValue> getDotProviders() {
		field = TbField.DOT_PROVIDER;
		return getValues();
	}
}

