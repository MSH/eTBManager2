package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DstResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Name("examDSTTable")
public class ExamDSTTable {
	
	@In(create=true) ExamDSTHome examDSTHome;
	
	/**
	 * @author Ricardo
	 * Keeps a list of exam results
	 */
	public class ResultItem {
		private ExamDST exam;
		private ExamDSTTable home;
		public ResultItem(ExamDSTTable home, ExamDST exam) {
			this.exam = exam;
			this.home = home;
		}
		public ExamDST getExamResult() {
			return exam;
		}
		public ExamDSTTable getHome() {
			return home;
		}
		public List<DstResult> getResults() {
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
	 * @return List of objects of class ResultItem carrying information about the DST
	 */
	public List<ResultItem> getAllResults() {
		if (examDSTHome.isLastResult()) {
			examDSTHome.setLastResult(false);
			results = null;
		}

		return getResults();
	}
	
	protected List<ResultItem> createResultTable() {
		List<ExamDST> res;

		examDSTHome.setLastResult(false);
		res = examDSTHome.getResults();

		substances = new ArrayList<Substance>();
		
		List<ResultItem> lst = new ArrayList<ResultItem>();
		for(ExamDST exam: res) {
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
	protected void addSubstances(ExamDST exam) {
		for (ExamDSTResult medres: exam.getResults()) {
			Substance m = medres.getSubstance();
			if (!substances.contains(m))
				substances.add(m);
		}
	}

	protected List<DstResult> getResults(ExamDST exam) {
		List<DstResult> res = new ArrayList<DstResult>();
		for (Substance m: substances) {
			res.add(getResult(exam, m));
		}
		return res;
	}
	
	protected DstResult getResult(ExamDST exam, Substance m) {
		for (ExamDSTResult mr: exam.getResults()) {
			if (mr.getSubstance().equals(m)) {
				return mr.getResult();
			}
		}
		return DstResult.NOTDONE;
	}
	
	public void setLastResult(boolean lastres) {
		examDSTHome.setLastResult(lastres);
		results = null;
	}
	
	public boolean isLastResult() {
		return examDSTHome.isLastResult();
	}
}
