package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;
import org.msh.utils.EntityQuery;

/**
 * Generate list or registration numbers, which exist in batches, in scope current conversation
 * @author A.M.
 */
@Name("registrationNumbers")
@BypassInterceptors
@Scope(ScopeType.CONVERSATION)
public class RegistrationNumbers extends EntityQuery<SelectItem> {
	private static final long serialVersionUID = -3220352813656097835L;
	
	private List<SelectItem> resultList;
	private RegistrationCard selected;
	private Integer indexSelected;
	
	@Override
	public String getEjbql() {
		//Integer currMedId = getCurrMedId();
		String hql = "select b.registCardNumber, b.registCardBeginDate, b.registCardEndDate " +
		"from Batch b " +
		"where b.registCardNumber!=null "+
		(getCurrMedId()!=null ? "and b.medicine.id = #{medicineReceivingUAHome.batch.medicine.id}":"");
		return hql;
	}
	/**
	 * Current selected medicine
	 * @return
	 */
	public Integer getCurrMedId() {
		MedicineReceivingUAHome medicineReceivingHome = (MedicineReceivingUAHome)App.getComponent("medicineReceivingUAHome");
		if (medicineReceivingHome!=null && medicineReceivingHome.getBatch()!=null && medicineReceivingHome.getBatch().getMedicine()!=null){
			return medicineReceivingHome.getBatch().getMedicine().getId();
		}
		return null;
	}
	
	@Override
	public String getOrder() {
		return "b.registCardNumber, b.registCardBeginDate, b.registCardEndDate";
	}
	
	@Override
	public String getGroupBy() {
		return "b.registCardNumber, b.registCardBeginDate, b.registCardEndDate";
	}
	

	private void fillList(List<Object[]> lst) {
		resultList = new ArrayList<SelectItem>();
		for (Object[] vals: lst) {
			RegistrationCard rc = new RegistrationCard();
			rc.setNumber((String)vals[0]);
			if (vals[1]!=null)
				rc.setIniDate((Date)vals[1]);
			if (vals[2]!=null)
				rc.setEndDate((Date)vals[2]);
			SelectItem si = new SelectItem();
			si.setLabel(rc.getNumber());
			si.setValue(rc);
			resultList.add(si);
		}
	}
	
	public void clearList(){
		resultList = null;
		setEjbql(null);
	}
	
	
	
	@Override
	public List<SelectItem> getResultList() {
		if (resultList == null)
		{
			javax.persistence.Query query = createQuery();
			List<Object[]> lst = query==null ? null : query.getResultList();
			fillList(lst);
		}
		return resultList;
	}

	public void setSelected(RegistrationCard selected) {
		this.selected = selected;
		if (selected==null) indexSelected = -1;
		for (SelectItem si:getResultList()){
			RegistrationCard rc = (RegistrationCard)si.getValue();
			if (rc.getNumber().equals(selected.getNumber()))
				indexSelected = resultList.indexOf(si);
		}
	}

	public RegistrationCard getSelected() {
		return selected;
	}

	public void setIndexSelected(Integer indexSelected) {
		this.indexSelected = indexSelected;
		if (indexSelected == -1) 
			selected = null;
		else
		if (getResultList().size()>indexSelected)
			selected = (RegistrationCard)getResultList().get(indexSelected).getValue();
	}

	public Integer getIndexSelected() {
		return indexSelected;
	}

	/**
	 * Type structure for registration card
	 * @author A.M.
	 */
	public static class RegistrationCard{
		private String number;
		private Date iniDate;
		private Date endDate;
		
		public RegistrationCard() {
			super();
		}
		
		public RegistrationCard(String number, Date iniDate, Date endDate) {
			super();
			this.number = number;
			this.iniDate = iniDate;
			this.endDate = endDate;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RegistrationCard){
				RegistrationCard rc = (RegistrationCard)obj;
				if (rc.getNumber().equals(this.getNumber()) 
						&& rc.getIniDate().equals(this.getIniDate()) 
						&& rc.getEndDate().equals(this.getEndDate()))
					return true;
			}
			return false;
		}
		
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		public Date getIniDate() {
			return iniDate;
		}
		public void setIniDate(Date iniDate) {
			this.iniDate = iniDate;
		}
		public Date getEndDate() {
			return endDate;
		}
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}
	}
	
}
