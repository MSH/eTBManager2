package org.msh.tb.client;

import com.google.gwt.core.client.GWT;
import org.msh.tb.client.resources.ImageResources;
import org.msh.tb.client.resources.ReportConstants;
import org.msh.tb.client.shared.ReportService;
import org.msh.tb.client.shared.ReportServiceAsync;
import org.msh.tb.reports2.ReportResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Expose all service interfaces that interact with the server side
 * Created by ricardo on 10/07/14.
 */
public class AppResources {
    private static final AppResources singleton = new AppResources();

    private ReportServiceAsync reportService;
    private HashMap<String, Object> resources = new HashMap<String, Object>();
    private ImageResources imageResources;
    private ReportConstants messages;

    /**
     * Return the unique instance of the services
     * @return
     */
    public static AppResources instance() {
        return singleton;
    }


    /**
     * Return the object with exposed services to the report
     * @return instance of {@link org.msh.tb.client.shared.ReportServiceAsync}
     */
    public static ReportServiceAsync reportServices() {
        if (singleton.reportService == null) {
            singleton.reportService = GWT.create(ReportService.class);
        }
        return singleton.reportService;
    }

    /**
     * Get a resource included previously in the application
     * @param name the resource name
     * @return Object resource
     */
    public Object get(String name) {
        return resources.get(name);
    }

    /**
     * Store an in-memory resource in the application
     * @param name the resource name
     * @param resource the resource object
     */
    public void set(String name, Object resource) {
        resources.put(name, resource);
    }

    /**
     * Remove all resources that start with the given resource name
     * @param startName the prefix of the resource name
     */
    public void clearResources(String startName) {
        if ((startName == null) || (startName.isEmpty())) {
            resources.clear();
            return;
        }
        List<String> names = new ArrayList<String>();
        for (String key: resources.keySet()) {
            if (key.startsWith(startName)) {
                names.add(key);
            }
        }

        for (String key: names) {
            names.remove(key);
        }
    }

    /**
     * Return the available images used in the application
     * @return instance of {@link org.msh.tb.client.resources.ImageResources}
     */
    public static ImageResources imageResources() {
        if (singleton.imageResources == null) {
            singleton.imageResources = GWT.create(ImageResources.class);
        }

        return singleton.imageResources;
    }

    /**
     * Return the list of messages available in the application
     * @return instance of ReportConstants interface
     */
    public static ReportConstants messages() {
        if (singleton.messages == null) {
            singleton.messages = GWT.create(ReportConstants.class);
        }
        return singleton.messages;
    }
}
