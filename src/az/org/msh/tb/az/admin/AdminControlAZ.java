package org.msh.tb.az.admin;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.App;
import org.msh.tb.az.cases.CaseAZHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;

@Name("adminControlAZ")
public class AdminControlAZ {
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) CaseHome caseHome;
	
	/**
	 * Generate caseNumbers for all cases, which don't have it
	 */
	public void generateCasesNumbersForEmpty(){
		String hql = "select c from TbCase c where c.patient.recordNumber = null and c.notificationUnit.workspace.id = 8";
		List<TbCase> lst = App.getEntityManager().createQuery(hql).getResultList();
		CaseAZHome caseAZHome = App.getComponent(CaseAZHome.class);
		int col = 0;
		int err = 0;
		StringBuffer log = new StringBuffer();
		for (TbCase tbcase:lst){
			try{
				caseAZHome.generateCaseNumber(tbcase);
				log.append("Record number "+tbcase.getPatient().getRecordNumber() + " sets to case "+tbcase.getPatient().getFullName()+'\n');
				col++;
			}
			catch (Exception e) {
				log.append("ERROR. Cannot generate number for patient "+tbcase.getPatient().getFullName()+" because of "+e.getLocalizedMessage()+'\n');
				err++;
			}
		}
		String gen = "Generating "+col+" case numbers; errors - "+err+".";
		// save result to log file
		log.append(gen);
		System.out.println(log);
		/*beginTransaction();
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "Correct case numbers", null, null, null);
		commitTransaction();*/
		//caseHome.updateCaseTags();
		facesMessages.add(gen);
	}
	
	
}
