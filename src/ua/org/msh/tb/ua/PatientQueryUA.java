package org.msh.tb.ua;

import java.text.Collator;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.EntityQuery;
/**
 * Patient query, special ukrainian edition. Main query changed and pagination works now
 * @author alexey
 *
 */
@Name("patientsUA")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PatientQueryUA extends EntityQuery {
	private static final long serialVersionUID = -2441729659688297288L;

	protected Patient patient;

	private boolean searching;
	private List<Item> patientList;
	private javax.persistence.Query query;

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
		patient = (Patient)Component.getInstance("patient");
		if (patient != null) {
			if (patient.getBirthDate() != null)
				cond = "(p.birthDate = #{patient.birthDate})";
			if (getNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.name like #{patientsUA.nameLike})";
			if (getMiddleNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.middleName like #{patientsUA.middleNameLike})";
			if (getLastNameLike() != null)
				cond = (cond == null? "": cond + " and ") + "(p.lastName like #{patientsUA.lastNameLike})";
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
		setCurrentPage(1);
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
	private void createPatientList() {
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
				if (name1.equals(name2))
					name2 = name1+"_"+o2.patient.getId();
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
			
			Map<String, String> msgs = Messages.instance();

			String s;
			
			if (tbcase.getDiagnosisType() == DiagnosisType.SUSPECT)
				 s = msgs.get(tbcase.getClassification().getKeySuspect());
			else s = msgs.get(tbcase.getClassification().getKey());
	
			s = "<b>" + s + "</b><br/>";

			SimpleDateFormat f = new SimpleDateFormat("MMM-yyyy");
			
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
			
			return s;
		}
	}
	@Override
	public void refresh(){
		patientList = null;
		super.refresh();
	}
	/**
	 * Every time we are need to recreate query, parameters may be changed
	 * @author alexey
	 */
	@Override
	protected Query createQuery() {
      parseEjbql();
      evaluateAllParameters();
      joinTransaction();
      String hql = getEjbql();
      query = getEntityManager().createQuery( hql );
      if(getFirstResult() != null) query.setFirstResult(getFirstResult());
      if (getMaxResults() != null) query.setMaxResults(getMaxResults()+1);
      return query;
	}
	@Override
	public void setCurrentPage(Integer page) {
		Integer maxresults = getMaxResults();
		if (maxresults != null)
			setFirstResult(maxresults * (page - 1));
	}
}
