package org.msh.tb.medicines.dispensing;

import org.msh.tb.entities.Source;

/**
 * Class to join instances of {@link Source} and {@link BatchDispensingTable}
 * @author Ricardo Memoria
 *
 */
public class SourceItem {

	private Source source;
	private BatchDispensingTable table = new BatchDispensingTable();

	public SourceItem(Source source) {
		this.source = source;
	}
	
	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(Source source) {
		this.source = source;
	}
	/**
	 * @return the table
	 */
	public BatchDispensingTable getTable() {
		return table;
	}
	/**
	 * @param table the table to set
	 */
	public void setTable(BatchDispensingTable table) {
		this.table = table;
	}
}
