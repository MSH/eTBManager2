package org.msh.tb.br;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.SideEffectHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.misc.FieldsQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;


@Name("sideEffectBRHome")
public class SideEffectHomeBR {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	@In(create=true) SideEffectHome sideEffectHome;
	@In(required=true) private CaseHome caseHome;
	@In(create=true) private FieldsQuery fieldsQuery;
	@In(create = true) EntityManager entityManager;
	
	private List<ItemSelect<CaseSideEffect>> items;

	/**
	 * Returns side effect list of the case to be selected by the user
	 * @return
	 */
	public List<ItemSelect<CaseSideEffect>> getItems() {
		if (items == null)
			createItems();
		return items;
	}

	/**
	 * Create the side effect list to be selected by the user
	 */
	protected void createItems() {
		List<FieldValue> sideEffects = fieldsQuery.getSideEffects();
		
		TbCase tbcase = caseHome.getInstance();
		
		items = new ArrayList<ItemSelect<CaseSideEffect>>();
		
		for (FieldValue sideEffect: sideEffects) {
			ItemSelect item = new ItemSelect();

			CaseSideEffect cse = new CaseSideEffect();
			cse.getSideEffect().setValue(sideEffect);

			item.setItem(cse);
			items.add(item);
		}
	}

	/**
	 * Saves the changes made to the side effects of the case
	 * Used when the form is a list of checkbox
	 * @return - "persisted" if it was successfully saved
	 */
	public String saveSideEffectList() {
		TbCase tbcase = caseHome.getInstance();
		
		List<CaseSideEffect> lst = ItemSelectHelper.getSelectedItems(getItems(), true);
		for (CaseSideEffect it: lst) {
			it.setTbcase(tbcase);
			if ((it.getSubstance() != null) && (it.getSubstance2() != null) && (it.getSubstance().equals(it.getSubstance2())))
				it.setSubstance2(null);
			if ((it.getSubstance() == null) && (it.getSubstance2() != null)) {
				it.setSubstance(it.getSubstance2());
				it.setSubstance2(null);
			}
			String name ="";
			if(it.getSubstance()!= null)
				name += it.getSubstance().getAbbrevName().getName1();
			if(it.getSubstance2()!= null)
				name += " "+it.getSubstance2().getAbbrevName().getName1();
			it.setMedicines(name);
			
			sideEffectHome.setInstance(it);
			tbcase.getSideEffects().add(it);
			
			entityManager.persist(it);
		}

		TagsCasesHome.instance().updateTags(tbcase);
		
		return "persisted";
	}
	
}