package org.msh.tb.misc;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.TbField;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Store list of TB fields globally in the system for all workspaces.
 * The options are stored in memory until application shutdown
 * @author Ricardo Memoria
 *
 */
@Name("fieldOptions")
@Scope(ScopeType.APPLICATION)
public class FieldsOptions {

	@In EntityManager entityManager;
	@In(create=true) Workspace defaultWorkspace;

	private List<ItemWorkspace> items = new ArrayList<ItemWorkspace>();

	/**
	 * Returns list of detection values
	 * @return
	 */
	public List<FieldValue> getDetections() {
		return getOptions(TbField.TBDETECTION);
	}
	

	/**
	 * Returns list of position values
	 * @return
	 */
	public List<FieldValue> getPositions() {
		return getOptions(TbField.POSITION);
	}


	/**
	 * Returns list of diagnosis values
	 * @return
	 */
	public List<FieldValue> getDiagnosis() {
		return getOptions(TbField.DIAG_CONFIRMATION);
	}


	/**
	 * Returns list of side effects values
	 * @return
	 */
	public List<FieldValue> getSideEffects() {
		return getOptions(TbField.SIDEEFFECT);
	}


	/**
	 * Returns list of comorbidities
	 * @return
	 */
	public List<FieldValue> getComorbidities() {
		return getOptions(TbField.COMORBIDITY);
	}


	/**
	 * Returns list of contact types
	 * @return
	 */
	public List<FieldValue> getContactTypes() {
		return getOptions(TbField.CONTACTTYPE);
	}


	/**
	 * Returns list of contact conducts
	 * @return
	 */
	public List<FieldValue> getContactConducts() {
		return getOptions(TbField.CONTACTCONDUCT);
	}

	
	/**
	 * Returns list of physical exams
	 * @return
	 */
	public List<FieldValue> getPhysicalExams() {
		return getOptions(TbField.PHYSICAL_EXAMS);
	}


	/**
	 * Return list of DST methods in use
	 * @return
	 */
	public List<FieldValue> getDSTMethods() {
		return getOptions(TbField.DST_METHOD);
	}

	/**
	 * Return list of Biopsy methods in use
	 * @return
	 */
	public List<FieldValue> getBiopsyMethods() {
		return getOptions(TbField.BIOPSY_METHOD);
	}

	/**
	 * Return list of culture methods
	 * @return
	 */
	public List<FieldValue> getCultureMethods() {
		return getOptions(TbField.CULTURE_METHOD);
	}

	
	/**
	 * Return list of smear methods 
	 * @return
	 */
	public List<FieldValue> getSmearMethods() {
		return getOptions(TbField.SMEAR_METHOD);
	}
	

	/**
	 * Return list of Symptom options
	 * @return
	 */
	public List<FieldValue> getSymptoms() {
		return getOptions(TbField.SYMPTOMS);
	}

	
	/**
	 * Return list of X-Ray presentations
	 * @return
	 */
	public List<FieldValue> getXRayPresentations() {
		return getOptions(TbField.XRAYPRESENTATION);
	}


	/**
	 * Return list of pulmonary types
	 * @return
	 */
	public List<FieldValue> getPulmonaryTypes() {
		return getOptions(TbField.PULMONARY_TYPES);		
	}

	
	/**
	 * Return list of extrapulmonary types
	 * @return
	 */
	public List<FieldValue> getExtrapulmonaryTypes() {
		return getOptions(TbField.EXTRAPULMONARY_TYPES);		
	}

	
	/**
	 * Return list of skin colors
	 * @return
	 */
	public List<FieldValue> getSkincolors() {
		return getOptions(TbField.SKINCOLOR);		
	}

	
	/**
	 * Return list of extrapulmonary types
	 * @return
	 */
	public List<FieldValue> getPregnantPeriods() {
		return getOptions(TbField.PREGNANCE_PERIOD);		
	}

	
	/**
	 * Return list of educational degrees
	 * @return
	 */
	public List<FieldValue> getEducationalDegrees() {
		return getOptions(TbField.EDUCATIONAL_DEGREE);		
	}

	
	/**
	 * Return list of educational degrees
	 * @return
	 */
	public List<FieldValue> getContagPlaces() {
		return getOptions(TbField.CONTAG_PLACE);		
	}

	/**
	 * Return list of ART  Regimens.
	 * Namibia workspace implementation
	 * @UT 
	 * @return
	 */
	public List<FieldValue> getARTRegimens() {
		return getOptions(TbField.ART_REGIMEN);		
	}	

	/**
	 * Return list of Suspect Types.
	 * Cambodia workspace implementation
	 * @UT 
	 * @return
	 */
	public List<FieldValue> getSuspectTypes() {
		return getOptions(TbField.SUSPECT_TYPE);		
	}	
	
	/**
	 * Return the options of a TB Field
	 * @param field TB Field to retrieve the options
	 * @return List of options for the specific TB field
	 */
	public List<FieldValue> getOptions(TbField field) {
		Map<TbField, List<FieldValue>> lists = getListsWorkspace();
		
		List<FieldValue> values = lists.get(field);
		if (values != null)
			return values;

		values = entityManager
				.createQuery("from FieldValue f where f.field = :field " +
							 "and f.workspace.id = #{defaultWorkspace.id} order by f.displayOrder,f.name.name1")
				.setParameter("field", field)
				.getResultList();
	
		lists.put(field, values);
		return values;
	}


	/**
	 * Return the list of fields and options according to the workspace in use
	 * @return
	 */
	protected Map<TbField, List<FieldValue>> getListsWorkspace() {
		if (items == null) {
			items = new ArrayList<ItemWorkspace>();
		}
		
		int workspaceID = defaultWorkspace.getId();
		
		Map<TbField, List<FieldValue>> lst = null;
		
		for (ItemWorkspace item: items) {
			if (item.getWorkspaceId() == workspaceID) {
				lst = item.getLists();
				break;
			}
		}
		
		if (lst == null) {
			lst = new HashMap<TbField, List<FieldValue>>();

			ItemWorkspace item = new ItemWorkspace();
			item.setWorkspaceId(workspaceID);
			item.setLists(lst);
			items.add(item);
		}
		
		return lst;
	}


	/**
	 * Store list of TB fields and its values by workspace
	 * @author Ricardo Memoria
	 *
	 */
	protected class ItemWorkspace {
		private Map<TbField, List<FieldValue>> lists = new HashMap<TbField, List<FieldValue>>();
		private int workspaceId;
		/**
		 * @return the lists
		 */
		public Map<TbField, List<FieldValue>> getLists() {
			return lists;
		}
		/**
		 * @param lists the lists to set
		 */
		public void setLists(Map<TbField, List<FieldValue>> lists) {
			this.lists = lists;
		}
		/**
		 * @return the workspaceId
		 */
		public int getWorkspaceId() {
			return workspaceId;
		}
		/**
		 * @param workspaceId the workspaceId to set
		 */
		public void setWorkspaceId(int workspaceId) {
			this.workspaceId = workspaceId;
		}
	}
}
