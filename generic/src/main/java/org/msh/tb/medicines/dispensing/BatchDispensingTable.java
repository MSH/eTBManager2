package org.msh.tb.medicines.dispensing;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		if (row != null)
			return row;
		
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
	 * Search for first row in the table by its medicine
	 * @param med
	 * @return instance of {@link DispensingRow} if medicine matches, or null if medicine is not found
	 */
	public DispensingRow findFirstMedicineRow(Medicine med) {
		for (DispensingRow row: rows)
			if (row.getMedicine().equals(med)) {
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
			int spanCount = 0;
			int qtd = 0;

			for (DispensingRow row: rows) {
				if (!row.getMedicine().equals(rowaux.getMedicine())) {
					spanCount = 1;
					qtd = row.getQuantity();
					rowaux = row;
				}
				else {
					row.setRowSpan(-1);

					spanCount++;
					qtd += row.getQuantity();
				}
				rowaux.setRowSpan(spanCount);
				rowaux.setTotalQuantity(qtd);
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
