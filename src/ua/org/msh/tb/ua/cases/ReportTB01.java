package org.msh.tb.ua.cases;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.treatment.DayInfo;
import org.msh.tb.cases.treatment.MonthInfo;
import org.msh.tb.cases.treatment.TreatmentCalendarHome;
import org.msh.tb.cases.treatment.TreatmentCalendarHome.PhaseInfo;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.ExamMicroscopy;
import org.msh.tb.entities.ExamXRay;
import org.msh.tb.entities.MedicalExamination;
import org.msh.tb.entities.MedicineComponent;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.entities.enums.ExtraOutcomeInfo;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.InfectionSite;
import org.msh.tb.entities.enums.LocalityType;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.ua.cases.exams.HistologyHome;
import org.msh.tb.ua.entities.CaseDataUA;
import org.msh.tb.ua.entities.CaseSideEffectUA;
import org.msh.tb.ua.entities.Histology;
import org.msh.tb.ua.entities.enums.HistologyResult;
import org.msh.tb.ua.utils.PDFCreator;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Class for generating PDF-version form TB01 for UA locale
 * @author A.M.
 */
@Name("tb01pdf")
public class ReportTB01 extends PDFCreator {
	@In(create=true) CaseHome caseHome;
	@In(create=true) CaseDataUAHome caseDataUAHome;
	@In(create=true) TreatmentCalendarHome treatmentCalendarHome;
	
	private TbCase tc;
	private CaseDataUA cd;

	private CaseSideEffectUA[] sideeffects;
	
	private Substance subH;
	private Substance subR;
	private Substance subE;
	private Substance subS;
	private Substance subZ;
	
	/**
	 * Pairs of substance of prescribed medicines and week-dose (g) in intensive phase
	 */
	Map<Substance, BigDecimal> pmIntensPhase = new HashMap<Substance, BigDecimal>();
	/**
	 * Pairs of substance of prescribed medicines and week-dose (g) in continuous phase
	 */
	Map<Substance, BigDecimal> pmContPhase = new HashMap<Substance, BigDecimal>();
	
	
	@Override
	protected void initData() {
		tc = caseHome.getTbCase();
		cd = caseDataUAHome.getCaseDataUA();
		
		if (tc.getTreatmentPeriod()==null)
			tc.setTreatmentPeriod(new Period());
		
		sideeffects = new CaseSideEffectUA[4];
		for (int i = 0; i < 4; i++) {
			sideeffects[i] = new CaseSideEffectUA();
			if (tc.getSideEffects().size()>i)
				sideeffects[i] = (CaseSideEffectUA)tc.getSideEffects().get(i);
		}
		
		SubstancesQuery sq = (SubstancesQuery) App.getComponent(SubstancesQuery.class);
		for (Substance s:sq.getResultList()){
			if ("H".equals(s.getAbbrevName().getDefaultName()))
				subH = s;
			if ("R".equals(s.getAbbrevName().getDefaultName()))
				subR = s;
			if ("E".equals(s.getAbbrevName().getDefaultName()))
				subE = s;
			if ("S".equals(s.getAbbrevName().getDefaultName()))
				subS = s;
			if ("Z".equals(s.getAbbrevName().getDefaultName()))
				subZ = s;
		}
		
		fullPrescMedInPhase(pmIntensPhase,tc.getIntensivePhasePeriod(),5);
		fullPrescMedInPhase(pmContPhase,tc.getContinuousPhasePeriod(),3);
	}

	/**
	 * Full map of pairs of substance of prescribed medicines and week-dose (g) in stated phase
	 * @param pmPhase - map
	 * @param phase - 0 for intensive, 1 - for continuous
	 * @param colSub - quantity of substances
	 */
	private void fullPrescMedInPhase(Map<Substance, BigDecimal> pmPhase,Period phase,int colSub) {
		Substance[] subInUse = new Substance[]{subH,subR,subE,subS,subZ};
		for (PrescribedMedicine pm:tc.getPrescribedMedicines())
			if (phase.contains(pm.getPeriod()))
				for (int i = 0; i < colSub; i++) {
					MedicineComponent mc = pm.getMedicine().getComponentBySubstance(subInUse[i]);
					if (mc!=null && mc.getStrength()!=null){
						BigDecimal dose = new BigDecimal(pm.getDoseUnit());
						BigDecimal freq = new BigDecimal(pm.getFrequency());
						BigDecimal sth = new BigDecimal(mc.getStrength());
						BigDecimal res = dose.multiply(freq.multiply(sth));
						res = res.movePointLeft(3);//from mg to g
						res = res.setScale(2, RoundingMode.HALF_UP);
						pmPhase.put(subInUse[i], res);
					}
				}
	}
	
