package org.msh.tb;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.RegimenUnit;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.tb.login.UserSession;

@Name("regimenUnitHome")
@Scope(ScopeType.CONVERSATION)
public class RegimenUnitHome extends Controller {
	private static final long serialVersionUID = 226119601901088915L;

	@In(create=true) EntityManager entityManager;
	@In(create=true) RegimensQuery regimens;
	@In(create=true) UserSession userSession;

	private List<RegimenUnit> items;
	
	/**
	 * Returns the list of regimens by unit for displaying/configuring
	 * @return
	 */
	public List<RegimenUnit> getItems() {
		if (items == null)
			createItems();
		
		return items;
	}

	/**
	 * Save the minimum buffer stock for the unit and source 
	 * @return
	 */
	@Transactional
	public String persist() {
		if (items == null)
			return "error";
		
		for (RegimenUnit reg: items)
			entityManager.persist(reg);
		
		entityManager.flush();
		
		return "persisted";
	}


	/**
	 * Create list of regimens by unit for editing/displaying 
	 */
	protected void createItems() {		
		Tbunit unit = entityManager.find(Tbunit.class, userSession.getTbunit().getId());

		items = entityManager.createQuery("from RegimenUnit ru " + 
				"join fetch ru.regimen " +
				"where ru.tbunit.id = :uid")
				.setParameter("uid", unit.getId())
				.getResultList();
		
		for (Regimen r: regimens.getResultList()) {
			if (findRegimen(r) == null) {
				RegimenUnit ru = new RegimenUnit();
				ru.setRegimen(r);
				ru.setTbunit(unit);
				items.add(ru);
			}
		}
	}


	/**
	 * Search for configuration about a regimen
	 * @param regimen
	 * @return
	 */
	protected RegimenUnit findRegimen(Regimen regimen) {
		for (RegimenUnit ru: items) {
			if (ru.getRegimen().equals(regimen)) {
				return ru;
			}
		}
		return null;
	}
}
