package org.msh.tb.cases.exams;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ExamSusceptibilityResult;
import org.msh.mdrtb.entities.ExamSusceptibilityTest;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.enums.SusceptibilityResultTest;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.log.LogInfo;

@Name("examSusceptHome")
@LogInfo(roleName="EXAM_DST")
public class ExamSusceptHome extends SampleExamHome<ExamSusceptibilityTest> {
	private static final long serialVersionUID = 270035993717644991L;

	private List<ExamSusceptibilityResult> items;

	@In(create=true) SubstancesQuery substances;
	
	@Factory("examSuscept")
	public ExamSusceptibilityTest getExamSusceptibilityTest() {
		return getInstance();
	}
	

	public List<ExamSusceptibilityResult> getItems() {
		if (items == null)
			createItems();
		
		return items;
	}
	
	protected ExamSusceptibilityResult findResult(Substance s) {
		for (ExamSusceptibilityResult mr: getInstance().getResults()) {
			if (mr.getSubstance().equals(s)) {
				return mr;
			}
		}
		return null;
	}
	
	protected void createItems() {
		items = new ArrayList<ExamSusceptibilityResult>();
//		boolean bEdt = isManaged();
		
		for (Substance s: substances.getDstSubstances()) {
			ExamSusceptibilityResult res = findResult(s);
			if (res == null) {
				res = new ExamSusceptibilityResult();
				res.setSubstance(s);
			}

			if (res.getResult() == null)
				res.setResult(SusceptibilityResultTest.NOTDONE);
			
			items.add(res);
			res.setExam(getInstance());
		}
	}
	
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		// update exams
		if (items != null) {
			ExamSusceptibilityTest exam = getInstance();
			exam.setNumContaminated(0);
			exam.setNumResistant(0);
			exam.setNumSusceptible(0);
			
			for (ExamSusceptibilityResult ms: items) {
				// add new results
				if (ms.getResult() != SusceptibilityResultTest.NOTDONE) {
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
						getEntityManager().remove(ms);
					}
				}
			}
			
			if (exam.getResults().size() == 0)
				return "error";
		}
		
		return super.persist();
	}
	
	
	@Override
	public void setId(Object id) {
		super.setId(id);
		items = null;
	}
	
	@Override
	public String getExamPropertyName() {
		return "examSusceptibilityTest";
	}
	
	public void refreshSubstances() {
		substances.refresh();
	}
}
