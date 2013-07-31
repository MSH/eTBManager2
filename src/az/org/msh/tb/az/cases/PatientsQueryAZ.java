package org.msh.tb.az.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.msh.tb.cases.PatientsQuery;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.Period;

@Name("patientsAZ")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class PatientsQueryAZ extends PatientsQuery {
	private static final long serialVersionUID = -4368672383630128239L;

	private static final String[] orderValues = {
		"p.gender, #{patientsAZ.namesOrderBy}",
		"p.recordNumber, c.caseNumber", 
		"#{patientsAZ.namesOrderBy}", 
		"c.age", 
		"upper(c.notifAddress.adminUnit.name.name1)", 
		"c.classification", 
		"c.treatmentPeriod.iniDate", 
		"upper(c.notificationUnit.name.name1)",
		"c.state"};


	private String hqlCondition;
	private Integer newOrder;
	private boolean inverseOrder = false;

	/**
	 * Generate HQL conditions for filters that cannot be included in the restrictions clause
	 * @return
	 */
	@Override
	public String conditions() {
		hqlCondition = "";
		mountEIDSSCondition();

		if (!hqlCondition.isEmpty())
			hqlCondition = " and ".concat(hqlCondition);

		return hqlCondition;
	}

	@Override
	protected void createPatientList() {
		List<Item> patientList = new ArrayList<Item>();

		for (Object obj: getResultList()) {
			Object[] vals = (Object[])obj;
			Item item = new Item((Patient)vals[0], (TbCase)vals[1]);
			patientList.add(item);
		}
		setPatientList(patientList);
	}
	
	/**
	 * Return the HQL order by declaration
	 * @return String containing the order by clause
	 */
	public String getNamesOrderBy() {
		Workspace defaultWorkspace = UserSession.getWorkspace();
		String s = null;
		switch (defaultWorkspace.getPatientNameComposition()) {
		case FULLNAME: 
			s = "upper(p.name)"; break;
		case FIRSTSURNAME: 
			s = "upper(p.name), upper(p.middleName)"; break;
		case LAST_FIRST_MIDDLENAME: case LAST_FIRST_MIDDLENAME_WITHOUT_COMMAS:
			s = "upper(p.lastName), upper(p.name), upper(p.middleName)";
		case SURNAME_FIRSTNAME: 
			s = "upper(p.middleName), upper(p.name)";
		case FIRST_MIDDLE_LASTNAME:
		default:
			s = "upper(p.lastName), upper(p.name), upper(p.middleName)";
		}
		if (inverseOrder)
			s = addDesc(s);
		return s;
	}
	
	/**
	 * Change order sorting to descending
	 * @param s
	 * @return "order by" string
	 */
	private String addDesc(String s) {
		if (s!=null && !"".equals(s))
			s = s.replaceAll(",", " desc,");
		s += " desc";
		return s;
	}

	@Override
	public String getOrder() {
		if (getNewOrder()>100)
			return null;
		String s = orderValues[newOrder];
		if (inverseOrder)
			s = addDesc(s);
		s = Expressions.instance().createValueExpression(s).getValue().toString();
		return s;
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) "+getEjbql();
	}

	/**
	 * Add to condition verify to EIDSS-filters
	 */
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
	}

	@Override
	public void refresh(){
		setPatientList(null);
		setPageOptions(null);
		hqlCondition = null;
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
		if (getOrder()!=null)
			hql += " order by "+getOrder();
		javax.persistence.Query query = getEntityManager().createQuery( hql );
		if(getFirstResult() != null) 
			query.setFirstResult(getFirstResult());
		if (getMaxResults() != null)
			query.setMaxResults(getMaxResults()+1);
		return query;
	}
	
	@Override
	protected Query createCountQuery() {
		parseEjbql();
		evaluateAllParameters();
		joinTransaction();
		String hql = getCountEjbql();
		if (getOrder()!=null)
			hql += " order by "+getOrder();
		javax.persistence.Query query = getEntityManager().createQuery( hql );
		return query;
	}

	/**
	 * By default - by name
	 * @return the newOrder
	 */
	public Integer getNewOrder() {
		if (newOrder==null)
			newOrder = 2;
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

	@Override
	public List<SelectItem> getPageOptions() {
		if (getPageOptionsWithoutCreate() != null)
			return getPageOptionsWithoutCreate();

		Integer currentPage = getCurrentPage();
		if (currentPage == null)
			return null;

		Integer maxpage = getMaxPage();
		if (maxpage == null)
			return null;

		List<SelectItem> po = new ArrayList<SelectItem>();
		for (int i = 1; i <= maxpage; i++) {
			po.add(new SelectItem(i, Integer.toString(i)));
		}
		setPageOptions(po);
		return po;
	}


}
