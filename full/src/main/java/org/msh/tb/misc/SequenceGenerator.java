package org.msh.tb.misc;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.SequenceInfo;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;

@Name("sequenceGenerator")
public class SequenceGenerator {

	@In(create=true) EntityManager entityManager;
	@In(required=false) Workspace defaultWorkspace;

	/**
	 * Generates a new sequence number according to the ID
	 * @param id
	 * @return
	 */
	@Transactional
	public int generateNewNumber(String seq) {
		if (defaultWorkspace == null)
			return 0;
		
		SequenceInfo sequenceInfo;
		try {
			sequenceInfo = (SequenceInfo) entityManager.createQuery("from SequenceInfo s where s.sequence = :seq and s.workspace.id = :id")
			.setParameter("seq", seq)
			.setParameter("id", defaultWorkspace.getId())
			.getSingleResult();			
		} catch (Exception e) {
			sequenceInfo = null;
			e.printStackTrace();
		}
		
		if (sequenceInfo == null) {
			sequenceInfo = new SequenceInfo();
			sequenceInfo.setSequence(seq);
			sequenceInfo.setWorkspace(defaultWorkspace);
		}
		
		int val = sequenceInfo.getNumber() + 1;
		sequenceInfo.setNumber(val);
		
		entityManager.persist(sequenceInfo);
		entityManager.flush();
		
		return val;
	}
	
	public int generateNewNumber(String seq, Workspace ws) {
		defaultWorkspace = ws;
		return generateNewNumber(seq);
	}
	
	/**
	 * @return the defaultWorkspace
	 */
	public Workspace getDefaultWorkspace() {
		return defaultWorkspace;
	}

	/**
	 * @param defaultWorkspace the defaultWorkspace to set
	 */
	public void setDefaultWorkspace(Workspace defaultWorkspace) {
		this.defaultWorkspace = defaultWorkspace;
	}
}
