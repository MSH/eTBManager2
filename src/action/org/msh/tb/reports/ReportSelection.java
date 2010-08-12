package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.framework.EntityQuery;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.workspaces.WorkspaceViewService;
import org.msh.utils.date.DateUtils;


@Name("reportSelection")
public class ReportSelection {

	private int iniMonth;
	private int iniYear;
	private int endMonth;
	private int endYear;
	private Tbunit tbunit;
	private Source source;
	
	private String pageToPrint;
	private UserWorkspace userWorkspace;
	
	@In(create=true) EntityQuery sources;
	@In(create=true) EntityManager entityManager;
	
	private AdminUnitSelection auselection = new AdminUnitSelection(true);
	
	@Factory("reportDate")
	public Date getDate() {
		return new Date();
	}

	
	/**
	 * Initialize the workspace to the workspace of the user logged in
	 */
	public void initializeWorkspace() {
		if (userWorkspace != null)
			return;
		
		WorkspaceViewService srv = (WorkspaceViewService)Component.getInstance("workspaceViewService", true);
		if (srv.isFormPost())
			return;
		
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
		userWorkspace = userLogin.getUser().getDefaultWorkspace();
	}


	public AdminUnitSelection getAuselection() {
		return auselection;
	}

	/**
	 * If dates are null, initialize them to the previous quarter
	 */
	public void initializeDates() {
		// just initialize if year = 0
		if (getIniYear() > 0)
			return;

		Calendar c = Calendar.getInstance();
		setEndYear(c.get(Calendar.YEAR));
		setEndMonth(c.get(Calendar.MONTH));
		
		c.add(Calendar.MONTH, -3);
		setIniYear(c.get(Calendar.YEAR));
		setIniMonth(c.get(Calendar.MONTH));
	}
	
	public void setSourceId(Integer id) {
		if (id == null)
			 source = null;
		else source = entityManager.find(Source.class, id);
	}
	
	public void setRegionId(Integer regionId) {
		getAuselection().setSelectedUnitId(regionId);
	}

	public Integer getRegionId() {
		return getAuselection().getSelectedUnitId();
	}

	public Integer getSourceId() {
		return (source != null? source.getId(): null);
	}
	
	private List<SelectItem> createSelectItems() {
		List<SelectItem> items = new ArrayList<SelectItem>();		
		
		SelectItem it = new SelectItem();
		it.setValue("-");
		it.setLabel("-");
		items.add(it);
		
		return items;
	}

	public Tbunit getTbunit() {
		return tbunit;
	}
	
	public Source getSource() {
		return source;
	}
	
	
	public List<SelectItem> getSources() {
		List<SelectItem> items = createSelectItems();
		List<Source> lst = entityManager.createQuery("from Source s where s.workspace.id = #{defaultWorkspace.id} order by s.name")
				.getResultList();

		for (Source s: lst) {
			SelectItem it = new SelectItem();
			it.setValue(s.getId());
			it.setLabel(s.getName().getDefaultName());
			items.add(it);
		}
		
		return items;
	}

	public List<SelectItem> getMedicineSuppliers() {
		List<SelectItem> items = createSelectItems();
		return items;		
	}
	
	public List<SelectItem> getReceivingUnits() {
		List<SelectItem> items = createSelectItems();
		return items;		
	}

	public List<SelectItem> getHealthUnits() {
		List<SelectItem> items = createSelectItems();
		return items;		
	}

	public List<SelectItem> getRegions() {
		List<SelectItem> items = createSelectItems();
		for (AdministrativeUnit adm: auselection.getOptionsLevel1()) {
			SelectItem it = new SelectItem();
			it.setValue(adm.getId());
			it.setLabel(adm.getName().toString());
			items.add(it);
		}
		return items;
	}

	public List<SelectItem> getUnits() {
		List<SelectItem> items = createSelectItems();

/*		for (Tbunit ds: storageUnits) {
			SelectItem it = new SelectItem();
			it.setValue(ds.getId());
			it.setLabel(ds.getName().getDefaultName());
			items.add(it);
		}
*/
		
		return items;
	}

	public int getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(int endMonth) {
		this.endMonth = endMonth;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public int getIniMonth() {
		return iniMonth;
	}

	public void setIniMonth(int iniMonth) {
		this.iniMonth = iniMonth;
	}

	public int getIniYear() {
		return iniYear;
	}

	public void setIniYear(int iniYear) {
		this.iniYear = iniYear;
	}
	
	public Date getIniDate() {
		if (iniYear == 0)
			return null;
		Calendar iniDate = Calendar.getInstance();
		iniDate.setTime(DateUtils.getDate());
		iniDate.set(Calendar.DAY_OF_MONTH, 1);
		iniDate.set(Calendar.MONTH, iniMonth);
		iniDate.set(Calendar.YEAR, iniYear);
		return iniDate.getTime();
	}
	
	public Date getEndDate() {
		if (endYear == 0)
			return null;
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(DateUtils.getDate());
		endDate.set(Calendar.DAY_OF_MONTH, 1);
		endDate.set(Calendar.MONTH, endMonth);
		endDate.set(Calendar.YEAR, endYear);
		endDate.add(Calendar.MONTH, 1);
		endDate.add(Calendar.DAY_OF_MONTH, -1);
		return endDate.getTime();
	}
	
	public Date getDayAfterEndDate() {
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(DateUtils.getDate());
		endDate.set(Calendar.DAY_OF_MONTH, 1);
		endDate.set(Calendar.MONTH, endMonth);
		endDate.set(Calendar.YEAR, endYear);
		endDate.add(Calendar.MONTH, 1);
		return endDate.getTime();		
	}

	public Integer getTbunitId() {
		return (tbunit != null? tbunit.getId(): null);
	}

	public void setTbunitId(Integer id) {
		if (id == null)
			 tbunit = null;
		else tbunit = entityManager.find(Tbunit.class, id);
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * @param tbunit the tbunit to set
	 */
	public void setTbunit(Tbunit tbunit) {
		this.tbunit = tbunit;
	}


	public String getPageToPrint() {
		return pageToPrint;
	}


	public void setPageToPrint(String pageToPrint) {
		this.pageToPrint = pageToPrint;
	}


	public UserWorkspace getUserWorkspace() {
		return userWorkspace;
	}


	@RaiseEvent("report-workspace-changed")
	public void setUserWorkspace(UserWorkspace userWorkspace) {
		this.userWorkspace = userWorkspace;
	}
	
	public void setUserWorkspaceId(Integer id) {
		userWorkspace = entityManager.find(UserWorkspace.class, id);
	}
	
	public Integer getUserWorkspaceId() {
		return (userWorkspace != null? userWorkspace.getId(): null);
	}
}
