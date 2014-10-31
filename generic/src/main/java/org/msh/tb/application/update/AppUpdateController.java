package org.msh.tb.application.update;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.application.SystemConfigHome;
import org.msh.tb.entities.SystemConfig;

import java.io.File;
import java.net.URL;

/**
 * Created by ricardo on 27/10/14.
 */
@Name("appUpdateController")
public class AppUpdateController {

    @In(create = true) AppUpdateService appUpdateService;
    @In EtbmanagerApp etbmanagerApp;

    private boolean updateAvailable;
    private String errorMessage;

    public void checkForUpdate() {
        updateAvailable = appUpdateService.checkForUpdate();
        errorMessage = appUpdateService.getErrorMessage();
    }


    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public VersionInformation getVersionInformation() {
        return appUpdateService.getVersionInformation();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
