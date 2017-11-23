/**
 * 
 */
package org.msh.tb.test;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.tbunits.TBUnitSelection;

import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
@Name("unitseltest")
public class UnitSelTest {

	private HtmlSelectOneMenu comboau;
	private TBUnitSelection unitsel;
	private Integer adminUnitId;

	private List<AdministrativeUnit> adminUnits;
	private List<Tbunit> units;
	private List<SelectItem> auitems;
	
	private Tbunit unit;
	
	/**
	 * Test the execution
	 */
	public void execute() {
		unit = unitsel.getSelected();
		System.out.println("Selected unit = " + unit);
	}
	
	/**
	 * @return the unitsel
	 */
	public TBUnitSelection getUnitsel() {
		if (unitsel == null)
			unitsel = new TBUnitSelection("uaid");
		return unitsel;
	}

	
	/**
	 * @return
	 */
	public List<Tbunit> getUnits() {
		if ((units == null) && (adminUnitId != null)) {
			AdministrativeUnit adminunit = App.getEntityManager().find(AdministrativeUnit.class, adminUnitId);
			units = App.getEntityManager().createQuery("from Tbunit where adminUnit.code like :code and workspace.id = #{defaultWorkspace.id}")
					.setParameter("code", adminunit.getCode() + "%")
					.getResultList();
		}
		return units;
	}
	
	/**
	 * @return the comboau
	 */
	public HtmlSelectOneMenu getComboau() {
		return comboau;
	}

	/**
	 * @param comboau the comboau to set
	 */
	public void setComboau(HtmlSelectOneMenu comboau) {
		this.comboau = comboau;
		
		System.out.println("combo = " + comboau);
		
		System.out.println("value = " + comboau.getValue());
		FacesContext fc = FacesContext.getCurrentInstance();
		String id = comboau.getClientId(fc);
		System.out.println("id = " + id);

		String val = fc.getExternalContext().getRequestParameterMap().get(id);
		System.out.println("value = " + val);
		if ((val != null) && (!val.trim().isEmpty())) {
			try {
				adminUnitId = Integer.parseInt(val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the adminUnitId
	 */
	public Integer getAdminUnitId() {
		return adminUnitId;
	}

	/**
	 * @param adminUnitId the adminUnitId to set
	 */
	public void setAdminUnitId(Integer adminUnitId) {
		this.adminUnitId = adminUnitId;
	}

	/**
	 * @return the adminUnits
	 */
	public List<AdministrativeUnit> getAdminUnits() {
		if (adminUnits == null) {
			EntityManager em = App.getEntityManager();
			adminUnits = em
					.createQuery("from AdministrativeUnit where parent.id is null and workspace.id = #{defaultWorkspace.id}")
					.getResultList();
		}
		return adminUnits;
	}

	
	public List<SelectItem> getAuitems() {
		if (auitems == null) {
			auitems = new ArrayList<SelectItem>();
			auitems.add(new SelectItem(null, "-"));
			for (AdministrativeUnit au: getAdminUnits()) {
				auitems.add(new SelectItem(au.getId(), au.getName().toString()));
			}
		}
		return auitems;
	}

	/**
	 * @return the unit
	 */
	public Tbunit getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(Tbunit unit) {
		this.unit = unit;
	}
}
