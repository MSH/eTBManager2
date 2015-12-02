/**
 * 
 */
package org.msh.tb.sync;

import com.rmemoria.datastream.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.msh.tb.ETB;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.application.App;
import org.msh.tb.application.TransactionManager;
import org.msh.tb.entities.*;
import org.msh.tb.login.UserSession;
import org.msh.utils.DataStreamUtils;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * @author Ricardo Memoria
 *
 */
public class SyncFileImporter {

	private static final int BUFFER_SIZE = 65535;
	
	FileInputStream fstream;
	private Workspace workspace;
	private TransactionManager transaction;
	// store in-memory list of entity keys (client ID and server ID)
	private EntityKeyList entityKeys;
	// list of last versions in use by the client (will be used to generate response file)
	private List<EntityLastVersion> entityVersions;
	private List<Integer> casesUpdatedOrCriatedIds;


	/**
	 * Data interceptor to load entities
	 */
	private DataInterceptor interceptor = new DataInterceptor() {
		@Override
		public Object newObject(Class objectType, Map<String, Object> params) {
			if (params != null)
				 return createNewObject(objectType, params);
			else return null;
		}
		
		@Override
		public Class getObjectClass(Object obj) {
			return null;
		}
	};
	
	
	/**
	 * Start reading sync file sent from the client
	 * @param file instance of {@link File} that contains the server content
	 */
	public void start(File file) {
		// get the workspace in use
		workspace = UserSession.getWorkspace();
		
		// list of keys for the entity
		entityKeys = new EntityKeyList();

		// create list that will receive the last version of entities in use in the client
		entityVersions = new ArrayList<EntityLastVersion>();
		
		try {
			File destfile = File.createTempFile("temp", "etbm.tmp");
			uncompressFile(file, destfile);
			fstream = new FileInputStream(destfile);
			try {
				importData(fstream);
			}
			finally {
				fstream.close();
				destfile.delete();
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * Uncompress a file compressed with {@link GZIPInputStream} and generate a new file
	 * @param gzipfile instance of {@link File} containing the compressed file
	 * @param destfile instance of {@link File} where uncompressed file will be written to
	 */
	protected void uncompressFile(File gzipfile, File destfile) {
		try {
			if (destfile.exists())
				destfile.delete();
			
			byte[] buffer = new byte[BUFFER_SIZE];

			GZIPInputStream gzin = new GZIPInputStream(new FileInputStream(gzipfile));
			FileOutputStream out = new FileOutputStream(destfile);

			int noRead;
			while ((noRead = gzin.read(buffer)) != -1) {
			        out.write(buffer, 0, noRead);
			}
			gzin.close();
			out.close();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * Read the input stream provided and initialize the records in the database
	 * @param in instance of {@link InputStream}
	 */
	private void importData(InputStream in) {
		StreamContext context = DataStreamUtils.createContext("clientsyncfile-schema.xml");

		// add the interceptor
		context.addInterceptor(interceptor);

		// add converter to WeeklyFrequency object
		context.setConverter(WeeklyFrequency.class, new DataConverter() {
			@Override
			public String convertToString(Object obj) {
				return Integer.toString( ((WeeklyFrequency)obj).getValue() );
			}
			
			@Override
			public Object convertFromString(String s, Class clazz) {
				WeeklyFrequency wf = new WeeklyFrequency();
				wf.setValue( Integer.parseInt(s) );
				return wf;
			}
		});
		
		DataUnmarshaller um = DataStreamUtils.createXMLUnmarshaller(context);

		try {
			// start reading the objects
			um.unmarshall(in, new ObjectConsumer() {
				@Override
				public void onNewObject(Object obj) {
					handleNewObject(obj);
					
					// check if there is an on-going transaction
					if (getTransaction().isActive())
						getTransaction().commit();
				}

				@Override
				public void startObjectReading(Class objectClass) {
					if (objectClass != ServerSignature.class)
						getTransaction().begin();
				}
			});
			
		} finally {
			// if it's active that's because there was an error
			if (getTransaction().isActive())
				getTransaction().rollback();
		}

		/*TODO: [MAURICIO] ver com o Ricardo como implementar isso de forma mais adequada*/
		//Ricardo aprova que isso fique aqui, e que seja gerado um novo lasttransaction id para esses individuos.
		TagsCasesHome tagsCasesHome = (TagsCasesHome) Component.getInstance(TagsCasesHome.class);
		if(casesUpdatedOrCriatedIds!=null) {
			for (Integer id : casesUpdatedOrCriatedIds) {
				TbCase c = App.getEntityManager().find(TbCase.class, id);
				tagsCasesHome.updateTags(c);
			}
		}
	}

	/**
	 * Create a new object based on its class and parameters from the XML data.
	 * This method is called by the data stream when it's necessary to restore an
	 * object by the parameters available
	 * @param objectType is the class of the object
	 * @param params is the list of parameters available in the XML
	 * @return instance of the object class
	 */
	protected Object createNewObject(Class objectType, Map<String, Object> params) {
		objectType = ETB.getWorkspaceClass(objectType);

		// is information about the last version used in each entity?
		if (objectType == EntityLastVersion.class) {
			// let the library create the instance
			return null;
		}

		Integer clientId = (Integer)params.get("clientId");
		Integer id = (Integer)params.get("id");
		
		// handle embedded cases in the object
		TbCase tbcase = null;
		if ((id == null) && (params.containsKey("tbcase.id") && (params.containsKey("tbcase.clientId")))) {
			Map<String, Object> paramsCase = new HashMap<String, Object>();
			paramsCase.put("id", params.get("tbcase.id"));
			paramsCase.put("clientId", params.get("tbcase.clientId"));
			tbcase = (TbCase)createNewObject(TbCase.class, paramsCase);
		}

		// if there is a client ID, so the object is to be sync
		if (clientId != null) {
			EntityKey key = entityKeys.findEntityKey(objectType, clientId);
			if (key == null) {
				// register the keys of the object
				entityKeys.registerUpdatedKey(objectType, clientId, id);
			}
			else {
				// restore the key already generated in the server side during this synchronization
				id = key.getServerId();
			}
		}

		if (id != null) {
			Object o = App.getEntityManager().find(objectType, id);
			checkObjectCollection(o, params);
			return o;
		}

        if (params.size() == 0) {
            return null;
        }

		try {
			// create new instance of object by workspace
			Class wsclazz = ETB.getWorkspaceClass(objectType);
			Object obj = wsclazz.newInstance();

			if (tbcase != null) {
				PropertyUtils.setProperty(obj, "tbcase", tbcase);
			}

			checkObjectCollection(obj, params);

			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * It will check if any param of the object is a list, if it is, it will get each object from the list and
	 * will delete it.
	 * @param o object to have its params checked
	 * @param params params from the o
	 */
	private void checkObjectCollection(Object o, Map<String, Object> params){
		List<String> lst = new ArrayList<String>();

		for(String s : params.keySet()){
            try {
                // check if it is not a nested property
                if (!s.contains(".")) {
                    Class clazz = PropertyUtils.getPropertyType(o, s);
                    if (Collection.class.isAssignableFrom(clazz)) {
                        lst.add(s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
		}

		for(String s : lst){
			try{
				Collection c = (Collection)PropertyUtils.getProperty(o, s);
				if(c!=null){
					for(Object item : c){
						App.getEntityManager().remove(item);
					}
					c.clear();
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Called when a new object is just read from the input stream
	 * @param obj is the object read from the data stream
	 */
	protected void handleNewObject(Object obj) {
		if (obj instanceof EntityLastVersion) {
			entityVersions.add((EntityLastVersion)obj);
			return;
		}

		if(obj instanceof DeletedEntity){
			handleDeletedEntity((DeletedEntity)obj);
			return;
		}

		saveEntity(obj);
	}

	/**
	 * Delete entities deleted on the desktop
	 * @param obj
	 */
	protected void handleDeletedEntity(DeletedEntity obj){
		EntityManager em = App.getEntityManager();
		Object o = null;

		List<Object> l = em.createQuery("from " + obj.getEntityName() + " where id = :EntityId")
				.setParameter("EntityId", obj.getEntityId())
				.getResultList();

		if(l!=null && l.size() > 0)
			o = l.get(0);

		if(o!=null) {
			em.remove(o);
			em.flush();
		}
	}

	/**
	 * Save entity. The method is under a transaction, so it's safe to persist and continue
	 * @param obj
	 */
	protected void saveEntity(Object obj) {
		EntityManager em = App.getEntityManager();

		if (obj instanceof WSObject) {
			workspace = em.merge(workspace); 
			((WSObject)obj).setWorkspace(workspace);
		}

		// handle exceptions in the UserWorkspace class
		if (obj instanceof TbCase) {
			TbCase tbcase = (TbCase)obj;
			// save the patient
			Patient p = tbcase.getPatient();
			workspace = em.merge(workspace);
			p.setWorkspace(workspace);
			em.persist(p);
		}

		// get information about the keys (id and client id) of the entity being saved
		SyncKey objKeys = null;
		if (obj instanceof SyncKey)
			objKeys = (SyncKey)obj;
		boolean bNew = (objKeys != null) && (objKeys.getId() == null);

        System.out.println("Saving " + obj.getClass() + " => " + obj);
		// save the entity
		em.persist(obj);
		em.flush();

		if(obj instanceof TbCase){
			if(casesUpdatedOrCriatedIds == null)
				casesUpdatedOrCriatedIds = new ArrayList<Integer>();
			casesUpdatedOrCriatedIds.add(((TbCase)obj).getId());
		}

		// if it's a new entity, get the id to be sent back to the client
		if (bNew)
			entityKeys.updateServerKey(obj.getClass(), objKeys.getClientId(), objKeys.getId());
		
		em.clear();
	}

	/**
	 * The component responsible for managing the database transaction
	 * @return instance of {@link TransactionManager}
	 */
	protected TransactionManager getTransaction() {
		if (transaction == null)
			transaction = TransactionManager.instance();
		return transaction;
	}


	/**
	 * @return the entityKeys
	 */
	public EntityKeyList getEntityKeys() {
		return entityKeys;
	}


	/**
	 * @return the entityVersions
	 */
	public List<EntityLastVersion> getEntityVersions() {
		return entityVersions;
	}
	
	
	/**
	 * Create the instance of the {@link EntityHomeEx} for the given object
	 * that will allow the object to be validated and persisted
	 * @param obj entity instance
	 * @return instance of {@link EntityHomeEx}
	 */
/*	protected EntityHomeEx createEntityHome(Object obj) {
		Class clazz = getHomeComponentClass(obj);
		EntityHomeEx home = (EntityHomeEx)Component.getInstance(clazz);
		if (home == null)
			throw new RuntimeException("No home class defined for entity class " + obj.getClass());

		return home;
	}


	*//**
	 * Return component name of the home class for entity object
	 * @param obj
	 * @return
	 *//*
	protected Class getHomeComponentClass(Object obj) {
		if (obj instanceof TbCase)
			return CaseHome.class;

		if (obj instanceof Patient)
			return PatientHome.class;

		if (obj instanceof ExamMicroscopy)
			return ExamMicroscopyHome.class;

		if (obj instanceof ExamCulture)
			return ExamCultureHome.class;

		if (obj instanceof ExamXRay)
			return ExamXRayHome.class;

		if (obj instanceof ExamDST)
			return ExamDSTHome.class;

		if
	}
*/}
