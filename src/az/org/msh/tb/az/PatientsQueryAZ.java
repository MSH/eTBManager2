package org.msh.tb.az;

import java.util.Date;

import javax.persistence.Query;
import javax.swing.JOptionPane;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.cases.PatientsQuery;
import org.msh.utils.date.Period;

@Name("patientsAZ")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PatientsQueryAZ extends PatientsQuery {
	private static final long serialVersionUID = -4368672383630128239L;

		private String hqlCondition;
		
				
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
			return "select count(*) "+getEjbql();
		}
		
		private void mountEIDSSCondition() {
			EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
			hqlCondition += "exists(select az.id from TbCaseAZ az where az.id = c.id ";
			if (eidssFilters.getName()!=null){
				String[] names = eidssFilters.getName().split(" ");
				if (names.length != 0){
					String s="";
					for (String name: names) {
						if (!"".equals(name)){
							if (s.length() > 1)
								s += " and ";
							name = name.replaceAll("'", "''");
							s += "(((upper(p.name) like '%" + name.toUpperCase() + 
							"%') or (upper(p.middleName) like '%" + name.toUpperCase() + 
							"%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%')) or "+
							"(upper(az.EIDSSComment) like '%"+name.toUpperCase()+"%'))";
						}
					}
					if (!s.isEmpty())
						addCondition(s);
				}
			}
			if (eidssFilters.getAddress()!=null && !"".equals(eidssFilters.getAddress())){
				//String s="(c.eidssData.address like '%"+eidssFilters.getAddress()+"%')";
				String s="(az.EIDSSComment like '%"+eidssFilters.getAddress()+"%')";
				addCondition(s);
			}
			if (eidssFilters.getId()!=null && !"".equals(eidssFilters.getId())){
				String s="(c.legacyId like '"+eidssFilters.getId()+"')";
				addCondition(s);
			}
			if (eidssFilters.getNotifUnit()!=null && !"".equals(eidssFilters.getNotifUnit())){
				//String s="(c.eidssData.notifUnit like '%"+eidssFilters.getNotifUnit()+"%')";
				String s="(az.EIDSSComment like '%"+eidssFilters.getNotifUnit()+"%')";
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
				String s = "((az.EIDSSComment like '%"+eidssFilters.getYearBirth()+"%') or " +
				"(year(p.birthDate) = "+eidssFilters.getYearBirth()+"))";
				addCondition(s);
			}
			//=====DATES=====
			Period d = eidssFilters.getInDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				//generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.eidssData.inDate","inDate");
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"az.inEIDSSDate","inDate");
			}
			d = eidssFilters.getRegDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"c.registrationDate","regDate");
			}
			d = eidssFilters.getSysDate();
			if ((d.getIniDate()!=null) || (d.getEndDate()!=null)){
				generateHQLPeriodEIDSS(d.getIniDate(),d.getEndDate(),"az.systemDate","sysDate");
			}
			hqlCondition+=")";
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
				
		@Override
		public void search() {
			EIDSSFilters eidssFilters = (EIDSSFilters)Component.getInstance("eidssFilters");
			if (eidssFilters.someFieldNotEmpty()){
				setCurrentPage(1);
				setSearching(true);
				refresh();
			}
			else
				JOptionPane.showMessageDialog(null,App.getMessage("cases.new.datareq"),"",JOptionPane.WARNING_MESSAGE,null);
		}
		
		@Override
		public void refresh(){
			setPatientList(null);
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
	      if(getFirstResult() != null) 
	    	  query.setFirstResult(getFirstResult());
	      if (getMaxResults() != null)
	    	  query.setMaxResults(getMaxResults()+1);
	      return query;
		}
}
