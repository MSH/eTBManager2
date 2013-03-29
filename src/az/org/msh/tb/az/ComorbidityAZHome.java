package org.msh.tb.az;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.ComorbidityHome;
import org.msh.tb.entities.CaseComorbidity;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;

@Name("comorbidityAZHome")
public class ComorbidityAZHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<ItemSelect<CaseComorbidity>> comorbidities;
	
	/**
	 * Initialize the list of comorbidities to be selected by the user
	 */
	public void initializeSelection() {
		ComorbidityHome ch = (ComorbidityHome) App.getComponent("comorbidityHome");
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseComorbidity> lst = tbcase.getComorbidities();

		List<CaseComorbidity> fullList = new ArrayList<CaseComorbidity>();
		for (FieldValue value: fieldsQuery.getComorbidities()) {
			if (!canViewHIVField(value)) continue;
			CaseComorbidity com = ch.findComorbidity(lst, value);
			if (com == null) {
				com = new CaseComorbidity();
				com.setComorbidity(value);
			}
			fullList.add(com);
		}
		
		// create list of items
		comorbidities = ItemSelectHelper.createList(fullList);
		ItemSelectHelper.selectItems(comorbidities, lst, true);
		ch.setComorbidities(comorbidities);
	}
	
	public List<ItemSelect<CaseComorbidity>> getComorbidities() {
		if (comorbidities == null)  
			initializeSelection();
		return comorbidities;
	}
	
	private boolean canViewHIVField(FieldValue value){
		if (value.getId() == 939229){
			if (caseHome.checkRoleBySuffix("EDT_HIV_TB"))
				return true;
		}
		else
			return true;
		return false;
		
	}
}
