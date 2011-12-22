package org.msh.tb.cases.exams;


import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.transactionlog.LogInfo;


@Name("examMicroscopyHome")
@LogInfo(roleName="EXAM_MICROSC")
@Scope(ScopeType.CONVERSATION)
public class ExamMicroscopyHome extends LaboratoryExamHome<ExamMicroscopy> {
	private static final long serialVersionUID = -7854784222737606292L;

	private List<SelectItem> afbs;
	
	@Factory("examMicroscopy")
	public ExamMicroscopy getExamMicroscopy() {
		return (ExamMicroscopy)getInstance();
	}

	
	@Override
	public String persist() {
		ExamMicroscopy exam = getInstance();
		if (exam.getResult() != MicroscopyResult.POSITIVE)
			exam.setNumberOfAFB(null);
		
		return super.persist();
	}


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
