package org.msh.reports.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.msh.reports.ReportConfiguration;
import org.msh.reports.datatable.DataTable;

public class SQLQuery {

	private Map<String, Object> parameters = new HashMap<String, Object>();
	private Map<Integer, Object> parsedParameters = new HashMap<Integer, Object>();
	
	/**
	 * Execute the query and return an instance of the {@link DataTable} with its content
	 * @param sql Query to be executed in a SQL format
	 * @return {@link DataTable} instance containing result of the query
	 */
	public DataTable execute(String sql) {
		DataTable tbl = new DataTable();

		ResultSet rs = null;
		try {
			Connection conn = ReportConfiguration.instance().getConnection();

			String parsedSql = parseParameters(sql);
			System.out.println(parsedSql);
			PreparedStatement smt = conn.prepareStatement(parsedSql);
			fillParameters(smt);
			rs = smt.executeQuery();

			fillDataTable(tbl, rs);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return tbl;
	}

	
	/**
	 * Fill the parameters of the query
	 * @param smt
	 * @throws SQLException
	 */
	private void fillParameters(PreparedStatement smt) throws SQLException {
		for (Integer index: parsedParameters.keySet()) {
			Object value = parsedParameters.get(index);
			smt.setObject(index, value);
		}
	}


	/**
	 * Fill instance of {@link DataTable} with content of {@link ResultSet} from the executed SQL query
	 * @param tbl
	 * @param rs
	 * @throws SQLException
	 */
	private void fillDataTable(DataTable tbl, ResultSet rs) throws SQLException {
		// create columns
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int numCols = rsmd.getColumnCount();
		
		// initialize data table
		tbl.resize(numCols, 0);
		
		// create columns keys as name of the columns in the result set
		for (int i = 0; i < numCols; i++) {
			String colname = rsmd.getColumnName(i + 1);
			tbl.getColumns().get(i).setKey(colname);
		}

		// fill the data table
		while (rs.next()) {
			tbl.insertRow();
			int r = tbl.getRowCount() - 1;
			for (int c = 0; c < rsmd.getColumnCount(); c++) {
				Object obj = rs.getObject(c + 1);
				tbl.setValue(c, r, obj);
			}
		}
	}


	/**
	 * Set query parameters
	 * @param param
	 * @param value
	 */
	public SQLQuery setParameter(String param, Object value) {
		parameters.put(param, value);
		return this;
	}
	
	
	/**
	 * Parse parameters of SQL instruction
	 * @param sql
	 */
	protected String parseParameters(String sql) {
		StringBuilder builder = new StringBuilder(sql.length());

		int pnum = 1;
		int index = 0;
		while (index < sql.length()) {
			char c = sql.charAt(index);
			boolean handled = false;
			
			// handle ' and "
			if ((c == '\'') || (c == '"')) {
				int i = index + 1;
				i = sql.indexOf(c, i);
				if (i > index) {
					String s = sql.substring(index - 1, i);
					builder.append(s);
					handled = true;
					index = i;
				}
			}
			
			// handle char :
			if (c == ':') {
				int i = index;
				i++;
				if (i < sql.length()) {
					while ((i < sql.length()) && (Character.isJavaIdentifierPart(sql.charAt(i))))
						i++;
					String name = sql.substring(index + 1, i);
					if (!name.isEmpty()) {
						Object val = parameters.get(name);
						parsedParameters.put(pnum, val);
						pnum++;
						builder.append("?");
						handled = true;
						index = i - 1;
					}
				}
			}
			
			if ((c == '#') || (c == '$')) {
				int i = index + 1;
				if (i < sql.length()) {
					if (sql.charAt(i) == '{') {
						int f = sql.indexOf("}", i);
						if (f > i) {
							String name = sql.substring(i+1, f);
							parsedParameters.put(pnum, resolveName(name));
							pnum++;
							index = f;
							builder.append("?");
							handled = true;
						}
					}
				}
			}
			
			if (!handled)
				builder.append(c);

			index++;
		}
		
		return builder.toString();
	}
	
	
	/**
	 * @param name
	 * @return
	 */
	protected Object resolveName(String name) {
		return ReportConfiguration.instance().resolveName(name);
	}
}
