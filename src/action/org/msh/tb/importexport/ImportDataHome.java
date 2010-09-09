package org.msh.tb.importexport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.Workspace;

/**
 * Collects information to import data from a file to a table
 * @author Ricardo Memoria
 *
 */
@Name("importDataHome")
public class ImportDataHome {

	@In(create=true) ImportAsyncExecution importAsyncExecution;
	@In(required=true) Workspace defaultWorkspace;
	@In(create=true) FacesMessages facesMessages;
	
	// Informs which table to import
	private ImportTable table;
	
	// Data to be imported. Up to now, just csv files are supported
	private InputStream file;
	
	private Character delimiter = ';';
	
	private String charset = "UTF-8";

	private List<SelectItem> charsets;
	
	private List<SelectItem> delimiters;

	/**
	 * Execute the importing of data calling the proper class to handle the importing procedures.
	 * @return "imported" if successfully imported, otherwise returns "error"
	 */
	public String execute() {
		if (file == null)
			return "error";
		
		importAsyncExecution.execute(table, file, charset, delimiter, defaultWorkspace, (UserLogin)Component.getInstance("userLogin", true));

		facesMessages.add("The data is being imported. The system will send you an e-mail with the status of the importing when it's finished");
		return "imported";
	}
	

	/**
	 * Return list of available tables to be selected by the user
	 * @return Array of {@link ImportTable} enumerations
	 */
	public ImportTable[] getTables() {
		return ImportTable.values();
	}


	/**
	 * Define the tables that the system supports importing of data
	 * @author Ricardo Memoria
	 *
	 */
	public enum ImportTable {
		ADMIN_UNITS (AdminUnitImport.class);
		
		// class used to import data
		private Class importClass;
		
		private ImportTable(Class importClass) {
			this.importClass = importClass;
		}

		public Class getImportClass() {
			return importClass;
		}
		
		public String getMessageKey() {
			switch (this) {
			case ADMIN_UNITS:
				return "admin.adminunits";
			default:
				return toString();
			}
		}
	}

	public ImportTable getTable() {
		return table;
	}
	public void setTable(ImportTable table) {
		this.table = table;
	}
	public InputStream getFile() {
		return file;
	}
	public void setFile(InputStream file) {
		this.file = file;
	}


	public Character getDelimiter() {
		return delimiter;
	}


	public void setDelimiter(Character delimiter) {
		this.delimiter = delimiter;
	}


	public String getCharset() {
		return charset;
	}


	public void setCharset(String charset) {
		this.charset = charset;
	}


	public List<SelectItem> getCharsets() {
		if (charsets == null) {
			charsets = new ArrayList<SelectItem>();
			charsets.add(new SelectItem("US-ASCII", "US-ASCII"));
			charsets.add(new SelectItem("ISO-8859-1", "ISO-8859-1"));
			charsets.add(new SelectItem("UTF-8", "UTF-8"));
			charsets.add(new SelectItem("UTF-16BE", "UTF-16BE"));
			charsets.add(new SelectItem("UTF-16LE", "UTF-16LE"));
			charsets.add(new SelectItem("UTF-16", "UTF-16"));
		}
		return charsets;
	}


	public List<SelectItem> getDelimiters() {
		if (delimiters == null) {
			delimiters = new ArrayList<SelectItem>();
			delimiters.add(new SelectItem(';', ";"));
			delimiters.add(new SelectItem(',', ","));
		}
		return delimiters;
	}
}
