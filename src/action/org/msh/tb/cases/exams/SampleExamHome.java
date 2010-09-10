package org.msh.tb.cases.exams;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.Laboratory;
import org.msh.mdrtb.entities.LaboratoryExam;
import org.msh.mdrtb.entities.PatientSample;
import org.msh.tb.laboratories.LaboratorySelection;

/**
 * Home class for laboratory exam handling (culture, microscopy and DST)
 * @author Ricardo Memoria
 *
 * @param <E>
 */
public abstract class SampleExamHome<E> extends ExamHome<E>{
	private static final long serialVersionUID = 3707328854678910174L;

	@In(create=true) FacesMessages facesMessages;
	
	private List<PatientSample> patientSamples;
	private PatientSample sample;
	private LaboratorySelection labselection;
	
	private static final String[] extraNestedProperties = {"sample.dateCollected", "sample.sampleNumber"};

	
	/* (non-Javadoc)
	 * @see org.msh.tb.cases.ExamHome#persist()
	 */
	@Override
	public String persist() {
		if (!validateDates()) {
			facesMessages.addToControlFromResourceBundle("dtrelease", "cases.exams.datereleasebeforecol");
			return "error";
		}
		
		// set laboratory
		try {
			if (labselection != null) {
				PropertyUtils.setProperty(getInstance(), "laboratory", labselection.getLaboratory());
			}
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		
		// setup sample
		if (!setupSample()) 
			return "error";
		
		// save data
		getEntityManager().persist(getLaboratoryExam().getSample());
		String ret = super.persist();
		
		
		deleteOrphamSamples();
		return ret;
	}
	

	/**
	 * Check if date of release is before the date sample was collected
	 * @return
	 */
	public boolean validateDates() {
		Date dtRelease = getDateRelease();
		Date dtCollected = getSample().getDateCollected();
		
		if ((dtRelease == null) || (dtCollected == null))
			return true;
		
		if (dtRelease.before(dtCollected))
			 return false;
		else return true;
	}
	
	/**
	 * Adjust the sample associated to the exam
	 */
	protected boolean setupSample() {
		if (sample == null)
			return false;

		String propName = getExamPropertyName();
		
		String sampleNumber = sample.getSampleNumber();
		Date dateCollected = sample.getDateCollected();

		// search for a compatible sample already registered for the patient
		PatientSample aux = findCompactibleSample();
		
		LaboratoryExam exam = (LaboratoryExam)getInstance();

		try {
			if (aux != null) {
				LaboratoryExam exam2 = (LaboratoryExam)PropertyUtils.getProperty(aux, propName);
				// is the sample being used by another exam of the same type?
				if ((exam2 != null) && (exam2 != exam)) {
					facesMessages.addFromResourceBundle("cases.exams.datesampleexists");
					return false;
				}
			}
			
			// erase the link of the current sample with the exam
			if (exam.getSample() != null) {
				PropertyUtils.setProperty(exam.getSample(), getExamPropertyName(), null);
				exam.setSample(null);
			}
			
			// is a new sample ?
			if (aux == null) {
				exam.setSample(sample);
				sample.setTbcase(getTbCase());
				PropertyUtils.setProperty(sample, getExamPropertyName(), exam);
			}
			else {
				exam.setSample(aux);
				PropertyUtils.setProperty(aux, getExamPropertyName(), exam);
			}
		} catch (Exception e) {
			facesMessages.add(e.getMessage());
			return false;
		}
		
		aux = exam.getSample();
		aux.setDateCollected(dateCollected);
		aux.setSampleNumber(sampleNumber);

		return true;
	}


	/**
	 * Search for a patient sample by its id number
	 * @param sampleNumber
	 * @return
	 */
	protected PatientSample findSampleByNumber(String sampleNumber) {
		List<PatientSample> lst = getEntityManager().createQuery("from PatientSample s where s.sampleNumber = :sample " +
				"and s.tbcase.id = #{caseHome.id}")
				.setParameter("sample", sampleNumber)
				.getResultList();
		
		if (lst.size() == 0)
			 return null;
		else return lst.get(0);
	}


	/**
	 * Find a compatible sample based on information of the sample given by the user
	 * @return {@link PatientSample} instance of another sample for the case
	 */
	protected PatientSample findCompactibleSample() {
		if (sample == null)
			return null;
		
		String sampleNumber = sample.getSampleNumber();
		boolean bHasSample = (sampleNumber != null) && (!sampleNumber.isEmpty());
		
		Query qry = getEntityManager().createQuery("from PatientSample s " +
				"where (s.dateCollected = :dt" + (bHasSample? " or s.sampleNumber = :number": "") + 
				") and s.tbcase.id = #{caseHome.id}")
				.setParameter("dt", sample.getDateCollected());
		
		if (bHasSample)
			qry.setParameter("number", sample.getSampleNumber());
			
		List<PatientSample> lst = qry.getResultList();
		
		if (lst.size() == 0)
			 return null;
		else return lst.get(0);
	}


	/**
	 * Return date of release of the exam
	 * @return date 
	 */
	protected Date getDateRelease() {
		Object exam = getInstance();
		if (exam instanceof LaboratoryExam)
			return ((LaboratoryExam)exam).getDateRelease();
		else return null;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.ExamHome#getResultsHQL()
	 */
	@Override
	public String getResultsHQL() {
		String hql = "from " + getEntityClass().getSimpleName() + " exam " +
				"join fetch exam.sample s " +
				"where s.tbcase.id = #{tbCase.id}";
	
		if (isLastResult())
			hql = hql.concat(" and s.dateCollected = (select max(aux.dateCollected) " +
			"from PatientSample aux where aux.tbcase = s.tbcase) ");
		
		return hql.concat(" order by s.dateCollected");
	}


	/**
	 * Get list of available samples
	 * @return
	 */
	public List<PatientSample> getPatientSamples() {
		if (patientSamples == null) {
			String prop = getExamPropertyName();
			String cond = " and (s." + prop + " is null";
			if (isManaged())
				cond += " or s." + prop + "=" + getLaboratoryExam().getId().toString();
			cond += ")";
			
			patientSamples = getEntityManager()
				.createQuery("from PatientSample s where s.tbcase.id = #{caseHome.id}" + cond)
				.getResultList();
		}
		return patientSamples;
	}


	/**
	 * Return the current sample to be edited
	 * @return
	 */
	public PatientSample getSample() {
		if (sample == null) {
			sample = new PatientSample();
			if (isManaged()) {
				PatientSample aux = getLaboratoryExam().getSample();
				sample.setDateCollected(aux.getDateCollected());
				sample.setSampleNumber(aux.getSampleNumber());
			}
		}
		return sample;
	}


	/**
	 * Return the property name in the {@link PatientSample} class that is linked to the exam
	 * @return name of the property in the {@link PatientSample} class
	 */
	public abstract String getExamPropertyName();	


	/**
	 * delete all patient samples that are not related to any exam
	 */
	public void deleteOrphamSamples() {
		getEntityManager().createQuery("delete from PatientSample s " +
				"where s.examCulture.id is null and s.examSputumSmear.id is null and s.examSusceptibilityTest.id is null")
				.executeUpdate();
	}
	
	public LaboratoryExam getLaboratoryExam() {
		return (LaboratoryExam)getInstance();
	}


	@Override
	public String remove() {
		PatientSample patientSample;
		Object obj = getInstance();
		try {
			patientSample = (PatientSample)PropertyUtils.getProperty(obj, "sample");
			if (patientSample != null)
				PropertyUtils.setProperty(obj, "sample", null);
		} catch (Exception e) {
			patientSample = null;
		}
		
		if (patientSample != null) {
			String propName = getExamPropertyName();
			getEntityManager().createQuery("update PatientSample set " + propName + " = null where id=:id")
				.setParameter("id", patientSample.getId())
				.executeUpdate();
		}

		getEntityManager().createQuery("delete from " + obj.getClass().getSimpleName() + " where id = :id")
			.setParameter("id", getId())
			.executeUpdate();
		clearResults();
		setId(null);

//		saveTransactionLog(RoleAction.DELETE);

		return "exam-removed";
	}


	
	
	@Override
	public void setId(Object id) {
		super.setId(id);

		initializeLabselection();
		sample = null;
		patientSamples = null;
	}
	
	
	/**
	 * Initialize
	 */
	protected void initializeLabselection() {
		if (labselection != null) {
			try {
				Object obj = getInstance();
				Laboratory lab = (Laboratory)PropertyUtils.getProperty(obj, "laboratory");
				labselection.setLaboratory(lab);
			} catch (Exception e) {
				labselection.setLaboratory(null);
			}
		}
	}



	@Override
	public List<String> getExtraNestedProperties() {
		return Arrays.asList(extraNestedProperties);
	}


	/**
	 * Return object for laboratory selection
	 * @return
	 */
	public LaboratorySelection getLabselection() {
		if (labselection == null) {
			labselection = new LaboratorySelection();
			initializeLabselection();
		}
		return labselection;
	}
}
