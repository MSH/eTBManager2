package org.msh.tb.md;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.message.MessageElement;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Patient;
import org.msh.mdrtb.entities.SystemParam;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.DispensingFrequency;
import org.msh.mdrtb.entities.enums.Gender;
import org.msh.tb.md.symeta.Get_casesResponseGet_casesResult;
import org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult;
import org.msh.tb.md.symeta.MDR_TBMISSSoapProxy;
import org.msh.utils.date.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Execute integration between SYMETA system and e-TB Manager.
 * e-Tb Manager calls symeta web-services to retrieve information to be integrated
 * @author Ricardo Memoria
 *
 */
@Name("symetaIntegration")
public class SymetaIntegration {

	public EntityManager entityManager;

	private MoldovaServiceConfig config;
	
	
	/**
	 * Execute the integration between Symeta and e-Tb manager
	 * @throws Exception
	 */
	public void execute() throws Exception {
		readConfig();
		readUnits();
		readCases();
	}
	
	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
	@Transactional
	public void readUnits() throws Exception  {
		int errorCount = 0;
		Element elem = null;
		while (true) {
			try {
				MDR_TBMISSSoapProxy tbmis = new MDR_TBMISSSoapProxy(config.getWebServiceURL());
				
				Get_institutionsResponseGet_institutionsResult res;

				res = tbmis.get_institutions(null);
				elem = res.get_any()[1].getAsDOM();

				break;
			} catch (Exception e) {
				errorCount++;
				if (errorCount > 3) {
					System.out.println("error trying to connect to SYMETA server: " + e.getMessage());
					break;
				}
				else System.out.println("TB-unit impoty: TRY #" + errorCount + ": " + e.getMessage());
			}
		}
		
		if (elem != null)
			importUnits(elem);
	}


	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
	@Transactional
	public void readCases() throws Exception {
		int errorCount = 0;
		Element elem = null;
		while (true) {
			try {
				MDR_TBMISSSoapProxy tbmis = new MDR_TBMISSSoapProxy(config.getWebServiceURL());
				
				Get_casesResponseGet_casesResult res = tbmis.get_cases(null);
				MessageElement[] msgs = res.get_any();
				elem = msgs[1].getAsDOM();
				break;
			} catch (Exception e) {
				errorCount++;
				if (errorCount > 3) {
					System.out.println("error trying to connect to SYMETA server: " + e.getMessage());
					break;
				}
				else System.out.println("Case import: TRY #" + errorCount + ": " + e.getMessage());
			}
		}

		if (elem != null)
			importCases(elem);
	}



