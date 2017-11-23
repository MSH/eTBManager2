package org.msh.tb.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.End;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.WsEntityHome;
import org.msh.tb.entities.CaseData;
import org.msh.tb.entities.TbCase;

import java.util.List;

public abstract class ExamHome<E> extends WsEntityHome<E> {
	private static final long serialVersionUID = -3507272066511042267L;
	
	private boolean orderByDateDec = true;
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
		
		String s = super.persist();
		if ("persisted".equals(s)) {
			TagsCasesHome.instance().updateTags(getTbCase());
		}
		
		return s;
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
	 * ordered by if false
	 * @return
	 */
	public String getResultsHQL() {
		String hql = "from " + getEntityClass().getSimpleName() + " exam where exam.tbcase.id = #{tbcase.id}";
		
		if(orderByDateDec)
			return hql.concat(" order by exam.date desc");
		else
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

	/**
	 * Returns all exams results ordered chronologically reversed
	 * @return
	 */
	public List<E> getAllResults() {
		if(!isOrderByDateDec() && results != null)
			results = null;
		
		setOrderByDateDec(true);
		return getResults();
	}
	
	/**
	 * Returns all exams results ordered chronologically
	 * @return
	 */
	public List<E> getAllResultsChronologicallyOrdered() {
		if(isOrderByDateDec() && results != null)
			results = null;
		
		setOrderByDateDec(false);
		return getResults();
	}

	/**
	 * @return the orderByDateDec
	 */
	public boolean isOrderByDateDec() {
		return orderByDateDec;
	}

	/**
	 * @param orderByDateDec the orderByDateDec to set
	 */
	public void setOrderByDateDec(boolean orderByDateDec) {
		this.orderByDateDec = orderByDateDec;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#getLogDescription()
	 */
	@Override
	protected String getLogDescription() {
		return getTbCase().toString();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#getLogEntityId()
	 */
	@Override
	protected Integer getLogEntityId() {
		return getTbCase().getId();
	}

	
}
