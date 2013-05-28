package org.msh.tb.indicators.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.msh.tb.indicators.core.IndicatorTable.TableColumn;

/**
 * Generates indicator about DST resistance profile report
 * Class that creates the resistance profile table for the resistance profile report
 * @author Utkarsh Srivastava
 *
 */
public abstract class ResistProfileIndicatorBase extends Indicator2D {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1898429475968061720L;
	private String newp;
	private String newPer;
	private String oldp;
	private String oldPer;
	private String total;
	private String medicine;
	private String resistLabel;
	private String sqlStat;
	private int totalDST;
		

	//Abstract method to get the SQL query needed to run in order to generate the report.
	public abstract String getSqlQuery();
	
	//Abstract method to get the message to display for the column title.
	public abstract String getResistLabel();
	

	
	
	
	@Override
	protected void createIndicators() {
		
		newp 		= getMessage("manag.ind.dstprofile.nevertreated");
		newPer 		= getMessage("manag.ind.dstprofile.nevertreatedpercent");
		oldp 		= getMessage("manag.ind.dstprofile.prevtreated");
		oldPer 		= getMessage("manag.ind.dstprofile.prevtreatedpercent");
		total 		= getMessage("global.total");
		medicine 	= "";
		calcTotal();
		List result = super.getEntityManager().createNativeQuery(getSqlQuery()).getResultList();
		
		IndicatorTable table = getTable();
		table.addColumn(newp, null);
		//table.addColumn(oldp, null);

		TableColumn newPercent = table.addColumn(newPer, null);
		newPercent.setHighlight(true);
		
		table.addColumn(oldp, null);
		TableColumn oldPercent = table.addColumn(oldPer, null);
		oldPercent.setHighlight(true);
		
		TableColumn totalCell = table.addColumn(total, null);
		totalCell.setRowTotal(false);
		
		String resistLabel = getResistLabel();
		Float newPerc = null;
		Float oldPerc = null;
		for (int i = 0; i < result.size(); i++) {
			Object[] object = (Object[]) result.get(i);
			addValue(newp, getResistLabel()+ " " + object[0].toString(), ((BigDecimal) object[1]).floatValue());
			//addValue(oldp, resistLabel+ " " + object[0].toString(), ((BigDecimal) object[3]).floatValue());
			newPerc=(((BigDecimal) object[2]).floatValue()/totalDST*100);
			addValue(newPer, resistLabel+ " " + object[0].toString(), newPerc);
			addValue(oldp, resistLabel+ " " + object[0].toString(), ((BigDecimal) object[3]).floatValue());
			oldPerc=(((BigDecimal) object[4]).floatValue()/totalDST*100);
			addValue(oldPer, resistLabel+ " " + object[0].toString(), oldPerc);
			addValue(total, resistLabel+ " " + object[0].toString(), ((BigInteger) object[5]).floatValue());
		}	
		
	}
	
	/**
	 * Calculates total quantity of cases with DST exam
	 */
	protected void calcTotal() {
		String cond = "exists(select exam.id from ExamDST exam where exam.tbcase.id = c.id)";
		setCondition(cond);
		totalDST = ((Long) createQuery().getSingleResult()).intValue();
	}

}
