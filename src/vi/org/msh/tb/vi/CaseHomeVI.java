package org.msh.tb.vi;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.TreatmentHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.TbCase;
import org.msh.tb.kh.entities.TbCaseKH;
import org.msh.tb.ng.entities.TbCaseNG;
import org.msh.tb.vi.entities.TbCaseVI;



/**
 * Handle basic operations with a TB/MDR case. Check {@link CaseEditingHome} for notification and editing of case data
 * Specific operations concerning exams, case regimes, heath units and medical consultations are handled by other classes.
 * @author Ricardo Memória 
 * Cambodia - Vani Rao
 */
@Name("caseHomeVI")
public class CaseHomeVI {
	
	@In CaseHome caseHome;


	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3357892182242568552L;

	/**
	 * Return an instance of a {@link TbCaseVI} class
	 * 
	 * @return
	 */
	@Factory("tbcasevi")
	public TbCaseVI getTbCaseVI() {
		return (TbCaseVI) caseHome.getTbCase();
	}
	
	
}

