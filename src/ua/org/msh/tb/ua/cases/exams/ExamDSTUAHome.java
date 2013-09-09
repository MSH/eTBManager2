package org.msh.tb.ua.cases.exams;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.enums.DstResult;

@Name("examDSTUAHome")
public class ExamDSTUAHome{
	private static final long serialVersionUID = 715060453658324690L;
	private ExamDSTHome examDSTHome;

	public Boolean validateAndPrepareFields(){
		// update exams
		examDSTHome = (ExamDSTHome)Component.getInstance("examDSTHome");
		List<ExamDSTResult> items = examDSTHome.getItems();
		if (items != null) {
			ExamDST exam = examDSTHome.getExamDST();
			exam.setNumContaminated(0);
			exam.setNumResistant(0);
			exam.setNumSusceptible(0);
			
			for (ExamDSTResult ms: items) {
				// add new results
				if (ms.getResult() != DstResult.NOTDONE) {
					if (!exam.getResults().contains(ms))
						exam.getResults().add(ms);
					switch (ms.getResult()) {
					case CONTAMINATED:
						exam.setNumContaminated(exam.getNumContaminated() + 1);
						break;
					case RESISTANT:
						exam.setNumResistant(exam.getNumResistant() + 1);
						break;
					case SUSCEPTIBLE:
						exam.setNumSusceptible(exam.getNumSusceptible() + 1);
						break;
					}
				}
				else {
					// remove undone results
					if (exam.getResults().contains(ms)) {
						exam.getResults().remove(ms);
						examDSTHome.getEntityManager().remove(ms);
					}
				}
			}
			
			if (exam.getResults().size() == 0)
				return false;
			else
				return true;
		}
		return false;	
	}
	
	public String persistUA(){
		if (!validateAndPrepareFields())
			return "error";
		
		return examDSTHome.persistWithoutValidation();
	}
	
}