	/**
	 * Import the MDR cases from the root element of the XML
	 * @param rootElemen
	 * @throws Exception 
	 */
	protected void importCases(Element rootElemen) throws Exception {
		NodeList nodes = rootElemen.getElementsByTagName("NewDataSet").item(0).getChildNodes();

		int countNew = 0;
		int countCases = 0;

// salva em arquivo texto
/*		FileOutputStream fo = new FileOutputStream("c:\\cases.txt");
		OutputStreamWriter writer = new OutputStreamWriter(fo, "UTF-8");
		writer.write("id,first name,last name,father name,gender,birth date,unit ID\n");
*/		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				boolean bNew = true;
				
				Element el = (Element)n;
				String id = getValue(el, "ID");
				String firstName = getValue(el, "FIRSTNAME");
				String lastName = getValue(el, "LASTNAME");
				String fatherName = getValue(el, "FATHERNAME");
//				String securityId = getValue(el, "PATIENTIDNP");
				Integer gender = getValueAsInteger(el, "GENDER");
				Date birthDate = getValueAsDate(el, "BIRTHDATE_string");
				Date notifDate = getValueAsDate(el, "MDRNOTIFICATIONDATE_string");
//				Date treatDate = getValueAsDate(el, "BEGINTREATMENTDATE_string");
				String unitId = getValue(el, "NOTIFICATIONUNIT");
//				String unitId = getValue(el, "TREATMENTUNIT");

/*				writer.write(id + "," + firstName + "," + lastName + "," + fatherName + "," + gender + "," + birthDate + "," + notifDate + "," + unitId + "\n");
				writer.flush();
*/				
			
				if ((unitId != null) || (notifDate != null)) {
					TbCase tbcase;
					try {
						List<TbCase> lstcases = entityManager.createQuery("from TbCase c " +
								"join fetch c.patient p " +
								"where c.legacyId = :id " +
								"and p.workspace.id = " + getWorkspace().getId().toString())
								.setParameter("id", id)
								.getResultList();
						if (lstcases.size() == 0) {
							tbcase = new TbCase();
							tbcase.setClassification(CaseClassification.MDRTB_DOCUMENTED);
							tbcase.setState(CaseState.WAITING_TREATMENT);
						}
						else {
							tbcase = lstcases.get(0);
							bNew = false;
						}
					} catch (Exception e) {
						tbcase = new TbCase();
						tbcase.setClassification(CaseClassification.MDRTB_DOCUMENTED);
						tbcase.setState(CaseState.WAITING_TREATMENT);
					}
					
					Tbunit unit = loadTBUnit(unitId, null);
					if (unit == null) {
						System.out.println("Importing Case: NO TBUNIT FOUND FOR ID " + unitId + ": case ID=" + id + " (" + firstName + " " + lastName + ")");
						unit = entityManager.merge(config.getDefaultTbunit());
					}
					
					tbcase.setNotificationUnit(unit);
					tbcase.setLegacyId(id);
					tbcase.setDiagnosisDate(notifDate);
					tbcase.setRegistrationDate(notifDate);
//					tbcase.setIniTreatmentDate(treatDate);
					
					Patient p = tbcase.getPatient();
					if (p == null) {
						p = new Patient();
						tbcase.setPatient(p);					
					}
					p.setName(firstName);
					p.setLastName(lastName);
					
					Gender g = null;
					if (gender != null) {
						if (gender == 1)
							 g = Gender.MALE;
						else g = Gender.FEMALE;
					}
					else {
						g = Gender.MALE;
						p.setRecordNumber(123456789);
					}
					
					p.setGender(g);
					p.setBirthDate(birthDate);
//					p.setSecurityNumber(securityId);
					p.setWorkspace(getWorkspace());
					p.setMiddleName(fatherName);

					if (p.getBirthDate() != null)
						tbcase.setAge(DateUtils.yearsBetween(p.getBirthDate(), tbcase.getRegistrationDate()));
					else tbcase.setAge(200);
					
					entityManager.persist(p);
					entityManager.persist(tbcase);
					entityManager.flush();
					
					countCases++;
					if (bNew) 
						countNew++;
				}
				else System.out.println("NOT IMPORTED: " + id + "," + firstName + "," + lastName + "," + fatherName + "," + gender + "," + birthDate + "," + notifDate + "," + unitId + "\n");
			}
		}

		System.out.println("Cases imported: " + countCases + " ... New cases: " + countNew);
	}

	
	/**
	 * Import units from the XML file element
	 * @param elem represents the XML file
	 * @throws Exception 
	 */
	protected void importUnits(Element elem) throws Exception {
		NodeList nodes = elem.getElementsByTagName("NewDataSet").item(0).getChildNodes();
		
//		FileOutputStream fo = new FileOutputStream("c:\\units.txt");
//		OutputStreamWriter writer = new OutputStreamWriter(fo, "UTF-8");
//		writer.write("ID,second ID,unit name,unit name RU,region ID\n");

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				elem = (Element)n; 
//				String id = elem.getElementsByTagName("ID").item(0).getChildNodes().item(0).getNodeValue();
				String id = elem.getElementsByTagName("second_id_in_table").item(0).getChildNodes().item(0).getNodeValue();
				String unitName = elem.getElementsByTagName("UNIT_NAME_RO").item(0).getChildNodes().item(0).getNodeValue();
				String unitNameRu = elem.getElementsByTagName("UNIT_NAME_RU").item(0).getChildNodes().item(0).getNodeValue();
				String regStr = elem.getElementsByTagName("REGION_ID").item(0).getChildNodes().item(0).getNodeValue();
//				System.out.println(id + " - " + unitName + " - " + unitNameRu + " - " + regStr);

//				writer.write(id + "," + secondid + "," + unitName + "," + unitNameRu + "," + regStr + "\n");
				
				Tbunit unit = loadTBUnit(id, unitName);
				if (unit == null)
					unit = new Tbunit();
				
				unit.getName().setName1(unitName);
				unit.getName().setName2(unitNameRu);
				unit.setLegacyId(id);
				unit.setBatchControl(true);
				unit.setChangeEstimatedQuantity(true);
				unit.setDispensingFrequency(DispensingFrequency.MONTHLY);
				unit.setMedicineStorage(true);
				unit.setNumDaysOrder(120);
				unit.setTreatmentHealthUnit(true);
				unit.setWorkspace(getWorkspace());
				unit.setAdminUnit(loadAdminUnit(regStr));
				unit.setHealthSystem(getHealthSystem());
				entityManager.persist(unit);
				entityManager.flush();
			}
		}
