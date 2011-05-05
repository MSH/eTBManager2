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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AdministrativeUnit;
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
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserPermission;
import org.msh.tb.entities.UserProfile;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.TbField;
import org.msh.tb.log.LogService;
import org.msh.tb.test.dbgen.TokenReaderException;
import org.omg.ETF.Profile;

/**
 * Copy data from one workspace into another
 * @author Ricardo Memoria
 *
 */
@Name("workspaceCopy")
public class WorkspaceCopy {

	@In(required=true) WorkspaceHome workspaceHome;
	@In EntityManager entityManager;

	private static final Class[] entityClasses = {AdministrativeUnit.class, HealthSystem.class, Tbunit.class, 
		Source.class, Substance.class, Medicine.class, Regimen.class, Laboratory.class, User.class,
		UserWorkspace.class, TbField.class, UserProfile.class, MedicineComponent.class, UserPermission.class, MedicineRegimen.class,
		CountryStructure.class, FieldValue.class};


	private Workspace sourceWorkspace;
	private boolean adminUnits;
	private boolean healthSystems;
	private boolean tbunits;
	private boolean sources;
	private boolean substances;
	private boolean medicines;
	private boolean regimens;
	private boolean laboratories;
	private boolean ageRanges;
	private boolean tbfields;
	private boolean users;
	private boolean profiles;
	
	private List<Workspace> options;
	
	private Map<Object, Object> loop;


	/**
	 * Copy data from the workspace set in sourceWorkspace property to the workspace set in workspaceHome component
	 * @return
	 */
	public String copy() {
		if (sourceWorkspace == null) 
			return "error";
		
		if (adminUnits)
			copyEntities(AdministrativeUnit.class);
		
		if (healthSystems)
			copyEntities(HealthSystem.class);
		
		if (tbunits)
			copyEntities(Tbunit.class);
		
		if (sources)
			copyEntities(Source.class);

		if (substances)
			copyEntities(Substance.class);

		if (medicines)
			copyEntities(Medicine.class);

		if (regimens)
			copyEntities(Regimen.class);

		if (laboratories)
			copyEntities(Laboratory.class);

		if (users)
			copyEntities(UserWorkspace.class);

		if (profiles)
			copyEntities(Profile.class);

		if (tbfields)
			copyEntities(FieldValue.class);

		Integer id = workspaceHome.getInstance().getId();
		workspaceHome.setId(null);
		workspaceHome.setId(id);

		saveTransactionLog();
		
		return "copied";
	}

	
	/**
	 * Save in the transaction log the workspace copy procedure
	 */
	protected void saveTransactionLog() {
		LogService logService = new LogService();
		logService.addValue("admin.workspaces.copy.sourcews", sourceWorkspace);
		if (adminUnits)
			logService.addMessageValue("admin.adminunits", "admin.workspaces.copy.selcopied");
		if (healthSystems)
			logService.addMessageValue("admin.healthsys", "admin.workspaces.copy.selcopied");
		if (tbunits)
			logService.addMessageValue("admin.tbunits", "admin.workspaces.copy.selcopied");
		if (sources)
			logService.addMessageValue("admin.sources", "admin.workspaces.copy.selcopied");
		if (substances)
			logService.addMessageValue("admin.substances", "admin.workspaces.copy.selcopied");
		if (medicines)
			logService.addMessageValue("admin.medicines", "admin.workspaces.copy.selcopied");
		if (laboratories)
			logService.addMessageValue("admin.labs", "admin.workspaces.copy.selcopied");
		if (users)
			logService.addMessageValue("admin.users", "admin.workspaces.copy.selcopied");
		if (profiles)
			logService.addMessageValue("admin.profiles", "admin.workspaces.copy.selcopied");
		if (profiles)
			logService.addMessageValue("admin.fields", "admin.workspaces.copy.selcopied");

		logService.saveExecuteTransaction(workspaceHome.getInstance(), "WSCOPY");
	}


