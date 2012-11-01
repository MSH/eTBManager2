package org.msh.tb.az;

import java.text.Collator;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.swing.JOptionPane;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.App;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.utils.EntityQuery;
import org.msh.utils.date.Period;

@Name("patientsAZ")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PatientsQueryAZ extends EntityQuery {
	private static final long serialVersionUID = -4368672383630128239L;

		private String hqlCondition;
		private List<Item> patientList;

		private boolean searching;
		
		@Override
		public String getEjbql() {
			return "from TbCaseAZ c left outer join c.patient p " +
				"where p.workspace.id = #{defaultWorkspace.id} " + conditions() +
				" and c.registrationDate in (select max(aux.registrationDate) from TbCase aux where aux.patient.id = p.id)";
		}
		
		/**
		 * Generate HQL conditions for filters that cannot be included in the restrictions clause
		 * @return
		 */
		public String conditions() {
			hqlCondition = "";
			mountEIDSSCondition();

			if (!hqlCondition.isEmpty())
				hqlCondition = " and ".concat(hqlCondition);

			return hqlCondition;
		}
		
		@Override
		protected String getCountEjbql() {
			return "select count(*) from TbCaseAZ c left outer join c.patient p " +
				"where p.workspace.id = #{defaultWorkspace.id} " + conditions() +
				" and c.registrationDate in (select max(aux.registrationDate) from TbCase aux where aux.patient.id = p.id)";
		}
		
		private void mountEIDSSCondition() {
			EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
			if (eidssFilters.getName()!=null){
				String[] names = eidssFilters.getName().split(" ");
				if (names.length != 0){
					String s="";
					for (String name: names) {
						if (s.length() > 1)
							s += " and ";
						name = name.replaceAll("'", "''");
						if (!"".equals(name))
							s += "(((upper(p.name) like '%" + name.toUpperCase() + 
							"%') or (upper(p.middleName) like '%" + name.toUpperCase() + 
							"%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%')) or "+
							"(upper(c.EIDSSComment) like '%"+name.toUpperCase()+"%'))";
					}
					addCondition(s);
				}
			}
			if (eidssFilters.getAddress()!=null && !"".equals(eidssFilters.getAddress())){
				//String s="(c.eidssData.address like '%"+eidssFilters.getAddress()+"%')";
				String s="(c.EIDSSComment like '%"+eidssFilters.getAddress()+"%')";
				addCondition(s);
			}
			if (eidssFilters.getId()!=null && !"".equals(eidssFilters.getId())){
				String s="(c.legacyId like '"+eidssFilters.getId()+"')";
				addCondition(s);
			}
			if (eidssFilters.getNotifUnit()!=null && !"".equals(eidssFilters.getNotifUnit())){
				//String s="(c.eidssData.notifUnit like '%"+eidssFilters.getNotifUnit()+"%')";
				String s="(c.EIDSSComment like '%"+eidssFilters.getNotifUnit()+"%')";
				addCondition(s);
			}
			if (eidssFilters.getTbunit().getAdminUnit()!=null){
				String s="";
				if (eidssFilters.getTbunit().getTbunit()!=null)
					s += "c.notificationUnit.id = "+eidssFilters.getTbunit().getTbunit().getId();
				else
					s += "c.notificationUnit.adminUnit.code like '"+eidssFilters.getTbunit().getAdminUnit().getCode()+"%'";
				addCondition(s);
			}
			if (eidssFilters.getYearBirth()!=null){
				//String s = "c.eidssData.yearBirth = "+eidssFilters.getYearBirth();
				String s = "((c.EIDSSComment like '%"+eidssFilters.getYearBirth()+"%') or " +
				"(year(p.birthDate) = "+eidssFilters.getYearBirth()+"))";
				addCondition(s);
			}
			//=====DATES=====
			Period d = eidssFilters.getInDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				//generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.eidssData.inDate","inDate");
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.inEIDSSDate","inDate");
			}
			d = eidssFilters.getRegDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.registrationDate","regDate");
			}
			d = eidssFilters.getSysDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.systemDate","sysDate");
			}
		}

		
		/**
		 * Generates HQL condition to a date field filter
		 * @param dtIni - Initial date of the filter
		 * @param dtEnd - Ending date of the filter
		 * @param dateFieldCase - Date field name in the HQL query
		 * @param dateFieldEIDSS - name field in eidssFilters
		 */
		private void generateHQLPeriodEIDSS(Date dtIni, Date dtEnd, String dateFieldCase, String dateFieldEIDSS) {
			if (dtIni != null)
				addCondition("(" + dateFieldCase + ">=#{eidssFilters."+dateFieldEIDSS+".iniDate})");
			if (dtEnd != null)
				addCondition("(" + dateFieldCase + "<=#{eidssFilters."+dateFieldEIDSS+".endDate})");		
		}
		
		/**
		 * Includes a condition to the hql instruction
		 * @param hql - the hql instruction
		 * @param condition - the condition to be included
		 */
		private void addCondition(String condition) {
			if (hqlCondition.isEmpty())
				hqlCondition = condition;
			else hqlCondition = hqlCondition + " and " + condition;
		}
		
		public List<Item> getPatientList() {
			if (patientList == null)
				createList();

			return patientList;
		}
			
		/**
		 * Create list of patient wrapper in an {@link Item} object
		 */
		private void createList() {
			patientList = new ArrayList<Item>();

			for (Object obj: getResultList()) {
				Object[] vals = (Object[])obj;
				Item item = new Item((Patient)vals[1], (TbCase)vals[0]);
				patientList.add(item);
			}
			/**
			 * Sort {@link patientList} considering locale language
			 * @author am
			 */
			Collections.sort(patientList, new Comparator<Item>() {
				  public int compare(Item o1, Item o2) {
					String name1, name2;
					name1 = o1.getPatient().getLastName() == null ? o1.getPatient().getName() : o1.getPatient().getLastName();
					name2 = o2.getPatient().getLastName() == null ? o2.getPatient().getName() : o2.getPatient().getLastName();
					
					if (name1.equals(name2)){
						name1 = o1.getPatient().getName();
						name2 = o2.getPatient().getName();
					}
					if (name1.equals(name2)){
						name1 = o1.getPatient().getMiddleName() == null ? o1.getPatient().getName() : o1.getPatient().getMiddleName();
						name2 = o2.getPatient().getMiddleName() == null ? o2.getPatient().getName() : o2.getPatient().getMiddleName();
					}
					if (name1.equals(name2)){
						name2 = name1+"_"+o2.getPatient().getId();
					}
					Collator myCollator = Collator.getInstance();			    
					return myCollator.compare(name1,name2);
				  }
			});
		}

		public void search() {
			EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
			if (eidssFilters.someFieldNotEmpty()){
				setCurrentPage(1);
				searching = true;
				refresh();
			}
			else
				JOptionPane.showMessageDialog(null,App.getMessage("cases.new.datareq"),"",JOptionPane.WARNING_MESSAGE,null);
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
	      javax.persistence.Query query = getEntityManager().createQuery( hql );
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


		/**
		 * @return the searching
		 */
		public boolean isSearching() {
			return searching;
		}

		/**
		 * @param searching the searching to set
		 */
		public void setSearching(boolean searching) {
			this.searching = searching;
		}
		@Override
		public Integer getMaxResults() {
			return 50;
		}
		@Override
		public String getOrder() {
			return "p.name";
		}
}
