package org.msh.tb.cases;


import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.EntityQuery;


@Name("patients")
public class PatientsQuery extends EntityQuery {
	private static final long serialVersionUID = -2441729659688297288L;

	@In(required=false) Patient patient;

	private boolean searching;
	private List<Item> patientList;


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from Patient p left outer join p.cases c " +
			"where p.workspace.id = #{defaultWorkspace.id} " + conditions() +
			" and c.registrationDate in (select max(aux.registrationDate) from TbCase aux where aux.patient.id = p.id)";
	}

	/**
	 * Generates dynamic conditions for patient search
	 * @return
	 */
	public String conditions() {
		String cond = null;
		if (patient != null) {
			if (patient.getBirthDate() != null)
				cond = "(p.birthDate = #{patient.birthDate})";
			if (getNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.name like #{patients.nameLike})";
			if (getMiddleNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.middleName like #{patients.middleNameLike})";
			if (getLastNameLike() != null)
				cond = (cond == null? "": cond + " or ") + "(p.lastName like #{patients.lastNameLike})";
		}
		return (cond == null? "": " and (" + cond + ")");
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getCountEjbql()
	 */
	@Override
	protected String getCountEjbql() {
		return "select count(*) from Patient p where p.workspace.id = #{defaultWorkspace.id} " + conditions();
	}

	@Override
	public Integer getMaxResults() {
		return 50;
	}

	public List<Item> getPatientList() {
		if (patientList == null)
			createPatientList();
		return patientList;
	}
	
	@Override
	public String getOrder() {
		return "p.name";
	}

	public void search() {
		searching = true;
		refresh();
	}

	public String getNameLike() {
		return (patient == null) || (patient.getName() == null) || (patient.getName().isEmpty()) ? null: patient.getName().toUpperCase() + "%"; 
	}

	public String getMiddleNameLike() {
		return (patient == null) || (patient.getMiddleName() == null) || (patient.getMiddleName().isEmpty()) ? null: patient.getMiddleName().toUpperCase() + "%"; 
	}

	public String getLastNameLike() {
		return (patient == null) || (patient.getLastName() == null) || (patient.getLastName().isEmpty()) ? null: patient.getLastName().toUpperCase() + "%"; 
	}

	public boolean isSearching() {
		return searching;
	}

	public void setSearching(boolean searching) {
		this.searching = searching;
	}

	/**
	 * Create list of patient wrapper in an {@link Item} object
	 */
	private void createPatientList() {
		patientList = new ArrayList<Item>();

		for (Object obj: getResultList()) {
			Object[] vals = (Object[])obj;
			Item item = new Item((Patient)vals[0], (TbCase)vals[1]);
			patientList.add(item);
		}
	}


	public class Item {
		private Patient patient;
		private TbCase tbcase;

		public Item(Patient patient, TbCase tbcase) {
			super();
			this.patient = patient;
			this.tbcase = tbcase;
		}
		/**
		 * @return the patient
		 */
		public Patient getPatient() {
			return patient;
		}
		/**
		 * @param patient the patient to set
		 */
		public void setPatient(Patient patient) {
			this.patient = patient;
		}
		/**
		 * @return the tbcase
		 */
		public TbCase getTbcase() {
			return tbcase;
		}
		/**
		 * @param tbcase the tbcase to set
		 */
		public void setTbcase(TbCase tbcase) {
			this.tbcase = tbcase;
		}
		
		public String getCaseStatus() {
			if (tbcase == null)
				return null;
			
			Map<String, String> msgs = Messages.instance();

			String s;
			
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)
				 s = msgs.get(tbcase.getClassification().getKeySuspect());
			else s = msgs.get(tbcase.getClassification().getKey());
	
			s = "<b>" + s + "</b><br/>";

			SimpleDateFormat f = new SimpleDateFormat("MMM-yyyy");
			
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT) {
				s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.SUSP.date"), f.format(tbcase.getRegistrationDate())) + "</div>";
			}
			else 
			if (tbcase.getState() == CaseState.WAITING_TREATMENT) {
				s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.CONF.date"), f.format( tbcase.getDiagnosisDate() )) + "</div>";
			}
			else 
			if ((tbcase.getState() == CaseState.ONTREATMENT) || (tbcase.getState() == CaseState.TRANSFERRING)) {
				s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.ONTREAT.date"), f.format( tbcase.getTreatmentPeriod().getIniDate() )) + "</div>";
			}
			else 
			if (tbcase.getState().ordinal() > CaseState.TRANSFERRING.ordinal()) {
				s += MessageFormat.format(msgs.get("cases.sit.OUTCOME.date"), f.format( tbcase.getOutcomeDate() )) + "<br/>" +
					msgs.get(tbcase.getState().getKey());
	
			}
			
			return s;
		}
	}
}