package org.msh.tb.vi;


import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.entities.Patient;


/**
 * Handle TB and DR-TB cases editing and new notification
 * @author Ricardo Memoria
 *
 */
@Name("caseEditingHomeVI")
@Scope(ScopeType.CONVERSATION)
public class CaseEditingHomeVI extends CaseEditingHome{

	public String initializeNewNotification() {
		Patient p = patientHome.getInstance();
		p.setName(p.getName()!=null?p.getName().toUpperCase():null);
		p.setLastName(p.getLastName()!=null?p.getLastName().toUpperCase():null);
		p.setMiddleName(p.getMiddleName()!=null?p.getMiddleName().toUpperCase():null);
		return super.initializeNewNotification();
		
	}
}