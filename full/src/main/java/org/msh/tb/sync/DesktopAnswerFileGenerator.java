/**
 * 
 */
package org.msh.tb.sync;

import com.rmemoria.datastream.*;
import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.login.UserSession;
import org.msh.utils.DataStreamUtils;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author Ricardo Memoria
 *
 */
@Name("desktopAnswerFileGenerator")
public class DesktopAnswerFileGenerator implements ObjectProvider, DataInterceptor {

    /**
     * Indicate the block of data being processed by getNextList()
     */
    protected enum ListStep { QUERY, NEWKEYS, VERSIONS, DELETEDENTITIES };

	@In EntityManager entityManager;

	// maximum number of records per query
	private int MAX_RESULTS = 200;
	
	private Integer unitId;
	private Integer workspaceId;
	private StreamContext context;
	private int queryIndex;
	private int recordIndex;
	private int firstResult;
	private List list;
	private List<String> hqls;
	private EntityManager em;
	private boolean initialized;
	private ServerSignature serverSignature;
	private int unitLinkIndex = -1;
	// if defined, the system will generate just differences between the client versions
	// and the server versions
	private List<EntityLastVersion> entityVersions;
	private EntityKeyList keyList;
	private List<EntityLastVersion> clientEntityVersions;
    private ListStep listStep;
    private List<DeletedEntity> deletedEntities = new ArrayList<DeletedEntity>();
	
	
	public DesktopAnswerFileGenerator() {
//		MAX_RESULTS = 50;
		hqls = new ArrayList<String>();

		hqls.add("from UserProfile a where a.workspace.id = :wsid");
		hqls.add("from CountryStructure a where a.workspace.id = :wsid");
		hqls.add("from AdministrativeUnit a where a.workspace.id = :wsid order by code");
		hqls.add("from HealthSystem a where a.workspace.id = :wsid");
		hqls.add("from Source a where a.workspace.id = :wsid");
		hqls.add("from Tbunit a where a.workspace.id = :wsid ");
		hqls.add("select a.id, a.authorizerUnit.id, a.firstLineSupplier.id, a.secondLineSupplier.id from Tbunit a where a.workspace.id = :wsid");
		unitLinkIndex = hqls.size() - 1;
		hqls.add("from Substance a where a.workspace.id = :wsid");
		hqls.add("from Medicine a where a.workspace.id = :wsid");
		hqls.add("from Regimen a where a.workspace.id = :wsid");
		hqls.add("from Laboratory a where a.workspace.id = :wsid");
		hqls.add("from FieldValue a where a.workspace.id = :wsid");
		hqls.add("from UserWorkspace a join fetch a.user left join fetch a.adminUnit where a.tbunit.id = :wsid");
//		hqls.add("from TbCase a join fetch a.patient where a.ownerUnit.id = #{desktopIniGeneratorService.unitId}");

		// case data
		hqls.add("from TbCase a join fetch a.patient left join fetch a.regimen left join fetch a.notifAddress.adminUnit "
				+ "where a.ownerUnit.id = :unitid");
		//hqls.add("from PrescribedMedicine a join fetch a.tbcase join fetch a.medicine join fetch a.source where a.tbcase.ownerUnit.id = :unitid");
		//hqls.add("from TreatmentHealthUnit a join fetch a.tbunit join fetch a.tbcase where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamCulture a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamXpert a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamMicroscopy a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from MedicalExamination a join fetch a.tbcase where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamHIV a join fetch a.tbcase where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamXRay a join fetch a.tbcase left join fetch a.presentation where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from ExamDST a join fetch a.tbcase where a.tbcase.ownerUnit.id = :unitid");
		//hqls.add("from ExamDSTResult a join fetch a.substance join fetch a.exam where a.exam.tbcase.ownerUnit.id = :unitid");
		//hqls.add("from TreatmentMonitoring a join fetch a.tbcase where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from TbContact a join fetch a.tbcase left join fetch a.contactType left join fetch a.conduct where a.tbcase.ownerUnit.id = :unitid");
		hqls.add("from CaseSideEffect a join fetch a.tbcase left join fetch a.substance left join fetch a.substance2 where a.tbcase.ownerUnit.id = :unitid");
		//hqls.add("from CaseComorbidity a join fetch a.tbcase left join fetch a.comorbidity where a.tbcase.ownerUnit.id = :unitid");

		addSpecificWorkspacesEntities();

		hqls.add("from DeletedEntity");
	}

