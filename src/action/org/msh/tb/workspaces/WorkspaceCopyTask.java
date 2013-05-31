package org.msh.tb.workspaces;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.tasks.DbBatchTask;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.AgeRange;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.HealthSystem;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TransactionLog;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserPermission;
import org.msh.tb.entities.UserProfile;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.WSObject;
import org.msh.tb.entities.Workspace;
import org.msh.tb.test.dbgen.TokenReaderException;
import org.msh.tb.transactionlog.TransactionLogService;

/**
 * Background task that copies data from one workspace (source) to another workspace (target).
 * <p/>
 * The class expects the parameters <code>sourceWorkspace</code> and <code>targetWorkspace</code>
 * indicating the workspace data that will be copied from and to.
 * 
 * @author Ricardo Memoria
 *
 */
public class WorkspaceCopyTask extends DbBatchTask {

	private static Class[] entityClasses = {
		AdministrativeUnit.class, 
		HealthSystem.class, 
		Tbunit.class, 
		Source.class, 
		Substance.class, 
		Medicine.class, 
		Regimen.class, 
		Laboratory.class, 
		AgeRange.class, 
		User.class, 
		UserProfile.class,
		MedicineComponent.class,
		UserPermission.class,
		MedicineRegimen.class,
		FieldValue.class,
		CountryStructure.class};

	private Workspace sourceWorkspace;
	private Workspace targetWorkspace;

	// store the number of entities copied
	private Map<Class, Integer> entityCount;
	
	private int counter;

	// list of objects already imported, to avoid an infinite loop
	private Map<Object, Object> loop;
	
	private List<String> propsToIgnore;
	
