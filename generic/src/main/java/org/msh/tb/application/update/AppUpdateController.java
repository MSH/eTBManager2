package org.msh.tb.application.update;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.login.SessionData;

import java.io.File;

/**
 * UI controller used to control the update flow. The flow is:
 * <ul>
 *     <li>checkForUpdate</li>
 *     <li>downloadNewVersion</li>
 *     <li>updateNewVersion</li>
 * </ul>
 * There are some flow control properties that must be used to check the right
 * step on the flow
 * <p/>
 * Created by ricardo on 27/10/14.
 */
@Name("appUpdateController")
public class AppUpdateController {

    private final static String KEY_VERSIONINFO = "versionInfo";

    public enum UpdateStatus {
        INACTIVE,
        UPDATE_AVAILABLE,
        DOWNLOADING,
        READY_TO_UPDATE,
        UPDATED
    };

    @In(create = true) AppUpdateService appUpdateService;
    @In EtbmanagerApp etbmanagerApp;

    private String errorMessage;
    private boolean newVersionUpdated;


    /**
     * Return the status of the update process
     * @return
     */
    public UpdateStatus getStatus() {
        if (newVersionUpdated) {
            return UpdateStatus.UPDATED;
        }

        if (!isUpdateAvailable()) {
            return UpdateStatus.INACTIVE;
        }

        if (isDownloading()) {
            return UpdateStatus.DOWNLOADING;
        }

        if (isNewVersionDownloaded()) {
            return UpdateStatus.READY_TO_UPDATE;
        }

        return UpdateStatus.UPDATE_AVAILABLE;
    }



    public void checkForUpdate() {
        if (isDownloading()) {
            return;
        }
        SessionData.instance().setValue(KEY_VERSIONINFO, null);

        VersionInformation vi = appUpdateService.checkForUpdate();
        errorMessage = appUpdateService.getErrorMessage();
        // save in the session the result
        SessionData.instance().setValue(KEY_VERSIONINFO, vi);
    }


    /**
     * Start the process to download a new version. The download process starts
     * as a background task, and is monitored by the method {@link AppUpdateController#getDownloadProgress()}
     */
    public void downloadNewVersion() {
        if (!isUpdateAvailable()) {
            throw new RuntimeException("There is no newer version available");
        }

        appUpdateService.downloadNewVersion(getVersionInformation());

        // this is just a small delay in order to get the status 'DOWNLOADING', i.e,
        // the necessary time to start the background task
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the download progress, which is a number between 0 and 100.
     * @return int value indicating the download progress between 0 and 100. If value is 100, so the
     * download is completed. If the value returns null, either the download has finished or there was
     * an error while downloading the file. In this case, check the errorMessage property
     */
    public Integer getDownloadProgress() {
        return appUpdateService.getDownloadProgress();
    }


    public boolean isNewVersionDownloaded() {
        VersionInformation vi = getVersionInformation();
        return (vi != null) && (vi.getDownloadedFile() != null);
    }

    /**
     * Update the new version of the system with the one just downloaded
     */
    public void updateNewVersion() {
        VersionInformation vi = getVersionInformation();
        File fname = vi != null? vi.getDownloadedFile(): null;

        if (fname == null) {
            throw new RuntimeException("Version not downloaded");
        }

        newVersionUpdated = appUpdateService.updateNewVersion(fname);

        errorMessage = appUpdateService.getErrorMessage();
    }


    /**
     * Return true if new version is being downloaded
     * @return boolean value
     */
    public boolean isDownloading() {
        return appUpdateService.getDownloadTask() != null;
    }


    /**
     * Return true if there is a new version is available for download
     * @return boolean value
     */
    public boolean isUpdateAvailable() {
        VersionInformation vi = getVersionInformation();
        return vi != null? vi.isUpdateAvailable(): false;
    }

    /**
     * Return information about the version in the update site
     * @return instance of {@link org.msh.tb.application.update.VersionInformation}
     */
    public VersionInformation getVersionInformation() {
        return (VersionInformation)SessionData.instance().getValue(KEY_VERSIONINFO);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
