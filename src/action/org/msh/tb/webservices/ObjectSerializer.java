package org.msh.tb.webservices;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.utils.DataStreamUtils;

import com.rmemoria.datastream.DataInterceptor;
import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;

/**
 * Serialize and deserialize objects to and from XML
 * @author Ricardo Memoria
 *
 */
public class ObjectSerializer implements DataInterceptor {

	private static ObjectSerializer _instance;

	private StreamContext context;
	
	/**
	 * Private constructor of the class
	 */
	private ObjectSerializer() {
		context = DataStreamUtils.createContext("webservice-response-schema.xml");
		context.addInterceptor(this);
		_instance = this;
	}
	
	/**
	 * Initialize instance of {@link ObjectSerializer} class, running inside a synchronized thread
	 */
	protected static synchronized void createInstance() {
		if (_instance == null)
			_instance = new ObjectSerializer();
	}
	
	/**
	 * Return singleton instance of {@link ObjectSerializer} class
	 * @return
	 */
	public static ObjectSerializer instance() {
		if (_instance == null) {
			createInstance();
		}
		return _instance;
	}
	
	/**
	 * Serialize an object to an XML representation
	 * @param obj
	 * @return
	 */
	public static String serializeToXml(Object obj) {
		DataMarshaller dm = DataStreamUtils.createXMLMarshaller("webservice-response-schema.xml");
		OutputStream out = DataStreamUtils.createStringOutputStream();
		dm.marshall(obj, out);
		return out.toString();
	}
	
	
	/**
	 * @param xml
	 * @param clazz
	 * @return
	 */
	public static <E> E deserializeFromXml(String xml, Class<E> clazz) {
		DataUnmarshaller du = DataStreamUtils.createXMLUnmarshaller(instance().context);
		InputStream is = DataStreamUtils.createStringInputStream(xml);
		return (E)du.unmarshall(is);
	}

	/**
	 * @return the context
	 */
	public StreamContext getContext() {
		return context;
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object newObject(Class clazz, Map<String, Object> params) {
		Integer id = (Integer)params.get("id");
		if (clazz == TbCase.class) {
			CaseHome home = (CaseHome)Component.getInstance("caseHome", true);
			home.setId(id);
			return home.getInstance();
		}
		
		EntityManager em = (EntityManager)Component.getInstance("entityManager");

		try {
			return em.find(clazz, id);
		} catch (Exception e) {
			return null;
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public Class getObjectClass(Object obj) {
		return obj.getClass();
	}
}
