package org.msh.tb.vi;


import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.PatientHome;
import org.msh.tb.cases.PrevTBTreatmentHome;
import org.msh.tb.cases.exams.ExamCultureHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.cases.exams.MedicalExaminationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.entities.Address;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.MedAppointmentType;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.entities.enums.YesNoType;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.DateUtils;


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