//		writer.flush();
//		writer.close();
	}

	
	/**
	 * Load the administrative unit with the corresponding legacy ID. If the record is not found,
	 * the default administrative unit is loaded
	 * @param legacyId
	 * @return
	 */
	protected AdministrativeUnit loadAdminUnit(String legacyId) {
		try {
			return (AdministrativeUnit)entityManager
			.createQuery("from AdministrativeUnit adm where adm.legacyId = :id and adm.workspace.id = :wsid")
			.setParameter("id", legacyId)
			.setParameter("wsid", getWorkspace().getId())
			.getSingleResult();
			
		} catch (Exception e) {
//			System.out.println("Administrative Unit not found. ID = " + legacyId);
		}
		
		return entityManager.merge(config.getDefaultAdminUnit());
	}

	
	/**
	 * Load the health system with the corresponding legacy ID. If the record is not found,
	 * the default health system is loaded
	 * @param legacyId
	 * @return
	 */
	protected HealthSystem loadHealthSystem(String legacyId) {
		try {
			return (HealthSystem)entityManager
			.createQuery("from HealthSystem hs where hs.legacyId = :id and hs.workspace.id=" + getWorkspace().getId().toString())
			.setParameter("id", legacyId)
			.getSingleResult();
			
		} catch (Exception e) {
//			System.out.println("Health System not found. ID = " + legacyId);
		}
		
		return entityManager.merge(config.getDefaultHealthSystem());		
	}


	
	/**
	 * Load the TB Unit with the corresponding legacy ID. If the record is not found,
	 * the default health system is loaded
	 * @param legacyId
	 * @return
	 */
	protected Tbunit loadTBUnit(String legacyId, String name) {
		String hql = "from Tbunit unit where unit.workspace.id = " + getWorkspace().getId().toString(); 

		String restriction = "unit.legacyId = :id";
		if (name != null)
			restriction = "(" + restriction + " or unit.name.name1 = '" + name + "')";
		
		hql += " and " + restriction;
		
		List<Tbunit> lst = entityManager
			.createQuery(hql)
			.setParameter("id", legacyId)
			.getResultList();
			
		return (lst.size() > 0? lst.get(0): null);		
	}
	

	/**
	 * Return the Health System in use in Moldova
	 * @return {@link HealthSystem} instance
	 */
	protected HealthSystem getHealthSystem() {
		return (HealthSystem)entityManager.merge(config.getDefaultHealthSystem());
	}
	

	/**
	 * Returns Moldova workspace
	 * @return
	 */
	protected Workspace getWorkspace() {
		return (Workspace)entityManager.merge(config.getWorkspace());
//		return (Workspace)entityManager.find(Workspace.class, MOLDOVA_WORKSPACEID);
	}

	protected String getValue(Element el, String tag) {
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
	
	protected Integer getValueAsInteger(Element el, String tag) {
		String s = getValue(el, tag);
		if (s == null)
			return null;
		return Integer.valueOf(s);
	}

	
	/**
	 * Return tag value as a Date object. The value must be in the format YYYY-MM-DD
	 * @param el
	 * @param tag
	 * @return
	 */
	protected Date getValueAsDate(Element el, String tag) {
		String s = getValue(el, tag);
		if (s == null)
			return null;
		String vals[] = s.split("-");
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, Integer.valueOf(vals[0]));
		c.set(Calendar.MONTH, Integer.valueOf(vals[1]) - 1);
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(vals[2]));
		return c.getTime();
	}


	public void setConfig(MoldovaServiceConfig config) {
		this.config = config;
	}

	
	/**
	 * Read configuration information from the database
	 */
	@Transactional
	public void readConfig() {
		config = new MoldovaServiceConfig();
		Workspace ws = entityManager.find(Workspace.class, 22564);
		config.setWorkspace(ws);	

		String val = readParameter("symeta_url_webservice");
		config.setWebServiceURL(val);
		
		Integer num = readIntegerParameter("executing_interval");
		config.setInterval(num);
		
		config.setDefaultAdminUnit( entityManager.find(AdministrativeUnit.class, readIntegerParameter("default_adminunit") ));
		config.setDefaultTbunit( entityManager.find(Tbunit.class, readIntegerParameter("default_tbunit") ));
		config.setDefaultHealthSystem( entityManager.find(HealthSystem.class, readIntegerParameter("default_healthsystem") ));
	}


	/**
	 * Read a parameter and convert it to an integer value
	 * @param param parameter
	 * @return integer value of the parameter
	 */
	protected Integer readIntegerParameter(String param) {
		String val = readParameter(param);
		return Integer.parseInt(val);
	}
	
	
	/**
	 * Read a string parameter from the database
	 * @param param name of the parameter
	 * @return parameter value
	 */
	protected String readParameter(String param) {
		SystemParam sysparam = (SystemParam)entityManager
			.createQuery("from SystemParam sp where sp.workspace.id = :id and sp.key = :param")
			.setParameter("id", config.getWorkspace().getId())
			.setParameter("param", param)
			.getSingleResult();
		return sysparam.getValue();
	}



	public MoldovaServiceConfig getConfig() {
		return config;
	}
	
	public void readXML() throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		Document doc = docFactory.newDocumentBuilder().parse(new File("C:\\Projetos\\MSH\\Moldova\\Database\\units.xml"));
		Element elem = doc.getDocumentElement();
		importUnits(elem);
	}


}
