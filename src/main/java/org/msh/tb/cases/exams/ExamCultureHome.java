package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.ExamStatus;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;


@Name("examCultureHome")
@LogInfo(roleName="EXAM_CULTURE", entityClass=ExamCulture.class)
public class ExamCultureHome extends LaboratoryExamHome<ExamCulture> {
	private static final long serialVersionUID = -5720233346817646475L;

	private List<SelectItem> numColonies;
    @In
    FacesMessages facesMessages;

	@Factory("examCulture")
	public ExamCulture getExamCulture() {
		return getInstance();
	}
	
	
	@Override
	public String persist() {
		ExamCulture exam = getInstance();

        // avoid errors in legacy UIs
        if (exam.getStatus() == null  &&  exam.getResult() != null) {
            exam.setStatus(ExamStatus.PERFORMED);
        }

		if (exam.getResult() != CultureResult.POSITIVE)
			exam.setNumberOfColonies(null);

        if(exam.getStatus().equals(ExamStatus.PERFORMED) && exam.getResult() == null){
            facesMessages.addToControlFromResourceBundle("resultfield", "javax.faces.component.UIInput.REQUIRED");
            return "error";
        }else if(!exam.getStatus().equals(ExamStatus.PERFORMED)){
            exam.setResult(null);
            exam.setMethod(null);
            exam.setDateRelease(null);
            exam.setComments(null);
            exam.setNumberOfColonies(null);
        }

		return super.persist();
	}


	public List<SelectItem> getNumColonies() {
		if (numColonies == null) {
			numColonies = new ArrayList<SelectItem>();
			
			SelectItem item = new SelectItem();
			item.setLabel("-");
			numColonies.add(item);
			
			for (int i = 1; i <= 9; i++) {
				item = new SelectItem();
				item.setLabel(Integer.toString(i));
				item.setValue(i);
				numColonies.add(item);
			}
		}
		return numColonies;
	}
	
	/**
	 * Create list of NumColonies of required count
	 * */
	
	public List<SelectItem> getNumColonies(int count) {
		if (numColonies == null) {
			numColonies = new ArrayList<SelectItem>();
			
			SelectItem item = new SelectItem();
			item.setLabel("-");
			numColonies.add(item);
			
			for (int i = 1; i <= count; i++) {
				item = new SelectItem();
				item.setLabel(Integer.toString(i));
				item.setValue(i);
				numColonies.add(item);
			}
		}
		return numColonies;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#getLogEntityClass()
	 */
	@Override
	public String getLogEntityClass() {
		return ExamCulture.class.getSimpleName();
	}
	

	@Override
	public String getJoinFetchHQL() {
		return super.getJoinFetchHQL() + " left join fetch exam.method met ";
	}
}
