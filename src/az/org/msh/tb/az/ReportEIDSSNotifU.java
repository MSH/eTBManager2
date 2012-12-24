package org.msh.tb.az;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.seam.annotations.Name;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.indicators.core.CaseHQLBase;

@Name("reportEIDSSNotifU")
public class ReportEIDSSNotifU extends CaseHQLBase{
	private static final long serialVersionUID = -8569857853251030982L;

	private Map<String, List<Integer>> list;

	private void generateList() {
		list = new TreeMap<String, List<Integer>>();
		List<TbCaseAZ> lst = createQuery().getResultList();
		for (TbCaseAZ tc: lst){
			String nu = tc.getEIDSSComment().split(" / ")[0];
			if ("".equals(nu)) nu = "<empty>";
			int ind = 0;
			if (tc.getNotificationUnit()!=null)
				ind = 1;
			if (list.containsKey(nu)){
				list.get(nu).set(ind,list.get(nu).get(ind)+1);
				
			}
			else{
				Integer[] ii;
				if (tc.getNotificationUnit()!=null)
					ii = new Integer[]{0,1};
				else
					ii = new Integer[]{1,0};
				list.put(nu, Arrays.asList(ii));
			}
		}
		//Collections.sort(list,)
		
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
		String hql = "where c.EIDSSComment != null";
		return hql;
		
	}
	
	@Override
	protected String getHQLFrom() {
		return "from TbCase c";
	}

	@Override
	protected String getHQLSelect() {
		return "select c";
	}
	
	@Override
	protected String getHQLJoin() {
		return null;
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
	 * @param list the list to set
	 */
	public void setList(Map<String, List<Integer>> list) {
		this.list = list;
	}

}