	@Override
	protected void fullDocument(){
		if (caseHome.getTbCase() == null)
			return;
		try {
			setCurPaddingBottom(4);

			generateHeadTable();
			
			//REPORT MAIN TITLE
			Paragraph tmp_parag = new Paragraph(App.getMessage("uk_UA.tb01pdf.title"),times12b);
			tmp_parag.setAlignment(Rectangle.ALIGN_CENTER);
			getDocument().add(tmp_parag);
			
			generateRegistNumberBlock();
			generateOwnerUnitsBlock();
			
			generateTable1();
			generateTable2();
			generateTable34();
			generateTable5();
			
			setCurPaddingBottom(2);
			generateTable67();
			generateDispansingTable(RegimenPhase.INTENSIVE.ordinal());
			generateDispansingTable(RegimenPhase.CONTINUOUS.ordinal());
			
			generateTable10();
			generateTable11();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void generateTable11() throws DocumentException {
		tab = new PdfPTable(4);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{1,80,18,1};
		tab.setWidths(tmp_table_widths);

		addTableHeader(App.getMessage("uk_UA.tb01pdf.11"),4);
		
		cell = generateTableCell();
		cell.setRowspan(4);
		borderCell(true, true, false, true);
		tab.addCell(cell);
		
		cell = generateTableCell(times10,"");
		borderCell(false, true, false, true);
		cell.setColspan(2);
		tab.addCell(cell);
		
		cell = generateTableCell();
		cell.setRowspan(4);
		borderCell(false, true, true, true);
		tab.addCell(cell);
		
		cell = generateTableCell(times10,"");
		borderCell(false, false, false, true);
		cell.setColspan(2);
		tab.addCell(cell);
		
		cell = generateTableCell(times10,"");
		borderCell(false, false, false, true);
		cell.setColspan(2);
		tab.addCell(cell);
		
		cell = generateTableCell(times10,"");
		borderCell(false, false, false, true);
		cell.setColspan(2);
		cell.setFixedHeight(5);
		tab.addCell(cell);
		
		addEmptyCell(1);
		tab.addCell(generateTableCell(times10b, App.getMessage("uk_UA.tb01pdf.footer")));
		tab.addCell(generateTableCell(times10b, App.getMessage("uk_UA.tb01pdf.footer.person")));
		addEmptyCell(1);
		
		getDocument().add(tab);
	}

	private void generateTable10() throws DocumentException {
		tab = new PdfPTable(5);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{18,19,25,18,20};
		tab.setWidths(tmp_table_widths);

		addTableHeader(App.getMessage("uk_UA.tb01pdf.10"),4);
		
		LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
		tmp_map.put("10.8. "+App.getMessage("global.date"), times10);
		tmp_map.put(getFieldDate(tc.getOutcomeDate()), times10b);
		cell = generateTableCell(tmp_map);
		formatCell(Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT);
		tab.addCell(cell);
		
		tab.addCell(generateCheckBoxTableCell(times10, "10.1.", CaseState.CURED.equals(tc.getState()), App.getMessage(CaseState.CURED.getKey())));
		tab.addCell(generateCheckBoxTableCell(times10, "10.3.", CaseState.DIED.equals(tc.getState()), App.getMessage(CaseState.DIED.getKey())));
		tab.addCell(generateCheckBoxTableCell(times10, "10.4.", CaseState.FAILED.equals(tc.getState()), App.getMessage(CaseState.FAILED.getKey())));
		
		cell = generateCheckBoxTableCell(times10, "10.5.", CaseState.TREATMENT_INTERRUPTION.equals(tc.getState()), App.getMessage(CaseState.TREATMENT_INTERRUPTION.getKey()));
		cell.setColspan(2);
		tab.addCell(cell);
		
		tab.addCell(generateCheckBoxTableCell(times10, "10.2.", CaseState.TREATMENT_COMPLETED.equals(tc.getState()), App.getMessage(CaseState.TREATMENT_COMPLETED.getKey())));
		
		cell = generateCheckBoxTableCell(times10, "10.3.1.", ExtraOutcomeInfo.TB.equals(cd.getExtraOutcomeInfo()), App.getMessage(ExtraOutcomeInfo.TB.getKey()));
		cell.setIndent(30);
		tab.addCell(cell);
		
		cell = generateCheckBoxTableCell(times10, "10.4.1.", ExtraOutcomeInfo.CULTURE_SMEAR.equals(cd.getExtraOutcomeInfo()), App.getMessage("uk_UA.tb01pdf.10.4.1"));
		cell.setIndent(30);
		tab.addCell(cell);
		
		cell = generateCheckBoxTableCell(times10, "10.6.", CaseState.NOT_CONFIRMED.equals(tc.getState()), App.getMessage(CaseState.NOT_CONFIRMED.getKey()));
		cell.setColspan(2);
		tab.addCell(cell);
		
		addEmptyCell(2);
		
		cell = generateCheckBoxTableCell(times10, "10.3.2.", ExtraOutcomeInfo.OTHER_CAUSES.equals(cd.getExtraOutcomeInfo()), App.getMessage(ExtraOutcomeInfo.OTHER_CAUSES.getKey()));
		cell.setIndent(30);
		tab.addCell(cell);
		
		cell = generateCheckBoxTableCell(times10, "10.4.2.", ExtraOutcomeInfo.CLINICAL_EXAM.equals(cd.getExtraOutcomeInfo()), App.getMessage("uk_UA.tb01pdf.10.4.2"));
		cell.setIndent(30);
		tab.addCell(cell);
		
		String where = getFieldForFill(18);
		if (cd.getTransferOutDescription()!=null && !"".equals(cd.getTransferOutDescription()))
			where = cd.getTransferOutDescription();
		cell = generateCheckBoxTableCell(times10, "10.7.", CaseState.TRANSFERRED_OUT.equals(tc.getState()), App.getMessage("uk_UA.tb01pdf.10.7"),"");
		cell.getPhrase().add(new Chunk(where,times10b));
		cell.setColspan(2);
		cell.setRowspan(2);
		tab.addCell(cell);
		
		addEmptyCell(1);
		
		cell = generateCheckBoxTableCell(times10, "10.4.3.", ExtraOutcomeInfo.TRANSFER_CATIV.equals(cd.getExtraOutcomeInfo()), App.getMessage("uk_UA.tb01pdf.10.4.3"));
		cell.setIndent(30);
		tab.addCell(cell);
		
		addEmptyCell(1);
		
		getDocument().add(tab);
	}

	private void generateDispansingTable(int phase) throws DocumentException {
		tab = new PdfPTable(34);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[34];
		tmp_table_widths[0]=5.5f;
		for (int i = 1; i < 34; i++) {
			if (i==32 || i==33)
				tmp_table_widths[i]=5.4f;
			else
				tmp_table_widths[i]=2.7f;
		}
		tab.setWidths(tmp_table_widths);
		
		addTableHeader(App.getMessage("uk_UA.tb01pdf."+(phase+8)),34);
		
		cell = generateTableCell();
		Paragraph p1 = new Paragraph(App.getMessage("global.number"),times8);
		p1.setLeading(8);
		p1.setAlignment(Element.ALIGN_RIGHT);
		Paragraph p2 = new Paragraph(App.getMessage("global.month"),times8);
		p2.setAlignment(Element.ALIGN_LEFT);
		cell.addElement(p1);
		cell.addElement(p2);
		DottedDiagLine diag = new DottedDiagLine();
		cell.setCellEvent(diag);
		cell.setBorder(Rectangle.BOX);
		tab.addCell(cell);
		
		for (int i = 1; i <= 31; i++) {
			addCellMiddleCenter(String.valueOf(i), times10, true);
		}
		addCellMiddleCenter(App.getMessage("cases.treat.presc"), times10, true);
		addCellMiddleCenter(App.getMessage("cases.treat.disp"), times10, true);
		
		for (int i = 1; i <= 34; i++) {
			addCellMiddleCenter(String.valueOf(i), times7, true);
		}
		
		int maxMonthLines = 6+phase*2;
		int emptyLines = maxMonthLines;
		String totPresc = "";
		String totDisp = "";
		if (tc.getTreatmentPeriod().getIniDate()!=null){
			PhaseInfo phaseDisp = treatmentCalendarHome.getPhases().get(phase);
			emptyLines = maxMonthLines-phaseDisp.getMonths().size();
			
			int j=0;
			for (MonthInfo m:phaseDisp.getMonths())
				if (j<maxMonthLines){
					DateFormatSymbols symbols = new DateFormatSymbols(LocaleSelector.instance().getLocale());
					String smonth = symbols.getShortMonths()[m.getMonth()]+"-"+m.getYear();
					addCellMiddleCenter(smonth, times8, true);
					for (DayInfo day:m.getDays()){
						String s = " ";
						if (day.isPrescribed() && !day.isTreated())
							s = "-";
						if (day.isTreated())
							s = "+";
						addCellMiddleCenter(s,times8,true);
					}
					addCellMiddleCenter(String.valueOf(m.getTotalPrescribed()), times8, true);
					addCellMiddleCenter(String.valueOf(m.getDispensingDays()), times8, true);
					j++;
				}
			totPresc = String.valueOf(phaseDisp.getTotalPresc());
			totDisp = String.valueOf(phaseDisp.getTotalDisp());
		}
		if (emptyLines>0)
			for (int i = 0; i < 34*emptyLines; i++) {
				addCellMiddleCenter(" ", times8, true);
			}
		
		cell = generateTableCell(times10, App.getMessage("AtAll"));
		cell.setColspan(32);
		formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_RIGHT);
		tab.addCell(cell);
		
		addCellMiddleCenter(totPresc, times10b, true);
		addCellMiddleCenter(totDisp, times10b, true);
		
		getDocument().add(tab);
	}

	private void generateTable67() throws DocumentException {
		tab = new PdfPTable(18);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{7,3.5F,3.5F,3.5F,3.5F,3.5F,7,1,4,4,4,7,1.5F,7,10,10,10,10};
		tab.setWidths(tmp_table_widths);
		tab.setKeepTogether(true);
		
		//TABLE 6-7 HEADER
		addTableHeader(App.getMessage("uk_UA.tb01pdf.6"),13);
		addTableHeader(App.getMessage("uk_UA.tb01pdf.7"),5);
		
		// CELL 6.1-3 begin
			Integer caseCatId = 0;
			if (cd.getRegistrationCategory().getValue()!=null)
				caseCatId = cd.getRegistrationCategory().getValue().getId();
			cell = generateCheckBoxTableCell(times10, "6.1.", caseCatId.intValue()==938221, App.getMessage("uk_UA.tb01pdf.6.1"));//cat1
			cell.setColspan(2);
			borderCell(true, true, false, false);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10, "6.2.", caseCatId.intValue()==938222, App.getMessage("uk_UA.tb01pdf.6.2"));//cat2
			cell.setColspan(3);
			borderCell(false, true, false, false);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10, "6.3.", caseCatId.intValue()==938223, App.getMessage("uk_UA.tb01pdf.6.3"));//cat3
			cell.setColspan(7);
			borderCell(false, true, true, false);
			tab.addCell(cell);
			