	private void addSpecificWorkspacesEntities(){
		//Specific entities of Bangladesh
		if("bd".equals(UserSession.getWorkspace().getExtension())){
			hqls.add("from ExamSkin a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = :unitid");
			hqls.add("from ExamBiopsy a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = :unitid");
		}
	}

	
	/**
	 * Generate the client initialization file
	 */
	public void generateFile(Tbunit unit, OutputStream out) {
		// initialize variables
		unitId = unit.getId();
		workspaceId = unit.getWorkspace().getId();

		processDeletedEntityVersion();

        // initial state of the list of objects to serialize
        list = null;
        listStep = ListStep.QUERY;

		initialized = false;

		context = DataStreamUtils.createContext("clientinifile-schema.xml");
		context.addInterceptor(this);
		addConverter(context);
		DataMarshaller m = DataStreamUtils.createXMLMarshaller(context);

		try {
			// adjust name of the unit
			GZIPOutputStream outzip = new GZIPOutputStream(out);
			try{
				m.marshall(outzip, this);
			}
			finally {
				outzip.finish();
				outzip.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if(unitId == null)
			throw new RuntimeException("unit id should not be null");

		Tbunit synchronizedUnit = App.getEntityManager().find(Tbunit.class, unitId);
		synchronizedUnit.setLastSyncDate(DateUtils.getDate());
		App.getEntityManager().merge(synchronizedUnit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object getObjectToSerialize(int index) {
		if (serverSignature == null)
			return getServerSignature();

		if (!initialized) {
			// get first query
			queryIndex = 0;
			
			em = App.getEntityManager();

			initialized = true;

			// return workspace as the initial item
			return Component.getInstance("defaultWorkspace");
		}

		return getObject();
	}

	/**
	 * Return the server signature to be sent to the desktop
	 * @return instance of {@link ServerSignature}
	 */
	protected ServerSignature getServerSignature() {
		if (serverSignature == null) {
			serverSignature = new ServerSignature();
			SystemConfig config = EtbmanagerApp.instance().getConfiguration();
			serverSignature.setPageRootURL(config.getPageRootURL());
			serverSignature.setSystemURL(config.getSystemURL());
			serverSignature.setCountryCode(EtbmanagerApp.instance().getCountryCode());
			serverSignature.setAdminMail(config.getAdminMail());
		}
		return serverSignature;
	}

	/**
	 * Return the object from the list
	 */
	private Object getObject() {
		if (list == null) {
			queryIndex = -1;
			recordIndex = 0;
			firstResult = 0;
			list = getNextList();
		}
		
		if (recordIndex >= list.size()) {
			list = getNextList();
			recordIndex = 0;
			if ((list == null) || (list.size() == 0))
				return null;
		}

		Object obj = list.get(recordIndex++);
		return obj;
	}

	/**
	 * Get the next list to be sent to XML
	 * @return
	 */
	private List getNextList() {
        // check if it is the end of the process
        if (listStep == null) {
            return null;
        }

        // get the current block list being processed
        switch (listStep) {
            case QUERY:
                list = getNextQueryList();
                if (list == null) {
                    listStep = ListStep.NEWKEYS;
                }
                break;

            case NEWKEYS:
                list = getNewKeysList();
                listStep = ListStep.VERSIONS;
                break;

            case VERSIONS:
                list = getVersionsList();
                listStep = ListStep.DELETEDENTITIES;
                break;

            case DELETEDENTITIES:
                /* DeletedEntitys being controled in another kind of control
				list = getDeletedList();*/
                listStep = null;
                break;
        }

        if (list == null || list.size() == 0) {
            return getNextList();
        }

        return list;
	}


    /**
     * Return the list of deleted keys
     * @return
     */
    private List getDeletedList() {
        return deletedEntities;
    }

    /**
     * Return the list of entity versions for this synchronization
     * @return
     */
    private List getVersionsList() {
        return entityVersions;
    }

    /**
     * Return the list of new keys created in the system
     * @return
     */
    private List getNewKeysList() {
        List newKeys = (keyList != null? keyList.getAllKeys(): null);
        return newKeys;
    }

    /**
     * Return the next list resulted from a HQL query
     * @return
     */
    private List getNextQueryList() {
        // is the end of the list ?
        if (recordIndex < MAX_RESULTS) {
            queryIndex++;
            if (queryIndex >= hqls.size()) {
                return null;
            }
            recordIndex = 0;
            firstResult = 0;
        }
        else {
            firstResult += MAX_RESULTS;
        }

        em.clear();

        String hql = hqls.get(queryIndex);

        // is the first list of the entity ?
        if (firstResult == 0) {
            processEntityVersion(hql);
            //processDeletedEntities(hql);
        }

		List lst = loadList(hql, firstResult);

        // if there is nothing to return, move to the next list
        if (lst.size() == 0) {
            recordIndex = 0;
            return getNextList();
        }

        if (queryIndex == unitLinkIndex)
            return getUnitLinks((List<Object[]>)lst);

        return lst;
    }
	
	/**
	 * Query the database and return the result list 
	 * @param hql the HQL instruction to be executed
	 * @param firstResult the index of the first result of the query
	 * @return list of objects
	 */
	protected List loadList(String hql, Integer firstResult) {
		// check about version information (last transaction executed)
		EntityLastVersion ver = null;
		String entityName = retrieveEntityName(hql);

		if ((clientEntityVersions != null) && (entityName != null)) {
			if("DeletedEntity".equals(entityName)){
				ver = findClientLastVersion(entityName);
				if(ver != null)
					hql = hql + " where id > :txid ";
			}else{
				Class entClass = getEntityClass(entityName);
				if ((entClass != null) && (Transactional.class.isAssignableFrom(entClass))) {
					ver = findClientLastVersion(entityName);
					if (ver != null)
						 hql = hql.replace("where ", "where a.lastTransaction.id > :txid and ");
					else hql = hql.replace("where ", "where a.lastTransaction.id is not null and ");
				}
				else return new ArrayList();
			}
		}

		// create the query
		Query qry = em.createQuery(hql);
		if (hql.contains(":unitid"))
			qry.setParameter("unitid", unitId);
		if (hql.contains("wsid"))
			qry.setParameter("wsid", workspaceId);
		if (ver != null)
			qry.setParameter("txid", ver.getLastVersion());

		// query the database
		List lst = qry
				.setFirstResult(firstResult)
				.setMaxResults(MAX_RESULTS)
				.getResultList();

		return lst;
	}

	
	/**
	 * Return the entity class by its name
	 * @param entityName
	 * @return
	 */
	private Class getEntityClass(String entityName) {
		try {
			//TODO: [MAURICIO] fazer melhor, voce consegue!
			String pkg = "org.msh.tb.entities.";
			if("ExamSkin".equals(entityName) || "ExamBiopsy".equals(entityName))
				pkg = "org.msh.tb.bd.cases.exams.";

			Class clazz = Class.forName( pkg + entityName);
			return clazz;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Search for the last entity version notified by the client. Used when generating
	 * the answer file
	 * @param entityName is the name of the entity to look up
	 * @return instance of {@link EntityLastVersion}, or null if not found
	 */
	protected EntityLastVersion findClientLastVersion(String entityName) {
		if (clientEntityVersions == null)
			return null;

		for (EntityLastVersion ver: clientEntityVersions) {
			if (ver.getEntityClass().equals(entityName))
				return ver;
		}
		
		return null;
	}
	
	/**
	 * Retrieve the entity name from the HQL instruction, if available there.
	 * It simply search for the from clause and get the next name after that
	 * @param hql is the HQL instruction containing the entity name in the "from" clause
	 * @return entity name, or null if entity name not found
	 */
	protected String retrieveEntityName(String hql) {
		String[] s = hql.split(" ");

		for(int i = 0; i<s.length ; i++){
			if(s[i].equals("from"))
				return s[i+1];
		}

		return null;
	}


    private boolean isEntityQuery(String hql) {
        String[] s = hql.split(" ");
        // is an entity query
        return ((s.length > 0) && (s[0].equals("from")));
    }

    /**
     * Retrieve the deleted entities of the given entity class being processed
     * @param hql
     * UNSING ANOTHER CONTROL***
    protected void processDeletedEntities(String hql) {
        if (!isEntityQuery(hql)) {
            return;
        }
        // retrieve the entity name from the hql instruction
        String entityName = retrieveEntityName(hql);
        if (entityName == null)
            return;

        EntityLastVersion ver = findClientLastVersion(entityName);
        Integer txid = ver != null? ver.getLastVersion(): 0;

        String sql = "select entityId from transactionlog where entityClass = :ent " +
                "and workspacelog_id = :wsid and action = :act and id > :id";
        List<Integer> lst = em.createNativeQuery(sql)
                .setParameter("wsid", workspaceId)
                .setParameter("ent", entityName)
                .setParameter("act", RoleAction.DELETE.ordinal())
                .setParameter("id", txid)
                .getResultList();

        // return the deleted IDs
        for (Integer id: lst) {
            DeletedEntity item = new DeletedEntity();
            item.setEntityId(id);
            item.setEntityName(entityName);

            deletedEntities.add(item);
        }
    }*/

	/**
	 * Get the last version of the entity
	 * @param hql is the HQL instruction containing the data
	 */
	protected void processEntityVersion(String hql) {
        if (!isEntityQuery(hql)) {
            return;
        }

		// retrieve the entity name from the hql instruction
		String entityName = retrieveEntityName(hql);
		if (entityName == null)
			return;

		String sql = "select max(id) from transactionlog where entityClass = :ent and workspacelog_id = :id";
		Integer val = (Integer)em.createNativeQuery(sql)
				.setParameter("id", workspaceId)
				.setParameter("ent", entityName)
				.getSingleResult();
		if (val == null)
			return;

		EntityLastVersion  ver = new EntityLastVersion();
		ver.setEntityClass(entityName);
		ver.setLastVersion(val);

		if (entityVersions == null)
			entityVersions = new ArrayList<EntityLastVersion>();
		entityVersions.add(ver);
	}

	/**
	 * Get the last DeletedEntity and include this on entityVersion
	 */
	protected void processDeletedEntityVersion(){
		EntityLastVersion lastDelEntProcessed = findClientLastVersion("DeletedEntity");
		Integer lastIdProcessed = new Integer(0);

		if(lastDelEntProcessed!=null)
			lastIdProcessed = lastDelEntProcessed.getLastVersion();

		Integer val = (Integer)entityManager.createNativeQuery("select max(id) from DeletedEntity where id > :lastIdProcessed")
				.setParameter("lastIdProcessed", lastIdProcessed)
				.getSingleResult();

		EntityLastVersion  ver = new EntityLastVersion();

		if(val == null && lastDelEntProcessed == null) {
			return;
		}else if(val == null && lastDelEntProcessed != null){
			ver = lastDelEntProcessed;
		}else if(val!=null){
			ver.setEntityClass("DeletedEntity");
			ver.setLastVersion(val);
		}

		if (entityVersions == null)
			entityVersions = new ArrayList<EntityLastVersion>();
		entityVersions.add(ver);
	}
	
	
	/**
	 * Return object containing the links between units. They can't be represented in the TB Unit object
	 * because it may have multiple dependencies
	 * @param lst
	 * @return
	 */
	private List getUnitLinks(List<Object[]> lst) {
		List<TBUnitLinks> res = new ArrayList<TBUnitLinks>();
		for (Object[] vals: lst) {
			res.add(new TBUnitLinks((Integer)vals[0], (Integer)vals[1], (Integer)vals[2], (Integer)vals[3]));
		}
		return res;
	}

	/**
	 * Add the converters for the serialization/deserialization
	 * @param context
	 */
	protected void addConverter(StreamContext context) {
		DataConverter converter = new DataConverter() {
			@Override
			public String convertToString(Object obj) {
				if (obj == null)
					return null;

				int val = ((WeeklyFrequency)obj).getValue();
				return Integer.toString(val);
			}
			
			@Override
			public Object convertFromString(String data, Class classType) {
				int val = Integer.parseInt(data);
				return new WeeklyFrequency(val);
			}
		};
		context.setConverter(WeeklyFrequency.class, converter);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object newObject(Class objectType, Map<String, Object> params) {
		return null;
	}

	/** {@inheritDoc}
	 */
	@Override
	public Class getObjectClass(Object obj) {
		/*if (obj instanceof TbCase)
			return TbCase.class;
		if (obj instanceof ExamCulture)
			return ExamCulture.class;
		if (obj instanceof ExamDST)
			return ExamDST.class;
		if (obj instanceof MedicalExamination)
			return MedicalExamination.class;
		if (obj instanceof ExamHIV)
			return ExamHIV.class;
		if (obj instanceof ExamMicroscopy)
			return ExamMicroscopy.class;
		*/
		if (obj instanceof HibernateProxy)
			return Hibernate.getClass(obj);
		
		return null;
	}

	/**
	 * @return the unitId
	 */
	public Integer getUnitId() {
		return unitId;
	}

	/**
	 * @return the workspaceId
	 */
	public Integer getWorkspaceId() {
		return workspaceId;
	}

	/**
	 * @return the entityVersions
	 */
	public List<EntityLastVersion> getEntityVersions() {
		return entityVersions;
	}


	/**
	 * @return the keyList
	 */
	public EntityKeyList getKeyList() {
		return keyList;
	}


	/**
	 * @param keyList the keyList to set
	 */
	public void setKeyList(EntityKeyList keyList) {
		this.keyList = keyList;
	}

	
	/**
	 * Return the answer file name from a given file token
	 * @param fileToken the file token
	 * @return instance of {@link File} pointing to the file name
	 */
	public static File getAnswerFileName(String fileToken) {
		return new File(App.getTempDir(), fileToken + ".answer.etbm");
	}


	/**
	 * @return the clientEntityVersions
	 */
	public List<EntityLastVersion> getClientEntityVersions() {
		return clientEntityVersions;
	}


	/**
	 * @param clientEntityVersions the clientEntityVersions to set
	 */
	public void setClientEntityVersions(List<EntityLastVersion> clientEntityVersions) {
		this.clientEntityVersions = clientEntityVersions;
	}
}
