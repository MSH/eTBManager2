/**
 * 
 */
package org.msh.tb.sync.actions;

import java.io.File;

import org.jboss.seam.annotations.Name;
import org.msh.tb.sync.DesktopAnswerFileGenerator;

/**
 * @author Ricardo Memoria
 *
 */
@Name("downloadAnswerAction")
public class DownloadAnswerFileAction extends StandardAction {

	private String fileToken;
	
	/** {@inheritDoc}
	 */
	@Override
	protected void generateResponse() {
		if (fileToken == null)
			throw new RuntimeException("File token was not defined");
	
		File file = DesktopAnswerFileGenerator.getAnswerFileName(fileToken);
		if (!file.exists())
			throw new RuntimeException("Answer file not found for the file token " + fileToken);

		sendFile(file, file.getName());
	}

	/**
	 * @return the fileToken
	 */
	public String getFileToken() {
		return fileToken;
	}

	/**
	 * @param fileToken the fileToken to set
	 */
	public void setFileToken(String fileToken) {
		this.fileToken = fileToken;
	}

}
