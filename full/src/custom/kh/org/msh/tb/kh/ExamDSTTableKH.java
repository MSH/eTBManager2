package org.msh.tb.kh;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.kh.entities.ExamDST_Kh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Name("examDSTTableKh")
public class ExamDSTTableKH {
	
	@In(create=true) ExamDSTKHHome examDSTKHHome;
	
	/**
	 * @author Ricardo
	 * Keeps a list of exam results
	 * Cambodia - Vani
	 */
	public class ResultItem {
		private ExamDST_Kh exam;
		private ExamDSTTableKH home;
		public ResultItem(ExamDSTTableKH home, ExamDST_Kh exam) {
			this.exam = exam;
			this.home = home;
		}
		public ExamDST_Kh getExamResult() {
			return exam;
		}
		public ExamDSTTableKH getHome() {
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
		return getResults();
	}
	
	protected List<ResultItem> createResultTable() {
		List<ExamDST_Kh> res;

		res = examDSTKHHome.getResults();

		substances = new ArrayList<Substance>();
		
		List<ResultItem> lst = new ArrayList<ResultItem>();
		for(ExamDST_Kh exam: res) {
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
	protected void addSubstances(ExamDST_Kh exam) {
		for (ExamDSTResult medres: exam.getResults()) {
			Substance m = medres.getSubstance();
			if (!substances.contains(m))
				substances.add(m);
		}
	}

	protected List<DstResult> getResults(ExamDST_Kh exam) {
		List<DstResult> res = new ArrayList<DstResult>();
		for (Substance m: substances) {
			res.add(getResult(exam, m));
		}
		return res;
	}
	
	protected DstResult getResult(ExamDST_Kh exam, Substance m) {
		for (ExamDSTResult mr: exam.getResults()) {
			if (mr.getSubstance().equals(m)) {
				return mr.getResult();
			}
		}
		return DstResult.NOTDONE;
	}
	
}
