package org.msh.tb.tbunits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.UserWorkspace;
import org.msh.tb.login.UserSession;

@Name("unitSelection")
public class UnitSelection {

	@In(create=true) UserSession userSession;
	@In(create=true) EntityManager entityManager;
	@In(create=true) UserWorkspace userWorkspace;
	@In(create=true) Map<String, String> messages;

	/**
	 * Store information about a region and its list 
	 * @author Ricardo Memoria
	 * 
	 */
	public class Item {
		private String name;
		private List<Tbunit> units = new ArrayList<Tbunit>();

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the units
		 */
		public List<Tbunit> getUnits() {
			return units;
		}
		/**
		 * @param units the units to set
		 */
		public void setUnits(List<Tbunit> units) {
			this.units = units;
		}
	}
	
	private boolean selecting;
	private Integer unitId;
	private List<Item> items;
	private String name;
	private int unitCount;


	/**
	 * Select a unit for the medicine management module 
	 * @return "unitselected" if the unit was successfully selected, otherwise return "error"
	 */
	public String selectUnit() {
		if (unitId == null)
			return "error";
		
		Tbunit unit = entityManager.find(Tbunit.class, unitId);
		unit.getAdminUnit().getParentsTreeList(false);
		// avoid lazy init error
		unit.getHealthSystem().getName();
		userSession.setTbunit(unit);
		
  	    return "med-home";
	}

	
	/**
	 * Return list of items for selection
	 * @return List of Item object
	 */
	public List<Item> getItems() {
		if (items == null)
			createItems();
		return items;
	}


	protected String conditionHQLAdminUnit(AdministrativeUnit adm) {
		String code = adm.getCode();
		return "and (u.adminUnit.code like '" + code + "%')"; 
	}
	
	
	/**
	 * Create a list of items and its units 
	 */
	protected void createItems() {
		if (!selecting)
			return;
		
		// return list of main administrative units (level 0)
		List<AdministrativeUnit> regs = entityManager.createQuery("from AdministrativeUnit adm " +
				"where adm.parent.id = null and adm.workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		
		String cond;
		Tbunit unit = userWorkspace.getTbunit();

		switch (userWorkspace.getView()) {
		case ADMINUNIT: cond = conditionHQLAdminUnit(userWorkspace.getAdminUnit());
			break;
		case TBUNIT: cond = "and u.id = " + unit.getId();
			break;
		default: cond = "";
		}
		
		if ((name != null) && (!name.isEmpty())) {
			cond += " and upper(u.name.name1) like '%" + name.toUpperCase() + "%'";
		}
		
		if (userWorkspace.getHealthSystem() != null)
			 cond += " and u.healthSystem.id = " + userWorkspace.getHealthSystem().getId();
		
		List<Tbunit> units = entityManager.createQuery("select u from Tbunit u join fetch u.adminUnit a1 " +
				"where u.workspace.id = #{defaultWorkspace.id} and u.active = :active " + 
				cond +
				" order by u.adminUnit.name.name1, u.name.name1")
				.setParameter("active", true)
				.getResultList();

		items = new ArrayList<Item>();
		unitCount = units.size();

		for (Tbunit aux: units) {
			String regName = aux.getAdminUnit().getCode();
			for (AdministrativeUnit adm: regs) {
				if (adm.isSameOrChildCode(regName)) {
					regName = adm.getName().toString();
					break;
				}
			}
			Item item = findItemByName(regName);
			item.getUnits().add(aux);
		}
		
		// sort by region name
		Collections.sort(items, new Comparator<Item>() {
			public int compare(Item o1, Item o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}

	
	/**
	 * Search an item by its name
	 * @param name of the item to search
	 * @return instance of Item object
	 */
	private Item findItemByName(String name) {
		for (Item item: items) {
			if (name.equals(item.getName())) {
				return item;
			}
		}
		
		Item item = new Item();
		item.setName(name);
		items.add(item);
		
		return item;
	}

	public boolean isSelecting() {
		return selecting;
	}
	
	public void startSelecting() {
		selecting = true;
	}

	public Integer getUnitId() {
		return unitId;
	}

	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
		selectUnit();
	}

	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the unitCount
	 */
	public int getUnitCount() {
		if (items == null)
			createItems();
		return unitCount;
	}
	
	public String getUnitCountText() {
		int num = getUnitCount();
		if (num == 0)
			return messages.get("medicines.nounit");
		else {
			MessageFormat format = new MessageFormat(messages.get("medicines.unitsfound"));
			Object[] vals = {Integer.toString(num)};
			return format.format(vals);
		}
	}
}
