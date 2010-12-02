package org.msh.tb.application;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Return client browser info and test if it's a valid browser.
 * This class check if browser is a valid one (for example, Internet Explorer 6 is not allowed).
 * If browser is not allowed, user will have to use another one
 * @author Ricardo Memoria
 *
 */
@Name("browserInfo")
@BypassInterceptors
public class BrowserInfo {

	public enum BrowserType {
		MSIE, FIREFOX, CHROME, OTHER;
	}
	
	private String userAgent;
	private BrowserType browserType;
	private Integer browserVersion;

	
	/**
	 * Check if browser is a valid  browser for e-TB manager
	 * @return
	 */
	public boolean isValidBrowser() {
		if (browserType == null)
			updateBrowserInfo();

		// is Internet Explorer under version 8? so it's not valid 
		if ((browserType == BrowserType.MSIE) && (browserVersion < 800))
			return false;
		
		return true;
	}

	
	public BrowserType getBrowserType() {
		if (browserType == null)
			updateBrowserInfo();
		return browserType;
	}


	/**
	 * Get browser version in the format AABB, where AA is the major version and BB is the minor version number
	 * @return
	 */
	public int getBrowserVersion() {
		if (browserVersion == null)
			updateBrowserInfo();
		return browserVersion;
	}

	private void updateBrowserInfo() {
		updateUserAgent();
		if (userAgent == null)
			return;
		
		if (userAgent.indexOf("Firefox/") != -1)
			getFirefoxInfo();
		else
		if (userAgent.indexOf("MSIE") != -1) 
			getInternetExplorerInfo();
		else
		if (userAgent.indexOf("Chrome/") != -1)
			getGoogleChromeInfo();
		else {
			browserType = null;
			browserVersion = 0;
		}
	}

	/**
	 * Get user agent, with information about the client browser
	 */
	private void updateUserAgent() {
		if (userAgent != null)
			return;
		
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		if (request == null)
			return;
		
		userAgent = request.getHeader("User-Agent");
	}


	/**
	 * Get information about Mozilla Firefox version 
	 */
	private void getFirefoxInfo() {
		browserType = BrowserType.FIREFOX;
		updateBrowserVersion("Firefox/", ' ');
	}


	/**
	 * Get information about Internet Explorer version
	 */
	private void getInternetExplorerInfo() {
		browserType = BrowserType.MSIE;
		updateBrowserVersion("MSIE ", ';');
	}

	
	/**
	 * Get information about Google Chrome version
	 */
	private void getGoogleChromeInfo() {
		browserType = BrowserType.CHROME;
		updateBrowserVersion("Chrome/", ' ');
	}
	
	private void updateBrowserVersion(String token, char delimiter) {
		int pos = userAgent.indexOf(token);
		String s = userAgent.substring(pos + token.length());
		pos = s.indexOf(delimiter);
		if (pos != -1)
			s = s.substring(0, pos);
		String[] vals = s.split("\\.");
		if (vals.length == 0)
			browserVersion = 0;
		else
		if (vals.length == 1)
			browserVersion = Integer.parseInt(vals[0]) * 100;
		else {
			browserVersion = (Integer.parseInt(vals[0]) * 100) + Integer.parseInt(vals[1]);
		}
	}
}
