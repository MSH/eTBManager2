package org.msh.tb.resistpattern;


import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.ResistancePattern.PatternCriteria;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
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
		List<Substance> itens = ItemSelectHelper.getSelectedItems(getSelectableSubstances(), true);

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
		res.setCriteria(PatternCriteria.EXACT_RESISTANT);
		
		s = super.persist();
		
		if ("persisted".equals(s))
			updateCasesResistancePattern(getInstance());
		
		return s;
	}
	
	/**
	 * Update the cases that match this resistance pattern. This method is called every
	 * time a resistance pattern is created or changed.
	 * @param respattern
	 */
	private void updateCasesResistancePattern(ResistancePattern respattern) {
		if (respattern.getCriteria() != PatternCriteria.EXACT_RESISTANT)
			throw new IllegalArgumentException("Only EXACT_RESISTANT patterns are supported");
		
		EntityManager em = getEntityManager();
		
		// remove this resistance pattern from the cases
		em.createQuery("delete from CaseResistancePattern where resistancePattern.id = :id")
			.setParameter("id", respattern.getId())
			.executeUpdate();

		// insert again the resistance patterns
		// create the resistance pattern for cases with NO Xpert exam with RIF positive
		String subs = "";
		for (Substance sub: respattern.getSubstances()) {
			if (!subs.isEmpty())
				subs += ",";
			subs += sub.getId();
		}
		String sql = "insert into caseresistancepattern (case_id, resistpattern_id, diagnosis) " +
				"select a.id, " + respattern.getId() + ", false " +
				"from tbcase a " +
				"inner join patient b on b.id=a.patient_id " +
				"where workspace_id= " + respattern.getWorkspace().getId() + 
				" and not exists(select * from examxpert where examxpert.case_id=a.id and rifresult=" + XpertResult.TB_DETECTED.ordinal() + 
				" and result=" + XpertRifResult.RIF_DETECTED.ordinal() + ") " +
				"and (select count(distinct substance_id) from examdst e inner join examdstresult r on r.exam_id=e.id " +
				"  where r.result=" + DstResult.RESISTANT.ordinal() + " and r.substance_id in (" + subs + ") and e.case_id=a.id) = " + respattern.getSubstances().size() +
				" and not exists(select * from examdst e inner join examdstresult r on r.exam_id=e.id " +
				"where r.result=1 and r.substance_id not in (" + subs + ") and e.case_id=a.id)";
		em.createNativeQuery(sql).executeUpdate();

		// create the resistance pattern for cases with Xpert exam where RIF is detected
		subs = "";
		String subs2 = "";
		int count = 0;
		for (Substance sub: respattern.getSubstances()) {
			if (!sub.getAbbrevName().toString().equals("R")) {
				if (!subs.isEmpty())
					subs += ",";
				subs += sub.getId();
				count++;
			}
			if (!subs2.isEmpty())
				subs2 += ",";
			subs2 += sub.getId();
		}
		sql = "insert into caseresistancepattern (case_id, resistpattern_id, diagnosis) " +
				"select a.id, " + respattern.getId() + ", false " +
				"from tbcase a " +
				"inner join patient b on b.id=a.patient_id " +
				"where workspace_id= " + respattern.getWorkspace().getId() + 
				" and exists(select * from examxpert where examxpert.case_id=a.id and rifresult=" + XpertResult.TB_DETECTED.ordinal() + 
				" and result=" + XpertRifResult.RIF_DETECTED.ordinal() + ") " +
				"and (select count(distinct substance_id) from examdst e inner join examdstresult r on r.exam_id=e.id " +
				"  where r.result=" + DstResult.RESISTANT.ordinal() +
				(count > 0? "": " and r.substance_id in (" + subs + ") ") + 
				" and e.case_id=a.id) = " + count +
				" and not exists(select * from examdst e inner join examdstresult r on r.exam_id=e.id " +
				"where r.result=1 and r.substance_id not in (" + subs2 + ") and e.case_id=a.id)";
		em.createNativeQuery(sql).executeUpdate();
	}

	/**
	 * Return the list of substances to be selected by the user in the UI
	 * @return
	 */
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
