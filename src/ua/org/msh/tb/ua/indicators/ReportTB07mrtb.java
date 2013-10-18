package org.msh.tb.ua.indicators;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.application.App;
import org.msh.tb.cases.PrevTBTreatmentHome;
import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.DrugResistanceType;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.PrevTBTreatmentOutcome;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;
import org.msh.tb.ua.entities.CaseDataUA;

@Name("reportTB07mrtb")
@Scope(ScopeType.CONVERSATION)
public class ReportTB07mrtb extends IndicatorVerify<TbCase> {
	private static final long serialVersionUID = -8462692609433997419L;
	
	private IndicatorTable table1000;

	@Override
	protected void createIndicators() {
		initTable1000();

		generateTables();
		sortAllLists();
	}

	/**
	 * Create all tables of the report and generate indicators
	 */
	@Override
	protected void createTable() {
		table1000 = new IndicatorTable();
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
				addTo1000(tc,"total");
				if (tc.getInfectionSite()==null){
					addToVerList(tc,1,2);
					continue;
				}
				if (InfectionSite.PULMONARY.equals(tc.getInfectionSite()) || InfectionSite.BOTH.equals(tc.getInfectionSite())){
							if(tc.getPatientType() == null){
								tc.setPatientType(PatientType.NEW);
							}
							switch (tc.getPatientType()) {
							case NEW: case RELAPSE: 
								addTo1000(tc, tc.getPatientType());
								break;
							default:
								addTo1000(tc, PatientType.OTHER);
								Integer d = dutationPrevTreats(tc);
								if (d == null)
									continue;
								if (d<12)
									addTo1000(tc, "prev12");
								else if (d<24)
									addTo1000(tc, "prev1224");
								else
									addTo1000(tc, "prev24");
								break;
							}	
					}
			}
			generateRepList(lst);
		}
		else 
			setOverflow(true);
	}


	private Integer dutationPrevTreats(TbCase tc) {
		/*String hql = "from PrevTBTreatment p where p.tbcase.id in " +
		"(select c.id from TbCase c where c.patient.id = :id and c.treatmentPeriod.iniDate = " +
		"(select max(c2.treatmentPeriod.iniDate) from TbCase c2 where c2.patient.id = c.patient.id))";

		List<PrevTBTreatment> lst = App.getEntityManager()
										.createQuery(hql)
										.setParameter("id", tc.getPatient().getId())
										.getResultList();
		if (lst.isEmpty())
			return null;
		
		int m = 0;
		for (PrevTBTreatment pt:lst){
			m += pt.get //TODO necessary field
		}*/
		return null;
	}

	private void addTo1000(TbCase tc, Object rowId) {
		if (tc.getDrugResistanceType()==null){
			addToVerList(tc,1,0);
			return;
		}
		if (DrugResistanceType.MULTIDRUG_RESISTANCE.equals(tc.getDrugResistanceType()))
			getTable1000().addIdValue("mrtb", rowId, 1F);
		else if (DrugResistanceType.EXTENSIVEDRUG_RESISTANCE.equals(tc.getDrugResistanceType())){
			getTable1000().addIdValue("rrtb", rowId, 1F);
			//TODO rrtb 2nd line
		}
	}

	@Override
	protected String getHQLJoin() {
		return null;
	}
	
	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		hql += " and c.classification = 1 and c.diagnosisType=1";
		return hql;
	}

	/**
	 * Initialize the layout of the table 2000 of the TB07 report
	 */
	private void initTable1000() {
		IndicatorTable table = getTable1000();
		table.addColumn(getMessage("uk_UA.reports.tb11.1.headerN"), "N");
		
		table.addColumn("MR TB", "mrtb");
		table.addColumn("RR TB", "rrtb");
		table.addColumn("RR TB treat 2nd line", "rrtb2line");

		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.rowtotal"), "total");
		table.setValue("N","total",1F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row2"), "pulmonary");
		table.setValue("N","pulmonary",2F);
		table.addRow(App.getMessage("PatientType.NEW"), PatientType.NEW);
		table.addIdValue("N",PatientType.NEW,3F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row4"), "fail1cat");
		table.setValue("N","fail1cat",4F);
		table.addRow(App.getMessage("PatientType.RELAPSE"), PatientType.RELAPSE);
		table.addIdValue("N",PatientType.RELAPSE,5F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row6"), PatientType.OTHER);
		table.addIdValue("N",PatientType.OTHER,6F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row7"), "prev12");
		table.setValue("N","prev12",7F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row8"), "prev1224");
		table.setValue("N","prev1224",8F);
		table.addRow(App.getMessage("uk_UA.reports.tb07.mrtb.1000.row9"), "prev24");
		table.setValue("N","prev24",9F);
	}


	/**
	 * @return the table2000
	 */
	public IndicatorTable getTable1000() {
		if (table1000 == null)
			createTable();
		return table1000;
	}
	/**
	 * Clear all tables and verifyList
	 * */
	public void clear(){
		table1000 = null;
		setVerifyList(null);
	}
}
