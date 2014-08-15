package org.msh.tb.laboratories;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.entities.PatientSample;
import org.msh.tb.entities.TbCase;

public class SampleReconstructionTask extends DbBatchTask {

	private List<Integer> cases;
	private List<PatientSample> samples;
	private Integer caseid;
	
	@Override
	protected void starting() {
		beginTransaction();
		cases = getEntityManager().createQuery("select id from TbCase").getResultList();
		commitTransaction();

		setRecordCount(cases.size());
		setAutomaticProgress(true);
		setCommitCounter(1);
	}

	@Override
	protected void finishing() {
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#processBatchRecord()
	 */
	@Override
	protected boolean processBatchRecord() throws Exception {
		if (getRecordIndex() >= getRecordCount())
			return false;

		caseid = cases.get(getRecordIndex());
		
		// load all samples of the patient
		samples = getEntityManager().createQuery("from PatientSample where tbcase.id = :id")
			.setParameter("id", caseid)
			.getResultList();

		checkExam("exammicroscopy");
		checkExam("examculture");
		checkExam("examdst");

		return true;
	}


	/**
	 * Check microscopy exams
	 */
	protected void checkExam(String tablename) {
		EntityManager em = getEntityManager();
		
		List<Object[]> lst = em.createNativeQuery("select id, sampleNumber, dateCollected " +
				"from " + tablename + " where case_id = :id and sample_id is null and dateCollected is not null")
				.setParameter("id", caseid)
				.getResultList();
		
		for (Object[] vals: lst) {
			Integer examid = (Integer)vals[0];
			String sampleNum = (String)vals[1];
			Date dateCollected = (Date)vals[2];
			PatientSample sample = getSample(dateCollected, sampleNum);

			em.createNativeQuery("update " + tablename + " set sample_id = :sampleid where id = :id")
				.setParameter("sampleid", sample.getId())
				.setParameter("id", examid)
				.executeUpdate();
		}
	}
	
	
	/**
	 * Return a sample by its date collected and sample number
	 * @param dateCollected
	 * @param sampleNum
	 * @return
	 */
	protected PatientSample getSample(Date dateCollected, String sampleNum) {
		for (PatientSample sample: samples) {
			if ((sampleNum != null) && (!sampleNum.isEmpty())) {
				if ((sample.getDateCollected().equals(dateCollected)) && (sampleNum.equals(sample.getSampleNumber())))
					return sample;
			}
			else {
				if (sample.getDateCollected().equals(dateCollected))
					return sample;
			}
		}

		EntityManager em = getEntityManager();

		PatientSample sample = new PatientSample();
		sample.setDateCollected(dateCollected);
		sample.setSampleNumber(sampleNum);

		sample.setTbcase( em.find(TbCase.class, caseid) );
		
		em.persist(sample);
		em.flush();
		
		samples.add(sample);
		
		return sample;
	}
}
