package org.msh.tb.az.eidss;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.tasks.AsyncTaskImpl;
import org.msh.tb.application.tasks.TaskManager;


@Name("eidssTaskImport")
public class EidssTaskImport extends AsyncTaskImpl {
	
	EidssIntHome eidssIntHome;
	@In TaskManager taskManager;
	List<String> cases;
	List<CaseInfo> InfoForExport=new ArrayList<CaseInfo>();
	CaseImporting ci;
	EidssIntConfig config;

	
	@Override
	protected void starting() {
//TODO
	}
	

	
	/* (non-Javadoc)
	 * @see org.msh.tb.application.tasks.DbBatchTask#finishing()
	 */
	@Override
	protected void finishing() {
		
	}

	
	@Override
	public void execute() {	

	}

	/**
	 * login to the EIDSS web services
	 * @return 
	 */
	public boolean loginEIDSS() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Load cases by defined date
	 * @param date Selection criteria - Date
	 * @return 
	 */
	public String loadCaseListByDate(Date date) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Load case by case id from previous created case id list
	 * @return 
	 */
	public void loadCaseById() {
		throw new UnsupportedOperationException();
	}

	/**
	 * If any real import occurred - rollback by delete imported cases
	 * @return 
	 */
	public boolean rollBack() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set special variable to cancel database record loop
	 * @return 
	 */
	public void cancelRecordLoop() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Return human readable task message to show
	 * @return 
	 */
	public String getStateMessage() {
		throw new UnsupportedOperationException();
	}

}