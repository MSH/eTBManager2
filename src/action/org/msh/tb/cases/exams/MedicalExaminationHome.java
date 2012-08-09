package org.msh.tb.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.WorkspaceViewService;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.MedicalExamination;


@Name("medicalExaminationHome")
public class MedicalExaminationHome extends ExamHome<MedicalExamination>{
	private static final long serialVersionUID = 4240214890485645788L;

	
	@Factory("medicalExamination")
	public MedicalExamination getMedicalExamination() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		if (!validate())
			return "error";
		return super.persist();
	}

	
	/**
	 * Validate the information of the medical examination
	 * @return
	 */
	public boolean validate() {
		String hql = "select count(*) from MedicalExamination where tbcase.id = #{caseHome.id} and date = :dt";
		
		if (isManaged())
			hql += " and id <> " + getInstance().getId().toString();
		
		// check if date is duplicated
		Long num = (Long)getEntityManager()
			.createQuery(hql)
			.setParameter("dt", getInstance().getDate())
			.getSingleResult();
		
		if (num > 0)
			FacesMessages.instance().addToControlFromResourceBundle("medexamdate", "form.duplicatedname");
		
		return num == 0;
	}


	/**
	 * Initialize a new medical examination for Brazil - The height is automatically imported from the previous
	 */
	public void initialize() {
		if (WorkspaceViewService.instance().isFormPost())
			return;
		
		MedicalExamination medInst = getInstance();
		if (medInst.getHeight() != null)
			return;
		
		CaseHome caseHome = (CaseHome)Component.getInstance("caseHome");
		if ((caseHome == null) || (!caseHome.isManaged()))
			return;

		Integer age = caseHome.getInstance().getAge();
		if ((age == null) || (age < 18))
			return;

		Float height = (Float)getEntityManager().createQuery("select max(a.height) from MedicalExamination a where a.tbcase.id = :id " +
				"and a.date = (select max(a.date) from MedicalExamination b where b.tbcase.id=a.tbcase.id and b.height is not null)")
				.setParameter("id", caseHome.getId())
				.getSingleResult();

		medInst.setHeight(height);
	}


	@Override
	public void createdMessage() {
	}

	@Override
	public void updatedMessage() {
	}
}
