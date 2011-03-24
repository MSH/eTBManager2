package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.CaseComorbidity;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.RoleAction;
import org.msh.tb.log.LogService;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


/**
 * Handle common operation (including and removing) of comorbidities of a case. This class is basically used by the UI
 * to list all comorbidities available and allows the user to select them by checking or unchecking the list
 * @author Ricardo Memoria
 *
 */
@Name("comorbidityHome")
public class ComorbidityHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<ItemSelect<CaseComorbidity>> comorbidities;


	/**
	 * Initialize the list of comorbidities to be selected by the user
	 */
	public void initializeSelection() {
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseComorbidity> lst = tbcase.getComorbidities();

		List<CaseComorbidity> fullList = new ArrayList<CaseComorbidity>();
		for (FieldValue value: fieldsQuery.getComorbidities()) {
			CaseComorbidity com = findComorbidity(lst, value);
			if (com == null) {
				com = new CaseComorbidity();
				com.setComorbidity(value);
			}
			fullList.add(com);
		}
		
		// create list of items
		comorbidities = ItemSelectHelper.createList(fullList);
		ItemSelectHelper.selectItems(comorbidities, lst, true);
	}


	/**
	 * Search for a comorbidity in the list of {@link CaseComorbidity} items
	 * @param lst
	 * @param comorbidity
	 * @return
	 */
	public CaseComorbidity findComorbidity(List<CaseComorbidity> lst, FieldValue comorbidity) {
		for (CaseComorbidity casecom: lst) {
			if (comorbidity.getId().equals(casecom.getComorbidity().getId())) {
				return casecom;
			}
		}
		return null;
	}


	/**
	 * @return the comorbidities
	 */
	public List<ItemSelect<CaseComorbidity>> getComorbidities() {
		if (comorbidities == null)  
			initializeSelection();
		return comorbidities;
	}

	
	/**
	 * Save the comorbidities selected by the user
	 * @return
	 */
	public String save() {
		if (comorbidities == null)
			return "error";
		
		List<CaseComorbidity> lst = ItemSelectHelper.createItemsList(comorbidities, true);
		
		registerLog(lst);
		
		TbCase tbcase = caseHome.getInstance();
		
		EntityManager em = (EntityManager)Component.getInstance("entityManager");
		
		// add new comorbidities
		for (ItemSelect<CaseComorbidity> item: comorbidities) {
			CaseComorbidity com = item.getItem();
			CaseComorbidity aux = findComorbidity(tbcase.getComorbidities(), com.getComorbidity());

			// item is selected ?
			if (item.isSelected()) {
				if (aux == null) {
					com.setTbcase(tbcase);
					tbcase.getComorbidities().add(com);
				}
			}
			else {
				// item is removed
				if (aux != null) {
					em.remove(aux);
					tbcase.getComorbidities().remove(aux);
				}
			}
		}
		
		if(!tbcase.isTbContact())
			tbcase.setPatientContactName(null);
//		tbcase.setComorbidities(lst);
		
		return caseHome.persist();
	}

	
	/**
	 * Register in log the comorbidities changed
	 * @param newComorbidities
	 */
	public void registerLog(List<CaseComorbidity> newComorbidities) {
		LogService logService = new LogService();
		
		TbCase tbcase = caseHome.getInstance();

		// check new comorbidities
		for (CaseComorbidity fld: newComorbidities) {
			if (!tbcase.getComorbidities().contains(fld))
				logService.addValue("RoleAction.NEW", fld.getComorbidity());
		}

		// check removed comorbidities
		for (CaseComorbidity fld: tbcase.getComorbidities()) {
			if (!newComorbidities.contains(fld))
				logService.addValue("RoleAction.DELETE", fld.getComorbidity());
		}
		
		logService.setCaseClassification(tbcase.getClassification());
		logService.saveTransaction(tbcase, "COMORBIDITIES", RoleAction.EDIT);
	}
}
