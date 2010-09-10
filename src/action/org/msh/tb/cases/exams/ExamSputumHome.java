package org.msh.tb.cases.exams;


import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.tb.log.LogInfo;

@Name("examSputumHome")
@LogInfo(roleName="EXAM_MICROSC")
@Scope(ScopeType.CONVERSATION)
public class ExamSputumHome extends SampleExamHome<ExamSputumSmear> {
	private static final long serialVersionUID = -7854784222737606292L;

	private List<SelectItem> afbs;
	
	@Factory("examSputum")
	public ExamSputumSmear getExamSputum() {
		return (ExamSputumSmear)getInstance();
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
	public String getExamPropertyName() {
		return "examSputumSmear";
	}
	
}
