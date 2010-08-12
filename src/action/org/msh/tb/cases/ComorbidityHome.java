package org.msh.tb.cases;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


@Name("comorbidityHome")
public class ComorbidityHome {

	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	
	private List<ItemSelect<FieldValue>> comorbidities;

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

	public String save() {
		if (comorbidities == null)
			return "error";
		
		List<FieldValue> lst = ItemSelectHelper.createItemsList(comorbidities, true);
		
		caseHome.getInstance().setComorbidities(lst);
		
		String s = caseHome.persist();
		System.out.println(s);
		return s;
	}
}
