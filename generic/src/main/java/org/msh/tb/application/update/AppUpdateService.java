package org.msh.tb.application.update;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.application.tasks.AsyncTask;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.entities.SystemConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ricardo on 27/10/14.
 */
@Name("appUpdateService")
public class AppUpdateService {

    @In EntityManager entityManager;
    @In EtbmanagerApp etbmanagerApp;


    private VersionInformation versionInformation;
    private String errorMessage;


    /**
     * Check if the server address is incomplete. Append the name 'etbmanager'
     * at the end of address and the protocol 'http://', if missing
     *
     * @param url is the address of eTB Manager web version
     * @return the address with complements
     */
    protected String checkServerAddress(String url) {
        String server = url;
        // try to fill gaps in the composition of the server address
        if (!server.startsWith("http")) {
            server = "http://" + server;
        }

        if ((!server.endsWith("etbmanager")) && (!server.endsWith("sitetb"))) {
            if ((!server.endsWith("/")) || (!server.endsWith("\\"))) {
                server += "/";
            }
            server += "etbmanager";
        }
        return server;
    }

    /**
     * Return the URL of the new version
     * @return String value
     */
    public String getNewVersionURL() {
        SystemConfig config = etbmanagerApp.getConfiguration();

        String warfile = "etbmanager";
        String countryCode = etbmanagerApp.getCountryCode();
        if ((countryCode != null) && (!countryCode.trim().isEmpty())) {
            warfile += "-" + countryCode.trim().toLowerCase();
        }
        warfile += ".war";

        return config.getUpdateSite() + "/" + warfile;
    }

    /**
     * Return the URL of the XML file containing meta information about the new version
     * @return String value
     */
    public String getMetaInfoURL() {
        return getNewVersionURL() + ".xml";
    }


    /**
     * Connect to the update site and get information about the latest version stored there
     * @return true if there is a new version available
     */
    public VersionInformation checkForUpdate() {
        String path = getMetaInfoURL();

        try {
            System.out.println("Checking new version at " + path);
            HttpResult res = HttpUtils.getURL(new URL(path));
            String content = res.getResult();

            if (res.getResponseCode() != 200) {
                errorMessage = "(" + Integer.toString(res.getResponseCode()) + ") " + res.getErrorMessage();
                return null;
            }

            versionInformation = readXMLInformation(content);
            versionInformation.setUpdateAvailable(isNewVersion(versionInformation));
            return versionInformation;
        }
        catch (Exception e) {
            errorMessage = e.getMessage();
            return null;
        }
    }

    /**
     * Start the download process to get a new version.
     * @return File where the new version will be download to (check getDownloadProgress). If it returns
     * null, check errorMessage for details about the error
     */
    public File downloadNewVersion(VersionInformation vi) {
        if (TaskManager.instance().findTaskByClass(DownloadTask.class) != null) {
            errorMessage = "download on progress";
            return null;
        }

        Map<String, Object> params =  new HashMap<String, Object>();

        // set URL
        params.put(DownloadTask.PARAM_URL, getNewVersionURL());

        // set downloaded file
        File tmpFile;
        try {
            tmpFile = File.createTempFile("etbm", ".war");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        params.put(DownloadTask.PARAM_DESTFILE, tmpFile);

        // set version information
        params.put(DownloadTask.PARAM_VERSIONINFO, vi);

        TaskManager.instance().runTask(DownloadTask.class, params);

        return tmpFile;
    }


    /**
     * Update the current version by the version pointer by the give downloaded file
     * @param newVersionFile points to a file that is the version to replace the current version
     */
    public boolean updateNewVersion(File newVersionFile) {
        if (!newVersionFile.exists()) {
            throw new RuntimeException("File not found: " + newVersionFile);
        }

        String path = etbmanagerApp.getConfiguration().getJbossPath();
        if ((path == null) || (path.isEmpty())) {
            errorMessage = "JBOSS path was not configured. Check system e-TB Manager setup";
            return false;
        }

        // mount war file name
        String countryCode = etbmanagerApp.getCountryCode();
        String warFileName = "etbmanager";
        if ((countryCode != null) && (!countryCode.isEmpty())) {
            warFileName += '-' + countryCode.toLowerCase() + ".war";
        }
        else {
            warFileName += countryCode.toLowerCase() + ".war";
        }

        File warFile = new File(path, "server/default/deploy/MSH/" + warFileName);

        // current war file exists ?
        if (!warFile.exists()) {
            errorMessage = "Current version not found at " + warFile;
            return false;
        }

        // can delete current file ?
        if (!warFile.delete()) {
            errorMessage = "Error when trying to replace " + warFile;
            return false;
        }

        // moving file to the new place
        if (!newVersionFile.renameTo(warFile)) {
            errorMessage = "Error when trying to rename " + newVersionFile + " to " + warFile;
            return false;
        }

        errorMessage = null;
        return true;
    }

    /**
     * Return the download progress of the new version, or null if no version is being downloaded
     * @return file progress between 0 and 100 (%) or null, if no file is being downloaded
     */
    public Integer getDownloadProgress() {
        AsyncTask task = TaskManager.instance().findTaskByClass(DownloadTask.class);
        return task != null? task.getProgress() : null;
    }


    /**
     * Return the object in charge of downloading the data
     * @return instance of {@link org.msh.tb.application.update.DownloadTask}
     */
    public DownloadTask getDownloadTask() {
        return (DownloadTask)TaskManager.instance().findTaskByClass(DownloadTask.class);
    }

    /**
     * Compare the version information in the remote repository with the current version
     * and return true if it's a new version
     * @param vi {@link org.msh.tb.application.update.VersionInformation} containing information about the remote version
     * @return true if the remove version is newer than the current one
     */
    protected boolean isNewVersion(VersionInformation vi) {
        Integer currv;
        try {
            currv = Integer.parseInt(etbmanagerApp.getBuildNumber());
        }
        catch (Exception e) {
            currv = null;
        }
        Integer newv = Integer.parseInt( vi.getBuildNumber() );

        if ((currv == null) && (newv == null)) {
            return false;
        }

        if (currv == null) {
            return true;
        }

        return newv > currv;
    }


    protected VersionInformation readXMLInformation(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));

            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            root.normalize();

            if (!"package".equals(root.getNodeName())) {
                abort("Invalid XML document. Package element not found");
            }

            VersionInformation vi = new VersionInformation();

            vi.setBuildNumber( root.getElementsByTagName("build-number").item(0).getTextContent() );
            vi.setFileName( root.getElementsByTagName("file-name").item(0).getTextContent() );
            vi.setMd5CheckSum( root.getElementsByTagName("MD5").item(0).getTextContent() );
            vi.setBuildDate( convertToDate(root.getElementsByTagName("build-date").item(0).getTextContent()) );
            vi.setVersion(root.getElementsByTagName("version").item(0).getTextContent());

            return vi;
        }
        catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
    }


    /**
     * Convert the given date string into a Date object
     * @param date string containing date in the format yyyy-MM-dd_HH:mm
     * @return Date object
     */
    private Date convertToDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        try {
            return format.parse(date);
        }
        catch (ParseException e) {
            return null;
        }
    }

    private void abort(String msg) {
        throw new RuntimeException(msg);
    }

    public VersionInformation getVersionInformation() {
        return versionInformation;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
