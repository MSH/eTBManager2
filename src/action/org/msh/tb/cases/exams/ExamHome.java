package org.msh.tb.cases.exams;

import java.util.List;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.msh.mdrtb.entities.CaseData;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseHome;


public class ExamHome<E> extends EntityHomeEx<E> {
	private static final long serialVersionUID = -3507272066511042267L;
	
	@In(required=true) CaseHome caseHome;
	
	private boolean lastResult = true;
	private List<E> results;
	
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		Object obj = getInstance();
		
		if (obj instanceof CaseData)
			((CaseData)obj).setTbcase(caseHome.getInstance());

		results = null;
		
		return super.persist();
	}
	
	/**
	 * Returns the managed TB Case in use
	 * @return
	 */
	public TbCase getTbCase() {
		return caseHome.getInstance();
	}
	
	/**
	 * Generates the HQL expression to retrieve the results
	 * @param lastRes
	 * @return
	 */
	public String getResultsHQL() {
		String hql = "from " + getEntityClass().getSimpleName() + " exam where exam.tbcase.id = #{tbCase.id}";
		
		if (lastResult)
			hql = hql.concat(" and exam.date = (select max(aux.date) " +
			"from " + getEntityClass().getSimpleName() + " aux where aux.tbcase = exam.tbcase) ");
		
		return hql.concat(" order by exam.date");
	}
	
	@Override
	public String remove() {
		results = null;
		super.remove();
		return "exam-removed";
	}
	
	public String removeExam(Integer id) {
		setId(id);
		results = null;
		remove();
		
		return "exam-removed";
	}
	
	public void clearResults() {
		results = null;
	}

	
	/**
	 * Creates the list of exam results
	 * @param lastRes
	 * @return
	 */
	protected List<E> createResults() {
		if (!caseHome.isManaged())
			return null;

		return getEntityManager()
			.createQuery(getResultsHQL())
			.getResultList();
	}
	
	/**
	 * Returns the exam result list
	 * @return
	 */
	public List<E> getResults() {
		if (results == null)
			results = createResults();
		return results;
	}

	public boolean isLastResult() {
		return lastResult;
	}

	public void setLastResult(boolean alastResult) {
		if (lastResult == alastResult)
			return;
		lastResult = alastResult;
		results = null;
	}

	public List<E> getAllResults() {
		setLastResult(false);
		return getResults();
	}
	
	public List<E> getLastResultList() {
		setLastResult(true);
		return getResults();
	}
}
