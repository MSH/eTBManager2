package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.AuthorizationException;
import org.msh.tb.application.ViewService;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.entities.TbCase;
import org.msh.tb.laboratories.LaboratorySelection;

import java.util.Date;
import java.util.List;

/**
 * Home class for laboratory exam handling (culture, microscopy and DST)
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public abstract class LaboratoryExamHome<E> extends ExamHome<E>{
	private static final long serialVersionUID = 3707328854678910174L;

	@In(required=true) CaseHome caseHome;
	
	private LaboratorySelection labselection;
	
	public LaboratoryExam getExamResult() {
		return (LaboratoryExam)getInstance();
	}

	
	/**
	 * Initialize the form. This method is called just once, when the form
	 * is displayed, i.e, when a GET method is requested.
	 * 
	 * It also checks if the user has permission to use this form
	 */
	public void initializeForm() {
		// check permission
		if (!caseHome.isCanEditExams())
			throw new AuthorizationException("Permission denied");

		// if it's a POST, doesn't initialize
		if (ViewService.instance().isFormPost())
			return;

		Laboratory lab = getLaboratoryExam().getLaboratory();
		getLabselection().setSelected(lab);
	}
	
	
	/* (non-Javadoc)
	 * @see org.msh.tb.cases.ExamHome#persist()
	 */
	@Override
	public String persist() {
		if (!validateDates()) {
			FacesMessages.instance().addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
			return "error";
		}

		LaboratoryExam exam = getLaboratoryExam();
		if (exam.getTbcase() == null) {
			exam.setTbcase(caseHome.getInstance());
		}
		
		// set laboratory
		try {
			if (labselection != null) {
				getLaboratoryExam().setLaboratory(labselection.getSelected());
			}
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		
		// save data
		return super.persist();
	}
	

	/**
	 * Check if date of release is before the date sample was collected
	 * @return
	 */
	public boolean validateDates() {
		LaboratoryExam res = getLaboratoryExam();
		Date dtRelease = res.getDateRelease();
		Date dtCollected = res.getDateCollected();
		
		if ((dtRelease == null) || (dtCollected == null))
			return true;
		
		if (dtRelease.before(dtCollected))
			 return false;
		else return true;
	}
	
	
	/**
	 * Search for an instance of the exam by its sample id and case
	 * @param sampleId
	 * @return
	 */
	public Integer findExamBySampleId(TbCase tbcase, String sampleId) {
		List lst = getEntityManager()
			.createQuery("select id from " + getEntityClass().getSimpleName() + " where sample.sampleNumber = :id  and tbcase.id = :caseid")
			.setParameter("id", sampleId)
			.setParameter("caseid", tbcase.getId())
			.getResultList();

		if (lst.size() == 0)
			return null;

		Integer examId = (Integer)lst.get(0);
		return examId;
	}
	

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.ExamHome#getResultsHQL()
	 */
	@Override
	public String getResultsHQL() {
		String entityClass = getEntityClass().getSimpleName();
		String hql = "from " + entityClass + " exam " +
				getJoinFetchHQL() +
				" where exam.tbcase.id = #{tbcase.id}";
	
		if (isLastResult())
			hql = hql.concat(" and s.patientSample.dateCollected = (select max(aux.sample.dateCollected) " +
			"from " + entityClass + " aux where aux.tbcase = s.tbcase) ");
		
		if(super.isOrderByDateDec())
			return hql.concat(" order by exam.sample.dateCollected desc");
		else
			return hql.concat(" order by exam.sample.dateCollected");
	}

	
	/**
	 * Return the fetch HQL instruction to fetch extra data from the exam
	 * @return String value
	 */
	public String getJoinFetchHQL() {
		return "left join fetch exam.laboratory lab ";
	}

	/**
	 * Return an instance of the exam object being handled
	 * @return instance of {@link org.msh.tb.entities.LaboratoryExam}
	 */
	public LaboratoryExam getLaboratoryExam() {
		return (LaboratoryExam)getInstance();
	}


	/** {@inheritDoc}
	 */
	@Override
	public String remove() {
		super.remove();
		return "exam-removed";
	}


	/**
	 * Return object for laboratory selection
	 * @return
	 */
	public LaboratorySelection getLabselection() {
		if (labselection == null) {
			labselection = new LaboratorySelection("labid");
		}
		return labselection;
	}
}
