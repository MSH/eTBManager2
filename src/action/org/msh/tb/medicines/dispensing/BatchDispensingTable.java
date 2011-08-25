package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Source;

/**
 * Table used for displaying the quantity dispensed of medicine, containing information about
 * source, batch, medicine, quantity dispensed of the batch and total quantity dispensed by medicine.
 * <p/>
 * The table is intended to be used for displaying consolidated data (period) or detailed data (by day).
 *  
 * @author Ricardo Memoria
 *
 */
public class BatchDispensingTable {

	private List<DispensingRow> rows = new ArrayList<DispensingRow>();

	/**
	 * Add a new row in the table
	 * @param batch
	 * @return
	 */
	public DispensingRow addRow(Source source, Batch batch) {
		DispensingRow row = findRowByBatch(source, batch);
		if (row == null)
			row = new DispensingRow(this, source, batch);
		rows.add(row);
		return row;
	}


	/**
	 * Search row by its batch. If there is no row with the given batch, the return is null 
	 * @param batch
	 * @return
	 */
	public DispensingRow findRowByBatch(Source source, Batch batch) {
		for (DispensingRow row: rows) {
			if ((row.getSource().equals(source)) && (row.getBatch().equals(batch)))
				return row;
		}
		return null;
	}


	/**
	 * Update the layout of the table to be displayed, calculating row spans and sorting rows
	 */
	public void updateLayout() {
		// sorting rows
		Collections.sort(rows, new Comparator<DispensingRow>() {
			@Override
			public int compare(DispensingRow row1, DispensingRow row2) {
				int res = row1.getBatch().getMedicine().toString().compareTo(row2.getBatch().getMedicine().toString());
				if (res == 0) {
					res = row1.getBatch().getExpiryDate().compareTo(row2.getBatch().getExpiryDate());
				}
				return res;
			}
		});

		// calculate row spans
		if (rows.size() > 0) {
			DispensingRow rowaux = rows.get(0);
			int spanCount = 1;
			int qtd = 0;

			for (DispensingRow row: rows) {
				if (!row.getMedicine().equals(rowaux.getMedicine())) {
					rowaux.setRowSpan(spanCount);
					rowaux.setTotalQuantity(qtd);
					spanCount = 1;
					qtd = 0;
				}
				else {
					spanCount++;
					qtd += row.getQuantity();
				}
			}
		}
	}
	
	/**
	 * Return list of rows of the table
	 * @return
	 */
	public List<DispensingRow> getRows() {
		return rows;
	}
}
