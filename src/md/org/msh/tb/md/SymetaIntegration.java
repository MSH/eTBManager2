package org.msh.tb.md;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.axis.message.MessageElement;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.User;
import org.msh.tb.md.symeta.Get_casesResponseGet_casesResult;
import org.msh.tb.md.symeta.Get_institutionsResponseGet_institutionsResult;
import org.msh.tb.md.symeta.Get_localitiesResponseGet_localitiesResult;
import org.msh.tb.md.symeta.Get_regionsResponseGet_regionsResult;
import org.msh.tb.md.symeta.MDR_TBMISSSoapProxy;
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

	@In EntityManager entityManager;

	private MoldovaServiceConfig config;
	private List<String> newCases;
	private List<WarnMessage> warnings = new ArrayList<WarnMessage>();
	private int countCasesRead;
	private int countNewCases;
	private int countCases;
	private int countNewUnits;
	private int countUnits;
	private List<String> emails = new ArrayList<String>();
	private UserTransaction userTx;


	/**
	 * Execute the integration between Symeta and e-Tb manager
	 * @throws Exception
	 */
	@Asynchronous
	public void execute(MoldovaServiceConfig config, User user) throws Exception {
		this.config = config;
		
		Contexts.getEventContext().set("defaultWorkspace", config.getWorkspace());
		
		if (config.isWebServiceURLEmpty())
			addWarning("WEB Service URL not found. Please go to e-TB Manager | Administration | SYMETA setup");
		
		if (config.getWorkspace() == null)
			addWarning("Workspace to import was not setup. Please go to e-TB Manager | Administration | SYMETA setup");

		userTx = (UserTransaction) org.jboss.seam.Component.getInstance("org.jboss.seam.transaction.transaction");
		userTx.setTransactionTimeout(10 * 60);  //set timeout to 60 * 10 = 600 secs = 10 mins
		userTx.begin();
		entityManager.joinTransaction();
		try {
			if (warnings.size() == 0) {
				readRegions();
				
				userTx.commit();
				userTx.begin();
				
				readLocalities();
				
				userTx.commit();
				userTx.begin();
				
//				readUnits();
				readCases();
				
				userTx.commit();
			}
		} catch (Exception e) {
			addWarning("FATAL ERROR: " + e.getClass().toString() + ": " + e.getMessage());
			e.printStackTrace();
			userTx.rollback();
		}
		
		if (user != null)
			emails.add(user.getEmail());

		sendReportMessage();
	}

	
	protected void commitAndRestart() throws Exception {
		userTx.commit();
		entityManager.clear();
		userTx.begin();
	}
	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
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


	protected void readLocalities() throws Exception {
		int errorCount = 0;
		Element elem = null;
		while (true) {
			try {
				MDR_TBMISSSoapProxy tbmis = new MDR_TBMISSSoapProxy(config.getWebServiceURL());
				
				Get_localitiesResponseGet_localitiesResult res = tbmis.get_localities(null);
				MessageElement[] msgs = res.get_any();
				elem = msgs[1].getAsDOM();
				break;
			} catch (Exception e) {
				errorCount++;
				if (errorCount > 3) {
					System.out.println("error trying to connect to SYMETA server: " + e.getMessage());
					break;
				}
				else System.out.println("Locality import: TRY #" + errorCount + ": " + e.getMessage());
			}
		}

		if (elem != null)
			importLocalities(elem);		
	}
	

	protected void readRegions() throws Exception {
		int errorCount = 0;
		Element elem = null;
		while (true) {
			try {
				MDR_TBMISSSoapProxy tbmis = new MDR_TBMISSSoapProxy(config.getWebServiceURL());
				
				Get_regionsResponseGet_regionsResult res = tbmis.get_regions(null);
				MessageElement[] msgs = res.get_any();
				elem = msgs[1].getAsDOM();
				break;
			} catch (Exception e) {
				errorCount++;
				if (errorCount > 3) {
					System.out.println("error trying to connect to SYMETA server: " + e.getMessage());
					break;
				}
				else System.out.println("Locality import: TRY #" + errorCount + ": " + e.getMessage());
			}
		}

		if (elem != null)
			importRegions(elem);		
	}
	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
	protected void readCases() throws Exception {
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
	 * Import localities from SYMETB
	 * @param rootElemen
	 * @throws Exception
	 */
	protected void importLocalities(Element rootElemen) throws Exception {
		NodeList nodes = rootElemen.getElementsByTagName("NewDataSet").item(0).getChildNodes();
		
		AdminUnitImporting adminUnitImporting = new AdminUnitImporting();
		adminUnitImporting.setWarning(warnings);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				adminUnitImporting.importLocality((Element)n, config.getWorkspace());
			}
		}
	}

	
	/**
	 * Import localities from SYMETB
	 * @param rootElemen
	 * @throws Exception
	 */
	protected void importRegions(Element rootElemen) throws Exception {
		NodeList nodes = rootElemen.getElementsByTagName("NewDataSet").item(0).getChildNodes();
		
		AdminUnitImporting adminUnitImporting = new AdminUnitImporting();
		adminUnitImporting.setWarning(warnings);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				adminUnitImporting.importRegion((Element)n, config.getWorkspace());
			}
		}
	}
	

	/**
	 * Import the MDR cases from the root element of the XML
	 * @param rootElemen
	 * @throws Exception 
	 */
	protected void importCases(Element rootElemen) throws Exception {
		NodeList nodes = rootElemen.getElementsByTagName("NewDataSet").item(0).getChildNodes();

		countNewCases = 0;
		countCases = 0;
		
		newCases = new ArrayList<String>();

		CaseImporting caseImporting = new CaseImporting();
		caseImporting.setWarning(warnings);
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element)n;
				if (caseImporting.importCase(el, config.getWorkspace())) {
					countCases++;
					
					if (caseImporting.isNewCase()) {
						countNewCases++;
						TbCase tbcase = caseImporting.getTbcase();
						newCases.add("(" + tbcase.getLegacyId() + ") " + tbcase.getPatient().getFullName());
					}
				}
				countCasesRead++;
			}
		}
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

