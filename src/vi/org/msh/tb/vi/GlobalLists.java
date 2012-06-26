package org.msh.tb.vi;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CultureResult;

@Name("globalLists_vi")
public class GlobalLists {

	private static final CultureResult cultureResults[] = {
		CultureResult.NOTDONE,
		CultureResult.NEGATIVE,
		CultureResult.POSITIVE,
		CultureResult.PLUS,
		CultureResult.PLUS2,
		CultureResult.PLUS3,
		CultureResult.PLUS4,
		CultureResult.CONTAMINATED
	};


	/**
	 * Return the results of culture customized to Vietnam
	 * @return
	 */
	public CultureResult[] getCultureResults() {
		return cultureResults;
	}
	
	
	/**
	 * Return the number of AFBs in a microscopy exam customized to Vietnam
	 * @return
	 */
	public List<SelectItem> getNumberOfAFBs() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		SelectItem item = new SelectItem();
		item.setLabel("-");
		lst.add(item);
		
		for (int i = 1; i <= 19; i++) {
			item = new SelectItem();
			item.setLabel(Integer.toString(i));
			item.setValue(i);
			lst.add(item);
		}
		
		return lst;
	}
}
