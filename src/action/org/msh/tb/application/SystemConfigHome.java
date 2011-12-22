package org.msh.tb.application;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.SystemConfig;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserProfile;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.TransactionLogService;

@Name("systemConfigHome")
@Scope(ScopeType.CONVERSATION)
public class SystemConfigHome {

	@In(create=true) EntityManager entityManager;
	@In(create=true) EtbmanagerApp etbmanagerApp;
	
	private SystemConfig systemConfig;
	private Workspace workspace;
	private List<Tbunit> units;
	private List<UserProfile> profiles;
	private TransactionLogService logService;

	/**
	 * Return the system configuration
	 * @return instance of {@link SystemConfig}
	 */
//	@Factory("systemConfig")
	public SystemConfig getSystemConfig() {
		if (systemConfig != null)
			return systemConfig;

		systemConfig = etbmanagerApp.loadConfiguration();

		return systemConfig;
	}


	/**
	 * Save changes in the system configuration
	 * @return "success" if it was successfully saved
	 */
	public String saveCofiguration() {
		if (systemConfig == null)
			return "error";

		systemConfig.setWorkspace(workspace);
		
		entityManager.persist(systemConfig);
		entityManager.flush();
		
		if (logService != null) {
			logService.save("SYSSETUP", RoleAction.EDIT, null, null);
		}
		
		etbmanagerApp.setConfiguration(systemConfig);
		
		return "success";
	}


	/**
	 * Return the list of TbUnits for the selected workspace
	 * @return List of {@link Tbunit} objects
	 */
	public List<Tbunit> getUnits() {
		if ((units == null) && (workspace != null)) {
			units = entityManager.createQuery("from Tbunit unit where unit.workspace.id = :id")
				.setParameter("id", workspace.getId())
				.getResultList();
		}
		return units;
	}

	
	/**
	 * Return the list of profiles for the selected workspace
	 * @return List of {@link UserProfile} objects
	 */
	public List<UserProfile> getProfiles() {
		if ((profiles == null) && (workspace != null)) {
			profiles = entityManager.createQuery("from UserProfile p where p.workspace.id = :id")
						.setParameter("id", workspace.getId())
						.getResultList();
		}
		return profiles;
	}
	
	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		units = null;
		profiles = null;
	}

	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	
	/**
	 * Initialize the system configuration for editing 
	 */
	public void initializeEditing() {
		if (workspace == null) {
			workspace = getSystemConfig().getWorkspace();
			logService = new TransactionLogService();
			logService.recordEntityState(getSystemConfig(), Operation.EDIT);
		}
	}


	/**
	 * Force the system to use the brazilian locale
	 */
	public void selectBrazilianLocale() {
		LocaleSelector localeSelector = LocaleSelector.instance();
		localeSelector.setLanguage("pt_BR");
		localeSelector.select();
	}
}
