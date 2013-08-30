package org.msh.tb.cases.exams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.resistpattern.ResistancePatternService;
import org.msh.tb.transactionlog.LogInfo;

/**
 * Handle basic operations of a DST exam test - New, edit, remove and open the 
 * data of a DST exam result
 * 
 * @author Ricardo Memoria
 *
 */
@Name("examDSTHome")
@LogInfo(roleName="EXAM_DST", entityClass=ExamDST.class)
public class ExamDSTHome extends LaboratoryExamHome<ExamDST> {
	private static final long serialVersionUID = 270035993717644991L;

	private List<ExamDSTResult> items;

	@In(create=true) SubstancesQuery substances;
	
	@Factory("examDST")
	public ExamDST getExamDST() {
		return getInstance();
	}
	

	/**
	 * Return the list of items of DST results to be edited
	 * @return List of {@link ExamDSTResult} to post results
	 */
	public List<ExamDSTResult> getItems() {
		if (items == null)
			createItems();
		
		return items;
	}
	
	/**
	 * Find an instance of the {@link ExamDSTResult} of the current case 
	 * by its {@link Substance} instance
	 * @param s
	 * @return
	 */
	protected ExamDSTResult findResult(Substance s) {
		for (ExamDSTResult mr: getInstance().getResults()) {
			if (mr.getSubstance().equals(s)) {
				return mr;
			}
		}
		return null;
	}


	/**
	 * Create the items of the DST result to be edited
	 */
	public void createItems() {
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


	/**
	 * Validate and prepare the fields to be saved
	 * @return true if validation was successfully done
	 */
	public boolean validateAndPrepareFields(){
		// if there is no item to evaluate, validation fails
		if (items == null)
			return false;

		// count the results
		int numResistants = 0;
		int numContaminated = 0;
		int numSusceptibles = 0;
		int numNotDone = 0;
		
		// count the number of items by results
		for (ExamDSTResult item: items) {
			switch (item.getResult()) {
			case RESISTANT:
				numResistants++;
				break;
			case CONTAMINATED:
				numContaminated++;
				break;
			case NOTDONE:
				numNotDone++;
				break;
			case SUSCEPTIBLE:
				numSusceptibles++;
				break;
			default:
			}
		}
		// check if case is TB and any resistance was notified
/*		if (getTbCase().getClassification().equals(CaseClassification.TB) && resistantQuantity > 0) {
			facesMessages.addFromResourceBundle("DSTExam.msg01");
			return false;
		}
*/

		// check if case is DR-TB and there is no resistance in the result 
/*		if (getTbCase().getClassification().equals(CaseClassification.DRTB) && numResistants <= 0) {
			facesMessages.addFromResourceBundle("DSTExam.msg02");
			return false;
		}
*/
		if (items.size() == numNotDone) {
			facesMessages.addFromResourceBundle("DSTExam.msg03");
			return false;
		}

		// update results
		ExamDST exam = getInstance();
		exam.setNumContaminated(numContaminated);
		exam.setNumResistant(numResistants);
		exam.setNumSusceptible(numSusceptibles);

		// remove NOTDONE and include new results
		for (ExamDSTResult ms: items) {
			// add new results
			if (ms.getResult() != DstResult.NOTDONE) {
				if (!exam.getResults().contains(ms))
					exam.getResults().add(ms);
			}
			else {
				// remove undone results
				if (exam.getResults().contains(ms)) {
					exam.getResults().remove(ms);
					getEntityManager().remove(ms);
				}
			}
		}

		// there is no result, so validation fails
		if (exam.getResults().size() == 0)
			return false;

		return true;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.LaboratoryExamHome#persist()
	 */
	@Override
	@End(beforeRedirect=true)
	public String persist() {
		
		if(!validateAndPrepareFields())
			return "error";
		
		return persistWithoutValidation();
	}
	
	/**
	 * Persist the DST result without validation
	 * @return
	 */
	@End(beforeRedirect=true)
	public String persistWithoutValidation() {
		String result = super.persist();
		
		//Verify the resistance type according to the DST and set it in TBcase.
		if(setResistanceType())
			caseHome.persist();

		// update the resistance patterns
		ResistancePatternService srv = (ResistancePatternService)Component.getInstance("resistancePatternService");
		srv.updateCase(getInstance().getTbcase());
		
		return result;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.LaboratoryExamHome#remove()
	 */
	@Override
	public String remove() {
		String result = super.remove();
		
		//Verify the resistance type according to the DST and set it in TBcase.
		if(setResistanceType())
			caseHome.persist();

		// update the resistance patterns
		ResistancePatternService srv = (ResistancePatternService)Component.getInstance("resistancePatternService");
		srv.updateCase(getInstance().getTbcase());
		
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
