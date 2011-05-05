package org.msh.tb.md;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportingBase {

	/**
	 * Warnings about case importing
	 */
	private List<WarnMessage> warning = new ArrayList<WarnMessage>();
	
	/**
	 * Workspace to use
	 */
	private Workspace workspace;
	
	private MoldovaServiceConfig config;
	
	/**
	 * return the entity Manager
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}


	/**
	 * Search for a TB Unit by its id
	 * @param data.id
	 * @return
	 */
	protected Tbunit loadTBUnit(String legacyId) {
		String hql = "from Tbunit unit where unit.workspace.id = " + getWorkspace().getId().toString() + " and unit.legacyId = :id"; 

		List<Tbunit> lst = getEntityManager()
			.createQuery(hql)
			.setParameter("id", legacyId)
			.getResultList();
			
		return (lst.size() > 0? lst.get(0): null);		
	}
	
	
	/**
	 * Load the administrative unit with the corresponding legacy ID. If the record is not found,
	 * the default administrative unit is loaded
	 * @param legacyId
	 * @return
	 */
	protected AdministrativeUnit loadAdminUnit(String legacyId) {
		List<AdministrativeUnit> lst = getEntityManager()
				.createQuery("from AdministrativeUnit adm where adm.legacyId = :id and adm.workspace.id = :wsid")
				.setParameter("id", legacyId)
				.setParameter("wsid", getWorkspace().getId())
				.getResultList();

		return (lst.size() > 0? lst.get(0): null);		
	}

	
	/**
	 * Load the health system with the corresponding legacy ID. If the record is not found,
	 * the default health system is loaded
	 * @param legacyId
	 * @return
	 */
	protected HealthSystem loadHealthSystem(String legacyId) {
		List<HealthSystem> lst = getEntityManager()
			.createQuery("from HealthSystem hs where hs.legacyId = :id and hs.workspace.id=" + getWorkspace().getId().toString())
			.setParameter("id", legacyId)
			.setParameter("wsid", getWorkspace().getId())
			.getResultList();

		return (lst.size() > 0? lst.get(0): null);		
	}

	/**
	 * Get the value from a XML tag inside an element
	 * @param el
	 * @param tag
	 * @return
	 */
	protected String getValue(Element el, String tag, boolean required) {
		String value =  nodeTagValue(el, tag);
		if (value == null) {
			if (required)
				addMessage("'" + tag + "' must be entered");
			return null;
		}
		return value; 
	}


	/**
	 * @param el
	 * @param tag
	 * @return
	 */
	private String nodeTagValue(Element el, String tag) {
		if (el == null)
			return null;
		NodeList nodeList = el.getElementsByTagName(tag);
		if (nodeList == null)
			return null;
		Node node = nodeList.item(0);
		if (node == null)
			return null;
		
		if (node.getChildNodes().item(0) == null)
			return null;

		return el.getElementsByTagName(tag).item(0).getChildNodes().item(0).getNodeValue();
	}
	

	/**
	 * Get the value from a XML tag inside an element and convert it to integer type
	 * @param el
	 * @param tag
	 * @return
	 */
	protected Integer getValueAsInteger(Element el, String tag, boolean required) {
		String s = getValue(el, tag, required);
		if (s == null) {
			if (required)
				addMessage("'" + tag + "' must be entered");
			return null;
		}
		return Integer.valueOf(s);
	}

	
	/**
	 * Return tag value as a Date object. The value must be in the format YYYY-MM-DD
	 * @param el
	 * @param tag
	 * @return
	 */
	protected Date getValueAsDate(Element el, String tag, boolean required) {
		String s = getValue(el, tag, required);
		if (s == null) {
			if (required)
				addMessage("'" + tag + "' must be entered");
			return null;
		}

		String vals[] = s.split("-");
		
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, Integer.valueOf(vals[0]));
		c.set(Calendar.MONTH, Integer.valueOf(vals[1]) - 1);
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(vals[2]));
		return c.getTime();
	}

	
	/**
	 * Add just a message. Ideally must be overwritten
	 * @param description
	 * @return
	 */
	protected WarnMessage addMessage(String description) {
		return addWarnMessage(null, null, description);
	}


	/**
	 * Add a new warning message from the importing program
	 * @param id
	 * @param name
	 * @param description
	 * @return
	 */
	protected WarnMessage addWarnMessage(String id, String name, String description) {
		WarnMessage msg = new WarnMessage();
		msg.setId(id);
		msg.setName(name);
		msg.setDescription(description);
		warning.add(msg);
		return msg;
	}
	
	public List<WarnMessage> getWarning() {
		return warning;
	}


	public Workspace getWorkspace() {
		return workspace;
	}


	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}


	public MoldovaServiceConfig getConfig() {
		return config;
	}


	public void setConfig(MoldovaServiceConfig config) {
		this.config = config;
	}


	public void setWarning(List<WarnMessage> warning) {
		this.warning = warning;
	}
}
