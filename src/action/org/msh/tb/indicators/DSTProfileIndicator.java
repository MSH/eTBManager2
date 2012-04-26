package org.msh.tb.indicators;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import javax.persistence.ColumnResult;
import javax.persistence.SqlResultSetMapping;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ResistancePattern;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorSeries;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.ResistProfileIndicatorBase;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

/**
 * Generates indicator about DST resistance profile report
 * @author Utkarsh Srivastava
 *
 */
@Name("dstProfileIndicator")
public class DSTProfileIndicator extends ResistProfileIndicatorBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253026619827637677L;
	
	@In Workspace defaultWorkspace;

	@Override
	public String getResistLabel() {
		return getMessage("manag.ind.dstprofile.anyresistto");
	}

	@Override
	public String getSqlQuery() {
		//VR: removing count(*) in percentage_new as the percentage is to be taken of all DST results irrespective of resistant / Susceptible
//		String sqlStat = "select s.ABBREV_NAME1, sum(if(c.patientType=0, 1, 0)) as newp, " +
//		"		(sum(if(c.patientType=0, 1, 0))/count(*)*100) as percentage_new, " +
//		"		sum(if(c.patientType>0, 1, 0))as other, " +
//		"		(sum(if(c.patientType>0, 1, 0))/count(*)*100) as percentage_old, " +
//		"		count(*) as total " +
//		" from tbcase c, tbunit u, examdst ed, examdstresult er, substance s" +
//		" where c.NOTIFICATION_UNIT_ID = u.id" +
//		"		and u.WORKSPACE_ID = " +defaultWorkspace.getId()+
//		"		and ed.CASE_ID = c.id " +
//		"		and er.EXAM_ID = ed.id " +
//		"		and er.SUBSTANCE_ID = s.id " +
//		"		and s.ABBREV_NAME1 in ('H', 'R', 'E', 'S') " +
//		"		and er.result = " + DstResult.RESISTANT.ordinal()+
//		"		and ed.dateCollected = (select MAX(aux.dateCollected) " +
//		"								from examdst aux where aux.CASE_ID = c.id) " +
//		"		group by s.ABBREV_NAME1";
		
		String sqlStat = "select s.ABBREV_NAME1, sum(if(c.patientType=0, 1, 0)) as newp, " +
		"		sum(if(c.patientType=0, 1, 0)) as percentage_new, " +
		"		sum(if(c.patientType>0 and c.patientType<>12,1,0 ))as other, " +
		"		sum(if(c.patientType>0, 1, 0)) as percentage_old, " +
		"		count(*) as total " +
		" from tbcase c, tbunit u, examdst ed, examdstresult er, substance s" +
		" where c.NOTIFICATION_UNIT_ID = u.id" +
		"		and u.WORKSPACE_ID = " +defaultWorkspace.getId()+
		"		and ed.CASE_ID = c.id " +
		"		and er.EXAM_ID = ed.id " +
		"		and er.SUBSTANCE_ID = s.id " +
		"		and s.ABBREV_NAME1 in ('H', 'R', 'E', 'S') " +
		"		and er.result = " + DstResult.RESISTANT.ordinal()+
		"		and ed.dateCollected = (select MAX(aux.dateCollected) " +
		"								from examdst aux where aux.CASE_ID = c.id) " +
		"		group by s.ABBREV_NAME1";
		return sqlStat;
	}
	
}
