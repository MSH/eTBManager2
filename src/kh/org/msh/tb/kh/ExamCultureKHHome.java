package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.kh.entities.ExamCulture_Kh;
import org.msh.tb.transactionlog.LogInfo;

@Name("examCultureKHHome")
@LogInfo(roleName="EXAM_CULTURE_KH")
public class ExamCultureKHHome<E> extends LaboratoryExamHome<ExamCulture_Kh>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1298829141838748484L;
	@In(create=true)
	private EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	
	@Factory("examCulture_Kh")
	public ExamCulture_Kh getExamCulture_Kh() {
		return getInstance();
	}
	
	private List<SelectItem> numColonies;
	@Override
	public String persist() {
		ExamCulture_Kh exam = getInstance();
		exam.setTbcase(caseHome.getInstance());
		if (exam.getResult() != CultureResult.POSITIVE)
			exam.setNumberOfColonies(null);
		
		entityManager.persist(exam);
		return "persisted";
	}

	@Override
	public String remove() {
		ExamCulture_Kh exam = getInstance();
		exam.setTbcase(caseHome.getInstance());
		entityManager.remove(exam);
		return"exam-removed";
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
