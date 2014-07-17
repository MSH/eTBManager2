package org.msh.tb.bd;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.bd.entities.CaseSideEffectBD;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.misc.FieldsQuery;

import java.util.List;


@Name("sideEffectBDHome")
@Scope(ScopeType.CONVERSATION)
public class SideEffectBDHome extends EntityHomeEx<CaseSideEffectBD>{

	private static final long serialVersionUID = -4954787062161078703L;
	
	@In(required=true) CaseHome caseHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) FacesMessages facesMessages;
	private List<CaseSideEffectBD> results;
	
	@Factory("caseSideEffectBD")
	public CaseSideEffectBD getCaseSideEffectBD() {
		return getInstance();
	}
	
	/**
	 * Return the list of adverse reactions of a case
	 * @return List of objects of {@link CaseSideEffect} type 
	 */
	public List<CaseSideEffectBD> getResults(){
		if (results == null)
			results = createResults();
		return results;
	}
	
	/**
	 * Creates the list of exam results
	 * @param lastRes
	 * @return
	 */
	protected List<CaseSideEffectBD> createResults() {
		List<CaseSideEffectBD> r = getEntityManager()
			.createQuery(getResultsHQL())
			.getResultList();
		return r;
	}
	
	public String getResultsHQL() {
		String hql = "from CaseSideEffectBD c where c.tbcase.id = #{tbcase.id}";
		return hql.concat(" order by c.effectSt desc");
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public String remove(){
		String s = super.remove();
		results = createResults();
		if(s.equals("removed"))
			return "sideeffectremoved";
		else
			return s;
	}
	
	/**
	 * Saves the changes made to the side effects of the case
	 * @return - "persisted" if it was successfully saved
	 */
	public String save() {
		if(!validateForm()){
			return "error";
		}
		
		TbCase tbcase = caseHome.getInstance();

		CaseSideEffectBD it = getInstance();
		
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

		this.persist();

		TagsCasesHome.instance().updateTags(tbcase);
		
		return "persisted";
	}
	
	public boolean validateForm(){
		boolean validationError = false;
		
		getCaseSideEffectBD().setMonth(0);

		//If the case is not on treatment or is transferin but has no treatment
		if((!caseHome.getInstance().getState().equals(CaseState.ONTREATMENT)) || 
				(caseHome.getInstance().getState().equals(CaseState.TRANSFERRING) && (caseHome.getInstance().getTreatmentPeriod() == null || caseHome.getInstance().getTreatmentPeriod().getIniDate() == null))){
			facesMessages.addToControlFromResourceBundle("sideEffectfldoptions", "cases.sideeffects.errormsg1");
			validationError = true;
			return !validationError;
		}
		
		if(getCaseSideEffectBD().getEffectSt() == null){
			facesMessages.addToControlFromResourceBundle("sideEffect", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}
		
		if(getCaseSideEffectBD().getSideEffect() == null){
			facesMessages.addToControlFromResourceBundle("inidate", "javax.faces.component.UIInput.REQUIRED");
			validationError = true;
		}
		
		if(getCaseSideEffectBD().getEffectEnd() != null && getCaseSideEffectBD().getEffectEnd().compareTo(getCaseSideEffectBD().getEffectSt()) < 0){
			facesMessages.addToControlFromResourceBundle("inidate", "global.finalinitialdateerror");
			facesMessages.addToControlFromResourceBundle("enddate", "global.finalinitialdateerror");
			validationError = true;
		}
		
		//same side effect already recorded for the same initial date
		List<CaseSideEffectBD> lst = createResults();
		for(CaseSideEffectBD c : lst){
			//checks if it is a new register of if the editing side effect is not the same as the list object
			if(getCaseSideEffectBD().getId() == null || !getCaseSideEffectBD().getId().equals(c.getId())){
			//If it is the same side effect
			if(getCaseSideEffectBD().getSideEffect().getValue().getId().equals(c.getSideEffect().getValue().getId()))
				//Same start date.
				if(c.getEffectSt() != null && getCaseSideEffectBD().getEffectSt().compareTo(c.getEffectSt()) == 0){
					facesMessages.addToControlFromResourceBundle("inidate", "cases.sideeffects.samemonth");
					validationError = true;
				//start date of list object is before the editing object
				}else if(c.getEffectSt() != null && getCaseSideEffectBD().getEffectSt().compareTo(c.getEffectSt()) < 0){
					 if(c.getEffectEnd() == null){
						//exists the same side effect with the period open
						facesMessages.addToControlFromResourceBundle("inidate", "cases.sideeffects.sameperiod");
						facesMessages.addToControlFromResourceBundle("enddate", "cases.sideeffects.sameperiod");
						validationError = true;
					}else if(getCaseSideEffectBD().getEffectEnd().compareTo(c.getEffectSt()) >= 0){
						//exists the same side effect with that the period intercedes this one
						facesMessages.addToControlFromResourceBundle("inidate", "cases.sideeffects.sameperiod");
						facesMessages.addToControlFromResourceBundle("enddate", "cases.sideeffects.sameperiod");
						validationError = true;
					}
				//start date of list object is after the editing object
				}else if(c.getEffectSt() != null && getCaseSideEffectBD().getEffectSt().compareTo(c.getEffectSt()) >= 0){
					if(c.getEffectEnd()== null){
						//exists the same side effect with the period open
						facesMessages.addToControlFromResourceBundle("inidate", "cases.sideeffects.sameperiod");
						facesMessages.addToControlFromResourceBundle("enddate", "cases.sideeffects.sameperiod");
						validationError = true;
					}else if(c.getEffectEnd().compareTo(getCaseSideEffectBD().getEffectSt()) >= 0){
						//exists the same side effect with that the period intercedes this one
						facesMessages.addToControlFromResourceBundle("inidate", "cases.sideeffects.sameperiod");
						facesMessages.addToControlFromResourceBundle("enddate", "cases.sideeffects.sameperiod");
						validationError = true;
					}
				}
			}
		}
		
		return !validationError;
	}
		
}
