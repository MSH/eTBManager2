package org.msh.tb.az.cases.exams;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.ExamCulture_Az;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.ua.entities.MedicineReceivingUA;

@Name("examCultureAZHome")
@LogInfo(roleName="EXAM_CULTURE",entityClass=ExamCulture_Az.class)
public class ExamCultureAZHome extends LaboratoryExamHome<ExamCulture_Az>{
	private static final long serialVersionUID = 2244413651346563406L;

	@Factory("examCultureAZ")
	public ExamCulture_Az getInstance() {
		return super.getInstance();
	}

	private List<SelectItem> numColonies;

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

	@Override
	public String getJoinFetchHQL() {
		return super.getJoinFetchHQL() + " left join fetch exam.method met ";
	}
}
