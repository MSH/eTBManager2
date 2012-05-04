package org.msh.tb.br;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.br.entities.ExamMicroscopyBR;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.exams.ExamMicroscopyHome;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.TbCase;

/**
 * Handle microscopy exams adapted to the Brazilian needs
 * @author Ricardo Memoria
 *
 */
@Name("examMicroscopyHomeBR")
public class ExamMicroscopyHomeBR  {
	
	@In(create=true) ExamMicroscopyHome examMicroscopyHome;
	
	private AdminUnitSelection auselection;
	
	/**
	 * Return component for selection of the administrative unit of the laboratory. <p/>
	 * It's initially selected as the administrative unit of the treatment unit of the patient. If the treatment unit is not specified, 
	 * the system uses the administrative unit in the patient address
	 * @return
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null) {
			auselection = new AdminUnitSelection();
			
			// initialize administrative unit
			if (examMicroscopyHome.isManaged()) {
				auselection.setSelectedUnit(((ExamMicroscopyBR)examMicroscopyHome.getInstance()).getLaboratoryAdminUnit());
			}
			else {
				CaseHome caseHome = (CaseHome)Component.getInstance("caseHome");
				if (caseHome != null) {
					TbCase tbcase = caseHome.getInstance();
					AdministrativeUnit adminUnit = null;
					if (tbcase.getTreatmentUnit() != null)
						adminUnit = tbcase.getTreatmentUnit().getAdminUnit();
					else adminUnit = tbcase.getNotifAddress().getAdminUnit();
					
					if (adminUnit != null)
						auselection.setSelectedUnit(adminUnit.getParentLevel1());
				}
			}
		}
		return auselection;
	}

	
	/**
	 * Save the exam microscopy specific for Brazil
	 * @return
	 */
	public String persist() {
		ExamMicroscopyBR exam = (ExamMicroscopyBR)examMicroscopyHome.getInstance();

		exam.setLaboratoryAdminUnit(auselection.getSelectedUnit());

		return examMicroscopyHome.persist();
	}

}
