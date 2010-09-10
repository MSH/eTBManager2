package org.msh.tb.cases.exams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ExamSusceptibilityResult;
import org.msh.mdrtb.entities.ExamSusceptibilityTest;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;

@Name("examSusceptTable")
public class ExamSusceptTable {
	
	@In(create=true) ExamSusceptHome examSusceptHome;
	
	/**
	 * @author Ricardo
	 * Keeps a list of exam results
	 */
	public class ResultItem {
		private ExamSusceptibilityTest exam;
		private ExamSusceptTable home;
		public ResultItem(ExamSusceptTable home, ExamSusceptibilityTest exam) {
			this.exam = exam;
			this.home = home;
		}
		public ExamSusceptibilityTest getExamResult() {
			return exam;
		}
		public ExamSusceptTable getHome() {
			return home;
		}
		public List<SusceptibilityResultTest> getResults() {
			return home.getResults(exam);
		}
	}
	
	private List<Substance> substances;
	private List<ResultItem> results;

	public List<Substance> getSubstances() {
		if (substances == null)
			createResultTable();
		return substances;
	}
	
	public List<ResultItem> getResults() {
		if (results == null)
			results = createResultTable();
		return results;
	}


	/**
	 * Return all results
	 * @return List of objects of class ResultItem carrying information about the susceptibility test
	 */
	public List<ResultItem> getAllResults() {
		if (examSusceptHome.isLastResult()) {
			examSusceptHome.setLastResult(false);
			results = null;
		}

		return getResults();
	}
	
	protected List<ResultItem> createResultTable() {
		List<ExamSusceptibilityTest> res;

		examSusceptHome.setLastResult(false);
		res = examSusceptHome.getResults();

		substances = new ArrayList<Substance>();
		
		List<ResultItem> lst = new ArrayList<ResultItem>();
		for(ExamSusceptibilityTest exam: res) {
			lst.add(new ResultItem(this, exam));
			addSubstances(exam);
		}
		
		Collections.sort(substances, new Comparator<Substance>() {
			public int compare(Substance item1, Substance item2) {
				return item1.compare(item2);
			}
		});
		
		return lst;
	}

	/**
	 * Add the medicines from a result into the list of medicines (duplicates are not included)  
	 * @param exam
	 */
	protected void addSubstances(ExamSusceptibilityTest exam) {
		for (ExamSusceptibilityResult medres: exam.getResults()) {
			Substance m = medres.getSubstance();
			if (!substances.contains(m))
				substances.add(m);
		}
	}

	protected List<SusceptibilityResultTest> getResults(ExamSusceptibilityTest exam) {
		List<SusceptibilityResultTest> res = new ArrayList<SusceptibilityResultTest>();
		for (Substance m: substances) {
			res.add(getResult(exam, m));
		}
		return res;
	}
	
	protected SusceptibilityResultTest getResult(ExamSusceptibilityTest exam, Substance m) {
		for (ExamSusceptibilityResult mr: exam.getResults()) {
			if (mr.getSubstance().equals(m)) {
				return mr.getResult();
			}
		}
		return SusceptibilityResultTest.NOTDONE;
	}
	
	public void setLastResult(boolean lastres) {
		examSusceptHome.setLastResult(lastres);
		results = null;
	}
	
	public boolean isLastResult() {
		return examSusceptHome.isLastResult();
	}
}
