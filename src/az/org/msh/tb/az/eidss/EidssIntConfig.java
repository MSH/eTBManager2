package org.msh.tb.az.eidss;

import java.util.Date;

/**
 * Simple POJO class for temporary store Eidss integration config information
 * @author alexey
 *
 */
public class EidssIntConfig {
	/**
	 * URL to access EIDSS web service
	 */
	public String url;
	/**
	 * Organization name - required for WS login
	 */
	public String orgName;
	/**
	 * login name to access EIDSS web service
	 */
	public String login;
	/**
	 * password to access EIDSS web service
	 */
	public String password;
	/**
	 * only cases with these diagnosis must be loaded
	 * String - codes from EIDSS separated by comma
	 */
	public String diagnosis;
	/**
	 * only these case states must be loaded
	 * String - codes from EIDSS separated by comma
	 */
	public String  caseStates;
	/**
	 * load cases from this date (inclusive)
	 */
	public Date from;
	/**
	 * load cases to this date
	 */
	public Date toDate;
	/**
	 * if true - turn on auto loading
	 */
	public Boolean auto;
	/**
	 * language to login
	 */
	public String  language;
	/**
	 * WS timeout in ms
	 */
	public String timeout;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	public String getCaseStates() {
		return caseStates;
	}
	public void setCaseStates(String caseStates) {
		this.caseStates = caseStates;
	}
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	
	public Date getToDate() {
		return toDate;
	}
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	public Boolean getAuto() {
		return auto;
	}
	public void setAuto(Boolean auto) {
		this.auto = auto;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getLanguage() {
		return this.language;
	}
	public void setLanguage(String language){
		this.language = language;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

}