		// CELL 6.1-3 end
			
			addEmptyCell(6);
			
		// CELL 7.1-5 begin
			cell = generateCheckBoxTableCell(times10, "7.1.", tc.getSideEffects().isEmpty(), App.getMessage("uk_UA.tb01pdf.7.1"));
			cell.setBorder(Rectangle.BOX);
			cell.setRowspan(2);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10, "7.2.", !tc.getSideEffects().isEmpty(), App.getMessage("uk_UA.tb01pdf.7.2"));
			cell.setBorder(Rectangle.BOX);
			cell.setRowspan(2);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
			tab.addCell(cell);
			
			addCellMiddleCenter("7.3 "+App.getMessage("cases.sideeffects.desc"), times10, true,2,1,false);
			addCellMiddleCenter("7.4 "+App.getMessage("cases.sideeffects.month"), times10, true,2,1,false);
			addCellMiddleCenter("7.5 "+App.getMessage("cases.sideeffects.resolved"), times10, true,2,1,false);
			
		// CELL 7.1-5 end	
		
		// CELL 6.4-5 TABLE & SIDEEFFECTS begin
			cell = generateTableCell(times10i, App.getMessage("uk_UA.tb01pdf.6.4"));
			cell.setColspan(7);
			borderCell(true, false, false, false);
			tab.addCell(cell);
			
			addEmptyCell(4);
			
			cell = generateTableCell(times10i, App.getMessage("uk_UA.tb01pdf.6.5"));
			cell.setColspan(4);
			borderCell(false, false, true, false);
			tab.addCell(cell);
			
			addCellMiddleCenter(App.getMessage("medicines"), times8b, true);
			addCellMiddleCenter("H", times8, true);
			addCellMiddleCenter("R", times8, true);
			addCellMiddleCenter("Z", times8, true);
			addCellMiddleCenter("E", times8, true);
			addCellMiddleCenter("S", times8, true);
			addCellMiddleCenter(App.getMessage("PatientType.OTHER")+getFieldForFill(5), times8b, true);
			
			addCellMiddleCenter("H", times8, true);
			addCellMiddleCenter("R", times8, true);
			addCellMiddleCenter("E", times8, true);
			addCellMiddleCenter(App.getMessage("PatientType.OTHER")+getFieldForFill(5), times8b, true);
			
			addCellMiddleCenter("", times10, true,4,1,false);
			
			addSideEffectLine(0);
			
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.6.dose"), times8, true);
			
			addCellMiddleCenter((pmIntensPhase.containsKey(subH)?pmIntensPhase.get(subH).toString():""), times8, true);
			addCellMiddleCenter((pmIntensPhase.containsKey(subR)?pmIntensPhase.get(subR).toString():""), times8, true);
			addCellMiddleCenter((pmIntensPhase.containsKey(subZ)?pmIntensPhase.get(subZ).toString():""), times8, true);
			addCellMiddleCenter((pmIntensPhase.containsKey(subE)?pmIntensPhase.get(subE).toString():""), times8, true);
			addCellMiddleCenter((pmIntensPhase.containsKey(subS)?pmIntensPhase.get(subS).toString():""), times8, true);
			addCellMiddleCenter("", times8, true);//TODO other intens phase

			addCellMiddleCenter((pmContPhase.containsKey(subH)?pmContPhase.get(subH).toString():""), times8, true);
			addCellMiddleCenter((pmContPhase.containsKey(subR)?pmContPhase.get(subR).toString():""), times8, true);
			addCellMiddleCenter((pmContPhase.containsKey(subE)?pmContPhase.get(subE).toString():""), times8, true);
			addCellMiddleCenter("", times8, true);//TODO other cont phase

			addSideEffectLine(1);
			
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.6.dose"), times8, true);
			for (int i = 0; i < 10; i++) {
				addCellMiddleCenter("", times8, true);
			}
			
			addSideEffectLine(2);
		// CELL 6.4-5 TABLE & SIDEEFFECTS end
			
		// CELL 6.4-5 TABLE & SIDEEFFECTS FOOTER begin	
			cell = generateTableCell(times8, App.getMessage("uk_UA.tb01pdf.6.footer"));
			borderCell(true, false, true, true);
			cell.setColspan(12);
			tab.addCell(cell);
			
			addSideEffectLine(3);
			
			cell = generateTableCell(times8, "");
			cell.setColspan(13);
			cell.setFixedHeight(0.2f);
			tab.addCell(cell);
			
			cell = generateTableCell(times8, "");
			cell.setBorder(Rectangle.TOP);
			cell.setColspan(5);
			cell.setFixedHeight(0.2f);
			tab.addCell(cell);
		// CELL 6.4-5 TABLE & SIDEEFFECTS FOOTER end
			
		getDocument().add(tab);
	}

	/**
	 * Add 4 cells of i-th side effect from tbcase info
	 * @param i
	 */
	private void addSideEffectLine(int i) {
		DottedLine dot = new DottedLine(); 
		String ptp = (sideeffects[i].getSubstance()!=null ? sideeffects[i].getSubstance().getAbbrevName().getDefaultName():"")+
					(sideeffects[i].getSubstance2()!=null ? ", "+sideeffects[i].getSubstance2().getAbbrevName().getDefaultName() : "");
		cell = generateTableCell(times10, ptp);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.RIGHT);
		cell.setCellEvent(dot);
		tab.addCell(cell);
		
		cell = generateTableCell(times10, (sideeffects[i].getSideEffect().getValue()!=null?sideeffects[i].getSideEffect().getValue().getName().getDefaultName():""));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.RIGHT);
		cell.setCellEvent(dot);
		tab.addCell(cell);
		
		cell = generateTableCell(times10, (sideeffects[i].getId()!=null?String.valueOf(sideeffects[i].getMonthOfTreatment()):""));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.RIGHT);
		cell.setCellEvent(dot);
		tab.addCell(cell);
		
		String resSE = "";
		if (sideeffects[i].getOutcome()!=null)
			switch (sideeffects[i].getOutcome()) {
				case RESOLVED: case RESOLVING:
					resSE = "global.yes";
					break;	
				default:
					resSE = "global.no";					
			}
		
		cell = generateTableCell(times10, App.getMessage(resSE));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.RIGHT);
		cell.setCellEvent(dot);
		
		tab.addCell(cell);
	}

	private void generateHeadTable() throws DocumentException {
		tab = new PdfPTable(4);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[] {45,13,5,37};
		tab.setWidths(tmp_table_widths);
		tab.getDefaultCell().setPadding(1F);
		
		setCurLeadTab(0F);
		
		//HEALTH SYSTEM begin
			LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col1.1"), times8);
			String hs = getFieldForFill(73);
			if (tc.getNotificationUnit()!=null && tc.getNotificationUnit().getHealthSystem()!=null)
				hs = tc.getNotificationUnit().getHealthSystem().getName().getDefaultName();
			tmp_map.put(hs, times8b);
			cell = generateTableCell(tmp_map);
			borderCell(true, true, true, false);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			tab.addCell(cell);
		//HEALTH SYSTEM end
		
		//YEAR begin
			cell = generateTableCell(times8, App.getMessage("uk_UA.tb01pdf.tabhead1.col2.1"));
			borderCell(false, true, false, false);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_RIGHT);
			tab.addCell(cell);
			
			cell = generateTableCell(times8b, (tc.getDiagnosisDate()!=null) ? Integer.toString(DateUtils.yearOf(tc.getDiagnosisDate())) : getFieldForFill(4));
			borderCell(false, true, false, false);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			tab.addCell(cell);
		//YEAR end
		
		//OFFICIAL DETAILS begin
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col3.1")+"\n", times8);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col3.2")+"\n", times8);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col3.3")+"\n", times8b);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col3.4")+"\n", times8);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col3.5"), times8);
			cell = generateTableCell(tmp_map);
			cell.setRowspan(3);
			borderCell(true, true, true, true);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
			tab.addCell(cell);
		//OFFICIAL DETAILS end
		
		//TBUNIT NAME & ADDRESS begin
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.tabhead1.col1.2"), times8);
			String tbu = getFieldForFill(89);
			if (tc.getNotificationUnit()!=null)
				tbu = tc.getNotificationUnit()+", "+(tc.getNotificationUnit().getAddress()!=null ? tc.getNotificationUnit().getAddress() : "");
			tmp_map.put(tbu, times8b);
			cell = generateTableCell(tmp_map);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			borderCell(true, false, true, false);
			tab.addCell(cell);
		//TBUNIT NAME & ADDRESS end
		
		//COHORT begin
			cell = generateTableCell(times8, App.getMessage("uk_UA.tb01pdf.tabhead1.col2.2"));
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_RIGHT);
			tab.addCell(cell);
			
			cell = generateTableCell(times8b, getFieldForFill(4));
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			tab.addCell(cell);
		//COHORT end
		
		//INN begin
			cell = generateTableCell(times8, App.getMessage("uk_UA.tb01pdf.tabhead1.col1.3"),getFieldForFill(32));
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			borderCell(true, false, true, true);
			tab.addCell(cell);
		//INN end
		
		//CATEGORY begin
			cell = generateTableCell(times8, App.getMessage("uk_UA.tb01pdf.tabhead1.col2.3"));
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_RIGHT);
			borderCell(false, false, false, true);
			tab.addCell(cell);
			
			String cat=getFieldForFill(4);
			if (cd.getRegistrationCategory().getValue()!=null)
				cat = cd.getRegistrationCategory().getValue().getShortName().getDefaultName();
			cell = generateTableCell(times8b, cat);
			formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_LEFT);
			borderCell(false, false, false, true);
			tab.addCell(cell);
		//CATEGORY end
		tab.setHeaderRows(0);
		tab.setFooterRows(0);
			
		getDocument().add(tab);
	}


	private void generateOwnerUnitsBlock() throws DocumentException {
		tab = new PdfPTable(2);
		tab.setSpacingBefore(2F);
		tab.setSpacingAfter(0F);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{50,50};
		tab.setWidths(tmp_table_widths);
		
		String tuInt = getFieldForFill(46);
		String tuCont = getFieldForFill(44);
		if (tc.getTreatmentPeriod() != null){
			for (TreatmentHealthUnit thu: tc.getHealthUnits()){
				Date d = thu.getPeriod().getIniDate();
				if (DateUtils.daysBetween(d, tc.getTreatmentPeriod().getIniDate())==0)
					tuInt = thu.getTbunit().getName().getDefaultName();
				if (thu.getPeriod().isDateInside(tc.getIniContinuousPhase()))
					tuCont = thu.getTbunit().getName().getDefaultName();
			}
		}
		
		LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
		tmp_map.put(App.getMessage("uk_UA.tb01pdf.ownerUnit.intensPhase"),times10b);
		tmp_map.put(tuInt,times10);
		cell = generateTableCell(tmp_map);
		tab.addCell(cell);
		
		tmp_map.clear();
		tmp_map.put(App.getMessage("uk_UA.tb01pdf.ownerUnit.contPhase"),times10b);
		tmp_map.put(tuCont,times10);
		cell = generateTableCell(tmp_map);
		tab.addCell(cell);
		getDocument().add(tab);
	}

	private void generateRegistNumberBlock() throws DocumentException {
		tab = new PdfPTable(15);
		tab.setSpacingBefore(10F);
		tab.setWidthPercentage(50F);
		tab.setHorizontalAlignment(Element.ALIGN_LEFT);
		float[] tmp_table_widths = new float[]{36,5F,5F,5F,5F,5F,5F,5F,5F,5F,5F,5F,5F,5F,5F};
		tab.setWidths(tmp_table_widths);
		
		setCurLeadTab(0F); // leading for all cells of registration code
		
		cell = generateTableCell(times10b, App.getMessage("uk_UA.tb01pdf.regNumb"));
		cell.setRowspan(2);
		formatCell(Rectangle.ALIGN_TOP, Rectangle.ALIGN_LEFT);
		cell.setBorder(Rectangle.NO_BORDER);
		tab.addCell(cell);
		
		char[] regNumb = {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '};
		if (tc.getRegistrationCode()!=null && !"".equals(tc.getRegistrationCode()))
			for (int i = 0; i < regNumb.length; i++) 
				if (tc.getRegistrationCode().toCharArray().length>i)
					regNumb[i] = tc.getRegistrationCode().toCharArray()[i];
			
		
		addCellMiddleCenter(String.valueOf(regNumb[0]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[1]),times8,true);
		
		addEmptyCell(2);
		
		addCellMiddleCenter(String.valueOf(regNumb[2]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[3]),times8,true);
		
		addEmptyCell(2);
		
		addCellMiddleCenter(String.valueOf(regNumb[4]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[5]),times8,true);
		
		addEmptyCell(2);
		
		addCellMiddleCenter(String.valueOf(regNumb[6]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[7]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[8]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[9]),times8,true);
		addCellMiddleCenter(String.valueOf(regNumb[10]),times8,true);
		
		//curLeadTab = 3F; // leading for all cells of captions registration code
		
		addCellMiddleCenter(App.getMessage("uk_UA.code_reg_lev1"),times6,false,1,2,false);
		addCellMiddleCenter(App.getMessage("uk_UA.code_reg_lev2"),times6,false,1,2,false);
		addCellMiddleCenter(App.getMessage("uk_UA.year"),times6,false,1,2,false);
		addCellMiddleCenter(App.getMessage("uk_UA.npp"),times6,false,1,5,false);
		
		getDocument().add(tab);		
	}


	private void generateTable5() throws DocumentException {
		tab = new PdfPTable(18);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{4,2,11,11,7,11,11,7,2,2,2,2,2,2,7,11,2,4};
		tab.setWidths(tmp_table_widths);
		
		//TABLE 5 HEADER
		addTableHeader(App.getMessage("uk_UA.tb01pdf.5"),18);
		
		//TABLE 5 COLUMNS HEADER begin
			addCellMiddleCenter(App.getMessage("global.month"),times8,true,3,1,true);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.numcol"),times8,true,3,1,true);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.micro"),times8,true,1,11,false);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.hist"),times8,true,3,1,true);
			addCellMiddleCenter(App.getMessage("cases.examxray")+":",times8,true,1,3,false);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.m"),times8,true,3,1,true);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.micro.scopy"),times8,true,1,3,false);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.micro.cult"),times8,true,1,3,false);
			addCellMiddleCenter(App.getMessage("cases.examdst"),times8,true,1,5,false);
			addCellMiddleCenter(App.getMessage("global.date"),times8,true,2,1,false);
			String tmp_str = 	"0. "+App.getMessage("global.notdone")+"\n"+
								"1. "+App.getMessage("XRayResult.POSITIVE")+"\n"+
								"2. "+App.getMessage("XRayResult.NEGATIVE")+"\n"+
								"3. "+App.getMessage("XRayResult.NO_CHANGE")+"\n"+
								"4. "+App.getMessage("XRayResult.STABILIZED");
			addCellMiddleCenter(tmp_str,times8,true,2,1,false);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.xray.dest"),times8,true,2,1,true);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.n"),times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("global.date"),times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("cases.outcome"),times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("uk_UA.tb01pdf.5.n"),times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("global.date"),times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("cases.outcome"),times8,true,1,1,false);
			addCellMiddleCenter("H",times8,true,1,1,false);
			addCellMiddleCenter("R",times8,true,1,1,false);
			addCellMiddleCenter("S",times8,true,1,1,false);
			addCellMiddleCenter("E",times8,true,1,1,false);
			addCellMiddleCenter(App.getMessage("PatientType.OTHER"),times8,true,1,1,true);
		//TABLE 5 COLUMNS HEADER end
		
		addCellMiddleCenter(App.getMessage("uk_UA.reports.tb10.a"),times7,true);
		addCellMiddleCenter(App.getMessage("uk_UA.reports.tb10.b"),times7,true);
		for (int i = 1; i <= 16; i++) {
			addCellMiddleCenter(String.valueOf(i),times7,true);
		}
		List<Period> examlist = new ArrayList<Period>();

		examlist.add(new Period(cd.getDateFirstVisitGMC(),DateUtils.incDays(tc.getRegistrationDate(),-1)));
		examlist.add(new Period(tc.getRegistrationDate(),tc.getTreatmentPeriod().getIniDate()!=null?DateUtils.incDays(tc.getTreatmentPeriod().getIniDate(),-1):null));
		examlist.add(getTreatmentPeriodMonths(2,3));
		examlist.add(getTreatmentPeriodMonths(3,4));
		examlist.add(getTreatmentPeriodMonths(5,5));
		examlist.add(getTreatmentPeriodMonths(6,8));
		
		NoDataCell noDataCell = new NoDataCell();
		for (int i = 0; i < examlist.size(); i++) {
			cell = generateTableCell(times10, App.getMessage("uk_UA.tb01pdf.5.months."+(i+1)));
			cell.setBorder(Rectangle.BOX);
			tab.addCell(cell);
			
			addCellMiddleCenter(String.valueOf(i+1),times7,true);
			ExamBlock eb = getExams(examlist.get(i));
			
			addCellMiddleCenter(eb.getMicroscopy()!=null?eb.getMicroscopy().getSampleNumber():"",times10,true);
			addCellMiddleCenter(eb.getMicroscopy()!=null?getFieldDate(eb.getMicroscopy().getDateCollected()):"",times10,true);
			addCellMiddleCenter(eb.getMicroscopy()!=null?App.getMessage(eb.getMicroscopy().getResult().getKey()):"",times10,true);
			
			if (i!=0){
				addCellMiddleCenter(eb.getCulture()!=null?eb.getCulture().getSampleNumber():"",times10,true);
				addCellMiddleCenter(eb.getCulture()!=null?getFieldDate(eb.getCulture().getDateCollected()):"",times10,true);
				addCellMiddleCenter(eb.getCulture()!=null?App.getMessage(eb.getCulture().getResult().getKey()):"",times10,true);
				
				ExamDSTResult exH = eb.getDSTResult(subH);
				ExamDSTResult exR = eb.getDSTResult(subR);
				ExamDSTResult exS = eb.getDSTResult(subS);
				ExamDSTResult exE = eb.getDSTResult(subE);
				
				addCellMiddleCenter(exH!=null?App.getMessage(getShortResult(exH.getResult())):"",times10,true);
				addCellMiddleCenter(exR!=null?App.getMessage(getShortResult(exR.getResult())):"",times10,true);
				addCellMiddleCenter(exS!=null?App.getMessage(getShortResult(exS.getResult())):"",times10,true);
				addCellMiddleCenter(exE!=null?App.getMessage(getShortResult(exE.getResult())):"",times10,true);
				addCellMiddleCenter("",times10,true); //TODO dst others
			}
			else{
				for (int j = 0; j < 8; j++) {
					cell = generateTableCell();
					cell.setCellEvent(noDataCell);
					cell.setBorder(Rectangle.BOX);
					tab.addCell(cell);
				}
			}
			addCellMiddleCenter(eb.getHistology()!=null?eb.getHistology().getDescription():"",times10,true);
			
			addCellMiddleCenter(eb.getXray()!=null?getFieldDate(eb.getXray().getDate()):"",times10,true);
			addCellMiddleCenter((eb.getXray()!=null&&eb.getXray().getResult()!=null)?String.valueOf(eb.getXray().getResult().ordinal()+1):"0",times10,true);
			addCellMiddleCenter((eb.getXray()!=null&&eb.getXray().getDestruction()!=null)?(eb.getXray().getDestruction()?"+":"-"):"",times10,true);
			
			addCellMiddleCenter(eb.getMedexam()!=null?String.valueOf(eb.getMedexam().getWeight()):"",times10,true);
		}
		for (int i = 0; i < 18; i++) {
			cell = generateTableCell(times10, "");
			cell.setBorder(Rectangle.BOX);
			cell.setFixedHeight(15);
			tab.addCell(cell);
		}
		getDocument().add(tab);
	}

	/**
	 * Return dst-result in necessary table-view  
	 * @param result
	 * @return
	 */
	private String getShortResult(DstResult result) {
		/*if (DstResult.RESISTANT.equals(result))
			return "R";
		if (DstResult.CONTAMINATED.equals(result))
			return "C";
		if (DstResult.SUSCEPTIBLE.equals(result))
			return "S";*/
		if (DstResult.RESISTANT.equals(result))
			return "+";
		return "";
	}

	/**
	 * Returns exams, which were done in stated period
	 * @param period - period of examination
	 */
	private ExamBlock getExams(Period period) {
		ExamBlock eb = new ExamBlock();
		
		Date minD = DateUtils.getDate();
		for (ExamCulture ex: tc.getExamsCulture()){
			if (period.getEndDate()!=null)
				if (ex.getDateCollected().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDateCollected().before(period.getIniDate()))
					continue;
			
			if (ex.getDateCollected().before(minD)){
				minD = ex.getDateCollected();
				eb.setCulture(ex);
			}
		}
		
		minD = DateUtils.getDate();
		for (ExamMicroscopy ex: tc.getExamsMicroscopy()){
			if (period.getEndDate()!=null)
				if (ex.getDateCollected().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDateCollected().before(period.getIniDate()))
					continue;
			
			if (ex.getDateCollected().before(minD)){
				minD = ex.getDateCollected();
				eb.setMicroscopy(ex);
			}
		}
		
		minD = DateUtils.getDate();
		for (ExamDST ex: tc.getExamsDST()){
			if (period.getEndDate()!=null)
				if (ex.getDateCollected().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDateCollected().before(period.getIniDate()))
				continue;
			
			if (ex.getDateCollected().before(minD)){
				minD = ex.getDateCollected();
				eb.setDst(ex);
			}
		}
		
		minD = DateUtils.getDate();
		HistologyHome hh = (HistologyHome) App.getComponent("histologyHome");
		for (Histology ex: hh.getAllResults()){
			if (period.getEndDate()!=null)
				if (ex.getDate().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDate().before(period.getIniDate()))
					continue;
			
			if (ex.getDate().before(minD)){
				minD = ex.getDate();
				eb.setHistology(ex.getResult());
			}
		}
		
		minD = DateUtils.getDate();
		for (ExamXRay ex: tc.getResXRay()){
			if (period.getEndDate()!=null)
				if (ex.getDate().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDate().before(period.getIniDate()))
					continue;
			
			if (ex.getDate().before(minD)){
				minD = ex.getDate();
				eb.setXray(ex);
			}
		}
		
		minD = DateUtils.getDate();
		for (MedicalExamination ex: tc.getExaminations()){
			if (period.getEndDate()!=null)
				if (ex.getDate().after(period.getEndDate()))
					continue;
			if (period.getIniDate()!=null)
				if (ex.getDate().before(period.getIniDate()))
					continue;
			
			if (ex.getDate().before(minD)){
				minD = ex.getDate();
				eb.setMedexam(ex);
			}
		}
		return eb;
	}

	/**
	 * Return period included few months (from begin beginMonth to end endMonth)
	 * @param beginMonth
	 * @param endMonth
	 * @return
	 */
	private Period getTreatmentPeriodMonths(int beginMonth, int endMonth) {
		Period p = new Period();
		if (tc.getTreatmentPeriod().getIniDate()!=null){
			p.setIniDate(DateUtils.incMonths(tc.getTreatmentPeriod().getIniDate(), beginMonth-1));
			p.setEndDate(DateUtils.incDays(DateUtils.incMonths(tc.getTreatmentPeriod().getIniDate(), endMonth),-1));
		}
		return p;
	}

	private void generateTable34() throws DocumentException {
		tab = new PdfPTable(6);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{12,17,26,1,19,25};
		tab.setWidths(tmp_table_widths);
		
		//TABLE 3 HEADER
		addTableHeader(App.getMessage("uk_UA.tb01pdf.3"),4);
		
		//TABLE 4 HEADER
		addTableHeader(App.getMessage("uk_UA.tb01pdf.4"),2);
		
		PatientType pt = PatientType.NEW;
		if (tc.getPatientType() != null)
			pt = tc.getPatientType();
		
		// CELL 3.1 begin
			cell = generateCheckBoxTableCell(times10, "3.1.", PatientType.NEW.equals(pt), App.getMessage("PatientType.NEW"));
			borderCell(true, true, false, true);
			cell.setRowspan(2);
			tab.addCell(cell);
		// CELL 3.1 end	
			
		// CELL 3.3 begin
			cell = generateCheckBoxTableCell(times10, "3.3.", PatientType.FAILURE_FT.equals(pt)||PatientType.FAILURE_RT.equals(pt), App.getMessage("PatientType.FAILURE"));
			borderCell(false, true, false, false);
			tab.addCell(cell);
		// CELL 3.3 end	
		
		// CELL 3.5 begin
			cell = generateCheckBoxTableCell(times10, "3.5.", PatientType.TRANSFER_IN.equals(pt),
											App.getMessage("PatientType.TRANSFER_IN"), 
											App.getMessage("form.from"),
											getFieldForFill(22), 
											getFieldForFill(41));// TODO unit from transfer in 
			borderCell(false, true, true, true);
			cell.setRowspan(2);
			tab.addCell(cell);
		// CELL 3.5 end	
			
		addEmptyCell(2);

		InfectionSite is = null;
		if (tc.getInfectionSite() != null)
			is = tc.getInfectionSite();
		// CELL 4.1 begin
			cell = generateCheckBoxTableCell(times10,"4.1.",InfectionSite.PULMONARY.equals(is)||InfectionSite.BOTH.equals(is),App.getMessage("InfectionSite.PULMONARY"));
			borderCell(true, true, false, false);
			tab.addCell(cell);
		// CELL 4.1 end	
		
		// CELL 4.3 begin
			cell = generateCheckBoxTableCell(times10,"4.3.",InfectionSite.EXTRAPULMONARY.equals(is)||InfectionSite.BOTH.equals(is),App.getMessage("InfectionSite.EXTRAPULMONARY"));
			borderCell(false, true, true, false);
			tab.addCell(cell);
		// CELL 4.3 end	
			
		// CELL 3.4 begin
			cell = generateCheckBoxTableCell(times10,"3.4.",PatientType.AFTER_DEFAULT.equals(pt),App.getMessage("PatientType.AFTER_DEFAULT"));
			borderCell(false, false, false, true);
			cell.setRowspan(2);
			tab.addCell(cell);
		// CELL 3.4 end	
		
		// CELL 4.2 begin
			String tmp_str = getFieldForFill(13);
			if (tc.getPulmonaryType()!=null || tc.getExtrapulmonaryType()!=null || tc.getExtrapulmonaryType2()!=null){
				tmp_str = "";
				if (tc.getPulmonaryType()!=null)
					tmp_str += tc.getPulmonaryType().getShortName().getDefaultName();
				if (tc.getExtrapulmonaryType()!=null)
					tmp_str += ("".equals(tmp_str)?"":", ")+tc.getExtrapulmonaryType().getShortName().getDefaultName();
				if (tc.getExtrapulmonaryType2()!=null)
					tmp_str += ("".equals(tmp_str)?"":", ")+tc.getExtrapulmonaryType2().getShortName().getDefaultName();
			}
			
			LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
			tmp_map.put("4.2. "+App.getMessage("uk_UA.tb01pdf.4.2")+": ", times10);
			tmp_map.put(tmp_str, times10b);
			cell = generateTableCell(tmp_map);
			borderCell(true, false, false, true);
			tab.addCell(cell);
		// CELL 4.2 end	

		// CELL 4.4 begin
			tmp_str=getFieldForFill(13);// TODO localization
			tmp_map.clear();
			tmp_map.put("4.4. "+App.getMessage("uk_UA.tb01pdf.4.4")+": ", times10);
			tmp_map.put(tmp_str, times10b);
			cell = generateTableCell(tmp_map);
			borderCell(false, false, true, true);
			tab.addCell(cell);
		// CELL 4.4 end	

		// CELL 3.2 begin
			cell = generateCheckBoxTableCell(times10, "3.2.", PatientType.RELAPSE.equals(pt), App.getMessage("PatientType.RELAPSE"));
			borderCell(true, false, false, true);
			tab.addCell(cell);
		// CELL 3.2 end	
			
		// CELL 3.6 begin
			cell = generateCheckBoxTableCell(times10, "3.6.", PatientType.OTHER.equals(pt), App.getMessage("PatientType.OTHER"),":",tc.getPatientTypeOther());
			borderCell(false, false, true, true);
			cell.setRowspan(2);
			tab.addCell(cell);
		// CELL 3.6 end	

		getDocument().add(tab);
	}

	private void generateTable2() throws DocumentException {
		tab = new PdfPTable(6);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{14,19,25,9,24,9};
		tab.setWidths(tmp_table_widths);
		
		//TABLE 2 HEADER
		addTableHeader(App.getMessage("uk_UA.tb01pdf.2"),6);
		
		// CELL 2.1 begin
			cell = generateTableCell(times10, App.getMessage("uk_UA.tb01pdf.2.1"));
			borderCell(true, true, false, false);
			cell.setRowspan(2);
			tab.addCell(cell);
			
			boolean cond = false;
			if (cd.getDetection().getValue()!=null)
				cond = cd.getDetection().getValue().getId().intValue()==938007;
			cell = generateCheckBoxTableCell(times10,"", cond, App.getMessage("uk_UA.tb01pdf.2.1.1"));
			borderCell(false, true, false, false);
			tab.addCell(cell);
		// CELL 2.1.1 end
		
		// CELL 2.3 begin
			cell = generateTableCell(times10, "2.3.",App.getMessage("uk_UA.dateFirstVisitGMC"));
			borderCell(false, true, false, false);
			tab.addCell(cell);

			cell = generateTableCell(times10b,getFieldDate(cd.getDateFirstVisitGMC()));
			borderCell(false, true, false, false);
			tab.addCell(cell);
		// CELL 2.3 end
		
		// CELL 2.6 begin
			cell = generateTableCell(times10, "2.6.",App.getMessage("TbCase.diagnosisDate"));
			borderCell(false, true, false, false);
			tab.addCell(cell);

			cell = generateTableCell(times10b, getFieldDate(tc.getDiagnosisDate()));
			borderCell(false, true, true, false);
			tab.addCell(cell);
		// CELL 2.6 end
		
		// CELL 2.1.2 begin
			cond = false;
			if (cd.getDetection().getValue()!=null)
				cond = cd.getDetection().getValue().getId().intValue()==938241||cd.getDetection().getValue().getId().intValue()==938008;
			cell = generateCheckBoxTableCell(times10,"", cond, App.getMessage("uk_UA.tb01pdf.2.1.2"));
			tab.addCell(cell);
		// CELL 2.1.2 end
		
		// CELL 2.4 begin
			tab.addCell(generateTableCell(times10, "2.4.",App.getMessage("uk_UA.firstVisitDate")));
			tab.addCell(generateTableCell(times10b, getFieldDate(tc.getRegistrationDate())));
		// CELL 2.4 end
		
		// CELL 2.7 begin
			tab.addCell(generateTableCell(times10, "2.7.",App.getMessage("uk_UA.hospitalizationDate")));
			
			cell = generateTableCell(times10b, getFieldDate(cd.getHospitalizationDate()));
			borderCell(false, false, true, false);
			tab.addCell(cell);
		// CELL 2.7 end
		
		// CELL 2.2 begin
			LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
			tmp_map.put("2.2. "+App.getMessage("uk_UA.dateFirstSymptoms"), times10);
			tmp_map.put(getFieldDate(cd.getDateFirstSymptoms()), times10b);
			cell = generateTableCell(tmp_map);
			cell.setColspan(2);
			borderCell(true, false, false, true);
			tab.addCell(cell);
		// CELL 2.2 end
		
		// CELL 2.5 begin
			cell = generateTableCell(times10,"2.5.",App.getMessage("TbCase.iniTreatmentDate"));
			borderCell(false, false, false, true);
			tab.addCell(cell);
			
			cell = generateTableCell(times10b,getFieldDate(tc.getTreatmentPeriod().getIniDate()));
			borderCell(false, false, false, true);
			tab.addCell(cell);
		// CELL 2.5 end
		
		// CELL 2.8 begin
			cell = generateTableCell(times10,"2.8.",App.getMessage("uk_UA.dischargeDate"));
			borderCell(false, false, false, true);
			tab.addCell(cell);
			
			cell = generateTableCell(times10b,getFieldDate(cd.getDischargeDate()));
			borderCell(false, false, true, true);
			tab.addCell(cell);
		// CELL 2.8 end
		
		getDocument().add(tab);
	}

	private void generateTable1() throws DocumentException {
		tab = new PdfPTable(5);
		tab.setSpacingBefore(0F);
		tab.setSpacingAfter(0F);
		tab.setWidthPercentage(100F);
		float[] tmp_table_widths = new float[]{52,32,10,3,3};
		tab.setWidths(tmp_table_widths);
		
		addTableHeader(App.getMessage("uk_UA.tb01pdf.1"),5);
		
		setCurLeadTab(0F); // leading for all cells of table 1
		tab.getDefaultCell().setLeading(getCurLeadTab(), 1);
		//CELL 1.1 begin
			LinkedHashMap<String,Font> tmp_map = new LinkedHashMap<String, Font>();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.1"),times10);
			tmp_map.put(tc.getPatient().getFullName(), times10b);
			cell = generateTableCell(tmp_map);
			borderCell(true, true, false, false);
			tab.addCell(cell);
		//CELL 1.1 end
		
		//CELL 1.2 begin
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.2"),times10);
			tmp_map.put(getFieldDate(tc.getPatient().getBirthDate()), times10b);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.2.2"), times10);
			tmp_map.put(String.valueOf(tc.getPatientAge()), times10b);
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.2.3"), times10);
			cell = generateTableCell(tmp_map);
			borderCell(false, true, false, false);
			tab.addCell(cell);
		//CELL 1.2 end
		
		//CELL 1.3 begin
			cell = generateTableCell(times10, App.getMessage("uk_UA.tb01pdf.1.3"));
			borderCell(false, true, false, false);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10,"", Gender.MALE.equals(tc.getPatient().getGender()), App.getMessage("uk_UA.tb01pdf.1.3.m"));
			borderCell(false, true, false, false);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10,"", Gender.FEMALE.equals(tc.getPatient().getGender()), App.getMessage("uk_UA.tb01pdf.1.3.f"));
			borderCell(false, true, true, false);
			tab.addCell(cell);
		//CELL 1.3 end
		
		//CELL 1.4 begin
			String tmp_str = tc.getNotifAddress().toString()+
						((tc.getPhoneNumber()!=null && !tc.getPhoneNumber().isEmpty())?", "+tc.getPhoneNumber():"")+
						((tc.getMobileNumber()!=null && !tc.getMobileNumber().isEmpty())?", "+tc.getMobileNumber():"");
			tmp_str = tmp_str.replaceAll("\n", ", ");
			
			if (tmp_str.isEmpty()) tmp_str = getFieldForFill(110);
			
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.4"),times10);
			tmp_map.put(tmp_str, times10b);
			cell = generateTableCell(tmp_map);
			borderCell(true, false, false, false);
			cell.setColspan(2);
			tab.addCell(cell);
		//CELL 1.4 end
		
		//CELL 1.5 begin
			cell = generateTableCell(times10,App.getMessage("uk_UA.tb01pdf.1.5"));
			cell.setBorder(Rectangle.NO_BORDER);
			tab.addCell(cell);

			cell = generateCheckBoxTableCell(times10,"",LocalityType.URBAN.equals(tc.getNotifAddress().getLocalityType()),
											App.getMessage("uk_UA.tb01pdf.1.5.c"));//city
			cell.setBorder(Rectangle.NO_BORDER);
			tab.addCell(cell);
			
			cell = generateCheckBoxTableCell(times10,"",LocalityType.RURAL.equals(tc.getNotifAddress().getLocalityType()),
					App.getMessage("uk_UA.tb01pdf.1.5.v"));//village
			borderCell(false, false, true, false);
			tab.addCell(cell);
		//CELL 1.5 end
		
		//CELL 1.6 begin
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.6"),times10);
			String s = getFieldForFill(115);
			if (cd.getClosestContact()!=null && !"".equals(cd.getClosestContact()))
				s = cd.getClosestContact();
			tmp_map.put(s, times10b);
			cell = new PdfPCell(generateTableCell(tmp_map));
			borderCell(true, false, true, false);
			cell.setColspan(5);
			tab.addCell(cell);
		//CELL 1.6 end
		
		//CELL 1.7 begin
			tmp_map.clear();
			tmp_map.put(App.getMessage("uk_UA.tb01pdf.1.7"),times10);
			s = getFieldForFill(135);
			if (cd.getEmployerName()!=null && !"".equals(cd.getEmployerName()))
				s = cd.getEmployerName();
			tmp_map.put(s, times10b);
			cell = new PdfPCell(generateTableCell(tmp_map));
			borderCell(true, false, true, true);
			cell.setColspan(5);
			tab.addCell(cell);
		//CELL 1.7 end
		
		getDocument().add(tab);
	}

	private void addTableHeader(String tabname,int colspan) {
		cell = new PdfPCell();
		cell.addElement(new Chunk(tabname,times10bi));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setLeading(0F,1);
		cell.setColspan(colspan);
		tab.addCell(cell);
	}
	
	/**
	 * Class contains necessary exams for TB01 from case info
	 * @author A.M.
	 */
	private class ExamBlock{
		private ExamCulture culture;
		private ExamMicroscopy microscopy;
		private ExamDST dst;
		private HistologyResult histology;
		private ExamXRay xray;
		private MedicalExamination medexam;
		
		public ExamDSTResult getDSTResult(Substance sub){
			ExamDSTResult result = null;
			if (dst!=null){
				result = dst.findResultBySubstance(sub);
			}
			return result;
		}
		
		public ExamCulture getCulture() {
			return culture;
		}
		public void setCulture(ExamCulture culture) {
			this.culture = culture;
		}
		public ExamMicroscopy getMicroscopy() {
			return microscopy;
		}
		public void setMicroscopy(ExamMicroscopy microscopy) {
			this.microscopy = microscopy;
		}

		public void setDst(ExamDST dst) {
			this.dst = dst;
		}
		public HistologyResult getHistology() {
			return histology;
		}
		public void setHistology(HistologyResult histology) {
			this.histology = histology;
		}
		public ExamXRay getXray() {
			return xray;
		}
		public void setXray(ExamXRay xray) {
			this.xray = xray;
		}
		public MedicalExamination getMedexam() {
			return medexam;
		}
		public void setMedexam(MedicalExamination medexam) {
			this.medexam = medexam;
		}
	}

}
