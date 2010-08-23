package org.msh.tb.application;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.mdrtb.entities.SystemConfig;

/**
 * Store application specific information
 * @author Ricardo Memoria
 *
 */
@Name("etbmanagerApp")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class EtbmanagerApp {

	/**
	 * e-TB Manager global configuration 
	 */
	private SystemConfig configuration;
	
	/**
	 * e-TB Manager implementation version 
	 */
	private String implementationVersion;
	
	/**
	 * Title of the implementation version 
	 */
	private String implementationTitle;
	
	/**
	 * Title of the implementation version 
	 */
	private String implementationVendor;
	
	/**
	 * e-TB Manager version build date and time
	 */
	private String buildDate;
	
	/**
	 * Java version and build used to generate this version of e-TB Manager
	 */
	private String javaBuildVersion;

	/**
	 * Name of the user that built this version of e-TB Manager
	 */
	private String builtBy;
	
	/**
	 * Ant version that create this version of e-TB Manager
	 */
	private String antVersion;
	
	/**
	 * If it's a specific version for a country, it stores the country code, otherwise, it's empty
	 */
	private String countryCode;
	
	/**
	 * Check if version information was loaded
	 */
	private boolean versionInfoLoaded = false;


	/**
	 * Load configuration information from the database
	 */
	protected SystemConfig loadConfiguration() {
		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
		
		List<SystemConfig> lst = entityManager.createQuery("from SystemConfig s where s.id = 1").getResultList();
		if (lst.size() > 0) {
			return lst.get(0);
		}

		SystemConfig cfg = new SystemConfig();
		cfg.setId(1);
		entityManager.persist(cfg);
		entityManager.flush();
		return cfg;
	}


	/**
	 * Load version information from the MANIFEST.MF file in the e-TB Manager package war file
	 */
	protected void initializeVersionInfo() {
		versionInfoLoaded = false;
		if (versionInfoLoaded)
			return;
		
		ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
		
		Properties prop = new Properties();
		try {
			prop.load( servletContext.getResourceAsStream("/META-INF/MANIFEST.MF") );
			
			readManifestProperties(prop);
			
			versionInfoLoaded = true;
		} catch (IOException e) {
			// do nothing...
			e.printStackTrace();
		}
	}


	/**
	 * Read the MANIFEST.MF properties loaded in a {@link Properties} object
	 * @param prop
	 */
	private void readManifestProperties(Properties prop) {
		implementationVersion = (String)prop.get("Implementation-Version"); 
		implementationTitle = (String)prop.get("Implementation-Title"); 
		implementationVendor = (String)prop.get("Implementation-Vendor"); 
		builtBy = (String)prop.get("Built-By");
		antVersion = (String)prop.get("Ant-Version");
		javaBuildVersion = (String)prop.get("Build-Jdk");
		buildDate = (String)prop.get("Built-Date");
		countryCode = (String)prop.get("Country-Code");
	}

	
	
	
	public SystemConfig getConfiguration() {
		if (configuration == null)
			configuration = loadConfiguration();
		return configuration;
	}

	public void setConfiguration(SystemConfig configuration) {
		this.configuration = configuration;
	}



	public String getImplementationVersion() {
		initializeVersionInfo();
		return implementationVersion;
	}



	public String getBuildDate() {
		initializeVersionInfo();
		return buildDate;
	}




	public String getJavaBuildVersion() {
		return javaBuildVersion;
	}



	public String getBuiltBy() {
		return builtBy;
	}



	public String getAntVersion() {
		return antVersion;
	}


	public String getCountryCode() {
		return countryCode;
	}


	public String getImplementationTitle() {
		return implementationTitle;
	}


	public String getImplementationVendor() {
		return implementationVendor;
	}

}
