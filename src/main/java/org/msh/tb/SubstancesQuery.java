package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Substance;
import org.msh.utils.EntityQuery;

import java.util.List;

@Name("substances")
public class SubstancesQuery extends EntityQuery<Substance> {
	private static final long serialVersionUID = -5768778841232894814L;

	private String filter;
	private List<Substance> prevTBsubstances;
	private List<Substance> dstSubstances; 
	
	@Override
	public String getEjbql() {
		String s = "from Substance s where s.workspace.id = #{defaultWorkspace.id} ";
		
		if (filter != null)
			s = s.concat(filter);
		return s;
	}


	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "s.abbrevName";
		else return s;
	}


	@Override
	protected String getCountEjbql() {
		return "select count(*) from Substance s where workspace.id = #{defaultWorkspace.id}";
	}

	public void prepareDstForm() {
		filter = "and s.dstResultForm = true";
		setOrder("s.prevTreatmentOrder");
		refresh();
	}
	
	public void preparePrevTreatmentForm() {
		filter = "and s.prevTreatmentForm = true";
		setOrder("s.prevTreatmentOrder");		
		refresh();
	}
	
	public List<Substance> getDstSubstances() {
		if (dstSubstances == null) {
			prepareDstForm();
			dstSubstances = getResultList();
		}
		return dstSubstances;
	}
	
	public List<Substance> getPrevTBsubstances() {
		if (prevTBsubstances == null) {
			preparePrevTreatmentForm();
			prevTBsubstances = getResultList();
		}
		return prevTBsubstances;
	}
}