/*		for (int i = 0; i < nodes.getLength(); i++) {
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
*/
//		writer.flush();
//		writer.close();
	}


	/**
	 * Check if there is something to report
	 * @return
	 */
	public boolean isHasReportContent() {
		return (countCases > 0) || (countNewCases > 0) || (countUnits > 0) || (countNewUnits > 0) || (getWarnings().size() > 0);
	}


	/**
	 * Send a new report message to the report e-mail
	 */
	public void sendReportMessage() {
		if (!isHasReportContent())
			return;
		
		String emailReport = config.getEmailReport();
		if ((emailReport != null) && (!emailReport.isEmpty())) {
			String[] msgs = emailReport.split(",");
			for (String s: msgs) {
				if (!emails.contains(s))
					emails.add(s);
			}
		}
		
		for (String email: emails) {
			User user = new User();
			user.setEmail(email);
			
			Contexts.getEventContext().set("user", user);
			
			Renderer.instance().render("/custom/md/mail/importreport.xhtml");
		}
	}


	public MoldovaServiceConfig getConfig() {
		return config;
	}
	
/*	public void readXML() throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		Document doc = docFactory.newDocumentBuilder().parse(new File("C:\\Projetos\\MSH\\Moldova\\Database\\units.xml"));
		Element elem = doc.getDocumentElement();
		importUnits(elem);
	}
*/

	public List<String> getNewCases() {
		return newCases;
	}


	public List<WarnMessage> getWarnings() {
		return warnings;
	}


	public int getCountNewCases() {
		return countNewCases;
	}


	public int getCountCases() {
		return countCases;
	}


	public int getCountNewUnits() {
		return countNewUnits;
	}


	public int getCountUnits() {
		return countUnits;
	}

	public void addWarning(String description) {
		WarnMessage msg = new WarnMessage();
		msg.setDescription(description);
		warnings.add(msg);
	}


	public List<String> getEmails() {
		return emails;
	}


	public int getCountCasesRead() {
		return countCasesRead;
	}
}
