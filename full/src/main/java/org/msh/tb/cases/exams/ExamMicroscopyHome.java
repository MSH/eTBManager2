package org.msh.tb.cases.exams;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.entities.enums.MicroscopyResult;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;


/**
 * Handle operation of the microscopy exam
 * 
 * @author Ricardo Memoria
 *
 */
@Name("examMicroscopyHome")
@LogInfo(roleName="EXAM_MICROSC", entityClass=ExamMicroscopy.class)
@Scope(ScopeType.CONVERSATION)
public class ExamMicroscopyHome extends LaboratoryExamHome<ExamMicroscopy> {
	private static final long serialVersionUID = -7854784222737606292L;

	private List<SelectItem> afbs;

	@In
    FacesMessages facesMessages;

	/**
	 * @return
	 */
	@Factory("examMicroscopy")
	public ExamMicroscopy getExamMicroscopy() {
		return (ExamMicroscopy)getInstance();
	}

	
	/** {@inheritDoc}
	 */
	@Override
	public String persist() {
		ExamMicroscopy exam = getInstance();

        if (exam.getResult() != MicroscopyResult.POSITIVE)
			exam.setNumberOfAFB(null);

        // set pre condition to avoid problems with legacy versions
        if (exam.getResult() != null  &&  exam.getStatus() == null) {
            exam.setStatus(ExamStatus.PERFORMED);
        }
        else {
            // result was informed ?
            if (ExamStatus.PERFORMED.equals(exam.getStatus()) && exam.getResult() == null){
                facesMessages.addToControlFromResourceBundle("miccbres2", "javax.faces.component.UIInput.REQUIRED");
                return "error";
            }

            if (ExamStatus.PERFORMED != exam.getStatus()) {
                exam.setResult(null);
                exam.setDateRelease(null);
                exam.setComments(null);
                exam.setNumberOfAFB(null);
            }
        }

		return super.persist();
	}


	/**
	 * Return options to the AFB field
	 * @return {@link List} of {@link SelectItem} objects
	 */
	public List<SelectItem> getAFBs() {
		if (afbs == null) {
			afbs = new ArrayList<SelectItem>();
			
			SelectItem item = new SelectItem();
			item.setLabel("-");
			afbs.add(item);
			
			for (int i = 1; i <= 9; i++) {
				item = new SelectItem();
				item.setLabel(Integer.toString(i));
				item.setValue(i);
				afbs.add(item);
			}
		}
		return afbs;
	}
	

	@Override
	public String getJoinFetchHQL() {
		return super.getJoinFetchHQL() + " left join fetch exam.method met ";
	}
}
