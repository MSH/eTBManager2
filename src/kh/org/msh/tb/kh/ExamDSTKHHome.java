package org.msh.tb.kh;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.kh.entities.ExamDST_Kh;
import org.msh.tb.transactionlog.LogInfo;

@Name("examDSTKHHome")
@LogInfo(roleName="EXAM_DST")
public class ExamDSTKHHome extends LaboratoryExamHome<ExamDST_Kh>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4045090063790438556L;
	
	@Factory("examDST_KH")
	public ExamDST_Kh getExamDST_Kh() {
		return getInstance();
	}
	
	private List<ExamDSTResult> items;

	@In(create=true) SubstancesQuery substances;
	
	public List<ExamDSTResult> getItems() {
		if (items == null)
			createItems();
		
		return items;
	}
	
	protected ExamDSTResult findResult(Substance s) {
		for (ExamDSTResult mr: getInstance().getResults()) {
			if (mr.getSubstance().equals(s)) {
				return mr;
			}
		}
		return null;
	}
	
	protected void createItems() {
		items = new ArrayList<ExamDSTResult>();
//		boolean bEdt = isManaged();
		
		for (Substance s: substances.getDstSubstances()) {
			ExamDSTResult res = findResult(s);
			if (res == null) {
				res = new ExamDSTResult();
				res.setSubstance(s);
			}

			if (res.getResult() == null)
				res.setResult(DstResult.NOTDONE);
			
			items.add(res);
			res.setExam(getInstance());
		}
	}
	
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		// update exams
		if (items != null) {
			ExamDST_Kh exam = getInstance();
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
	
	public void refreshSubstances() {
		substances.refresh();
	}

}
