package org.msh.tb.indicators;


import java.util.List;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ResistancePattern;
import org.msh.mdrtb.entities.Substance;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.SubstancesQuery;
import org.msh.utils.ItemSelect;
import org.msh.utils.ItemSelectHelper;


/**
 * Home class to handle resistance patterns
 * @author Ricardo Memoria
 *
 */
@Name("resistancePatternHome")
public class ResistancePatternHome extends EntityHomeEx<ResistancePattern>{
	private static final long serialVersionUID = -947329475752402334L;

	@In(create=true) SubstancesQuery substances;
	@In(required=false) ResistancePatternsQuery resistancePatterns;
	
	private List<ItemSelect<Substance>> selectableSubstances;
	
	
	@Factory("resistancePattern")
	public ResistancePattern getResistancePattern() {		
		return getInstance();
	}
	
	/**
	 * Saves a new pattern or changes made to an existing pattern
	 * @return
	 */
	public String save() {
		List<Substance> itens = ItemSelectHelper.createItemsList(getSelectableSubstances(), true);

		// update the pattern name
		String s = "";
		for (Substance sub: itens) {
			if (!s.isEmpty())
				s += " ";
			s += sub.getAbbrevName();
		}
		
		ResistancePattern res = getInstance();
		res.setName(s);
		res.setSubstances(itens);
		
		return super.persist();
	}
	
	public List<ItemSelect<Substance>> getSelectableSubstances() {
		if (selectableSubstances == null) {
			selectableSubstances = ItemSelectHelper.createList(substances.getResultList());
			ItemSelectHelper.selectItems(selectableSubstances, getInstance().getSubstances(), true);
		}
		
		return selectableSubstances;
	}
	
	@Override
	public String remove() {
		if (resistancePatterns != null)
			resistancePatterns.refresh();
		
		return super.remove();
	}
	
	@End(beforeRedirect=false)
	public String saveAndEnd() {
		return save();
	}
}
