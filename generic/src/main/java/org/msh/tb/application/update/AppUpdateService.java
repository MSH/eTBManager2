package org.msh.tb.application.update;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.entities.SystemConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     * Connect to the update site and get information about the latest version stored there
     * @return true if there is a new version available
     */
    public boolean checkForUpdate() {
        SystemConfig config = etbmanagerApp.getConfiguration();

        String warfile = "etbmanager";
        String countryCode = etbmanagerApp.getCountryCode();
        if (countryCode != null) {
            warfile += "-" + countryCode.toLowerCase();
        }
        warfile += ".war";

        String path = config.getUpdateSite() + "/" + warfile + ".xml";

        try {
            HttpResult res = HttpUtils.getURL(new URL(path));
            String content = res.getResult();

            if (res.getResponseCode() != 200) {
                errorMessage = "(" + Integer.toString(res.getResponseCode()) + ") " + res.getErrorMessage();
                return false;
            }

            versionInformation = readXMLInformation(content);
            return isNewVersion(versionInformation);
        }
        catch (Exception e) {
            return false;
        }
    }


    /**
     * Compare the version information in the remote repository with the current version
     * and return true if it's a new version
     * @param vi {@link org.msh.tb.application.update.VersionInformation} containing information about the remote version
     * @return true if the remove version is newer than the current one
     */
    protected boolean isNewVersion(VersionInformation vi) {
        Integer currv = Integer.parseInt(etbmanagerApp.getBuildNumber());
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

            System.out.println(root.getNodeName());

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
