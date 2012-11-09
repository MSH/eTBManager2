package org.msh.tb.az;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.TreatResultIndicator;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

@Name("treatResultIndicatorAZ")
public class TreatResultIndicatorAZ extends TreatResultIndicator {
	private static final long serialVersionUID = -8750402499187749542L;

	
	@Override
	public void createItems(List<Object[]> lst) {
		for (Object[] vals: lst) {
			CaseState cs = (CaseState)vals[0];
			if (cs.ordinal()!=0 && cs.ordinal()!=2){
				Float val = ((Long)vals[2]).floatValue();
				if (val > 0) {
					addIdValue(vals[0], vals[1], val);
				}
			}
		}
		for (Object[] vals: lst) {
			CaseState cs = (CaseState)vals[0];
			if (cs.ordinal()==0 || cs.ordinal()==2){
				Float val = ((Long)vals[2]).floatValue();
				if (val > 0) {
					addIdValue(vals[0], vals[1], val);
				}
			}
		}
	}
	
	public String getColorCell(TableCell cell){
		if (cell==null) return "#E2EAE2";
		CaseState id = (CaseState)cell.getColumn().getId();
		if (id.equals(CaseState.WAITING_TREATMENT) || id.equals(CaseState.TRANSFERRING)){
			return "#E2EAE2";
		}
		return "#DBFADC";
	}
	
	public int getTotal(TableRow row){
		int total = 0;
		for (TableCell cell:row.getValues())
			if (cell != null)
				if (!cell.getColumn().isHighlight()){
					CaseState id = (CaseState)cell.getColumn().getId();
					if (!id.equals(CaseState.WAITING_TREATMENT) && !id.equals(CaseState.TRANSFERRING)){
						total += cell.getValue();
					}
				}
		return total;
	}
	
	@Override
	protected void createIndicators() {
		setNewCasesOnly(true);

		List<Object[]> lst = generateValuesByField("c.state, c.patientType", "c.state >= " + CaseState.WAITING_TREATMENT.ordinal());
		createItems(lst);
		
		String msg = getMessage("manag.ind.successrate");
		IndicatorTable table = getTable();
		TableColumn col = table.addColumn(msg, successRateID);
		col.setHighlight(true);
		col.setRowTotal(false);

		for (Object[] vals: lst) {
			CaseState cs = (CaseState)vals[0];
			PatientType pt = (PatientType)vals[1];
			Float count = ((Long)vals[2]).floatValue();

			if ((pt != null) && (CaseState.CURED.equals(cs))) {
				addIdValue(successRateID, pt, count);
			}
		}
		
		for (TableRow row: table.getRows()) {
			Float total = Integer.valueOf(getTotal(row)).floatValue();
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
