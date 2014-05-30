/**
 * 
 */
package org.msh.tb.sync;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.SystemConfig;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TbContact;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.WeeklyFrequency;
import org.msh.utils.DataStreamUtils;

import com.rmemoria.datastream.DataConverter;
import com.rmemoria.datastream.DataInterceptor;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.ObjectProvider;
import com.rmemoria.datastream.StreamContext;

/**
 * @author Ricardo Memoria
 *
 */
@Name("desktopIniFileGenerator")
public class DesktopIniFileGenerator implements ObjectProvider, DataInterceptor {

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
	private List<String> sqlversions;
	private EntityManager em;
	private boolean initialized;
	private ServerSignature serverSignature;
	private int unitLinkIndex = -1;
	private int caseTagIndex = -1;
	// if defined, the system will generate just differences between the client versions
	// and the server versions
	private List<EntityLastVersion> entityVersions;
	
	
	public DesktopIniFileGenerator() {
//		MAX_RESULTS = 50;
		hqls = new ArrayList<String>();
		sqlversions = new ArrayList<String>();

		hqls.add("from UserProfile a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from CountryStructure a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from AdministrativeUnit a where a.workspace.id = #{desktopIniFileGenerator.workspaceId} order by code");
		hqls.add("from HealthSystem a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Source a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Tbunit a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("select id, authorizerUnit.id, firstLineSupplier.id, secondLineSupplier.id from Tbunit where workspace.id = #{desktopIniFileGenerator.workspaceId}");
		unitLinkIndex = hqls.size() - 1;
		hqls.add("from Substance a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Medicine a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Regimen a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Laboratory a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from FieldValue a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from Tag a where a.workspace.id = #{desktopIniFileGenerator.workspaceId}");
		hqls.add("from UserWorkspace a join fetch a.user left join fetch a.adminUnit where a.tbunit.id = #{desktopIniFileGenerator.unitId}");
//		hqls.add("from TbCase a join fetch a.patient where a.ownerUnit.id = #{desktopIniFileGenerator.unitId}");

		// case data
		hqls.add("from TbCase a join fetch a.patient left join fetch a.regimen left join fetch a.notifAddress.adminUnit "
				+ "where a.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from PrescribedMedicine a join fetch a.tbcase join fetch a.medicine join fetch a.source where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from TreatmentHealthUnit a join fetch a.tbunit join fetch a.tbcase where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamCulture a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamMicroscopy a join fetch a.tbcase left join fetch a.method left join fetch a.laboratory where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from MedicalExamination a join fetch a.tbcase where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamHIV a join fetch a.tbcase where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamXRay a join fetch a.tbcase left join fetch a.presentation where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamDST a join fetch a.tbcase where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from ExamDSTResult a join fetch a.substance join fetch a.exam where a.exam.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from TreatmentMonitoring a join fetch a.tbcase where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from TbContact a join fetch a.tbcase left join fetch a.contactType left join fetch a.conduct where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from CaseSideEffect a join fetch a.tbcase left join fetch a.substance left join fetch a.substance2 where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		hqls.add("from CaseComorbidity a join fetch a.tbcase left join fetch a.comorbidity where a.tbcase.ownerUnit.id = #{desktopIniFileGenerator.unitId}");

		hqls.add("select a.id, b.id from TbCase a join a.tags b where a.ownerUnit.id = #{desktopIniFileGenerator.unitId}");
		caseTagIndex = hqls.size() - 1;
	}


	/**
	 * Add the queries to be executed
	 * @param hql is the HQL instruction to retrieve the entities
	 * @param sql is the SQL instruction to retrieve the last entity version
	 */
	protected void addQuery(String hql, String sql) {
		hqls.add(hql);
		sqlversions.add(sql);
	}
	
	/**
	 * Generate the client initialization file
	 */
	public void generateFile(Tbunit unit, OutputStream out) {
		// initialize variables
		unitId = unit.getId();
		workspaceId = unit.getWorkspace().getId();
		
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
			queryIndex = 0;
			recordIndex = 0;
			firstResult = 0;
			list = em.createQuery(hqls.get(queryIndex)).getResultList();
		}
		
		if (recordIndex >= list.size()) {
			list = getNextList();
			recordIndex = 0;
			if (list == null)
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
		// the entityVersions list is the last one to be processed.
		// if the current list is equals entityVerions, so it's the last one 
		// and must return null
		if (list == entityVersions)
			return null;

		// is the end of the list ?
		if (recordIndex < MAX_RESULTS) {
			queryIndex++;
			if (queryIndex >= hqls.size())
				return entityVersions;
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
		}

		list = em.createQuery(hql)
				.setFirstResult(firstResult)
				.setMaxResults(MAX_RESULTS)
				.getResultList();

		// if there is nothing to return, move to the next list
		if (list.size() == 0) {
			recordIndex = 0;
			return getNextList();
		}
		
		if (queryIndex == unitLinkIndex)
			return getUnitLinks((List<Object[]>)list);
		
		if (queryIndex == caseTagIndex)
			return getCaseTags((List<Object[]>)list);

		return list;
	}

	
	/**
	 * Process entity
	 * @param hql is the HQL instruction containing the data
	 */
	protected void processEntityVersion(String hql) {
		String[] s = hql.split(" ");
		// is an entity query
		if ((s.length == 0) || (!s[0].equals("from")))
			return;
		
		String entityName = s[1];

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
	 * Return object containing the links between units. They can't be represented in the TB Unit object
	 * because it may have multiple dependencies
	 * @param list2
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
	 * Return object containing the tags of all cases
	 * because it may have multiple dependencies
	 * @param list2
	 * @return
	 */
	private List getCaseTags(List<Object[]> lst) {
		List<CaseTag> res = new ArrayList<CaseTag>();
		for (Object[] vals: lst) {
			res.add(new CaseTag((Integer)vals[0], (Integer)vals[1]));
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
		if (obj instanceof TbCase)
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
		
		if (obj instanceof TbContact)
			return TbContact.class;

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
	 * @param entityVersions the entityVersions to set
	 */
	public void setEntityVersions(List<EntityLastVersion> entityVersions) {
		this.entityVersions = entityVersions;
	}
}
