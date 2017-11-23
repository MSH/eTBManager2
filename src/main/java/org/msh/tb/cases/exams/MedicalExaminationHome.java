package org.msh.tb.cases.exams;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.application.ViewService;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.enums.YesNoType;

import java.util.List;


/**
 * Handle basic operations of a medical examination related to a suspect or confirmed case
 * 
 * @author Ricardo Memoria
 *
 */
@Name("medicalExaminationHome")
@LogInfo(roleName="CASE_MED_EXAM", entityClass=MedicalExamination.class)
public class MedicalExaminationHome extends ExamHome<MedicalExamination>{
	private static final long serialVersionUID = 4240214890485645788L;
	
	@In(create=true) FacesMessages facesMessages;
	@In(required=true) CaseHome caseHome;
	
	/**
	 * Factory method to return the current instance of the {@link MedicalExamination} managed by this home case
	 * @return
	 */
	@Factory("medicalExamination")
	public MedicalExamination getMedicalExamination() {
		return getInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.ExamHome#persist()
	 */
	@Override
	public String persist() {
		if(getMedicalExamination().getUsingPrescMedicines()==YesNoType.NO && getMedicalExamination().getReasonNotUsingPrescMedicines().isEmpty()){
			facesMessages.addToControlFromResourceBundle("edtreason", "javax.faces.component.Reason.REQUIRED");
			return "error";
		}
		if (!validate())
			return "error";
		
		String s = super.persist();
		
		//Update List in caseHome
		List<MedicalExamination> lst = (List<MedicalExamination>) getEntityManager().createQuery("from MedicalExamination where tbcase.id = :id order by date desc")
																	.setParameter("id", caseHome.getId())
																	.getResultList();

		
		caseHome.getTbCase().setExaminations(lst);
				
		return s;
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
	 * Initialize a new medical examination - The height is automatically imported from the previous
	 */
	public void initialize() {
		if (ViewService.instance().isFormPost())
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
	
	public String remove(){
		getInstance().getTbcase().getExaminations().remove(getInstance());
		caseHome.getInstance().getExaminations().remove(getInstance());

		return super.remove();
	}

}
