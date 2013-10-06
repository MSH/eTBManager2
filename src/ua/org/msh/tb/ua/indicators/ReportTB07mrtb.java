package org.msh.tb.ua.indicators;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.application.App;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("reportTB07mrtb")
@Scope(ScopeType.CONVERSATION)
public class ReportTB07mrtb extends IndicatorVerify<TbCase> {
	private static final long serialVersionUID = -8462692609433997419L;
	
	private IndicatorTable table1000;
	private IndicatorTable table2000;

	@Override
	protected void createIndicators() {
		initTable1000();
		initTable2000();

		generateTables();
		sortAllLists();
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
		table2000 = new IndicatorTable();
	}

	@Override
	protected void addToAllowing(TbCase tc){
		/*Map<String, List<ErrItem>>  verifyList = getVerifyList();
		List<PrevTBTreatment> ptb = getEntityManager().createQuery("select t from PrevTBTreatment t where t.tbcase.id="+tc.getId()).getResultList();
		if (ptb!=null)
			if (tc.getPatientType().equals(PatientType.RELAPSE) && ptb.size()==0)
				addToVerList(tc,2,0);
		if (tc.getTreatmentPeriod()!=null)
			if (tc.getState().equals(CaseState.ONTREATMENT))
				if (tc.getTreatmentPeriod().getEndDate().before(new Date()))
					addToVerList(tc,2,1);*/
	}
	
	
	protected void generateTables() {
		List<TbCase> lst;
		setCounting(true);
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("verify.tb07mrtb.error",3,0,1);
			setCounting(false);
			setOverflow(false);
			lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			Map<String, List<ErrItem>>  verifyList = getVerifyList();
			
			while(it.hasNext()){
				TbCase tc = it.next();
				CaseDataUA cd = (CaseDataUA) App.getEntityManager().find(CaseDataUA.class, tc.getId());  //AK, previous line will rise exception, if no result
				if (cd == null){
					addToVerList(tc,1,0);
					continue;
				}
				
				if (cd.getRegistrationCategory().getValue()==null){
					addToVerList(tc,1,0);
				}
				else
					if (cd.getRegistrationCategory().getValue().getId()==938224) // cat 4
					{
						if (tc.getDiagnosisType()==null)
							addToVerList(tc,1,1);
						else {
							//table 1000
							String rowid = "reg";
							if (tc.getTreatmentPeriod()!=null)
								if (tc.getTreatmentPeriod().getIniDate()!=null){
									getTable1000().addIdValue(tc.getDiagnosisType(), rowid, 1F);
									rowid = "treat";
								}
							getTable1000().addIdValue(tc.getDiagnosisType(), rowid, 1F);
							
							//table 2000
							if(tc.getPatientType() == null){
								tc.setPatientType(PatientType.NEW);
							}
							switch (tc.getPatientType()) {
							case NEW: case RELAPSE: case AFTER_DEFAULT: case FAILURE_FT: case FAILURE_RT: case OTHER:  
								getTable2000().addIdValue("col", tc.getPatientType(), 1F);
								getTable2000().addIdValue("col", "total", 1F);
								break;
							default:
								addToVerList(tc,1,1);
								break;
							}
							
							if (tc.getInfectionSite()==null)
								addToVerList(tc,1,2);
							else
							{
								if (tc.getExtrapulmonaryType()!=null || tc.getExtrapulmonaryType2()!=null){
									getTable2000().addIdValue("col", "extrapul", 1F);
									//TODO may be accounted twice to the TOTAL!!!!
									getTable2000().addIdValue("col", "total", 1F);
								}
							}
/*							List<PrevTBTreatment> ptb = getEntityManager().createQuery("select t from PrevTBTreatment t where t.tbcase.id="+tc.getId()).getResultList();
							if (ptb.size()>0)
								if (PrevTBTreatmentOutcome.UNKNOWN.equals(ptb.get(ptb.size()-1).getOutcome())){
									getTable2000().addIdValue("col", "other", 1F);
									getTable2000().addIdValue("col", "total", 1F);
								}*/
							
						}
					}
			}
			generateRepList(lst);
		}
		else 
			setOverflow(true);
	}


	@Override
	protected String getHQLJoin() {
		return null;
	}
	
	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		hql += " and c.classification = 1 ";
		return hql;
	}

	/**
	 * Initialize layout of the table 1000 of the TB07 report
	 */
	private void initTable1000() {
		IndicatorTable table = getTable1000();

		table.addColumn(App.getMessage("DiagnosisType.CONFIRMED"), DiagnosisType.CONFIRMED);
		table.addColumn(App.getMessage("DiagnosisType.SUSPECT"), DiagnosisType.SUSPECT);
		
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row1"), "reg");
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row2"), "treat");	
	}


	/**
	 * Initialize the layout of the table 2000 of the TB07 report
	 */
	private void initTable2000() {
		IndicatorTable table = getTable2000();
		table.addColumn("Count", "col");
		
		table.addRow(App.getMessage("PatientType.NEW"), PatientType.NEW);
		table.addRow(App.getMessage("PatientType.RELAPSE"), PatientType.RELAPSE);
		table.addRow(App.getMessage("PatientType.AFTER_DEFAULT"), PatientType.AFTER_DEFAULT);
		table.addRow(App.getMessage("PatientType.FAILURE_FT"), PatientType.FAILURE_FT);
		table.addRow(App.getMessage("PatientType.FAILURE_RT"), PatientType.FAILURE_RT);
		table.addRow(App.getMessage("PatientType.OTHER"), PatientType.OTHER);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.2000.row6"), "extrapul");
		//table.addRow(App.getMessage("uk_UA.reports.other"), "other");
		table.addRow(App.getMessage("uk_UA.reports.all"), "total");
	}

	/**
	 * @return the table1000
	 */
	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
	}


	/**
	 * @return the table2000
	 */
	public IndicatorTable getTable2000() {
		if (table2000 == null)
			createTable();
		return table2000;
	}
	/**
	 * Clear all tables and verifyList
	 * */
	public void clear(){
		table1000 = null;
		table2000 = null;
		setVerifyList(null);
	}
}
