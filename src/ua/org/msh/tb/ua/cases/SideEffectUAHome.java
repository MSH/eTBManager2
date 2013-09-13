package org.msh.tb.ua.cases;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.WsEntityHome;
import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.TbCase;
import org.msh.tb.ua.entities.CaseSideEffectUA;


/**
 *  
 * @author A.M.
 *
 */
@Name("sideEffectUAHome")
public class SideEffectUAHome extends WsEntityHome<CaseSideEffectUA>{
	private static final long serialVersionUID = 4590228131339634325L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	private List<CaseSideEffectUA> results;

	
	/**
	 * Return the list of adverse reactions of a case
	 * @return List of objects of {@link CaseSideEffect} type 
	 */
	public List<CaseSideEffectUA> getResults(){
		if (results == null)
			results = createResults();
		return results;
	}

	/**
	 * Factory method to return an instance of {@link CaseSideEffect}
	 * @return instance of {@link CaseSideEffect} class
	 */
	@Factory("caseSideEffectUA")
	public CaseSideEffectUA getCaseSideEffect() {
		return getInstance();
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

		CaseSideEffectUA it = getInstance();
		
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

		getEntityManager().persist(it);
		getEntityManager().flush();

		TagsCasesHome.instance().updateTags(tbcase);
		
		return "persisted";
	}
	

	public String getResultsHQL() {
		String hql = "from CaseSideEffectUA c where c.tbcase.id = #{tbcase.id}";

		return hql.concat(" order by c.month desc");
	}
	
	/**
	 * Creates the list of exam results
	 * @param lastRes
	 * @return
	 */
	protected List<CaseSideEffectUA> createResults() {
		return getEntityManager()
			.createQuery(getResultsHQL())
			.getResultList();
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
	
	public boolean validateForm(){
		Date changeReg = getCaseSideEffect().getDateChangeReg();
		Date start = getCaseSideEffect().getEffectSt();
		Date end = getCaseSideEffect().getEffectEnd();
		
		if(changeReg!=null && start!=null)
			if (changeReg.before(start)){
				facesMessages.addFromResourceBundle("uk_UA.sideEffects.error1");
				return false;
			}
		
		if(end!=null && start!=null)
			if (end.before(start)){
				facesMessages.addFromResourceBundle("uk_UA.sideEffects.error2");
				return false;
			}
		
		if(end!=null && changeReg!=null)
			if (end.before(changeReg)){
				facesMessages.addFromResourceBundle("uk_UA.sideEffects.error3");
				return false;
			}
		
		return true;
	}
}
