package org.msh.tb.bd.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.LaboratoryExamHome;


	@Name("examBiopsyHome")
	public class ExamBiopsyHome extends LaboratoryExamHome<ExamBiopsy> {


		/**
		 * 
		 */
		private static final long serialVersionUID = 3613827878816022682L;

		@Factory("examBiopsy")
		public ExamBiopsy getExamBiopsy() {
			return getInstance();
		}
		
		@Override
		public String getJoinFetchHQL() {
			return super.getJoinFetchHQL() + " left join fetch exam.method met ";
		}
	}
