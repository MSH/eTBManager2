package org.msh.tb.ge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;

import org.msh.tb.entities.Workspace;

public abstract class ImportTableData {
	private String sql;
	
	public void uploadRegionData(Date lastIntDate, Connection con,
			ResultSet rsCases, Workspace workspace,
			int noOfRecord, EntityManager entityManager) throws Exception {
		loadDataToImport(lastIntDate, con, rsCases);
		importData(rsCases, workspace, noOfRecord, entityManager);

	}

	/**
	 * Load cases to import
	 * 
	 * @throws SQLException
	 */
	protected void loadDataToImport(Date lastIntDate, Connection con,
			ResultSet rsCases) throws SQLException {
		rsCases = con.createStatement().executeQuery(getSql());
	}
	
	/**
	 * Import a case
	 * 
	 * @throws Exception
	 */
	protected abstract void importData(ResultSet rsCases, Workspace workspace, 
			int noOfRecord, EntityManager entityManager)
			throws Exception ;

	protected abstract String getSql();

}
