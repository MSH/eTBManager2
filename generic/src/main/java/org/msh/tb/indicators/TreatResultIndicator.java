package org.msh.tb.indicators;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

@Name("treatResultIndicator")
public class TreatResultIndicator extends Indicator2D {
	private static final long serialVersionUID = 1488382939742056446L;
	protected static final String successRateID = "SRID";  

	@In(create=true)
	OutcomeIndicator outcomeIndicator;
	
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);

		List<Object[]> lst = generateValuesByField("c.state, c.patientType", "c.state >= " + CaseState.WAITING_TREATMENT.ordinal());
		createItems(lst);
		
		String msg = getMessage("manag.ind.successrate");
		IndicatorTable table = getTable();
		TableColumn col = table.addColumn(msg, successRateID);
//		col.setNumberPattern("#,###,##0");
		col.setHighlight(true);
		col.setRowTotal(false);

		for (Object[] vals: lst) {
			CaseState cs = (CaseState)vals[0];
			PatientType pt = (PatientType)vals[1];
			Float count = ((Long)vals[2]).floatValue();

			if ((pt != null) && (CaseState.CURED.equals(cs) || CaseState.TREATMENT_COMPLETED.equals(cs))) {
				addIdValue(successRateID, pt, count);
			}
		}
		
		for (TableRow row: table.getRows()) {
			Float total = ((Integer)row.getTotal()).floatValue();
			TableCell cell = row.findCellByColumnId(successRateID);

			if (cell != null) {
				Float sucRate = cell.getValue();
				if (sucRate != null) {
					sucRate = (sucRate / total) * 100;
					cell.setValue(sucRate);
				}
			}
		}
	}

}
