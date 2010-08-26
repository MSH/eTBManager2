package org.msh.tb.md;

import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.HealthSystem;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;

public class MoldovaServiceConfig {

	private String webServiceURL;
	private int interval;
	private Workspace workspace;
	private AdministrativeUnit defaultAdminUnit;
	private HealthSystem defaultHealthSystem;
	private Tbunit defaultTbunit;


	/**
	 * @return the webServiceURL
	 */
	public String getWebServiceURL() {
		return webServiceURL;
	}
	/**
	 * @param webServiceURL the webServiceURL to set
	 */
	public void setWebServiceURL(String webServiceURL) {
		this.webServiceURL = webServiceURL;
	}
	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}
	/**
	 * @return the workspace
	 */
	public Workspace getWorkspace() {
		return workspace;
	}
	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	/**
	 * @return the defaultAdminUnit
	 */
	public AdministrativeUnit getDefaultAdminUnit() {
		return defaultAdminUnit;
	}
	/**
	 * @param defaultAdminUnit the defaultAdminUnit to set
	 */
	public void setDefaultAdminUnit(AdministrativeUnit defaultAdminUnit) {
		this.defaultAdminUnit = defaultAdminUnit;
	}
	/**
	 * @return the defaultHealthSystem
	 */
	public HealthSystem getDefaultHealthSystem() {
		return defaultHealthSystem;
	}
	/**
	 * @param defaultHealthSystem the defaultHealthSystem to set
	 */
	public void setDefaultHealthSystem(HealthSystem defaultHealthSystem) {
		this.defaultHealthSystem = defaultHealthSystem;
	}
	/**
	 * @return the defaultTbunit
	 */
	public Tbunit getDefaultTbunit() {
		return defaultTbunit;
	}
	/**
	 * @param defaultTbunit the defaultTbunit to set
	 */
	public void setDefaultTbunit(Tbunit defaultTbunit) {
		this.defaultTbunit = defaultTbunit;
	}
}
