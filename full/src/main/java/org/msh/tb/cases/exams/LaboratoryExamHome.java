package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.In;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.AuthorizationException;
import org.msh.tb.application.App;
import org.msh.tb.application.ViewService;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.entities.TbCase;
import org.msh.tb.laboratories.ExamRequestHome;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.misc.EntityEvent;

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
     * Initialize the exam form from the laboratory module, where the
     * exam request is required, or initialized if the exam is managed
     */
    public void initializeLabForm() {
        ExamRequestHome home = (ExamRequestHome)App.getComponent("examRequestHome");

        if (!home.isManaged()) {
            if (!isManaged()) {
                throw new RuntimeException("Exam request was not found");
            }
            home.setId(getLaboratoryExam().getRequest().getId());
        }
        initializeForm();
    }

	/**
	 * Initialize the form. This method is called just once, when the form
	 * is displayed, i.e, when a GET method is requested.
	 * 
	 * It also checks if the user has permission to use this form
	 */
	public void initializeForm() {
        if ((!caseHome.isManaged()) && (isManaged())) {
            caseHome.setId(getLaboratoryExam().getTbcase().getId());
        }

		// check permission
		if (!caseHome.isCanEditExams())
			throw new AuthorizationException("Permission denied");

		// if it's a POST, doesn't initialize
		if (ViewService.instance().isFormPost())
			return;

        LaboratoryExam exam = getLaboratoryExam();
        System.out.println(exam.getId());
        System.out.println(exam.getLaboratory());

		Laboratory lab = exam.getLaboratory();
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
		if (labselection != null) {
			getLaboratoryExam().setLaboratory(labselection.getSelected());
		}

        persistSample();

        String s = super.persist();

        raiseEvent((isManaged() ? EntityEvent.EventType.EDIT : EntityEvent.EventType.NEW));

		// save data
		return s;
	}


    /**
     * Update the sample record based on the value of the sample
     */
    protected void persistSample() {
/*
        // sample was not initialized, so do nothing
        if (sample == null) {
            return;
        }

        LaboratoryExam exam = getLaboratoryExam();

        if (sample.isSampleInfoEmpty()) {
            // if sample entered by user is empty and there is no sample in
            // the original exam, so do nothig
            if (exam.getSample() == null) {
                return;
            }
        }
*/
    }

    /**
     * Return the number of records that this sample is related to
     * @param sample the instance of PatientSample
     * @return number of exams that refers to this sample
     */
/*
    protected int calcSampleReferenceCount(PatientSample sample) {
        String[] tables = {"examculture", "exammicroscopy", "examxpert", "examdst"};

        EntityManager em = App.getEntityManager();
        int count = 0;
        for (String tbl: tables) {
            Number val = (Number)em.createNativeQuery("select count(*) from " + tbl + " where sample_id = :id")
                    .setParameter("id", sample.getId())
                    .getSingleResult();
            count += val.intValue();
        }

        return count;
    }
*/

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
			.createQuery("select id from " + getEntityClass().getSimpleName() +
                    " where sampleNumber = :id  and tbcase.id = :caseid")
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
	
		if(super.isOrderByDateDec())
			return hql.concat(" order by exam.dateCollected desc");
		else
			return hql.concat(" order by exam.dateCollected");
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
        raiseEvent(EntityEvent.EventType.DELETE);
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

    private void raiseEvent(EntityEvent.EventType type){
        LaboratoryExam exam = (LaboratoryExam) getInstance();
        String entityName = "entity." + exam.getClass().getSimpleName();
        Events.instance().raiseEvent(entityName, new EntityEvent(type, exam));
    }
}
