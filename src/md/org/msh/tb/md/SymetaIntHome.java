package org.msh.tb.md;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.SystemParam;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.Workspace;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.tbunits.TBUnitSelection;

/**
 * POJO interface between the user and the execution of the SYMETA integration
 * @author Ricardo Memoria
 *
 */
@Name("symetaIntHome")
@Scope(ScopeType.CONVERSATION)
public class SymetaIntHome {

	private final static String SYMETA_URL_WEBSERVICE = "symeta_url_webservice";
	private final static String DEFAULT_WORKSPACE = "default_workspace";
	private final static String DEFAULT_ADMINUNIT = "default_adminunit";
	private final static String DEFAULT_TBUNIT = "default_tbunit";
	private final static String DEFAULT_HEALTHSYSTEM = "default_healthsystem";
	private final static String EMAIL_REPORTS = "email_reports";
	
	@In EntityManager entityManager;
	@In(required=false) Workspace defaultWorkspace;
	@In(create=true) SymetaIntegration symetaIntegration;
	@In(create=true) FacesMessages facesMessages;

	
	private MoldovaServiceConfig config;
	private boolean configSaved;
	private TBUnitSelection tbunitSelection;
	private AdminUnitSelection auselection;

	/**
	 * Execute the integration with SYMETA system 
	 */
	public String execute() {
		try {
			UserLogin userLogin = (UserLogin)Component.getInstance("userLogin", false);
			symetaIntegration.execute( getConfig(), (userLogin != null? userLogin.getUser(): null) );
			
		} catch (Exception e) {
			facesMessages.add(e.getMessage());
			return "error";
		}
		
		facesMessages.addFromResourceBundle("ro.admin.symeta.success");
		return "success";
	}

	
	@Observer("system-timer-event")
	public void systemTimerListener() {
		System.out.println("Executing automatic SYMETA integration...");
		execute();
	}


	/**
	 * Erase all cases imported
	 */
	public void eraseCases() {
		entityManager.createQuery("delete from TbCase " +
			 "where patient.id in (select aux.id from Patient aux where aux.workspace.id=#{defaultWorkspace.id})")
			 .executeUpdate();
	}


	/**
	 * Initialize GUI to make changes in the configuration
	 */
	@Begin(join=true)
	public void initConfigEditing() {
		AdministrativeUnit au = getConfig().getDefaultAdminUnit();
		getAuselection().setSelectedUnit(au);
		
		Tbunit unit = getConfig().getDefaultTbunit();
		getTbunitSelection().setTbunit(unit);

		configSaved = false;
	}


	/**
	 * Save symeta configuration parameters
	 * @return
	 */
	public String saveConfigs() {
		Tbunit unit = tbunitSelection.getTbunit();
		AdministrativeUnit au = auselection.getSelectedUnit();
		
		if ((unit == null) || (au == null))
			return "error"; 
		
		config.setDefaultAdminUnit(au);
		config.setDefaultTbunit(unit);
		
		saveParameter(SYMETA_URL_WEBSERVICE, config.getWebServiceURL());
		saveParameter(DEFAULT_ADMINUNIT, Integer.toString( config.getDefaultAdminUnit().getId() ));
		saveParameter(DEFAULT_TBUNIT, Integer.toString( config.getDefaultTbunit().getId() ));
		saveParameter(DEFAULT_HEALTHSYSTEM, Integer.toString( config.getDefaultHealthSystem().getId() ));
		saveParameter(DEFAULT_WORKSPACE, Integer.toString( config.getWorkspace().getId() ));
		saveParameter(EMAIL_REPORTS, config.getEmailReport() );

		configSaved = true;
		entityManager.flush();

		Conversation.instance().end();

		return "config-saved";
	}
	

	/**
	 * Create symeta configuration file
	 */
	private void createConfig() {
		config = new MoldovaServiceConfig();
		config.setWorkspace(getDefaultWorkspace());	

		config.setWebServiceURL( readParameter(SYMETA_URL_WEBSERVICE) );
		config.setEmailReport( readParameter(EMAIL_REPORTS) );
		
		config.setDefaultAdminUnit( getEntityFromSystemParameter(AdministrativeUnit.class, DEFAULT_ADMINUNIT) );
		config.setDefaultTbunit( getEntityFromSystemParameter(Tbunit.class, DEFAULT_TBUNIT) );
		config.setDefaultHealthSystem( getEntityFromSystemParameter(HealthSystem.class, DEFAULT_HEALTHSYSTEM) );
		config.setWorkspace( getEntityFromSystemParameter(Workspace.class, DEFAULT_WORKSPACE));
	}


	protected <E> E getEntityFromSystemParameter(Class<E> entityClass, String paramKey) {
		Integer id = readIntegerParameter(paramKey);
		if (id == null)
			return null;
		
		return (E)entityManager.find(entityClass, id);
	}
	
	protected void saveParameter(String key, String value) {
		SystemParam p;
		try {
			p = entityManager.find(SystemParam.class, key);
		} catch (Exception e) {
			p = null;
		}
		
		if (p == null) {
			p = new SystemParam();
			p.setKey(key);
			defaultWorkspace = entityManager.merge(defaultWorkspace);
			p.setWorkspace(defaultWorkspace);
		}
		
		p.setValue(value);
		entityManager.persist(p);
	}

	
	/**
	 * Read a parameter and convert it to an integer value
	 * @param param parameter
	 * @return integer value of the parameter
	 */
	protected Integer readIntegerParameter(String param) {
		String val = readParameter(param);
		if ((val == null) || (val.isEmpty()))
			return null;
		
		return Integer.parseInt(val);
	}
	
	
	/**
	 * Read a string parameter from the database
	 * @param param name of the parameter
	 * @return parameter value
	 */
	protected String readParameter(String param) {
		try {
			SystemParam sysparam = (SystemParam)entityManager
				.createQuery("from SystemParam sp where sp.workspace.id = :id and sp.key = :param")
				.setParameter("id", config.getWorkspace().getId())
				.setParameter("param", param)
				.getSingleResult();
			return sysparam.getValue();
		} catch (Exception e) {
			return null;
		}
	}

	
	protected Workspace getDefaultWorkspace() {
		if (defaultWorkspace == null) {
			defaultWorkspace = entityManager.find(Workspace.class, 22564);
		}
		return defaultWorkspace;
	}

	public MoldovaServiceConfig getConfig() {
		if (config == null)
			createConfig();
		return config;
	}


	public boolean isConfigSaved() {
		return configSaved;
	}


	public TBUnitSelection getTbunitSelection() {
		if (tbunitSelection == null)
			tbunitSelection = new TBUnitSelection();
		return tbunitSelection;
	}


	public AdminUnitSelection getAuselection() {
		if (auselection == null)
			auselection = new AdminUnitSelection();
		return auselection;
	}

}