	/**
	 * Copy all entities of an specific class
	 * @param <E>
	 */
	protected <E> void copyEntities(Class<E> entityClass) {
		// create list with entities from both workspaces
		String hql = "from " + entityClass.getSimpleName() + " a where a.workspace.id in (" + sourceWorkspace.getId() +
			"," + workspaceHome.getInstance().getId() + ")";

		List<E> lst = entityManager
			.createQuery(hql)
			.getResultList();

		// create an specific list for each workspace
		List<E> sourceLst = new ArrayList<E>();
		List<E> destLst = new ArrayList<E>();
		
		for (E entity: lst) {
			try {
				Workspace ws = (Workspace)PropertyUtils.getProperty(entity, "workspace");
				if (ws.equals(sourceWorkspace))
					 sourceLst.add(entity);
				else destLst.add(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		// copy entities from source to destination workspace
		for (E sourceEntity: sourceLst) {
			E destEntity = null;
			String sourceName = sourceEntity.toString();
			
			for (E aux: destLst) {
				String destName = aux.toString();
				if (destName.equalsIgnoreCase(sourceName)) {
					destEntity = aux;
					break;
				}
			}

			if (destEntity == null) {
				loop = new HashMap<Object, Object>();
				destEntity = (E)createEntityClone(sourceEntity);
				entityManager.persist(destEntity);
			}
		}

		entityManager.flush();
		entityManager.clear();
	}


	/**
	 * Sometimes classes in Hibernate are wrapped by lazy init classes, so this
	 * method returns the entity class of the given object
	 * @param entity - object to retrieve entity class
	 * @return
	 */
	protected Class entityClass(Object entity) {
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
	 * @param source
	 * @return
	 */
	protected Object createEntityClone(Object source) {
		// search for entity class
		Class entityClass = entityClass(source);

		// is an entity
		if (entityClass == null)
			return null;

		try {
			Object dest = entityClass.newInstance();
			copyProperties(source, dest);
			if (PropertyUtils.isWriteable(dest, "workspace"))
				PropertyUtils.setProperty(dest, "workspace", workspaceHome.getInstance());
			return dest;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	protected void copyProperties(Object source, Object dest) {
		try {
			// map properties of object 
			Map<String, Object> values = PropertyUtils.describe(source);
			
			// copy values
			for (String key: values.keySet()) {
				if ((PropertyUtils.isWriteable(dest, key)) && 
					((!("id".equals(key))) && (!("workspace".equals(key))))) 
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
		Class entClass = entityClass(obj);
		if (entClass == null)
			return null;

		String fieldName = displayFieldName(obj);
		
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
			 lst = entityManager.createQuery(hql)
			 	.setParameter("prop", fieldValue)
			 	.getResultList();
		else lst = entityManager.createQuery(hql + " and workspace.id = :wsid")
				.setParameter("prop", fieldValue)
				.setParameter("wsid", workspaceHome.getInstance().getId())
				.getResultList();
		
		Object ent = null;
		if (lst.size() > 0)
			ent = lst.get(0);
		
		if (ent == null) {
			ent = createEntityClone(obj);
			loop.put(obj, ent);
			entityManager.persist(ent);
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
	
	protected <E> List<E> copyListProperty( List<E> lst ) {
		List<E> destList = new ArrayList<E>();
		
		for (E entity: lst) {
			destList.add((E)createEntityClone(entity));
		}
		return destList;
	}
	
	
	/**
	 * Return the display field name of the given entity
	 * @param obj
	 * @return
	 */
	protected String displayFieldName(Object obj) {
		if ((obj instanceof CountryStructure) ||
				(obj instanceof AdministrativeUnit) ||
				(obj instanceof Tbunit) ||
				(obj instanceof HealthSystem) ||
				(obj instanceof Source) ||
				(obj instanceof Substance) ||
				(obj instanceof FieldValue))
			return "name.name1";
		else
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
			return "id";
		else return null;
	}

	
	public boolean isAdminUnits() {
		return adminUnits;
	}

	public void setAdminUnits(boolean adminUnits) {
		this.adminUnits = adminUnits;
	}

	public boolean isHealthSystems() {
		return healthSystems;
	}

	public void setHealthSystems(boolean healthSystems) {
		this.healthSystems = healthSystems;
	}

	public boolean isTbunits() {
		return tbunits;
	}

	public void setTbunits(boolean tbunits) {
		this.tbunits = tbunits;
	}

	public boolean isSources() {
		return sources;
	}

	public void setSources(boolean sources) {
		this.sources = sources;
	}

	public boolean isSubstances() {
		return substances;
	}

	public void setSubstances(boolean substances) {
		this.substances = substances;
	}

	public boolean isMedicines() {
		return medicines;
	}

	public void setMedicines(boolean medicines) {
		this.medicines = medicines;
	}

	public boolean isRegimens() {
		return regimens;
	}

	public void setRegimens(boolean regimens) {
		this.regimens = regimens;
	}

	public boolean isLaboratories() {
		return laboratories;
	}

	public void setLaboratories(boolean laboratories) {
		this.laboratories = laboratories;
	}

	public boolean isAgeRanges() {
		return ageRanges;
	}

	public void setAgeRanges(boolean ageRanges) {
		this.ageRanges = ageRanges;
	}

	public boolean isTbfields() {
		return tbfields;
	}

	public void setTbfields(boolean tbfields) {
		this.tbfields = tbfields;
	}

	public boolean isUsers() {
		return users;
	}

	public void setUsers(boolean users) {
		this.users = users;
	}

	public boolean isProfiles() {
		return profiles;
	}

	public void setProfiles(boolean profiles) {
		this.profiles = profiles;
	}

	public Workspace getSourceWorkspace() {
		return sourceWorkspace;
	}

	public void setSourceWorkspace(Workspace sourceWorkspace) {
		this.sourceWorkspace = sourceWorkspace;
	}


	/**
	 * Return list of workspaces to be selected by the user
	 * @return
	 */
	public List<Workspace> getOptions() {
		if (options == null) {
			options = entityManager.createQuery("from Workspace w where w.id != #{workspaceHome.id} " +
					"and exists(select uw.id from UserWorkspace uw where uw.user.id = #{userLogin.user.id} " +
					"and uw.workspace.id = w.id)")
					.getResultList();
		}
		return options;
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
}
