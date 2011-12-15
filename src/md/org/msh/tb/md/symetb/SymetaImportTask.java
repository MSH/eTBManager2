package org.msh.tb.md.symetb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.jboss.seam.Component;
import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.md.AdminUnitImporting;
import org.msh.tb.md.CaseImporting;
import org.msh.tb.md.MoldovaServiceConfig;
import org.msh.tb.md.WarnMessage;
import org.msh.tb.md.symetb.wsdlinterface.Get_casesResponseGet_casesResult;
import org.msh.tb.md.symetb.wsdlinterface.Get_cases_classicResponseGet_cases_classicResult;
import org.msh.tb.md.symetb.wsdlinterface.Get_localitiesResponseGet_localitiesResult;
import org.msh.tb.md.symetb.wsdlinterface.Get_regionsResponseGet_regionsResult;
import org.msh.tb.md.symetb.wsdlinterface.MDR_TBMISSSoapProxy;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SymetaImportTask extends DbBatchTask {

	private MoldovaServiceConfig config;
	private int countCasesRead;
	private int countNewCases;
	private int countCases;
	private List<String> newCases;
	private int batchBlock;
	private int batchBlockCount;

	
	// interface used to execute web service call
	protected interface WSCallBack {
		MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception;
	}

	// list of warnings generated during importing
	private List<WarnMessage> warnings = new ArrayList<WarnMessage>();


	@Override
	public void execute() {
		setAutomaticProgress(false);
		batchBlock = 0;
		batchBlockCount = 4;

		try {
			executeRegions();
			executeLocalities();
//			executeUnits();
			executeCases();
			executeCasesClassic();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#starting()
	 */
	@Override
	protected void starting() {
		if (!initConfigParams())
			return;

		System.out.println("Starting Symeta integration...");
		System.out.println("* WEB service URL = " + config.getWebServiceURL());
		System.out.println("* Workspace = " + config.getWorkspace().getName().toString());
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		try {
			FileWriter fw = new FileWriter("c:\\rmemoria\\simetb.txt");
			PrintWriter p = new PrintWriter(fw);
			
			p.println("Num cases read = " + countCasesRead);
			p.println("Num new cases  = " + countNewCases);
			p.println("Num cases      = " + countCases);
			for (WarnMessage s: warnings)
				p.println(s.getId() + " - " + s.getName() + " - " +  s.getDescription());
			
			p.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load initial configuration
	 * @return true if configuration was successfully loaded
	 */
	protected boolean initConfigParams() {
		SymetaIntHome home = (SymetaIntHome)Component.getInstance("symetaIntHome", true);
		config = home.getConfig();
		return config.getWorkspace().getId() != null;
	}


	/**
	 * Execute importing of regions into the system
	 * @throws Exception
	 */
	protected void executeRegions() throws Exception {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_regionsResponseGet_regionsResult res = proxy.get_regions(null);
				return res.get_any();
			}
		});

		if (nodes != null)
			importRegions(nodes);		
	}


	/**
	 * Execute importing of localities into the system
	 * @throws Exception
	 */
	protected void executeLocalities() throws Exception {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_localitiesResponseGet_localitiesResult res = proxy.get_localities(null);
				return res.get_any();
			}
		});

		if (nodes != null)
			importLocalities(nodes);		
	}
	

	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