	private TransactionLog transactionLog;


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.AsyncTaskImpl#starting()
	 */
	@Override
	protected void starting() {
		sourceWorkspace = (Workspace)getParameter("sourceWorkspace");
		targetWorkspace = (Workspace)getParameter("targetWorkspace");
		entityCount = new HashMap<Class, Integer>();

		// force default workspace
		Contexts.getEventContext().set("defaultWorkspace", targetWorkspace);
		
		// properties that will be ignored during copying of the properties from source to target
		propsToIgnore = new ArrayList<String>();
		propsToIgnore.add("id");
		propsToIgnore.add("workspace");
		propsToIgnore.add("lastTransaction");
		propsToIgnore.add("createTransaction");

		// transaction log that will be assigned to the new entities
		TransactionLogService srv = new TransactionLogService(); 
		Workspace ws = getEntityManager().merge( targetWorkspace );
		transactionLog = srv.saveExecuteTransaction("WSCOPY", ws.toString(), ws.getId(), ws.getClass().getSimpleName(), ws);
	}
	
	
	/**
	 * Check if an entity is to be imported
	 * @param entityClass
	 * @return
	 */
	protected boolean isEntityToImport(Class entityClass) {
		return Boolean.TRUE.equals( getParameter(entityClass.getSimpleName()) );
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#execute()
	 */
	@Override
	protected void execute() {
		if ((sourceWorkspace == null) || (targetWorkspace == null))
			return;
		
		for (Class clazz: entityClasses) {
			if (isEntityToImport(clazz))
				importEntities(clazz);
		}
	}


	/**
	 * Import the entities inside an iterator
	 * @param clazz
	 */
	private void importEntities(Class clazz) {
		// return list of IDs to be imported
		final List<Integer> ids = getEntityManager().createQuery("select a.id from " + clazz.getSimpleName() + 
				" a where a.workspace.id = " + sourceWorkspace.getId())
				.getResultList();
		
		final Class entityClass = clazz; 

		// counter of entities imported
		counter = 0;

		// batch iterator to copy entities
		executeBatch(new BatchIterator() {
			public boolean processRecord() {
				int index = getRecordIndex();
				Integer id = ids.get(index);

				Object entity = getEntityManager().find(entityClass, id);
				
				if (entity != null) {
					loop = new HashMap<Object, Object>();
					if (copyEntity(entity))
						counter++;
				}

//				updateProgress(ids.size(), index);

				return index < ids.size() - 1;
			}
		});
		
		if (counter > 0)
			entityCount.put(clazz, counter);
	}


	/**
	 * Copy entity to the target workspace
	 * @param sourceEntity
	 * @return true if entity was copied, or false if there is already an entity with same name 
	 * in the target workspace
	 */
	protected boolean copyEntity(Object sourceEntity) {
		Object destEntity = findTargetEntity(sourceEntity);
		if (destEntity != null)
			return false;

		destEntity = createEntityClone(sourceEntity);

		if (destEntity != null) {
			EntityManager em = getEntityManager();
			em.persist(destEntity);
		}
		
		return true;
	}


	/**
	 * Search for an entity in the target workspace with the same name of the source workspace
	 * @param sourceEntity
	 * @return
	 */
	private Object findTargetEntity(Object sourceEntity) {
		String s = getDisplayFieldName(sourceEntity);
		Class clazz = getEntityClass(sourceEntity);
		String hql = "from " + clazz.getSimpleName() + " where " + s + " = :name and workspace.id = :id";
		String name;
		try {
			name = (String)PropertyUtils.getProperty(sourceEntity, s);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		List lst = getEntityManager()
			.createQuery(hql)
			.setParameter("id", targetWorkspace.getId())
			.setParameter("name", name)
			.getResultList();

		if (lst.size() == 0)
			return null;

		return lst.get(0);
	}


	@Override
	protected void finishing() {
		saveTransactionLog();
	}

	
	/**
	 * @param source
	 * @return
	 */
	protected Object createEntityClone(Object source) {
		// search for entity class
		Class entityClass = getEntityClass(source);

		// is an entity
		if (entityClass == null)
			return null;

		try {
			EntityManager em = getEntityManager();

			Object dest = entityClass.newInstance();
			copyProperties(source, dest);
			if (PropertyUtils.isWriteable(dest, "workspace"))
				PropertyUtils.setProperty(dest, "workspace", getEntityManager().merge(targetWorkspace));
			
			if (dest instanceof WSObject) {
				WSObject wsobj = (WSObject)dest;
				transactionLog = em.merge(transactionLog);
				wsobj.setCreateTransaction(transactionLog);
				wsobj.setLastTransaction(transactionLog);
			}
			
			return dest;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Copy the properties of the source object to the same properties in the dest object
	 * @param source
	 * @param dest
	 */
	protected void copyProperties(Object source, Object dest) {
		try {
			// map properties of object 
			Map<String, Object> values = PropertyUtils.describe(source);
			
			// copy values
			for (String key: values.keySet()) {
				if ((PropertyUtils.isWriteable(dest, key)) &&
					(!propsToIgnore.contains(key)))
				{
					Object val = PropertyUtils.getProperty(source, key);
					Object newVal = null;

					// is it a list ?
					if (val instanceof List) {
						List lstSource = (List)val;
						List lstDest = copySpecificListProperty(dest, key, lstSource);
						
						if (lstDest == null) {
							Class cl = (Class)getGenericType(source, key);
							if (cl != null) {
								// is it a list of the same class ?
								if (!cl.isAssignableFrom(dest.getClass())) {
									newVal = copyListProperty((List)val);
								}
							}
						}
						else newVal = lstDest;
					}
					else {
						// is not a primitive type?
						if (!((val instanceof Number) ||
							(val instanceof String) ||
							(val instanceof Character) ||
							(val instanceof Boolean) ||
							(val instanceof Enum)))
						{
							if (val instanceof Date) {
								Date dt = new Date();
								dt.setTime(((Date)val).getTime());
								newVal = dt;
							}
							else
							if (val != null) {
								Object item = checkLoopList(val);
								if (item == null) {
									newVal = loadEntityProperty(val);
									if (newVal == null) {
										newVal = val.getClass().newInstance();
										copyProperties(val, newVal);
									}
								}
								else newVal = item;
							}
						}
						else newVal = val;
					}
					BeanUtils.setProperty(dest, key, newVal);						
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Load or create a entity based on an entity from the source workspace
	 * @param obj
	 * @return
	 */
	protected Object loadEntityProperty(Object obj) {
		Class entClass = getEntityClass(obj);
		if (entClass == null)
			return null;

		String fieldName = getDisplayFieldName(obj);
		
		if (fieldName == null)
			return null;

		Object fieldValue;
		try {
			fieldValue = PropertyUtils.getProperty(obj, fieldName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		List<Object> lst;
		String hql = "from " + entClass.getSimpleName() + " where " + fieldName + " = :prop";
		boolean isID = fieldName.equals("id");
		
		if (isID)
			 lst = getEntityManager().createQuery(hql)
			 	.setParameter("prop", fieldValue)
			 	.getResultList();
		else lst = getEntityManager().createQuery(hql + " and workspace.id = :wsid")
				.setParameter("prop", fieldValue)
				.setParameter("wsid", targetWorkspace.getId())
				.getResultList();
		
		Object ent = null;
		if (lst.size() > 0)
			ent = lst.get(0);
		
		if (ent == null) {
			ent = createEntityClone(obj);
			loop.put(obj, ent);
			getEntityManager().persist(ent);
		}

		return ent;
	}


	
	protected Object checkLoopList(Object obj) {
		for (Object aux: loop.entrySet()) {
			if ((aux.getClass().isAssignableFrom(obj.getClass())) && (aux.toString().equals(obj.toString()))) {
				return loop.get(aux);
			}
		}
		return null;
	}


	/**
	 * @param dest
	 * @param key
	 * @param lst
	 * @return
	 */
	protected <E> List<E> copySpecificListProperty(Object dest, String key, List<E> lst) {
		if ((dest instanceof Medicine) && (key.equals("components"))) {
			List<E> lstDest = new ArrayList<E>();
			for (E aux: lst) {
				MedicineComponent compSource = (MedicineComponent)aux;
				MedicineComponent compDest = new MedicineComponent();
				Medicine medDest = (Medicine)dest;
				compDest.setMedicine(medDest);
				compDest.setStrength(compSource.getStrength());
				compDest.setSubstance( (Substance)loadEntityProperty(compSource.getSubstance()) );
				
				lstDest.add((E)compDest);
			}
			return lstDest;
		}
		return null;
	}


	/**
	 * @param lst
	 * @return
	 */
	protected <E> List<E> copyListProperty( List<E> lst ) {
		List<E> destList = new ArrayList<E>();
		
		for (E entity: lst) {
			destList.add((E)createEntityClone(entity));
		}
		return destList;
	}
	


	/**
	 * Sometimes classes in Hibernate are wrapped by lazy init classes, so this
	 * method returns the entity class of the given object
	 * @param entity - object to retrieve entity class
	 * @return
	 */
	protected Class getEntityClass(Object entity) {
		// search for entity class
		Class entityClass = null;
		for (Class aux: entityClasses) {
			if ((entity.getClass() == aux) || (aux.isAssignableFrom(entity.getClass()))) {
				entityClass = aux;
				break;
			}
		}
		return entityClass;
	}

	
	/**
	 * Return the display field name of the given entity
	 * @param obj
	 * @return
	 */
	protected String getDisplayFieldName(Object obj) {
		if ((obj instanceof CountryStructure) ||
				(obj instanceof AdministrativeUnit) ||
				(obj instanceof Tbunit) ||
				(obj instanceof HealthSystem) ||
				(obj instanceof Source) ||
				(obj instanceof Substance) ||
				(obj instanceof FieldValue)) 
		{
			return "name.name1";
		}

		if ((obj instanceof UserProfile) ||
			(obj instanceof Regimen) ||
			(obj instanceof Laboratory)) {
			return "name";
		}
		else
		if (obj instanceof UserWorkspace) {
			return "user.login";
		}
		else
		if (obj instanceof Medicine) 
		{
			return "genericName.name1";
		}
		else
		if (obj instanceof User)
			return "login";
		else return null;
	}


	/**
	 * Return the generic type of the property (property List or Map)
	 * @param object
	 * @param propName
	 * @return
	 * @throws TokenReaderException
	 */
	protected Type getGenericType(Object object, String propName) throws TokenReaderException {
		ParameterizedType ptype = null;
		try {
			Field fld = object.getClass().getDeclaredField(propName);
			ptype = (ParameterizedType)fld.getGenericType();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (ptype.getActualTypeArguments().length == 0)
			throw new TokenReaderException("No generic type defined to list in property " + propName);
		
		return ptype.getActualTypeArguments()[0];
	}

	
	/**
	 * Save in the transaction log the workspace copy procedure
	 */
	protected void saveTransactionLog() {
		// check if anything was imported
		if (entityCount.keySet().size() == 0) {
			// if nothing was done, so remove the transaction log
			getEntityManager().createQuery("delete from TransactionLog where id = :id")
				.setParameter("id", transactionLog.getId())
				.executeUpdate();
		}
		
		TransactionLogService logService = new TransactionLogService();
		
		for (Class entityClass: entityCount.keySet()) {
			String name = Messages.instance().get(entityClass.getSimpleName());
			String s = Integer.toString(entityCount.get(entityClass)) + " " + Messages.instance().get("form.resultlist");
			logService.addTableRow(name, s);
		}

		logService.appendTransaction(transactionLog.getId());
//		Workspace ws = getEntityManager().merge( targetWorkspace );
//		logService.saveExecuteTransaction("WSCOPY", ws.toString(), ws.getId(), ws.getClass().getSimpleName(), ws);
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#processBatchRecord()
	 */
	@Override
	protected boolean processBatchRecord() throws Exception {
		// This method is not used, just implemented because it's an abstract method in the parent class
		return false;
	}

}
