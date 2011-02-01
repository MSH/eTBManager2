package org.msh.tb.cases;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
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
	
	private List<ItemSelect<FieldValue>> comorbidities;


	/**
	 * Initialize the list of comorbidities to be selected by the user
	 */
	public void initializeSelection() {
		TbCase tbcase = caseHome.getInstance();
		
		List<FieldValue> lst = tbcase.getComorbidities();

		// create list of items
		comorbidities = ItemSelectHelper.createList(fieldsQuery.getComorbidities());
		ItemSelectHelper.selectItems(comorbidities, lst, true);
	}


	/**
	 * @return the comorbidities
	 */
	public List<ItemSelect<FieldValue>> getComorbidities() {
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
		
		List<FieldValue> lst = ItemSelectHelper.createItemsList(comorbidities, true);
		
		registerLog(lst);
		
		TbCase tbCase = caseHome.getInstance();
		if(!tbCase.isTbContact())
			tbCase.setPatientContactName(null);
		tbCase.setComorbidities(lst);
		
		return caseHome.persist();
	}

	
	/**
	 * Register in log the comorbidities changed
	 * @param newComorbidities
	 */
	public void registerLog(List<FieldValue> newComorbidities) {
		LogService logService = new LogService();
		
		TbCase tbcase = caseHome.getInstance();

		// check new comorbidities
		for (FieldValue fld: newComorbidities) {
			if (!tbcase.getComorbidities().contains(fld))
				logService.addValue("RoleAction.NEW", fld);
		}

		// check removed comorbidities
		for (FieldValue fld: tbcase.getComorbidities()) {
			if (!newComorbidities.contains(fld))
				logService.addValue("RoleAction.DELETE", fld);
		}
		
		logService.setCaseClassification(tbcase.getClassification());
		logService.saveTransaction(tbcase, "COMORBIDITIES", RoleAction.EDIT);
	}
}