/*	public void importUnits() throws Exception  {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_institutionsResponseGet_institutionsResult res = proxy.get_institutions(null);
				return res.get_any();
			}
		});
		
		if (nodes != null)
			importUnits(nodes);
	}
*/

	
	/**
	 * Read DR-TB cases from the SYMETA server
	 * @throws Exception 
	 */
	protected void executeCases() throws Exception {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_casesResponseGet_casesResult res = proxy.get_cases(null);
				return res.get_any();
			}
		});

		if (nodes != null)
			importCases(nodes, CaseClassification.DRTB);
	}


	/**
	 * Read TB cases from the SYMETA server
	 * @throws Exception 
	 */
	protected void executeCasesClassic() throws Exception {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_cases_classicResponseGet_cases_classicResult res = proxy.get_cases_classic(null);
				return res.get_any();
			}
		});

		if (nodes != null)
			importCases(nodes, CaseClassification.TB);
	}


	/**
	 * Import the MDR cases from the root element of the XML
	 * @param rootElemen
	 * @throws Exception 
	 */
	protected void importCases(final NodeList nodes, final CaseClassification classification) throws Exception {
		if (newCases == null) {
			newCases = new ArrayList<String>();
			countNewCases = 0;
			countCases = 0;
		}

		final CaseImporting caseImporting = new CaseImporting();
		caseImporting.setWarning(warnings);

		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				int index = getRecordIndex();
				Node n = nodes.item(index);
				
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element el = (Element)n;
					if (caseImporting.importCase(el, config.getWorkspace(), classification)) {
						countCases++;
						
						if (caseImporting.isNewCase()) {
							countNewCases++;
							/* TbCase tbcase = */ caseImporting.getTbcase();
//							newCases.add("(" + tbcase.getLegacyId() + ") " + tbcase.getPatient().getFullName());
						}
					}
					countCasesRead++;
				}

				int count = nodes.getLength();
				updateProgress(nodes.getLength(), index, 67);

				return getRecordIndex() < count - 1;
			}
		});

//		for (WarnMessage w: caseImporting.getWarning())
//			newCases.add("****  " + w.getId() + " - " + w.getName() + " - " + w.getDescription());
	}

	
	/**
	 * Import localities from SYMETB
	 * @param rootElemen
	 * @throws Exception
	 */
	protected void importRegions(final NodeList nodes) throws Exception {
		final AdminUnitImporting adminUnitImporting = new AdminUnitImporting();
		adminUnitImporting.setWarning(warnings);
		
		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				int index = getRecordIndex();
				Node n = nodes.item(index);
				
				if (n.getNodeType() == Node.ELEMENT_NODE)
					adminUnitImporting.importRegion((Element)n, config.getWorkspace());

				int count = nodes.getLength();
				updateProgress(nodes.getLength(), index, 0);

				return getRecordIndex() < count - 1;
			}
		});
	}
	

	/**
	 * Import localities from SYMETB
	 * @param rootElemen
	 * @throws Exception
	 */
	protected void importLocalities(final NodeList nodes) throws Exception {
		final AdminUnitImporting adminUnitImporting = new AdminUnitImporting();
		adminUnitImporting.setWarning(warnings);
		
		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				int index = getRecordIndex();
				Node n = nodes.item(index);
				
				if (n.getNodeType() == Node.ELEMENT_NODE)
					adminUnitImporting.importLocality((Element)n, config.getWorkspace());

				int count = nodes.getLength();
				updateProgress(nodes.getLength(), index, 34);

				return getRecordIndex() < count - 1;
			}
		});
	}


	/**
	 * Connect to the web service and return the XML data
	 * @param wscall is a callback interface in charge of executing the method call 
	 * @return XML data in a {@link Element} format
	 */
	protected NodeList executeWSCall(WSCallBack wscall) {
		int errorCount = 0;
		Element elem = null;
		while (true) {
			try {
				MDR_TBMISSSoapProxy tbmis = new MDR_TBMISSSoapProxy(config.getWebServiceURL());

				MessageElement[] msgs = wscall.getData(tbmis);
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

		return elem.getElementsByTagName("NewDataSet").item(0).getChildNodes();
	}


	/**
	 * Update information about the progress of the importing
	 * @param total
	 * @param index
	 * @param offset
	 */
	protected void updateProgress(int total, int index, int offset) {
		setProgress(Math.round(offset + ((float)(index * 33)) / (float)total));
	}
	
	/**
	 * Used to generate the progress
	 */
	protected void startNewBatchBlock() {
		batchBlock++;
	}
}
