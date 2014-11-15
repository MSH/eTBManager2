/**
 *
 */
package org.msh.tb.application.update;

/**
 * Interface that must be implemented to receive feedback
 * about downloading of the initialization file
 * @author Ricardo Memoria
 *
 */
public interface DownloadProgressListener {

    /**
     * Called to update the progress of database importing
     * @param perc is the percentage indicator, ranging from 0 to 100
     */
    void onUpdateProgress(double perc);
}
