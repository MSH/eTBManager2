package org.msh.tb.client;

import org.msh.tb.client.reports.MainPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Application class on the client side, called when the page is loaded
 * 
 * @author Ricardo Memoria
 *
 */
public class App implements EntryPoint {

	private final RootPanel rootPanel = RootPanel.get("entryPointId"); 

	interface GlobalResources extends ClientBundle {
	    @NotStrict
	    @Source("app.css")
	    CssResource css();
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
	    GWT.<GlobalResources>create(GlobalResources.class).css().ensureInjected();

	    MainPage main = MainPage.instance();
	    rootPanel.getElement().removeAllChildren();
	    rootPanel.add(main);
	    main.initialize();
	}

}
