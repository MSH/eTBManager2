package org.msh.tb.vi;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.TbCase;
import org.msh.tb.transactionlog.LogInfo;


@Name("examCultureVI")
@LogInfo(roleName="EXAM_CULTURE", entityClass=ExamCulture.class)
public class ExamCultureVI implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4335023266573567258L;

	@In(create=true)
	ExamCultureHome examCultureHome;
	
	@In(create=true) ExamMicroscopyHome examMicroscopyHome;
	
	public void initView(){
		ExamMicroscopy examMicroscopy;
		List<ExamMicroscopy> results =  examMicroscopyHome.getAllResults();
		if(results.size()>0){
			examMicroscopy = results.get(0);
			examCultureHome.getInstance().setDateCollected(examMicroscopy.getDateCollected());
		}
	}
	
}
