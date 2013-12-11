package org.msh.tb.ua.export;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.Query;

import jxl.write.WritableCellFormat;
import jxl.write.WriteException;

import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.application.App;
import org.msh.tb.entities.CaseDispensing;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamHIV;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.export.CaseExport;
import org.msh.tb.export.ExcelCreator;
import org.msh.tb.indicators.core.IndicatorFilters;
import org.msh.tb.ua.entities.CaseDataUA;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

@Name("caseExportUATreatmentInfo")
public class CaseExportUATreatmentInfo extends CaseExport {
	private static final long serialVersionUID = -6105790013808048231L;

	public CaseExportUATreatmentInfo(ExcelCreator excel) {
		super(excel);
	}


	private List<CaseDataUA> cases;

	/**
	 * Return the list of cases based on the filters in {@link IndicatorFilters} session variable
	 * @return list of objects {@link TbCase}
	 */
	public List<CaseDataUA> getCasesUA() {
		if (cases == null)
			createCases();
		return cases;
	}


	/**
	 * Create the list of cases based on the filters in the {@link IndicatorFilters} session variable 
	 */
	@Override
	protected void createCases() {
		setNewCasesOnly(true);
		setCaseState(CaseState.ONTREATMENT);
		cases = createQuery().getResultList();
	}

	@Override
	protected String getHQLFrom() {
		return "from CaseDataUA data join fetch data.tbcase c";
	}

	@Override
	protected String getHQLJoin() {
		return super.getHQLJoin().concat(" join fetch c.ownerUnit u ");
	}

	@Override
	protected String getHQLValidationState() {
		return null;
	}

	public void addTitles() {
		ExcelCreator excel = getExcel();
		// add title line
		excel.addTextFromResource("Patient.name", "title");
		excel.addTextFromResource("CaseClassification", "title");
		excel.addTextFromResource("CaseState", "title");
		excel.addTextFromResource("User.region", "title");
		excel.addTextFromResource("TbCase.iniTreatmentDate", "title");
		excel.addTextFromResource("TbCase.iniContinuousPhase", "title");
		excel.addTextFromResource("TbCase.endTreatmentDate", "title");
		excel.addTextFromResource("manag.export.ptp", "title");
		excel.addTextFromResource("manag.export.regimen", "title");
		excel.addText(App.getMessage("manag.export.predcmed")+" ("+App.getMessage("RegimenPhase.INTENSIVE")+")", "title");
		excel.addText(App.getMessage("manag.export.predcmed")+" ("+App.getMessage("RegimenPhase.CONTINUOUS")+")", "title");
		excel.addTextFromResource("manag.export.disp", "title");
		//excel.addText("");
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.export.CaseIterator#exportContent(int)
	 */
	public TbCase exportContent(int index) {
		ExcelCreator excel = getExcel();
		
		CaseDataUA data = getCasesUA().get(index);
		TbCase tbcase = data.getTbcase();
		
		excel.addText(tbcase.getPatient().getFullName());
		excel.addTextFromResource(tbcase.getClassification().getKey());
		
		String s = getMessages().get( tbcase.getState().getKey() );
		if (data.getExtraOutcomeInfo() != null) {
			String extra = getMessages().get( data.getExtraOutcomeInfo().getKey() );
			if ((extra != null) && (!extra.isEmpty()))
				s += " - " + extra;
		}
		excel.addValue(s);
		
		excel.addValue(tbcase, "ownerUnit.adminUnit.parentLevel1");
		excel.addValue(tbcase, "treatmentPeriod.iniDate");
		excel.addValue(tbcase, "iniContinuousPhase");
		excel.addValue(tbcase, "treatmentPeriod.endDate");
		
		Set<Source> sources = new LinkedHashSet<Source>();
		LinkedHashMap<String,Period> regIntens = new LinkedHashMap<String, Period>();
		LinkedHashMap<String,Period> regCont = new LinkedHashMap<String, Period>();
		String ptpIntens = "";
		String ptpCont = "";
		boolean warnInt = false;
		boolean warnCont = false;
		
		for (PrescribedMedicine pm:tbcase.getPrescribedMedicines()){
			sources.add(pm.getSource());
			if (pm.getPeriod().getIniDate().before(tbcase.getIniContinuousPhase())){
				addMedicineToRegimenMap(regIntens, pm);
				ptpIntens = generatePTPSet(ptpIntens, pm,warnInt);
			}
			else{
				addMedicineToRegimenMap(regCont, pm);
				ptpCont = generatePTPSet(ptpCont, pm,warnCont);
			}
		}
		
		String sou = "";
		for (Source ss: sources){
			sou += ", "+ss.getAbbrevName();
		}
		sou = sou.substring(2);
		excel.addText(sou);
		
		excel.addText(generateRegimenAbbrev(regIntens)+" / "+generateRegimenAbbrev(regCont));

		
		excel.addText(ptpIntens,warnInt?"warning":null);
		excel.addText(ptpCont,warnCont?"warning":null);

		int disp = 0;
		if (tbcase.getDispensing()!=null)
		for (CaseDispensing cd:tbcase.getDispensing()){
			disp += cd.getTotalDays();
			
		}
		excel.addNumber(disp);

		return tbcase;
	}


	private String generatePTPSet(String ptp, PrescribedMedicine pm, boolean warning) {
		ptp += (!"".equals(ptp)?" ":"")+pm.getDoseUnit();
		for (MedicineComponent mc:pm.getMedicine().getComponents()){
			if (ptp.contains(" "+mc.getSubstance().getAbbrevName().getDefaultName())){
				warning = true;
			}
			ptp += " "+mc.getSubstance().getAbbrevName().getDefaultName()+mc.getStrength();
		}
		
		return ptp;
	}


	private String generateRegimenAbbrev(Map<String, Period> reg) {
		String res = "";
		Map<Integer,String> reg2 = new TreeMap<Integer, String>();
		for (String key:reg.keySet()){
			Integer m = reg.get(key).getMonths();
			String s = "";
			if (reg2.containsKey(m))
				s = reg2.get(m)+" ";
			reg2.put(m, s+key);
		}
		for (Integer key:reg2.keySet()){
			if (!"".equals(res))
				res += " ";
			res += key+" "+reg2.get(key);
		}
		return res;
	}


	private void addMedicineToRegimenMap(LinkedHashMap<String, Period> reg, PrescribedMedicine pm) {
		Period p = reg.get(pm.getMedicine().getAbbrevName());
		Period pp = pm.getPeriod();
		if (p!=null)
			pp.set(pp.getIniDate().before(p.getIniDate())?pp.getIniDate():p.getIniDate(), pp.getEndDate().after(p.getEndDate())?pp.getEndDate():p.getEndDate());
		reg.put(pm.getMedicine().getAbbrevName(),pp);
	}


	
	public int getResultCount() {
		return getCasesUA().size();
	}

	public int getCount() {
		String hql= "select count(*) "+ createHQL();
		//hql = hql.replace(getHQLJoin(), "");
		hql = hql.replace("fetch ", "");
		Query q = getEntityManager().createQuery(hql);
		setQueryParameters(q);
		return ((Long)q.getSingleResult()).intValue();
		//return getCasesUA().size();
	}
}
