package org.msh.tb.ph;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.FieldValue;
import org.msh.mdrtb.entities.TbCase;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseEditingHome;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.CaseValidationHome;
import org.msh.tb.cases.treatment.StartTreatmentHome;
import org.msh.tb.misc.FieldsQuery;
import org.msh.tb.ph.entities.CaseDataPH;
import org.msh.tb.ph.entities.CaseSymptom;
import org.msh.tb.ph.entities.PhysicalExam;
import org.msh.tb.ph.entities.enums.PhysicalExamResult;


/**
 * Specific code to handle case management functionalities for Philippines workspace
 * @author Ricardo Memoria
 *
 */
@Name("caseDataPHHome")
public class CaseDataPHHome extends EntityHomeEx<CaseDataPH> {
	private static final long serialVersionUID = -2304184269665624486L;

	@In EntityManager entityManager;
	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseEditingHome caseEditingHome;
	@In(create=true) FieldsQuery fieldsQuery;
	@In(create=true) StartTreatmentHome startTreatmentHome;
//	@In(create=true) TreatmentHealthUnitHome treatmentHealthUnitHome;
	@In(create=true) CaseSymptomsHome caseSymptomsHome;
	@In(create=true) CaseValidationHome caseValidationHome;
	

	private List<PhysicalExam> physicalExams;


	/**
	 * Return object with specific fields for Philippines workspace
	 * @return instance of {@link CaseDataPH} class
	 */
	@Factory("caseDataPH")
	public CaseDataPH getCaseData() {
		try {
			entityManager.createQuery("from CaseDataPH c where c.tbcase.id = #{caseHome.id}").getSingleResult();
			setId(caseHome.getId());
		} catch (Exception e) {
			setId(null);
		}
		return getInstance();
	}


	/**
	 * Save a new case for the Philippines workspace. Don't use the class {@link CaseEditingHome}, because this class
	 * already saves it using the {@link CaseEditingHome} component
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveNew() {
		String ret = caseEditingHome.saveNew();

		if (!ret.equals("persisted"))
			return ret;

		TbCase tbcase = caseEditingHome.getTbcase();
		CaseDataPH data = getInstance();
		
		data.setTbcase(tbcase);
		data.setId(tbcase.getId());
		
		caseSymptomsHome.save();
		//mountPhysicalExams();

		return persist();
	}

	
	/**
	 * Start the treatment as a standard regimen
	 * @return "standard-regimen" if successfully saved
	 */
	public String startTreatmentStandardRegimen() {
		String ret = startTreatmentHome.startStandardRegimen();
//		String ret = treatmentHealthUnitHome.saveStandardRegimen();
		if (!"standard-regimen".equals(ret))
			return "error";
		
		persist();
		return "standard-regimen";
	}


	/**
	 * Save changes made to an already existing case in the Philippines workspace
	 * @return "persisted" if successfully saved
	 */
	@Transactional
	public String saveEditing() {
		if (!caseEditingHome.saveEditing().equals("persisted"))
			return "error";
		
		CaseDataPH data = getInstance();
		TbCase tbcase = caseEditingHome.getTbcase();
		
		if (data.getId() == null)
			data.setId(tbcase.getId());
		caseSymptomsHome.save();
//		mountPhysicalExams();

		return persist(); 
	}


	@Transactional
	public String saveTBCase() {
		return "error";
	}
	
	protected void mountPhysicalExams() {
		CaseDataPH data = getInstance();
		
		// create list of physical exams
		if (physicalExams != null) {
			data.getPhysicalExams().clear();
			for (PhysicalExam pe: physicalExams) {
				if (pe.getResult() == PhysicalExamResult.NOT_DONE) {
					entityManager.remove(pe);
					data.getPhysicalExams().remove(pe);
				}
				else {
					if (!data.getPhysicalExams().contains(pe))
						data.getPhysicalExams().add(pe);
				}
			}
		}		
	}
	
	
	public List<CaseSymptom> getCaseSymptoms() {
		if (!caseSymptomsHome.isEditing())
			caseSymptomsHome.setEditing(true);
		
		return caseSymptomsHome.getCaseSymptoms();
	}
	
	
	/**
	 * Returns list of physical exams for editing
	 * @return List of PhysicalExams
	 */
	public List<PhysicalExam> getPhysicalExams() {
		if (physicalExams == null)
			createPhysicalExamList();
		
		return physicalExams;
	}
	
	/**
	 * Create list of physical exams including those not done
	 */
	protected void createPhysicalExamList() {
		physicalExams = new ArrayList<PhysicalExam>();
		
		CaseDataPH data = getInstance();
		List<FieldValue> lst = fieldsQuery.getPhysicalExams();
		
		for (FieldValue val: lst) {
			PhysicalExam pe = data.findExamResult(val);
			if (pe == null) {
				pe = new PhysicalExam();
				pe.setExam(val);
				pe.setCaseDataPH(data);
				pe.setResult(PhysicalExamResult.NOT_DONE);
			}
			physicalExams.add(pe);
		}
	}


	/**
	 * Validate a case and save the data specific for Philippines
	 * @return "validated" if successfully validated
	 */
	protected String validate() {
		String ret = caseValidationHome.validate();
		if (!("validated".equals(ret)))
			return ret;
		
		persist();
		return ret;
	}
}
