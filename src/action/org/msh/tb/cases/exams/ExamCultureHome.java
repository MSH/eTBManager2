package org.msh.tb.cases.exams;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.tb.log.LogInfo;

@Name("examCultureHome")
@LogInfo(roleName="EXAM_CULTURE")
public class ExamCultureHome extends LaboratoryExamHome<ExamCulture> {
	private static final long serialVersionUID = -5720233346817646475L;

	private List<SelectItem> numColonies;
	
	@Factory("examCulture")
	public ExamCulture getExamCulture() {
		return getInstance();
	}
	
	
	@Override
	public String persist() {
		ExamCulture exam = getInstance();
		if (exam.getResult() != CultureResult.POSITIVE)
			exam.setNumberOfColonies(null);
		
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

	@Override
	public String getJoinFetchHQL() {
		return super.getJoinFetchHQL() + " left join fetch exam.method met ";
	}
}
