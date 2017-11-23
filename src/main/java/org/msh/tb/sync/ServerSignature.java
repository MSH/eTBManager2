/**
 * 
 */
package org.msh.tb.sync;

/**
 * Store information about the server that will be serialized to the client version.
 * This information will be necessary for the client to communicate with the correct
 * server when synchronizing data (avoiding to send data to the wrong server)
 * 
 * @author Ricardo Memoria
 *
 */
public class ServerSignature {

	private String systemURL;
	private String pageRootURL;
	private String countryCode;
	private String adminMail;

	/**
	 * @return the systemURL
	 */
	public String getSystemURL() {
		return systemURL;
	}
	/**
	 * @param systemURL the systemURL to set
	 */
	public void setSystemURL(String systemURL) {
		this.systemURL = systemURL;
	}
	/**
	 * @return the pageRootURL
	 */
	public String getPageRootURL() {
		return pageRootURL;
	}
	/**
	 * @param pageRootURL the pageRootURL to set
	 */
	public void setPageRootURL(String pageRootURL) {
		this.pageRootURL = pageRootURL;
	}
	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}
	/**
	 * @param countryCode the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	/**
	 * @return the adminMail
	 */
	public String getAdminMail() {
		return adminMail;
	}
	/**
	 * @param adminMail the adminMail to set
	 */
	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}
}
