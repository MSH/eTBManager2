package org.msh.tb.indicators;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.indicators.core.ResistProfileIndicatorBase;

/**
 * Generates indicator about DST resistance profile report
 * @author Utkarsh Srivastava
 *
 */
@Name("dstUniqueResistIndicator")
public class DSTUniqueResistIndicator extends ResistProfileIndicatorBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4253026619827637677L;

	@In Workspace defaultWorkspace;

	@Override
	public String getResistLabel() {
		return getMessage("manag.ind.dstprofile.onlyresistto");
	}

	@Override
	public String getSqlQuery() {
		//VR: removing count(*) in percentage_new as the percentage is to be taken of all DST results irrespective of resistant / Susceptible
//		String sqlStat = " select s.ABBREV_NAME1, sum(if(c.patientType=0, 1, 0)) as newp, " +
//		"				(sum(if(c.patientType=0, 1, 0))/count(*)*100) as percentage_new, " +
//		"				sum(if(c.patientType>0, 1, 0))as other, " +
//		"				(sum(if(c.patientType>0, 1, 0))/count(*)*100) as percentage_old, " +
//		"				count(*) as total " +
//		"			from tbcase c, tbunit u, examdst ed, examdstresult er, substance s " +
//		"			where c.NOTIFICATION_UNIT_ID = u.id " +
//		"				and u.WORKSPACE_ID = " +defaultWorkspace.getId()+
//		"				and ed.CASE_ID = c.id " +
//		"				and er.EXAM_ID = ed.id " +
//		"				and er.SUBSTANCE_ID = s.id " +
//		"				and s.ABBREV_NAME1 in ('H', 'R', 'E', 'S') " +
//		"				and er.result = " + DstResult.RESISTANT.ordinal()+
//		"				and ed.dateCollected = (select MAX(aux.dateCollected) from examdst aux where aux.CASE_ID = c.id) " +
//		"				and (select count(*) from examdstresult auxer where ed.id = auxer.EXAM_ID " +
//		"				and auxer.result = 1) <= 1 " +
//		"			group by s.ABBREV_NAME1";
		
		String sqlStat = " select s.ABBREV_NAME1, sum(if(c.patientType=0, 1, 0)) as newp, " +
		"				sum(if(c.patientType=0, 1, 0)) as percentage_new, " +
		"				sum(if(c.patientType>0 and c.patientType<>12, 1, 0))as other, " +
		"				sum(if(c.patientType>0, 1, 0)) as percentage_old, " +
		"				count(*) as total " +
		"			from tbcase c, tbunit u, examdst ed, examdstresult er, substance s, patientsample ps " +
		"			where c.NOTIFICATION_UNIT_ID = u.id " +
                      " and ps.id=er.sample_id " +
		"				and u.WORKSPACE_ID = " +defaultWorkspace.getId()+
		"				and ed.CASE_ID = c.id " +
		"				and er.EXAM_ID = ed.id " +
		"				and er.SUBSTANCE_ID = s.id " +
		"				and s.ABBREV_NAME1 in ('H', 'R', 'E', 'S') " +
		"				and er.result = " + DstResult.RESISTANT.ordinal()+
		"				and ed.dateCollected = (select MAX(aux.sample.dateCollected) from examdst aux where aux.CASE_ID = c.id) " +
		"				and (select count(*) from examdstresult auxer inner join patientsample ps1 on ps1.id=auxer.sample_id " +
                      " where ed.id = auxer.EXAM_ID " +
		"				and auxer.result = 1) <= 1 " +
		"			group by s.ABBREV_NAME1";
		return sqlStat;
	}

}
