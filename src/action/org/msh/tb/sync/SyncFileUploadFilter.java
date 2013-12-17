/**
 * 
 */
package org.msh.tb.sync;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.msh.tb.sync.actions.ReceiveSyncFileAction;

/**
 * Receive the file send from the desktop application
 * @author Ricardo Memoria
 *
 */
public class SyncFileUploadFilter implements Filter {

	private static final int maxMemSize = 4 * 1024;
	
	private boolean isMultipart;
	private String token;

	/** {@inheritDoc}
	 */
	@Override
	public void destroy() {
		// do nothing
	}

	/** {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		token = req.getParameter("tk");
		if (token == null)
			throw new RuntimeException("No token defined");
		
		// check if there is a file attached
		isMultipart = ServletFileUpload.isMultipartContent((HttpServletRequest)req);
		PrintWriter out = resp.getWriter();

		if (!isMultipart) {
			throw new RuntimeException("No file informed");
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(maxMemSize);
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));  //File.createTempFile("etbm", null));
		
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> lst = upload.parseRequest((HttpServletRequest)req);
			for (FileItem item: lst) {
				if (!item.isFormField())
					processFileUpload(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
			out.print(e.getMessage());
		}
		filterChain.doFilter(req, resp);
	}

	/**
	 * @param item
	 */
	private void processFileUpload(FileItem item) {
		try {
			File file = ReceiveSyncFileAction.tempSyncFileName(token);
			item.write(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// do nothing
	}

}
