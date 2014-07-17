package org.msh.tb.importexport;

import org.jboss.seam.Component;
import org.jboss.seam.transaction.UserTransaction;
import org.msh.tb.entities.Workspace;
import org.msh.utils.CsvReader;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;


/**
 * Base class for the implementation of importation of data
 * @author Ricardo Memoria
 *
 */
public abstract class ImportBase {

	private InputStream data;

	// line number currently being read
	private int lineNumber;
	
	// values to be imported
	private String[] values;
	
	// columns of the csv file
	private List<String> columns;

	private CsvReader csvReader;
	
	private Integer columnId;
	
	private Workspace workspace;
	
	private String charSet = "UTF-8";
	
	private char delimiter = ';';


	/**
	 * Execute the importing of data
	 * @param data
	 * @param firstLineHeader
	 * @return
	 * @throws Exception 
	 */
	public boolean execute(InputStream data) throws Exception {
		this.data = data;

		csvReader = new CsvReader(data, Charset.forName(charSet));
		if (!initialize()) 
			return false;
		UserTransaction tx = (UserTransaction)Component.getInstance("org.jboss.seam.transaction.transaction");
		tx.begin();
		getEntityManager().joinTransaction();
		try {
			while (csvReader.readRecord()) {
				values = csvReader.getValues();
				importRecord(values);
				lineNumber++;
				
				if (lineNumber % 50 == 0) {
					clearEntityManager();
					tx.commit();
					tx.begin();
				}
			}
			tx.commit();
		}
		catch (Exception e) {
			tx.rollback();
			throw e;
		}
		
		return true;
	}


	public void clearEntityManager() {
		EntityManager em = getEntityManager();
		em.flush();
		em.clear();
	}
	
	/**
	 * Import a record of data
	 * @param values list of values representing the record to be read
	 */
	public abstract void importRecord(String[] values);

	
	/**
	 * Initialize the importing of data 
	 */
	public boolean initialize() {
		try {
			csvReader.setDelimiter(delimiter);

			if (!csvReader.readHeaders())
				return false;

			columns = Arrays.asList( csvReader.getHeaders() );
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	
	/**
	 * Return the column that represents the id of the record
	 * @return if the id column exists, returns a number >= 0, otherwise returns -1
	 */
	public int getColumnId() {
		if ((columnId == null)&&(columns != null)) {
			int count = 0;
			for (String s: columns) {
				if ((s.equalsIgnoreCase("id")) || (s.equalsIgnoreCase("code")) || (s.equalsIgnoreCase("custom_id")))
					return count;
				count++;
			}
			
			columnId = -1;
			
		}
		return columnId;
	}
	
	
	/**
	 * Return the index of the column name
	 * @param columnName name of the column to find index
	 * @return index of the column in the list of columns. Return -1 if column doesn`t exist
	 */
	public int getColumnIndex(String columnName) {
		if (columns == null)
			return -1;
		
		int index = 0;
		for (String s: columns) {
			if (s.trim().equalsIgnoreCase(columnName)) {
				return index;
			}
			index++;
		}
		return -1;
}

	
	/**
	 * Return the entity manager to be used to persist entities
	 * @return
	 */
	public EntityManager getEntityManager() {
		return (EntityManager)Component.getInstance("entityManager");
	}

	/**
	 * Return the ID of the current record
	 * @return ID of the current record
	 */
	public String getId() {
		int col = getColumnId();
		if (col == -1)
			return null;
		return getValues()[col];
	}


	public InputStream getData() {
		return data;
	}
	public void setData(InputStream data) {
		this.data = data;
	}


	public int getLineNumber() {
		return lineNumber;
	}


	public String[] getValues() {
		return values;
	}


	public List<String> getColumns() {
		return columns;
	}


	public Workspace getWorkspace() {
		if (workspace != null)
			workspace = getEntityManager().find(Workspace.class, workspace.getId());
		return workspace;
	}


	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}


	public String getCharSet() {
		return charSet;
	}


	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}


	public char getDelimiter() {
		return delimiter;
	}


	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
}
