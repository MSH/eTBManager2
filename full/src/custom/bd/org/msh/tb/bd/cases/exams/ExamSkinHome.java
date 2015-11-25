package org.msh.tb.bd.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.bd.entities.ExamSkin;
import org.msh.tb.cases.exams.LaboratoryExamHome;


@Name("examSkinHome")
@LogInfo(roleName="EXAM_SKIN", entityClass=ExamSkin.class)
public class ExamSkinHome extends LaboratoryExamHome<ExamSkin> {


		/**
		 * 
		 */
		private static final long serialVersionUID = 3613827878816022682L;

		@Factory("examSkin")
		public ExamSkin getExamBiopsy() {
			return getInstance();
		}
		
		@Override
		public String getJoinFetchHQL() {
			return super.getJoinFetchHQL() + " left join fetch exam.method met ";
		}
}

