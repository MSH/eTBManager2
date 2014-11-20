package org.msh.tb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.msh.tb.client.dashboard.DashboardMain;
import org.msh.tb.client.reports.ReportMain;
import org.msh.tb.client.resources.ReportConstants;

/**
 * Application class on the client side, called when the page is loaded
 * 
 * @author Ricardo Memoria
 *
 */
public class App implements EntryPoint {

	private final RootPanel rootPanel = RootPanel.get("entryPointId"); 

	// contain the list of messages to be displayed
	public static final ReportConstants messages = GWT.create(ReportConstants.class);

	/**
	 * Reference to the application CSS resource
	 * @author Ricardo Memoria
	 *
	 */
	interface GlobalResources extends ClientBundle {
	    @NotStrict
	    @Source("app.css")
	    CssResource css();
	}

	/**
	 * This is the entry point of the application
	 */
	public void onModuleLoad() {
	    GWT.<GlobalResources>create(GlobalResources.class).css().ensureInjected();

	    String modname = getModuleName();

	    Widget mainpage;
	    if ("dashboard".equals(modname)) {
	    	mainpage = new DashboardMain();
	    }
	    else {
		    mainpage = new ReportMain();
	    }
	    rootPanel.getElement().removeAllChildren();
	    rootPanel.add(mainpage);
        AppModule module = (AppModule)mainpage;
        AppResources.instance().setModule(module);
	    module.run();
	}

	
	/**
	 * Return the module name to be loaded declared in the HTML page
	 * @return module name
	 */
	public static native String getModuleName() /*-{
		return $wnd.gwtmodule;
	}-*/;
}
