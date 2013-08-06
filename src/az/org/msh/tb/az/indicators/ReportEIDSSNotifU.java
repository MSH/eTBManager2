package org.msh.tb.az.indicators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.az.cases.EIDSSFilters;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.indicators.core.CaseHQLBase;
import org.msh.tb.indicators.core.IndicatorFilters;

@Name("reportEIDSSNotifU")
public class ReportEIDSSNotifU extends CaseHQLBase{
	private static final long serialVersionUID = -8569857853251030982L;

	private Map<String, List<Integer>> list;

	private void generateList() {
		list = new TreeMap<String, List<Integer>>();
		List<TbCaseAZ> lst = createQuery().getResultList();
		lst = postFilters(lst);
		for (TbCaseAZ tc: lst){
			String nu = tc.getEIDSSNotifUnit();
			if ("".equals(nu)) nu = "<empty>";
			Integer ind;
			if (tc.getNotificationUnit()!=null){
				ind = 1;
				}else {
					ind = 0;
			}
			if (list.containsKey(nu)){
				List<Integer> ii=list.get(nu);
				list.get(nu).set(ind,ii.get(ind)+1);
				list.get(nu).set(2,ii.get(2)+1);
			}
			else{
				Integer[] ii;
				if (tc.getNotificationUnit()!=null)
					ii = new Integer[]{0,1,1};
				else
					ii = new Integer[]{1,0,1};
				list.put(nu, Arrays.asList(ii));
			}
		}
		
	}
	
	private List<TbCaseAZ> postFilters(List<TbCaseAZ> lst) {
		EIDSSFilters filt = (EIDSSFilters) App.getComponent("eidssFilters");
		if (filt.getInDate().getIniDate() == null && filt.getInDate().getEndDate() == null) 
				//&& diagDate.getIniDate() == null && diagDate.getEndDate() == null)
				return lst;
		
		List<TbCaseAZ> res = new ArrayList<TbCaseAZ>();
		for (TbCaseAZ tc:lst){
			Date iD = tc.getInEIDSSDate();
			if (iD==null) continue;
			if (filt.getInDate().getIniDate()!= null)
				if (filt.getInDate().getIniDate().after(iD))
					continue;
			if (filt.getInDate().getEndDate()!= null)
				if (filt.getInDate().getEndDate().before(iD))
					continue;
			res.add(tc);
		}
		return res;
	}

	public Integer total(int i){
		int res = 0;
		for (String key:list.keySet())
			res += list.get(key).get(i);
		return res;
	}
	
	@Override
	protected String getHQLWhere() {
		//String hql = super.getHQLWhere();
		/*String hql = "where c.notificationUnit.workspace.id = " + getWorkspace().getId().toString();
		
		UserWorkspace uw = (UserWorkspace)Component.getInstance("userWorkspace");
		if (uw.getHealthSystem() != null)
			hql += " and c.notificationUnit.healthSystem.id = " + uw.getHealthSystem().getId();
		
		if (uw!=null){
			if (UserView.ADMINUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.adminUnit.id = "+uw.getAdminUnit().getId() + " or c.ownerUnit.adminUnit.id = "+uw.getAdminUnit().getId()+")";
			}
			else if (UserView.TBUNIT.equals(uw.getView())){
				hql += " and (c.notificationUnit.id = "+uw.getTbunit().getId() + " or c.ownerUnit.id = "+uw.getTbunit().getId()+")";
			}
		}
		hql += " and c.EIDSSComment != null";*/
		EIDSSFilters filt = (EIDSSFilters) App.getComponent("eidssFilters");
		//String hql = "where c.EIDSSComment  != null";
		String hql = "where c.legacyId != null";
		//Date d = getBindDate();
		if (filt.getBindYear()!=null)
			hql += " and year(c.systemDate) = "+ filt.getBindYear();
		if (filt.getBindMonth()!=null)
			hql += " and month(c.systemDate) = "+filt.getBindMonth();
		if (filt.getTbunit().getTbunit()!=null){
			hql += " and c.notificationUnit.id = "+filt.getTbunit().getTbunit().getId();
		}
		if (filt.getTbunit().getAdminUnit()!=null){
			hql += " and c.notificationUnit.adminUnit.id = "+filt.getTbunit().getAdminUnit().getId();
		}
		return hql;
		
	}
	
	@Override
	protected String getHQLFrom() {
		return "from TbCaseAZ c";
	}

	@Override
	protected String getHQLSelect() {
		return "select c";
	}
	
	@Override
	protected String getHQLJoin() {
		return null;
	}

	@Override
	protected IndicatorFilters getIndicatorFilters() {
		if (super.getIndicatorFilters()!=null)
			return super.getIndicatorFilters();
		return new IndicatorFilters();
	}
	/**
	 * @return the list
	 */
	public Map<String, List<Integer>> getList() {
		if (list==null)
			generateList();
		return list;
	}

	/**
	 * Return the bind date based on the binding month and year
	 * @return
	 */
	/*public Date getBindDate() {
		if (bindYear == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, bindYear);
		if (bindMonth == null)
			 c.set(Calendar.MONTH, 0);
		else c.set(Calendar.MONTH, bindMonth);
		c.set(Calendar.DAY_OF_MONTH, 1);

		return c.getTime();
	}*/
	
	/**
	 * @param list the list to set
	 */
	public void setList(Map<String, List<Integer>> list) {
		this.list = list;
	}

	


}
