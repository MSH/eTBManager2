package org.msh.tb.az.eidss;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.entities.SystemParam;
import org.msh.tb.entities.Workspace;


/**
 * Class methods are associated with EIDSS-unit
 * @author A.M.
 */
public class EIDSS {
	
	/**
	 * load configuration from the SysConfig table
	 * use reflection to determine keys name and fixed prefix for these keys
	 */
	public static EidssIntConfig loadConfig() {
		EidssIntConfig config = new EidssIntConfig();
		Field[] flds = config.getClass().getFields();
		for(Field fld : flds){
			loadParameter(fld,config);
		}
		return config;
	}
	
	/**
	 * Load particular parameter from SysConfig
	 * @param name name of parameter
	 * @param config object to place parameter
	 */
	private static void loadParameter(Field fld, EidssIntConfig config) {
		String s = null;
		try {
			SystemParam sysparam = (SystemParam)getEntityManager()
			.createQuery("from SystemParam sp where sp.workspace.id = :id and sp.key = :param")
			.setParameter("id", getObservWorkspace().getId())
			.setParameter("param", "admin.eidss."+fld.getName())
			.getResultList().get(0);
			s = sysparam.getValue();
		} catch (Exception e) {
			s = null;
		}
		if (s != null) {
			setValue(fld, s, config);
		}
	}
	
	/**
	 * set parameter value. If impossible - do nothing i.e. assign default value
	 * @param fld class field to set value
	 * @param value field value as string
	 * @param config EIDSS config
	 * Only String, Date and Boolean allowed
	 */
	private static void setValue(Field fld, String value, EidssIntConfig config) {
		Object thisValue = null;
		try {
			if (fld.getType().getName().contains("Date")){
				//
				Date date = null;
				if (value.length()>10){
					try {
						date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(value);
						thisValue = date;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				else{
					java.sql.Date dt = java.sql.Date.valueOf(value);
					thisValue = new java.util.Date(dt.getTime());
				}
			}else if (fld.getType().getName().contains("Boolean")){
				thisValue = Boolean.valueOf(value);
			}else if (fld.getType().getName().contains("Integer")){
				thisValue = Integer.valueOf(value);
			} else{
				thisValue = value;
			}
			fld.set(config, thisValue);
		} catch (IllegalArgumentException e) {
			// do nothing
		} catch (IllegalAccessException e) {
			// do nothing
		}	
	}
	
	/**
	 * Load Azerbaijan workspace
	 */
	public static Workspace getObservWorkspace() {
		return getEntityManager().find(Workspace.class, 8);
	}
	
	private static EntityManager getEntityManager(){
		return (EntityManager)Component.getInstance("entityManager");
	}
}
