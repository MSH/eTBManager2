package org.msh.tb.vi;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.vi.entities.ExamDSTVI;

import java.util.ArrayList;
import java.util.List;

@Name("examDSTHomeVI")
public class ExamDSTHomeVI{
	private static final long serialVersionUID = 423921370976581854L;
	
	private DstResult[] dstResultsForRifampcin;
	
	private static final String geneXpertName = "GeneXpert";
	
	@In(create=true) ExamDSTHome examDSTHome;
	@In(create=true) GlobalLists globalLists_vi;
	
	private boolean geneXpert = false;
	
	public DstResult[] getDstResultsForRifampcin(){
		updateDstResultsForRifampcin();
		return dstResultsForRifampcin;
	}
	
	public void updateItems(){
		if(isGeneXpert()){
			ArrayList<ExamDSTResult> results = new ArrayList<ExamDSTResult> ();
			for(int i = 0; i < examDSTHome.getItems().size(); i++){
				if(examDSTHome.getItems().get(i).getSubstance().getAbbrevName().getName1().equalsIgnoreCase("R"))
					results.add(examDSTHome.getItems().get(i));
			}
			examDSTHome.getItems().clear();
			examDSTHome.getItems().addAll(results);
		}else{
			examDSTHome.createItems();
		}
	}
	
	public void updateDstResultsForRifampcin(){
		ExamDSTVI exam = (ExamDSTVI) examDSTHome.getInstance();
		 
		//Set rifampcin values 
		if(exam.getMethod() == null || exam.getMtbDetected() == null || !isGeneXpert() ){
			dstResultsForRifampcin = (DstResult[]) Component.getInstance("dstResults");
		}else if(isGeneXpert() && exam.getMtbDetected().equals(MtbDetected.YES)){
			dstResultsForRifampcin  = globalLists_vi.getDstResultsForMtbYes();
		}else if(isGeneXpert() && exam.getMtbDetected() == null){
			dstResultsForRifampcin  = globalLists_vi.getDstResultsForMtbYes();
		}else{
			dstResultsForRifampcin  = globalLists_vi.getDstResultsForMtbNo();
		}
		
	}
	 
	public boolean isGeneXpert(){
		ExamDSTVI exam = (ExamDSTVI) examDSTHome.getInstance();
				
		if(exam.getMethod()!=null)
			return geneXpert = exam.getMethod().getName().getName1().equals(geneXpertName);
		
		return false;
	}
	
	public List<ExamDSTResult> getItems(){
		updateItems();
		return examDSTHome.getItems();
	}
}
