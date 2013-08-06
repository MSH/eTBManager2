package org.msh.tb.az.cases.exams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.az.entities.ExamDSTAZ;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.LaboratoryExamHome;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.transactionlog.LogInfo;

@Name("examDSTAZHome")
@LogInfo(roleName="EXAM_DST")
public class ExamDSTAZHome extends LaboratoryExamHome<ExamDSTAZ>{
	private static final long serialVersionUID = 4678646241116862745L;
	
	@In(create=true) FacesMessages facesMessages;
	@In(required=true) CaseHome caseHome;
	@In(create=true) SubstancesQuery substances;
	private List<ExamDSTResult> items;

	@Factory("examDSTAZ")
	public ExamDSTAZ getInstance() {
		return super.getInstance();
	}
	

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
	
	public boolean validateAndPrepareFields(){
		ExamDSTAZ exam = getInstance();
		if (exam.getDateRelease()!=null)
			if (exam.getDateRelease().before(exam.getDateCollected())){
				facesMessages.addFromResourceBundle("DSTExam.msg04");
				return false;
			}
		if (exam.getDatePlating()!=null){
			if (exam.getDatePlating().before(exam.getDateCollected())){
				facesMessages.addFromResourceBundle("DSTExam.msg05");
				return false;
			}
			if (exam.getDateRelease()!=null)
				if (exam.getDateRelease().before(exam.getDatePlating())){
					facesMessages.addFromResourceBundle("DSTExam.msg06");
					return false;
				}
		}
		
		//Verifies if a TB case has resistance or if a DRTB case has at least one resistance
		//int resistantQuantity = 0;
		int notDoneExams = 0;
		for (ExamDSTResult ms: items) {
			/*if(getTbCase().getClassification().equals(CaseClassification.TB)
					&& ms.getResult().equals(DstResult.RESISTANT)){
				facesMessages.addFromResourceBundle("DSTExam.msg01");
				return false;
			}else if(ms.getResult().equals(DstResult.RESISTANT)){
				resistantQuantity++;
			}else */if(ms.getResult().equals(DstResult.NOTDONE)){
				notDoneExams++;
			}
		}
		if(items.size() == notDoneExams){
			facesMessages.addFromResourceBundle("DSTExam.msg03");
			return false;
		}

		/*if(getTbCase().getClassification().equals(CaseClassification.DRTB)
				&& resistantQuantity <= 0){
			facesMessages.addFromResourceBundle("DSTExam.msg02");
			return false;
		}*/
		
		// update exams
		if (items != null) {
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
				return false;
			else
				return true;

		}
		return false;	
	}
	
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		
		if(!validateAndPrepareFields())
			return "error";
		
		String result = super.persist();
		
		//Verify the resistance type according to the DST and set it in TBcase.
		if(setResistanceType())
			caseHome.persist();
		
		return result;
	}
	
	@Override
	public String remove() {
		String result = super.remove();
		
		//Verify the resistance type according to the DST and set it in TBcase.
		if(setResistanceType())
			caseHome.persist();
		
		return result;
	}

	private boolean setResistanceType(){
		ArrayList<String> abrevNameResistSubstance;
		
		//It will consider all the exams before the date that the treatment has been initiated
		Query query = getEntityManager().createQuery("select distinct a.substance.abbrevName.name1 " +
														"from ExamDSTResult a " +
														"where a.exam.tbcase.id = :tbcaseId " +
														"and a.result = :resistant " +
														"and a.exam.dateCollected <= :dateIniTreat");
		
		query.setParameter("tbcaseId", caseHome.getTbCase().getId());
		Date iniDate;
		if (caseHome.getTbCase().getTreatmentPeriod()!=null)
			iniDate = caseHome.getTbCase().getTreatmentPeriod().getIniDate();
		else
			iniDate = new Date();
		query.setParameter("dateIniTreat", iniDate);
		query.setParameter("resistant", DstResult.RESISTANT);
		
		abrevNameResistSubstance = (ArrayList<String>) query.getResultList();
		
		//If there is no DST exams before the date that the treatment has been initiated
		//It will consider the first DST exam after the date that the treatment has been initiated
		if(abrevNameResistSubstance == null || abrevNameResistSubstance.size() <= 0){
		
			query = getEntityManager().createQuery("select distinct a.substance.abbrevName.name1 " +
													"from ExamDSTResult a " +
													"where a.exam.tbcase.id = :tbcaseId " +
													"and a.result = :resistant " +
													"and a.exam.dateCollected = (select min(b.dateCollected) " +
																					"from ExamDST b " +
																					"where b.tbcase.id = a.exam.tbcase.id)");
			
			query.setParameter("tbcaseId", caseHome.getTbCase().getId());
			query.setParameter("resistant", DstResult.RESISTANT);
			
			abrevNameResistSubstance = (ArrayList<String>) query.getResultList();
		
		}
		
		//Now all the resistance substances are in the list above
		
		if(abrevNameResistSubstance != null && abrevNameResistSubstance.size() > 0){
			//Mono-Resistance
			//The patient has resistance to only one substance
			if(abrevNameResistSubstance.size() == 1){
				caseHome.getTbCase().setDrugResistanceType(DrugResistanceType.MONO_RESISTANCE);
				return true;
			}
			
			//Poli-Resistance
			//The patient has resistance to tow or more substances and only one of the is R or H.
			if(abrevNameResistSubstance.size() >= 2 && (
					(checkIfSubstanceExists(abrevNameResistSubstance, "R") && !checkIfSubstanceExists(abrevNameResistSubstance, "H")) || 
					(checkIfSubstanceExists(abrevNameResistSubstance, "H") && !checkIfSubstanceExists(abrevNameResistSubstance, "R")) )) {
				caseHome.getTbCase().setDrugResistanceType(DrugResistanceType.POLY_RESISTANCE);
				return true;
			}
			
			//Extensive-resistance
			//The patient has resistance to more than tow substances and tow of them are R and H.
			if(abrevNameResistSubstance.size() > 2 && checkIfSubstanceExists(abrevNameResistSubstance, "R") && checkIfSubstanceExists(abrevNameResistSubstance, "H")
					&& (checkIfSubstanceExists(abrevNameResistSubstance, "Ofx") || checkIfSubstanceExists(abrevNameResistSubstance, "Lfx") || checkIfSubstanceExists(abrevNameResistSubstance, "Mfx"))
					&& (checkIfSubstanceExists(abrevNameResistSubstance, "Am") || checkIfSubstanceExists(abrevNameResistSubstance, "Km") || checkIfSubstanceExists(abrevNameResistSubstance, "Cp")) ){
				caseHome.getTbCase().setDrugResistanceType(DrugResistanceType.EXTENSIVEDRUG_RESISTANCE);
				return true;
			}
			
			//Multi-resistance
			//The patient has resistance to tow or more substances and tow of them are R and H.
			if(abrevNameResistSubstance.size() >= 2 && 
					(checkIfSubstanceExists(abrevNameResistSubstance, "R") && checkIfSubstanceExists(abrevNameResistSubstance, "H")) ){
				caseHome.getTbCase().setDrugResistanceType(DrugResistanceType.MULTIDRUG_RESISTANCE);
				return true;
			}
					
			
			caseHome.getTbCase().setDrugResistanceType(null);
			return true;
		}else{
			caseHome.getTbCase().setDrugResistanceType(null);
			return true;
		}
	}
	
	private boolean checkIfSubstanceExists(List<String> abbrevNameSubstanceList, String substance){
		for(String s : abbrevNameSubstanceList){
			if(s.equalsIgnoreCase(substance))
				return true;
		}
		return false;
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
