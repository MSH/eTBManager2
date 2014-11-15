package org.msh.tb.application.update;

import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.login.SessionData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Background task to download a file from a given URL and return progress information
 *
 * Created by ricardo on 12/11/14.
 */
public class DownloadTask extends AsyncTaskImpl {

    public static final String PARAM_DESTFILE = "destFile";
    public static final String PARAM_URL = "url";
    public static final String PARAM_VERSIONINFO = "versionInfo";

    private File destFile;
    private URL url;
    private VersionInformation versionInformation;

    @Override
    protected void starting() {
        destFile = (File)getParameter(PARAM_DESTFILE);
        String surl = (String)getParameter(PARAM_URL);
        versionInformation = (VersionInformation)getParameter(PARAM_VERSIONINFO);

        if (destFile == null) {
            throw new RuntimeException("Destination file was not specified");
        }

        if (surl == null) {
            throw new RuntimeException("URL was not specified");
        }

        try {
            url = new URL(surl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void execute() {
/*
        for (int i = 0; i <= 100; i++) {
            setProgress(i);
            System.out.println("Progress = " + i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        destFile = new File("/tmp/etbm.war");
*/

        try {
            HttpUtils.downloadFromURL(url, destFile, new DownloadProgressListener() {
                @Override
                public void onUpdateProgress(double perc) {
                    setProgress((int) perc);

                    if (isCanceling()) {
                        throw new RuntimeException("Cancelling download...");
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (versionInformation != null) {
            versionInformation.setDownloadedFile(destFile);
        }
    }

    @Override
    protected void finishing() {
        System.out.println("Finished");
    }


    /* (non-Javadoc)
     * @see org.msh.tb.application.tasks.AsyncTaskImpl#isUnique()
     */
    @Override
    public boolean isUnique() {
        return true;
    }
}
