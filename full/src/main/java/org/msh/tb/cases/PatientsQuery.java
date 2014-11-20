package org.msh.tb.cases;


import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.EntityQuery;

import java.text.Collator;
import java.util.*;


@Name("patients")
public class PatientsQuery extends EntityQuery {
	private static final long serialVersionUID = -2441729659688297288L;

	@In(required=false) protected Patient patient;

	private boolean searching;
	private List<Item> patientList;


	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Query#getEjbql()
	 */
	@Override
	public String getEjbql() {
		return "from Patient p left outer join p.cases c join fetch c.notificationUnit " +
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
		if(conditions().equals(""))
			patientList = null;
		else if (patientList == null)
				createPatientList();

		return patientList;
	}
	
	@Override
	public Long getResultCount() {
		if(conditions().equals(""))
			return new Long(0);
		else
			return super.getResultCount();
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
		return (patient == null) || (patient.getName() == null) || (patient.getName().isEmpty()) ? null: "%" + patient.getName().toUpperCase() + "%"; 
	}

	public String getMiddleNameLike() {
		return (patient == null) || (patient.getMiddleName() == null) || (patient.getMiddleName().isEmpty()) ? null: "%" + patient.getMiddleName().toUpperCase() + "%"; 
	}

	public String getLastNameLike() {
		return (patient == null) || (patient.getLastName() == null) || (patient.getLastName().isEmpty()) ? null: "%" + patient.getLastName().toUpperCase() + "%"; 
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
	protected void createPatientList() {
		patientList = new ArrayList<Item>();

		for (Object obj: getResultList()) {
			Object[] vals = (Object[])obj;
			Item item = new Item((Patient)vals[0], (TbCase)vals[1]);
			patientList.add(item);
		}
		/**
		 * Sort {@link patientList} considering locale language
		 * @author am
		 */
		Collections.sort(patientList, new Comparator<Item>() {
			  public int compare(Item o1, Item o2) {
				String name1, name2;
				name1 = o1.patient.getLastName() == null ? o1.patient.getName() : o1.patient.getLastName();
				name2 = o2.patient.getLastName() == null ? o2.patient.getName() : o2.patient.getLastName();
				
				if (name1.equals(name2)){
					name1 = o1.patient.getName();
					name2 = o2.patient.getName();
				}
				if (name1.equals(name2)){
					name1 = o1.patient.getMiddleName() == null ? o1.patient.getName() : o1.patient.getMiddleName();
					name2 = o2.patient.getMiddleName() == null ? o2.patient.getName() : o2.patient.getMiddleName();
				}
				if (name1.equals(name2)){
					name2 = name1+"_"+o2.patient.getId();
				}
				Collator myCollator = Collator.getInstance();			    
				return myCollator.compare(name1,name2);
			  }
		});
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
			
			CaseHome home = (CaseHome)Component.getInstance("caseHome");
			
			Map<String, String> msgs = Messages.instance();

			String s;
			
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)
				 s = msgs.get(tbcase.getClassification().getKeySuspect());
			else s = msgs.get(tbcase.getClassification().getKey());
	
			s = "<b>" + s + "</b><br/>";

			s += "<div class='warn'>" + home.getStatusString(tbcase) + "</div>";
			String s2 = home.getStatusString2(tbcase);
			if (s2 != null)
				s += "<div>" + s2 + "</div>";
/*			SimpleDateFormat f = new SimpleDateFormat("MMM-yyyy");
			
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT) {
				if (tbcase.getRegistrationDate() != null)
					s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.SUSP.date"), f.format(tbcase.getRegistrationDate())) + "</div>";
			}
			else 
			if (tbcase.getState() == CaseState.WAITING_TREATMENT) {
				if (tbcase.getDiagnosisDate() != null)
					s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.CONF.date"), f.format( tbcase.getDiagnosisDate() )) + "</div>";
			}
			else 
			if ((tbcase.getState() == CaseState.ONTREATMENT) || (tbcase.getState() == CaseState.TRANSFERRING)) {
				if (tbcase.getTreatmentPeriod().getIniDate() != null)
					s += "<div class='warn'>" + MessageFormat.format(msgs.get("cases.sit.ONTREAT.date"), f.format( tbcase.getTreatmentPeriod().getIniDate() )) + "</div>";
			}
			else 
			if (tbcase.getState().ordinal() > CaseState.TRANSFERRING.ordinal()) {
				if (tbcase.getOutcomeDate() != null)
					s += MessageFormat.format(msgs.get("cases.sit.OUTCOME.date"), f.format( tbcase.getOutcomeDate() )) + "<br/>" +
						msgs.get(tbcase.getState().getKey());
	
			}
*/			
			return s;
		}
	}


	/**
	 * @param patientList the patientList to set
	 */
	public void setPatientList(List<Item> patientList) {
		this.patientList = patientList;
	}
}