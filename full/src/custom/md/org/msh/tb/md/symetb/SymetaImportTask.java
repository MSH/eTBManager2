package org.msh.tb.md.symetb;

import org.apache.axis.message.MessageElement;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.md.*;
import org.msh.tb.md.symetb.wsdlinterface.*;
import org.msh.tb.transactionlog.TransactionLogService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SymetaImportTask extends DbBatchTask {

	private MoldovaServiceConfig config;
	private int batchBlock;
	private int batchBlockCount;
	private ImportingBase importingBase;
	private StringBuffer log = new StringBuffer();

	
	// interface used to execute web service call
	protected interface WSCallBack {
		MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception;
	}


	@Override
	public void execute() {
		setAutomaticProgress(false);
		batchBlock = 0;
		batchBlockCount = 5;

		try {
			executeRegions();
			executeLocalities();
			executeUnits();
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

		addLog("* WEB service URL = " + config.getWebServiceURL());
		addLog("* Workspace = " + config.getWorkspace().getName().toString());

		// initializing context variables
		User user = getUser();
		UserWorkspace ws = (UserWorkspace)getEntityManager().createQuery("from UserWorkspace ws where ws.user.id = :uid and ws.workspace.id = :id")
			.setParameter("uid", user.getId())
			.setParameter("id", getWorkspace().getId())
			.getSingleResult();
		Contexts.getEventContext().set("userWorkspace", ws);
		// avoid lazy initialization problems
		ws.getTbunit().getAdminUnit().getId();
		UserLogin userLogin = new UserLogin();
		userLogin.setUser(user);
		userLogin.setWorkspace(getWorkspace());
		Contexts.getEventContext().set("userLogin", userLogin);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		beginTransaction();
		
		// save result in a log file
		TransactionLogService service = new TransactionLogService();
		service.getDetailWriter().addText(log.toString());
		service.save("TASK", RoleAction.EXEC, "SYMETB Integration", null, null, null);
		commitTransaction();
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
			executeImporting(nodes, RegionImporting.class, "IMPORTING REGIONS");
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
			executeImporting(nodes, LocalityImporting.class, "IMPORTING LOCALITIES");
	}
	

	
	/**
	 * Read units from the SYMETA server
	 * @throws Exception 
	 */
	public void executeUnits() throws Exception  {
		NodeList nodes = executeWSCall(new WSCallBack() {
			public MessageElement[] getData(MDR_TBMISSSoapProxy proxy) throws Exception {
				Get_institutionsResponseGet_institutionsResult res = proxy.get_institutions(null);
				return res.get_any();
			}
		});
		
		if (nodes != null)
			executeImporting(nodes, UnitImporting.class, "IMPORTING UNITS");
	}


	
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
			executeImporting(nodes, DRTBCaseImporting.class, "IMPORTING DR-TB CASES");
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
			executeImporting(nodes, TBCaseImporting.class, "IMPORTING TB CASES");
	}

	
	/**
	 * Execute the importing of an specific group of records in a XML node
	 * @param nodes
	 * @param clazz
	 * @param title
	 */
	protected void executeImporting(final NodeList nodes, Class<? extends ImportingBase> clazz, String title) {
		try {
			importingBase = clazz.newInstance();
			importingBase.setConfig(config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				int index = getRecordIndex();
				Node node = nodes.item(index);

				importingBase.processNode(node);

				int count = nodes.getLength();
				updateProgress(nodes.getLength(), index);

				return getRecordIndex() < count - 1;
			}
		});
		
		// check if there is something to log
//		if ((importingBase.getNewRecords() > 0) || (importingBase.getErrorsRecords() > 0)) {
			addLog("");
			addLog(">>" + title);
			addLog("   * Records read: " + importingBase.getImportedRecords());
			addLog("   * New records : " + importingBase.getNewRecords());
			addLog("   * Errors: " + importingBase.getErrorsRecords());
			addLog("");
			if (importingBase.getErrors().size() > 0) {
				for (String msg: importingBase.getErrors()) {
					addLog(msg);
				}
				addLog("");
//			}
		}
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

				batchBlock++;
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
	protected void updateProgress(int total, int index) {
		float dx = 1F / (float)batchBlockCount;
		
		float pos = ((batchBlock - 1) * dx) + ((float)index / (float)total) * dx;

		setProgress(Math.round(pos * 100));
	}
	
	/**
	 * Used to generate the progress
	 */
	protected void startNewBatchBlock() {
		batchBlock++;
	}
	
	
	/**
	 * Add log message
	 * @param s
	 */
	public void addLog(String s) {
		log.append(s);
		log.append('\n');
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#exceptionHandler(java.lang.Exception)
	 */
	@Override
	protected void exceptionHandler(Exception e) {
		super.exceptionHandler(e);
		addLog("Error: " + e.getLocalizedMessage());
		e.printStackTrace();
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#processBatchRecord()
	 */
	@Override
	protected boolean processBatchRecord() throws Exception {
		// This method is not used in this class, just implemented because it is abstract in the parent class
		return false;
	}
}
