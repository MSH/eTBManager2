package org.msh.tb.vi;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.exams.ExamDSTHome;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.vi.entities.ExamDSTVI;

@Name("examDSTHomeVI")
public class ExamDSTHomeVI{
	/*This class is not being used because of a problem in jsfunction, using this class was a better solution, I had commited it to improve it later*/
	private static final long serialVersionUID = 423921370976581854L;
	
	private DstResult[] dstResultsForRifampcin;
	
	private static final String specialMethodsCustomId = "spcMtd-R";
	
	@In(create=true) ExamDSTHome examDSTHome;
	
	public DstResult[] getDstResultsForRifampcin(){
		if(dstResultsForRifampcin == null)
			dstResultsForRifampcin = (DstResult[]) Component.getInstance("dstResults");
		return dstResultsForRifampcin;
	}
	
	/*A:jsfunction doesn't work properly, after almost 8 hours trying to use a better solution I had displayed all combinations
	 * on the page and hide/show with javascript according to the options. I tried using a conversation scope too.*/
	 public void updateDstResultsForRifampcin(){
		ExamDSTVI exam = (ExamDSTVI) examDSTHome.getInstance();
		boolean isGeneXpert = false;
		
		if(exam.getMethod()!=null)
			isGeneXpert = exam.getMethod().getCustomId().equals(specialMethodsCustomId);
		
		if(exam.getMethod() == null || exam.getMtbDetected() == null || !isGeneXpert ){
			dstResultsForRifampcin = (DstResult[]) Component.getInstance("dstResults");
		}else if(exam.getMethod().getName().getName1().equalsIgnoreCase("genexpert") && exam.getMtbDetected().equals(MtbDetected.YES)){
			/*dstResultsForRifampcin  = rifDstResultsForYes; get from globallists_vi*/
		}else{
			/*dstResultsForRifampcin  = rifDstResultsForOthers; get from globallists_vi*/
		}
		
	}
	
	
	

}
