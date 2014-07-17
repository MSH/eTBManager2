package org.msh.tb.na;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Patient;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.utils.date.Period;

import javax.persistence.EntityManager;
import java.util.List;

@Name("dotSupervisionIndicator")

public class DotSupervisionIndicator extends Indicator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8097784775097691524L;
	
	@In(create=true) EntityManager entityManager;
	
/*	private String groupFields;
	private List<CaseDispensingNA> dispensingList;
	private List<CaseDispensingNA> dOTdispensingListG;
*/
	// identification of the table columns
	private static final Integer COL_DAYS_PRESC= 1;
	private static final Integer COL_DAYS_TAKEN = 2;
	private static final Integer COL_DAYS_NOT_TAKEN = 3;
	private static final Integer COL_DAYS_SELF_ADMIN = 4;
	private static final Integer COL_COMPL_RATE = 5;

	@Override
	protected void createIndicators() {
		// initialize the selection
		setOutputSelected(false);
		setConsolidated(false);
		setHQLSelect("select c.id, c.patient.name, c.patient.middleName, c.patient.lastName, c.patient.recordNumber, c.caseNumber, "
				+ "c.treatmentPeriod, c.registrationCode, sum(d.totalDaysDot), sum(d.totalDaysSelfAdmin), sum(d.totalDaysNotTaken)");
		setGroupFields("c.id, c.patient.name, c.patient.middleName, c.patient.lastName, c.patient.recordNumber, c.caseNumber, "
				+ "c.treatmentPeriod, c.registrationCode");
		setOrderByFields("c.patient.name, c.patient.middleName, c.patient.lastName");
		setCondition("c.treatmentPeriod.iniDate is not null");

//		dispensingList = new ArrayList<CaseDispensingNA>();
		String hql = createHQL();
		List<Object[]> lst = getQueryResult(hql);

		Workspace ws = getWorkspace();
		
		// initialize the columns of the table
		IndicatorTable table = getTable();
		table.addColumn(getMessage("cases.treat.presc"), COL_DAYS_PRESC);
		table.addColumn(getMessage("cases.treat.disp.taken") + " (" + getMessage("global.days") + ")", COL_DAYS_TAKEN);
		table.addColumn(getMessage("cases.treat.disp.selfadmin") + " (" + getMessage("global.days") + ")", COL_DAYS_SELF_ADMIN);
		table.addColumn(getMessage("cases.treat.disp.nottaken") + " (" + getMessage("global.days") + ")", COL_DAYS_NOT_TAKEN);
		table.addColumn(getMessage("cases.treat.disp.dotCompletionRate") + " (%)", COL_COMPL_RATE);

		// populate table
		for (Object[] vals: lst) {
			// mock objects to help compose patient name and number
			TbCase tbcase = new TbCase();
			Patient pat = new Patient();
			tbcase.setPatient(pat);

			// set data from the result set
			tbcase.setId( (Integer)vals[0] );
			pat.setName((String)vals[1]);
			pat.setMiddleName((String)vals[2]);
			pat.setLastName((String)vals[3]);
			pat.setRecordNumber((Integer)vals[4]);
			pat.setWorkspace(ws);
			tbcase.setCaseNumber((Integer)vals[5]);
			tbcase.setTreatmentPeriod( (Period)vals[6] );
			tbcase.setRegistrationCode((String)vals[7]);

			float numDays = tbcase.getTreatmentPeriod().getDays();
			Long daysDot = (Long)vals[8];
			Long daysSelfAdmin = (Long)vals[9];
			Long daysNotTaken = (Long)vals[10];

			table.addRow(pat.getFullName(), tbcase);
			table.addIdValue(COL_DAYS_PRESC, tbcase, numDays);
			if (daysDot != null) {
				table.addIdValue(COL_DAYS_TAKEN, tbcase, daysDot.floatValue());
				float val = (daysDot / numDays) * 100;
				table.addIdValue(COL_COMPL_RATE, tbcase, val);
			}
			if (daysSelfAdmin != null)
				table.addIdValue(COL_DAYS_SELF_ADMIN, tbcase, daysSelfAdmin.floatValue());
			if (daysNotTaken != null)
				table.addIdValue(COL_DAYS_NOT_TAKEN, tbcase, daysNotTaken.floatValue());
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected String getHQLFrom() {
		return "from CaseDispensingNA d";
	}

	/** {@inheritDoc}
	 */
	@Override
	protected String getHQLJoin() {
		String joinStr = "inner join d.tbcase c ";
		setConsolidated(true);
		String s = super.getHQLJoin();
		setConsolidated(false);

		if (s != null)
			joinStr = joinStr.concat(s);
		return joinStr;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected String getPeriodCondition() {
		String s = "c.treatmentPeriod.iniDate >= #{indicatorFilters.iniDate}";
		return s;
	}
	
}
