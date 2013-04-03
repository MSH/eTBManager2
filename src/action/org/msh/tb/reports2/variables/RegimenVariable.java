package org.msh.tb.reports2.variables;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Regimen;
import org.msh.tb.reports2.VariableImpl;

public class RegimenVariable extends VariableImpl {

	private List<Regimen> regimens;
	
	public RegimenVariable() {
		super("regimen", "Regimen", "tbcase.regimen_id", null);
	}

	/**
	 * Return the list of regimens available
	 * @return
	 */
	public List<Regimen> getRegimens() {
		if (regimens == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			regimens = em.createQuery("from Regimen where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return regimens;
	}

	@Override
	public Object createKey(Object values) {
		return values;
	}

	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return Messages.instance().get("regimens.individualized");

		Integer id = (Integer)key;
		
		for (Regimen reg: getRegimens())
			if (reg.getId().equals(id)) {
				return reg.getName();
			}
		
		// if id is not found, return undefined 
		return super.getDisplayText(key);
	}
	
}
