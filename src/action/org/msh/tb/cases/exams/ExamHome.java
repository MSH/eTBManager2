package org.msh.tb.cases.exams;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.End;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.TbCase;


public class ExamHome<E> extends EntityHomeEx<E> {
	private static final long serialVersionUID = -3507272066511042267L;
	
	private boolean lastResult = true;
	private List<E> results;
	private CaseHome caseHome;
	
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		if (getCaseHome() == null) {
			return "error";
		}
		
		Object obj = getInstance();
		
		if (obj instanceof CaseData)
			((CaseData)obj).setTbcase(caseHome.getInstance());

		results = null;
		
		if (caseHome.isManaged())
			caseHome.updateCaseTags();
		
		return super.persist();
	}
	
	/**
	 * Returns the managed TB Case in use
	 * @return
	 */
	public TbCase getTbCase() {
		return getCaseHome().getInstance();
	}

	
	/**
	 * Return an instance of the {@link CaseHome} component
	 * @return
	 */
	public CaseHome getCaseHome() {
		if (caseHome == null)
			caseHome = (CaseHome)Component.getInstance("caseHome");
		return caseHome;
	}
	
	/**
	 * Generates the HQL expression to retrieve the results
	 * @param lastRes
	 * @return
	 */
	public String getResultsHQL() {
		String hql = "from " + getEntityClass().getSimpleName() + " exam where exam.tbcase.id = #{tbcase.id}";
		
		if (lastResult)
			hql = hql.concat(" and exam.date = (select max(aux.date) " +
			"from " + getEntityClass().getSimpleName() + " aux where aux.tbcase = exam.tbcase) ");
		
		return hql.concat(" order by exam.date");
	}
	
	@Override
	public String remove() {
		results = null;
		super.remove();
		
		if (getInstance() instanceof CaseData) {
			TbCase tbcase = ((CaseData)getInstance()).getTbcase();
			TagsCasesHome.instance().updateTags(tbcase);
		}
		
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
		if (!getCaseHome().isManaged())
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
