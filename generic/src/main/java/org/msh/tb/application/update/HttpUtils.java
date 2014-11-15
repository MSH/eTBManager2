package org.msh.tb.application.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by ricardo on 30/10/14.
 */
public class HttpUtils {

    private static final int BUFFER_SIZE = 65535;


    public static HttpResult getURL(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        try {
            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
                return new HttpResult(responseCode, null, getHttpResponseCodeMessage(responseCode));

            String disposition = conn.getHeaderField("Content-Disposition");
            String contentType = conn.getContentType();
            Integer contentLength = conn.getContentLength();

            String filename = null;
            if (disposition != null) {
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    filename = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            }

            InputStream in = conn.getInputStream();

            Scanner s = new Scanner(in).useDelimiter("\\A");

            return new HttpResult(responseCode, s.hasNext()? s.next(): "", getHttpResponseCodeMessage(responseCode));

        } finally {
            conn.disconnect();
        }
    }


    /**
     * Return the message according to the server answer
     * @param code
     * @return
     */
    public static String getHttpResponseCodeMessage(int code) {
        if (code == 200) {
            return "OK";
        }

        if ((code >= 500) && (code <= 599)) {
            return "Internal error";
        }

        if ((code >= 300) && (code <= 399)) {
            return "Server answered with a redirection";
        }

        if (code == 404) {
            return "Resource not found";
        }

        return "Unexpected response code from server";
    }


    /**
     * Download a file from an URL using a listener to get information about the download progress
     * @param url is the URL to download the file from
     * @param destFile where downloaded file will be saved to (path + filename)
     * @param listener instance of {@link DownloadProgressListener} to get information about download progress
     * @return instance of {@link java.io.File} pointing to the downloaded file
     * @throws Exception
     */
    public static HttpResult downloadFromURL(URL url, File destFile, DownloadProgressListener listener) throws Exception  {
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        try {
            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
                return new HttpResult(responseCode, null, getHttpResponseCodeMessage(responseCode));

            String disposition = conn.getHeaderField("Content-Disposition");
            String contentType = conn.getContentType();
            Integer contentLength = conn.getContentLength();

            String filename = null;
            if (disposition != null) {
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    filename = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            }

            InputStream in = conn.getInputStream();
            FileOutputStream fout = new FileOutputStream(destFile);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            long totalBytesRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                fout.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (listener != null) {
                    double perc = (double)totalBytesRead * 100/(double)contentLength;
                    listener.onUpdateProgress(perc);
                }
            }

            fout.close();
            return new HttpResult(200, destFile.getAbsolutePath(), null);

        } finally {
            conn.disconnect();
        }
    }

}
