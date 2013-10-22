/**
 * 
 */
package org.msh.tb.sync;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.webservices.RemoteActionHandler;

/**
 * Prepare the answer to the client app that sent the synchronization file. The file is read
 * in the filter {@link SyncFileUploadFilter} (because of SEAM limitations), and
 * this class just prepare the answer with the token ID to be used to follow up
 * the file answer  
 * @author Ricardo Memoria
 *
 */
@Name("receiveSyncFileAction")
public class ReceiveSyncFileAction extends StandardAction {

	private String userToken;
	private String fileToken;
	private File file;


	/** {@inheritDoc}
	 */
	@Override
	@Transactional
	protected void generateResponse() {
		file = tempSyncFileName(userToken);
		if (!file.exists()) {
			throw new RuntimeException("File does not exist");
		}
		
		// authenticate user and start execution
		RemoteActionHandler handler = new RemoteActionHandler(userToken) {
			@Override
			protected Object execute(Object data) throws ValidationException {
				initFileExecution();
				return null;
			}
		};
		handler.setTransactional(false);
		handler.run();
	}

	
	/**
	 * Initialize file execution
	 */
	protected void initFileExecution()  {
		// generate new file token
		UUID uid = UUID.randomUUID();
		fileToken = uid.toString().replace("-", "");

		// start background task
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("file", file);
		params.put("token", fileToken);
		TaskManager.instance().runTask(SyncFileTask.class, params);

		// send file token back to the client
		respondClient();
	}
	
	
	/**
	 * Send token to the user. This token will be used to get status information about
	 * the processing of the sync file and get the answer file
	 */
	private void respondClient() {
		HttpServletResponse resp = getResponse();
		PrintWriter out = getWriter();

		// send the token to the client
		resp.setContentType("text/plain");
		out.write("token=" + fileToken);
	}

	/**
	 * @return the userToken
	 */
	public String getUserToken() {
		return userToken;
	}

	/**
	 * @param userToken the userToken to set
	 */
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	
	/**
	 * Return the full name (including path) of the sync file sent from the client
	 * @param token is the user token ID
	 * @return String value
	 */
	public static File tempSyncFileName(String token) {
		try {
			String tempFolder = System.getProperty("java.io.tmpdir");
			File file = new File(tempFolder, "etbm_" + token + ".tmp");
			return file;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
