package org.msh.tb.az;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.cases.PatientsQuery;
import org.msh.utils.date.Period;

@Name("patientsAZ")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PatientsQueryAZ extends PatientsQuery {
	private static final long serialVersionUID = -4368672383630128239L;

		private String hqlCondition;
		private Integer newOrder;
		private boolean inverseOrder = false;
				
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
			/*else
				JOptionPane.showMessageDialog(null,App.getMessage("cases.new.datareq"),"",JOptionPane.WARNING_MESSAGE,null);*/
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
		
		/**
		 * Change order sorting patient list
		 * */
		public void changeOrder(){
			int ind = newOrder; 
			switch (ind) {
				case 0: { //recordNumber
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getPatient().getRecordNumber();
							Integer i2 = o2.getPatient().getRecordNumber();
							if (i1 == null) i1 = 0;
							if (i2 == null) i2 = 0;
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 1: { //gender
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getPatient().getGender().ordinal();
							Integer i2 = o2.getPatient().getGender().ordinal();
							if (i1 == null) i1 = 0;
							if (i2 == null) i2 = 0;
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 2: { //classification
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getTbcase().getClassification().ordinal();
							Integer i2 = o2.getTbcase().getClassification().ordinal();
							if (i1 == null) i1 = 0;
							if (i2 == null) i2 = 0;
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 3: { //name
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							return compareNames(o1,o2);
						}
					});
					break;
				}
				case 4: { //age
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getTbcase().getAge();
							Integer i2 = o2.getTbcase().getAge();
							if (i1 == null) i1 = 0;
							if (i2 == null) i2 = 0;
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 5: { //notificationUnit
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							String i1 = (o1.getTbcase().getNotificationUnit()!=null ? o1.getTbcase().getNotificationUnit().getName().getName1():"");
							String i2 = (o2.getTbcase().getNotificationUnit()!=null ? o2.getTbcase().getNotificationUnit().getName().getName1():"");
							
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else{
								return compare2strings(i1, i2);
								}
						}
					});
					break;
				}
				case 6: { //notifAddress admin unit
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							String i1 = "";
							if (o1.getTbcase().getNotifAddress()!=null)
								if (o1.getTbcase().getNotifAddress().getAdminUnit()!=null)
									if (o1.getTbcase().getNotifAddress().getAdminUnit().getParent()!=null)
										i1 = o1.getTbcase().getNotifAddress().getAdminUnit().getParent().getName().getName1();
							String i2 = "";
							if (o2.getTbcase().getNotifAddress()!=null)
								if (o2.getTbcase().getNotifAddress().getAdminUnit()!=null)
									if (o2.getTbcase().getNotifAddress().getAdminUnit().getParent()!=null)
										i2 = o2.getTbcase().getNotifAddress().getAdminUnit().getParent().getName().getName1();
							
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else{
								return compare2strings(i1, i2);
								}
						}
					});
					break;
				}
				case 7: { //notifAddress
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							String i1 = "";
							if (o1.getTbcase().getNotifAddress()!=null)
								if (o1.getTbcase().getNotifAddress().getAdminUnit()!=null)
										i1 = (o1.getTbcase().getNotifAddress()!=null ? o1.getTbcase().getNotifAddress().getAdminUnit().getName().getName1():"");
							String i2 = "";
							if (o2.getTbcase().getNotifAddress()!=null)
								if (o2.getTbcase().getNotifAddress().getAdminUnit()!=null)
										i2 = (o2.getTbcase().getNotifAddress()!=null ? o2.getTbcase().getNotifAddress().getAdminUnit().getName().getName1():"");
							
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else{
								return compare2strings(i1, i2);
							}
						}
					});
					break;
				}
				case 8: { //state
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getTbcase().getState().ordinal();
							Integer i2 = o2.getTbcase().getState().ordinal();
							
							if (i1 == null) i1 = -1;
							if (i2 == null) i2 = -1;
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 9: { //registrationDate
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Date i1 = o1.getTbcase().getRegistrationDate();
							Date i2 = o2.getTbcase().getRegistrationDate();
							
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				case 10: { //treatment inidate
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Date d1 = null;
							if (o1.getTbcase().getTreatmentPeriod()!=null)
								d1 = o1.getTbcase().getTreatmentPeriod().getIniDate();
							Date d2 = null;
							if (o2.getTbcase().getTreatmentPeriod()!=null)
								d2 = o2.getTbcase().getTreatmentPeriod().getIniDate();
							
							return compareDates(o1, o2, d1, d2);
						}
					});
					break;
				}
				case 11: { //validation state
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							Integer i1 = o1.getTbcase().getValidationState().ordinal();
							Integer i2 = o2.getTbcase().getValidationState().ordinal();
							
							if (i1 == null) i1 = -1;
							if (i2 == null) i2 = -1;
							
							if (i1.equals(i2)){
								return compareNames(o1,o2);
							}
							else
								return (!inverseOrder ? i1.compareTo(i2): i2.compareTo(i1));
						}
					});
					break;
				}
				//====================SORT BY EIDSS FIELDS==========================
				case 101: { //eidss name
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							String name1 = az1.getEIDSSName();
							String name2 = az2.getEIDSSName();
							
							if (name1.equals(name2)){
								return compareNames(o1, o2);
							}
							return compare2strings(name1, name2);
						}
					});
					break;
				}
				case 102: { //eidss date of birth
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							Date d1 = az1.getEIDSSBirthDate();
							Date d2 = az2.getEIDSSBirthDate();
							
							if (d1 == null || d2 == null){
								Integer a1 = az1.getEIDSSAge();
								Integer a2 = az2.getEIDSSAge();
								return a2.compareTo(a1);
							}
							
							return compareDates(o1, o2, d1, d2);
						}
					});
					break;
				}
				case 103: { //eidss address
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							String name1 = az1.getEIDSSNotifAddress();
							String name2 = az2.getEIDSSNotifAddress();
							    
							if (name1.equals(name2)){
								name2 = name1+"_"+o2.getPatient().getId();
							}
							return compare2strings(name1, name2);
						}
					});
					break;
				}
				case 104: { //eidss notification date
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							Date d1 = az1.getEIDSSNotifDate();
							Date d2 = az2.getEIDSSNotifDate();
							
							return compareDates(o1, o2, d1, d2);
						}
					});
					break;
				}
				case 105: { //eidss inner date
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							Date d1 = az1.getEIDSSInnerDate();
							Date d2 = az2.getEIDSSInnerDate();
							
							return compareDates(o1, o2, d1, d2);
						}
					});
					break;
				}
				case 106: { //eidss notif unit
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							TbCaseAZ az1 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o1.getTbcase().getId());
							TbCaseAZ az2 = (TbCaseAZ) App.getEntityManager().find(TbCaseAZ.class, o2.getTbcase().getId());
							
							String name1 = az1.getEIDSSNotifUnit();
							String name2 = az2.getEIDSSNotifUnit();
							    
							if (name1.equals(name2)){
								name2 = name1+"_"+o2.getPatient().getId();
							}
							
							return compare2strings(name1, name2);
						}
					});
					break;
				}
				case 107: { //eidss id
					Collections.sort(getPatientList(), new Comparator<Item>() {

						@Override
						public int compare(Item o1, Item o2) {
							String name1 = o1.getTbcase().getLegacyId();
							String name2 = o2.getTbcase().getLegacyId();
							
							if (name1 == null) name1="";
							if (name2 == null) name2="";
							
							if (name1.equals(name2)){
								name2 = name1+"_"+o2.getPatient().getId();
							}
							
							return compare2strings(name1, name2);
						}
					});
					break;
				}
			}
		}
		
		/**
		 * Compare two patients by full names
		 * */
		protected int compareNames(Item o1, Item o2) {
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
			return compare2strings(name1, name2);
		}

		/**
		 * @param s1
		 * @param s2
		 * @return
		 */
		protected int compare2strings(String s1, String s2) {
//			Collator myCollator = Collator.getInstance();			    
//			return (!inverseOrder ? myCollator.compare(s1,s2) : myCollator.compare(s2,s1));
			return (!inverseOrder ? s1.compareTo(s2) : s2.compareTo(s1));
		}

		/**
		 * Compare two dates
		 */
		protected int compareDates(Item o1, Item o2, Date d1, Date d2) {
			if (d1 == null && d2 == null)
				return compareNames(o1,o2);
			else{
				if (d1 == null)
					return -1;
				if (d2 == null)
					return 1;
			}
			if (d1.equals(d2))
				return compareNames(o1, o2);
			return (!inverseOrder ? d1.compareTo(d2) : d2.compareTo(d1));
		}

		/**
		 * @return the newOrder
		 */
		public Integer getNewOrder() {
			return newOrder;
		}
		
		/**
		 * @param newOrder the newOrder to set
		 */
		public void setNewOrder(Integer newOrder) {
			if (this.newOrder == newOrder)
				inverseOrder = !inverseOrder;
			this.newOrder = newOrder;
		}
	
}
