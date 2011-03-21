package org.msh.tb.cases;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.CaseComment;
import org.msh.mdrtb.entities.enums.CaseSection;
import org.msh.tb.EntityHomeEx;

@Name("caseCommentsHome")
public class CaseCommentsHome extends EntityHomeEx<CaseComment> {
	private static final long serialVersionUID = -7620376721300705176L;

	@In(required=true) CaseHome caseHome;
	
	private CaseSection section;

	private Map<CaseSection, List<CaseComment>> lists = new HashMap<CaseSection, List<CaseComment>>();

	@Factory("caseComment")
	public CaseComment getCaseComment() {
		return getInstance();
	}


	/**
	 * Add a comment to the case data section of the case 
	 * @return
	 */
	public String addCaseDataComment() {
		section = CaseSection.CASE_DATA;
		return addComment();
	}


	/**
	 * Add a comment to the exam section of the case 
	 * @return
	 */
	public String addExamComment() {
		section = CaseSection.EXAMS;
		return addComment();
	}


	/**
	 * Add a comment to the case data section of the case 
	 * @return
	 */
	public String addTreatmentComment() {
		section = CaseSection.TREATMENT;
		return addComment();
	}


	/**
	 * Add a new comment. The section property must be informed  
	 * @return
	 */
	public String addComment() {
		if (section == null)
			return "error";

		CaseComment comment = getInstance();
		
		comment.setUser(getUser());
		comment.setDate(new Date());
		comment.setSection(section);
		comment.setTbcase(caseHome.getInstance());
		
		persist();
		return "comment-added";
	}
	
	
	/**
	 * Return list of comments from the case data section
	 * @return
	 */
	public List<CaseComment> getCaseDataComments() {
		return getComments(CaseSection.CASE_DATA);
	}

	
	/**
	 * Return list of comments from the exam section
	 * @return
	 */
	public List<CaseComment> getExamComments() {
		return getComments(CaseSection.EXAMS);
	}

	
	/**
	 * Return list of comments from the treatment section
	 * @return
	 */
	public List<CaseComment> getTreatmentComments() {
		return getComments(CaseSection.TREATMENT);
	}


	/**
	 * Create a list of comments from a specific section. The first time the list is requested, it's loaded from
	 * the database, but it's them saved in memory to be reused during request
	 * @param section
	 * @return
	 */
	public List<CaseComment> getComments(CaseSection section) {
		if (section == null)
			return null;
		
		List<CaseComment> lst = lists.get(section);
		
		if (lst != null)
			return lst;

		EntityManager em = getEntityManager();
		lst = em.createQuery("from CaseComment c where c.section = :section")
			.setParameter("section", section)
			.getResultList();
		
		lists.put(section, lst);
		
		return lst;
	}

	/**
	 * @return the section
	 */
	public CaseSection getSection() {
		return section;
	}

	/**
	 * @param section the section to set
	 */
	public void setSection(CaseSection section) {
		this.section = section;
	}
}
