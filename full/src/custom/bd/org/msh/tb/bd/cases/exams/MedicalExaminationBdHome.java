package org.msh.tb.bd.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.application.ViewService;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.entities.MedicalExamination;


@Name("medicalExaminationBdHome")
@LogInfo(roleName="CASE_MED_EXAM", entityClass=MedicalExaminationBd.class)
public class MedicalExaminationBdHome extends ExamHome<MedicalExaminationBd>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2680118113742531608L;

	@Factory("medicalExaminationBd")
	public MedicalExaminationBd getMedicalExaminationBd() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		return super.persist();
	}

	/**
	 * Initialize a new medical examination - The height is automatically imported from the previous
	 */
	public void initialize() {
		if (ViewService.instance().isFormPost())
			return;

		MedicalExamination medInst = getInstance();
		if (medInst.getHeight() != null)
			return;

		CaseHome caseHome = (CaseHome) Component.getInstance("caseHome");
		if ((caseHome == null) || (!caseHome.isManaged()))
			return;

		Integer age = caseHome.getInstance().getAge();
		if ((age == null) || (age < 18))
			return;

		Float height = (Float)getEntityManager().createQuery("select max(a.height) from MedicalExaminationBd a where a.tbcase.id = :id " +
				"and a.date = (select max(a.date) from MedicalExaminationBd b where b.tbcase.id=a.tbcase.id and b.height is not null)")
				.setParameter("id", caseHome.getId())
				.getSingleResult();

		medInst.setHeight(height);
	}
}
