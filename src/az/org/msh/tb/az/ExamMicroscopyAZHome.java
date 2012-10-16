package org.msh.tb.az;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.ExamMicroscopyAZ;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.entities.enums.MicroscopyResult;

@Name("examMicroscopyAZHome")
public class ExamMicroscopyAZHome extends LaboratoryExamHome<ExamMicroscopyAZ>{
	private static final long serialVersionUID = 2244413651346563406L;

	@Factory("examMicroscopyAZ")
	public ExamMicroscopyAZ getInstance() {
		return super.getInstance();
	}

	private List<SelectItem> afbs;

	@Override
	public String persist() {
		ExamMicroscopyAZ exam = getInstance();
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
