package org.msh.tb.ge;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.jboss.seam.Component;
import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.application.tasks.TaskStatus;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.UserLog;
import org.msh.tb.entities.UserRole;
import org.msh.tb.entities.WorkspaceLog;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.ge.entities.IntegrationHistory;

/**
 * Importing of DR-TB cases from tbgeorgia system to eTB Manager
 * @author Utkarsh Srivastava
 *
 */
public class ImportTBGeoTask extends DbBatchTask {

	/**
	 * Constants used during importing
	 */
	private static final int regimenId = 940665;
	private static final int sourceId = 22107;

	private static final int healthSystemId = 904;

	private IntegrationHistQuery integrationHistQuery;
	private IntegrationHistHome integrationHistHome;

	private EntityManager entityManager;
	private DataSource tbGeoDataSource;
	private Connection connection;
	private ResultSet rsCases;
	private CountryStructure structure;

	private HealthSystem healthSystem;
	private Regimen regimen;
	private Source source;
	private List<Substance> substances;
	private List<Medicine> medicines;
	private int noOfRecord = 0;
	private Date lastIntDate;

	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#starting()
	 */
	@Override
	protected void starting() {
		setCommitCounter(1);
		
		connection = getConnection();
		entityManager = getEntityManager();
		
		if (getWorkspace() == null) {
			cancel();
			addLogMessage("No workspace defined");
		}
		
		if (isCanceling())
			return;
		
		integrationHistQuery = (IntegrationHistQuery)Component.getInstance("integrationHistQuery", true);
		integrationHistHome = (IntegrationHistHome)Component.getInstance("integrationHistHome", true);
		
		try{
			beginTransaction();
			getLastIntDate();
			getHealthSystem();
			commitTransaction();
		}catch (Exception e){
			e.printStackTrace();
			rollbackTransaction();
		}
	}

	/* 
	 *  gets the date of the last integration done
	 *  return date
	 */
	private Date getLastIntDate(){
		if(lastIntDate==null){
			List<IntegrationHistory> intHistList = integrationHistQuery.getResultList();
			if(intHistList.size()>0){
				lastIntDate = intHistList.get(0).getLastIntegrationDate();
			}else{
				//no last integration value present the default it to 1900 denoting no integration happened ever.
				Calendar c = Calendar.getInstance();
				c.set(1900, 01, 01);
				lastIntDate = c.getTime();
			}
		}
		return lastIntDate;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		try {
			if (connection != null)
				connection.close();
			integrationHistHome.clearInstance();
			IntegrationHistory intHistory = new IntegrationHistory();
			intHistory.setLastIntegrationDate(new Date());
			intHistory.setNoOfRecords(noOfRecord);
			intHistory.setUser(getUser());
			intHistory.setWorkspace(getWorkspace());
			integrationHistHome.setInstance(intHistory);
			integrationHistHome.persist();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addLogMessage(Integer.toString(getRecordIndex()) + " casos importados.");
		
		registerLog();
	}


	
	@Override
	protected void execute() {
		try {
			//Upload region data
//			RegionData regionData = new RegionData(lastIntDate, getStructure());
//			regionData.uploadRegionData(lastIntDate, getConnection(), rsCases, getWorkspace(), noOfRecord, entityManager);
			
			TbUnitData tbunitData = new TbUnitData(lastIntDate, getHealthSystem());
			tbunitData.uploadRegionData(lastIntDate, getConnection(), rsCases, getWorkspace(), noOfRecord, entityManager);
	
		} catch (Exception e) {
			exceptionHandler(e);
			e.printStackTrace();
		}
		finally {
			try {
				rsCases.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * Return substance by its abbreviated name
	 * @param abbrevName
	 * @return {@link Substance} with same abbreviated name, or null, if it doesn't exist
	 */
	protected Substance substanceByAbbrevName(String abbrevName) {
		for (Substance s: getSubstances()) {
			if (abbrevName.equalsIgnoreCase(s.getAbbrevName().toString())) {
				return s;
			}
		}
		return null;
	}


	protected HealthSystem getHealthSystem() {
		if (healthSystem == null) {
			healthSystem = entityManager.find(HealthSystem.class, healthSystemId);
		}
		return healthSystem;
	}


	protected Regimen getRegimen() {
		if (regimen == null) {
			regimen = entityManager.find(Regimen.class, regimenId);
		}
		return regimen;
	}
	
	protected Source getSource() {
		if (source == null)
			source = entityManager.find(Source.class, sourceId);
		return source;
	}
	
	
	/**
	 * Return list of substances
	 * @return
	 */
	protected List<Substance> getSubstances() {
		if (substances == null) {
			substances = entityManager
				.createQuery("from Substance s where s.workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return substances;
	}


	/**
	 * Return list of medicines to be used during importing
	 * @return
	 */
	public List<Medicine> getMedicines() {
		if (medicines == null) {
			medicines = entityManager
				.createQuery("from Medicine m where m.workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return medicines;
	}


	public Medicine findMedicineByLegacyId(String legacyId) {
		for (Medicine m: getMedicines()) {
			if ((m.getLegacyId() != null) && (m.getLegacyId().equals(legacyId)))
				return m;
		}
		return null;
	}


	/**
	 * Return a JDBC connection to Brazilian "Sistema TBMR" database
	 * @return
	 */
	protected Connection getConnection() {
		if (tbGeoDataSource == null) {
			try {
				InitialContext initialContext = new InitialContext();
				tbGeoDataSource = (DataSource)initialContext.lookup("java:tbgeoDatasource");
			} catch (NamingException e) {
				e.printStackTrace();
				return null;
			}
		}

		try {
			return tbGeoDataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}			
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		if (getStatus() == TaskStatus.RUNNING)
			 return "Importing Regions : " + getRecordIndex() + " " + getRecordCount();
		else return super.getDisplayName();
	}

	
	protected void registerLog() {
		UserRole role = entityManager.find(UserRole.class, 168);
		TransactionLog log = new TransactionLog();
		log.setAction(RoleAction.EXEC);
		log.setComments("Regions Imported: " + getRecordCount());
		log.setEntityDescription("Importing Regions from TB Georgia");
		log.setRole(role);
		log.setTransactionDate(new Date());
		
		UserLog user = entityManager.find(UserLog.class, getUser().getId());
		log.setUser(user);
		
		WorkspaceLog ws = entityManager.find(WorkspaceLog.class, getWorkspace().getId());
		log.setWorkspace(ws);
		
		beginTransaction();
		try {
			entityManager.persist(log);
			commitTransaction();
		} catch (Exception e) {
			e.printStackTrace();
			rollbackTransaction();
		}
	}

	public CountryStructure getStructure() {
		if(structure==null)
			structure = (CountryStructure) entityManager.createQuery("from CountryStructure c where c.level = 1 and c.workspace.id = :workspaceId")
			.setParameter("workspaceId", getWorkspace().getId())
			.getSingleResult();
		return structure;
	}

	@Override
	protected boolean processBatchRecord() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
}
