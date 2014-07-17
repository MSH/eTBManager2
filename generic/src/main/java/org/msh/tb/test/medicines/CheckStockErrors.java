/**
 * 
 */
package org.msh.tb.test.medicines;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ricardo Memoria
 *
 */
@Name("checkStockErrors")
public class CheckStockErrors {

	@In EntityManager entityManager;
	
	// list of units where it is found a difference between the quantity
	// in stock position with the sum of quantities in the movements 
	private List<DiffItem> unitsStockDiff;
	// units with difference between batch movements and medicine movements
	private List<DiffItem> unitsMovsDiff;
	// units with difference between the sum of batch quantities and sum of movements
	private List<DiffItem> unitsBatchDiff;
	
	// current workspace
	private Workspace ws;

	
	/**
	 * Create the report
	 */
	public void createReport() {
		ws = (Workspace)Component.getInstance("defaultWorkspace");
		createReportByMovements();
		createReportByBatch();
	}
	
	
	/**
	 * Create the report using the movement table
	 */
	private void createReportByMovements() {
		// create first report about quantity in stock position different from movements
/*		String sql = "select m.unit_id, u.name1, m.source_id, source.abbrev_name1, m.medicine_id, med.abbrevName, med.strength, sum(m.quantity * m.oper), s.quantity, " +
					"sum((select sum(a.quantity * m.oper) from batchquantity a inner join batch b on b.id=a.batch_id " + 
					"where a.source_id=m.source_id and a.unit_id=m.unit_id and b.medicine_id=m.medicine_id)) batch " +
					"from movement m " + 
					"inner join stockposition s on s.medicine_id=m.medicine_id and s.source_id=m.source_id and s.unit_id=m.unit_id " + 
					"inner join tbunit u on u.id=m.unit_id " + 
					"inner join medicine med on med.id=m.medicine_id " +
					"inner join source on source.id=m.source_id " +
					"where u.workspace_id = " + ws.getId() + 
					" group by m.unit_id, u.name1, m.source_id, m.medicine_id, med.name1 " +
					"having (s.quantity <> sum(m.quantity*m.oper)) or (sum(m.quantity*m.oper) <> batch) " + 
					"order by u.name1, med.name1 ";
*/		
		String sql = "select sp.unit_id, u.name1, sp.source_id, source.abbrev_name1, sp.medicine_id, med.abbrevName, med.strength ,sp.quantity " +
					", (select sum(m.quantity * m.oper) " +
					"from movement m where m.unit_id=sp.unit_id and m.medicine_id=sp.medicine_id and m.source_id=sp.source_id) summov " +
					", (select sum(a.quantity) from batchquantity a inner join batch b on b.id=a.batch_id " +
					"where a.source_id=sp.source_id and a.unit_id=sp.unit_id and b.medicine_id=sp.medicine_id) batch " +
					"from stockposition sp " +
					"inner join tbunit u on u.id=sp.unit_id " +
					"inner join medicine med on med.id=sp.medicine_id " +
					"inner join source on source.id=sp.source_id " +
					"where u.workspace_id = " + ws.getId() +
					" order by u.name1, med.name1";

		List<Object[]> lst = entityManager
			.createNativeQuery(sql)
			.getResultList();
		
		unitsStockDiff = new ArrayList<DiffItem>();
		unitsBatchDiff = new ArrayList<DiffItem>();
		for (Object[] vals: lst) {
			DiffItem item = new DiffItem((Integer)vals[0], (String)vals[1], 
					(Integer)vals[2], (String)vals[3], 
					(Integer)vals[4], (String)vals[5] + "(" +  (String)vals[6] + ")");
			
			Integer qtdSp = ((Integer)vals[7]);
			Integer qtdMovs = null;
			if (vals[8] != null)
				qtdMovs = ((BigDecimal)vals[8]).intValue();
			Integer qtdBatch = null;
			if (vals[9] != null)
				qtdBatch = ((BigDecimal)vals[9]).intValue();
			
			item.setQtyBatch(qtdBatch == null? 0: qtdBatch);
			item.setQtyMovements(qtdMovs == null? 0: qtdMovs);
			item.setQtyStockPos(qtdSp);

			// stockpos <> sum(movs) ?
			if (!qtdMovs.equals(qtdSp))
				unitsStockDiff.add(item);
			
			if (!qtdMovs.equals(qtdBatch))
				unitsBatchDiff.add(item);
		}
	}


	/**
	 * Create the report using the batch table
	 */
	private void createReportByBatch() {
		
	}

	/**
	 * @return the unitsStockDiff
	 */
	public List<DiffItem> getUnitsStockDiff() {
		if (unitsStockDiff == null)
			createReport();
		return unitsStockDiff;
	}
	/**
	 * @return the unitsMovsDiff
	 */
	public List<DiffItem> getUnitsMovsDiff() {
		if (unitsMovsDiff == null)
			createReport();
		return unitsMovsDiff;
	}


	/**
	 * @return the unitsBatchDiff
	 */
	public List<DiffItem> getUnitsBatchDiff() {
		return unitsBatchDiff;
	}
	
}
