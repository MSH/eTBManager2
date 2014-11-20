package org.msh.tb.md;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Workspace;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class ImportingBase {

	private int importedRecords;
	private int newRecords;
	private int errorsRecords;
	
	private boolean errorOnCurrentImport;
	
	private List<String> errors = new ArrayList<String>();
	
	/**
	 * Configuration of the importing
	 */
	private MoldovaServiceConfig config;
	
	/**
	 * Constructor for importing
	 * @param config
	 */
	public ImportingBase(MoldovaServiceConfig config) {
		this.config = config;
		Contexts.getEventContext().set("defaultWorkspace", config.getWorkspace());
	}
	
	public ImportingBase() {
		super();
	}

	public void processNode(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			errorOnCurrentImport = false;
			
			boolean isNew = importRecord((Element)node);

			if (!errorOnCurrentImport) {
				importedRecords++;
				if (isNew)
					newRecords++;
			}
			else errorsRecords++;
			
		}
	}
	
	/**
	 * Execute the importing of the given record represented by the element
	 * @param xmlLocData
	 * @return true if it's a new record, or false if the record already exists
	 */
	protected abstract boolean importRecord(Element xmlLocData);
	
	
	/**
	 * Add an error message to the record being imported
	 * @param msg
	 */
	protected void addError(String msg) {
		errorOnCurrentImport = true;
		errors.add(msg);
	}
	
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
				addError("'" + tag + "' must be entered");
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
				addError("'" + tag + "' must be informed");
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
				addError("'" + tag + "' must be entered");
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



	public Workspace getWorkspace() {
		return config.getWorkspace();
	}


	public MoldovaServiceConfig getConfig() {
		return config;
	}


	public void setConfig(MoldovaServiceConfig config) {
		this.config = config;
	}


	/**
	 * @return the importedRecords
	 */
	public int getImportedRecords() {
		return importedRecords;
	}

	/**
	 * @return the newRecords
	 */
	public int getNewRecords() {
		return newRecords;
	}

	/**
	 * @return the errorsRecords
	 */
	public int getErrorsRecords() {
		return errorsRecords;
	}

	/**
	 * @return the errorOnCurrentImport
	 */
	public boolean isErrorOnCurrentImport() {
		return errorOnCurrentImport;
	}

	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}
}
