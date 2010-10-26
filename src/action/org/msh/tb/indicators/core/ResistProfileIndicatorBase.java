package org.msh.tb.indicators.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import javax.persistence.ColumnResult;
import javax.persistence.SqlResultSetMapping;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.ResistancePattern;
import org.msh.mdrtb.entities.Substance;
import org.msh.mdrtb.entities.enums.CaseClassification;
import org.msh.mdrtb.entities.enums.DstResult;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorSeries;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

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
	

	//Abstract method to get the SQL query needed to run in order to generate the report.
	public abstract String getSqlQuery();
	
	//Abstract method to get the message to display for the column title.
	public abstract String getResistLabel();
	
	
	
	
	@Override
	protected void createIndicators() {
		
		newp 		= getMessage("manag.ind.dstprofile.nevertreated");
		newPer 		= getMessage("manag.ind.dstprofile.prevtreated");
		oldp 		= getMessage("manag.ind.dstprofile.nevertreatedpercent");
		oldPer 		= getMessage("manag.ind.dstprofile.prevtreatedpercent");
		total 		= getMessage("global.total");
		medicine 	= "";
		
		List result = super.getEntityManager().createNativeQuery(getSqlQuery()).getResultList();
		
		IndicatorTable table = getTable();
		table.addColumn(newp, null);
		table.addColumn(oldp, null);

		TableColumn newPercent = table.addColumn(newPer, null);
		newPercent.setHighlight(true);
		
		TableColumn oldPercent = table.addColumn(oldPer, null);
		oldPercent.setHighlight(true);
		
		TableColumn totalCell = table.addColumn(total, null);
		totalCell.setRowTotal(false);
		
		String resistLabel = getResistLabel();
		
		for (int i = 0; i < result.size(); i++) {
			Object[] object = (Object[]) result.get(i);
			addValue(newp, getResistLabel()+ " " + object[0].toString(), ((BigDecimal) object[1]).floatValue());
			addValue(oldp, resistLabel+ " " + object[0].toString(), ((BigDecimal) object[3]).floatValue());
			addValue(newPer, resistLabel+ " " + object[0].toString(), ((BigDecimal) object[2]).floatValue());
			addValue(oldPer, resistLabel+ " " + object[0].toString(), ((BigDecimal) object[4]).floatValue());
			addValue(total, resistLabel+ " " + object[0].toString(), ((BigInteger) object[5]).floatValue());
		}		
	}
	
	

}
