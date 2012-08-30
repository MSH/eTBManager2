package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.kh.entities.ExamCulture_Kh;
import org.msh.tb.transactionlog.LogInfo;

@Name("examCultureKHHome")
@LogInfo(roleName="EXAM_CULTURE")
public class ExamCultureKHHome extends LaboratoryExamHome<ExamCulture_Kh>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1298829141838748484L;
	
	@Factory("examCulture_Kh")
	public ExamCulture_Kh getExamCulture_Kh() {
		return getInstance();
	}
	
	private List<SelectItem> numColonies;

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
