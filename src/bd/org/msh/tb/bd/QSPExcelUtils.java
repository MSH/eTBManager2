package org.msh.tb.bd;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.bd.QuarterBatchExpiringReport.ExpiringBatchDetails;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.export.ExcelCreator;

@Name("qspExcelUtils")
public class QSPExcelUtils {
	@In(create=true) Map<String, String> messages;
	private ExcelCreator excel;
	private final static int MAX_CASES_PER_SHEET = 65500; // max: 65536 in excel 2003 or later
	private final static int TITLE_ROW = 1;
	
	public void downloadQuarterlyConsolidatedSP(Source source, Quarter quarter, AdministrativeUnit adminunit, Tbunit unit, List<QSPMedicineRow> rows, List<Tbunit> pendCloseQuarterUnits, List<Tbunit> unitsNotInitialized){
		int iSheets = 0;
		excel = new ExcelCreator();
		excel.setFileName(messages.get("manag.rel7") + " " + (messages.get(quarter.getQuarter().getKey()) + "-" + quarter.getYear()));
		excel.createWorkbook();
		
		excel.addSheet(messages.get("QCSP")+" - pg. " + iSheets+1, iSheets);
		excel.setRow(TITLE_ROW);
		
		//Add Super Tittle
		excel.addGroupHeaderFromResource("manag.rel7", 11, "title");
		excel.lineBreak();
		
		//Add Filters Used
		excel.addText(messages.get("Source") + ":");
		excel.addText(source == null ? messages.get("form.noselection") : source.getName().getName1());
		excel.lineBreak();
		
		excel.addText(messages.get("Quarter") + ":");
		excel.addText(messages.get(quarter.getQuarter().getKey()) + "/" + quarter.getYear());
		excel.lineBreak();
		
		if (unit != null){
			excel.addText(messages.get("Tbunit") + ":");
			excel.addText(unit.getName().getName1() + " - " + unit.getAdminUnit().getFullDisplayName2());
			excel.lineBreak();
		}else if(adminunit != null){
			excel.addText(messages.get("AdministrativeUnit") + ":");
			excel.addText(adminunit.getFullDisplayName2());
			excel.lineBreak();
		}else if(unit == null && adminunit == null){
			excel.addText(messages.get("AdministrativeUnit") + ":");
			excel.addText(messages.get("form.noselection"));
			excel.lineBreak();
		}
		
		excel.lineBreak();
		
		addTitlesQuarterlyConsolidatedSP();
		
		//Add Content
		for(QSPMedicineRow r : rows){
			excel.lineBreak();
			addContentQuarterlyConsolidatedSP(r);
			
			if(excel.getRow() == MAX_CASES_PER_SHEET){
				iSheets += 1;
				excel.addSheet(messages.get("cases")+" - pg. " + iSheets+1, iSheets);
				addTitlesQuarterlyConsolidatedSP();
			}
		}
		
		includePendAndNotInitializedUnits(pendCloseQuarterUnits, unitsNotInitialized);
		
		excel.sendResponse();
	}
	
	public void addTitlesQuarterlyConsolidatedSP(){
		excel.addTextFromResource("Medicine", "title");
		excel.addTextFromResource("quarter.openbal", "title");
		excel.addTextFromResource("quarter.received", "title");
		excel.addTextFromResource("quarter.posadjust", "title");
		excel.addTextFromResource("quarter.negadjust", "title");
		excel.addTextFromResource("manag.forecast.tabres2", "title");
		excel.addTextFromResource("quarter.expired", "title");
		excel.addTextFromResource("quarter.closbal", "title");
		excel.addTextFromResource("quarter.outofstk", "title");
		excel.addTextFromResource("meds.amc", "title");
		excel.addTextFromResource("meds.stockoutdate", "title");
	}
	
	public void addContentQuarterlyConsolidatedSP(QSPMedicineRow row){
		excel.addText(row.getMedicine().toString());
		excel.addNumber(row.getOpeningBalance());
		excel.addNumber(row.getReceivedFromCS());
		excel.addNumber(row.getPositiveAdjust());
		excel.addNumber(row.getNegativeAdjust());
		excel.addNumber(row.getDispensed());
		excel.addNumber(row.getExpired());
		excel.addNumber(row.getClosingBalance());
		excel.addNumber(row.getOutOfStockDays());
		excel.addNumber(row.getAmc());
		
		//Months of stock
		if(row.getAmc() == 0){
			excel.addTextFromResource("meds.undefined");
		}else if(row.getAmc() <= 1){
			excel.addTextFromResource("meds.onemonthless");
		}else if(row.getAmc() > 1){
			excel.addText(row.getEstimatedMonthsOfStock().toString());
		}
	}
	
	public void downloadQuarterlyBatchExpiringReport(Source source, Quarter quarter, AdministrativeUnit adminunit, Tbunit unit, List<ExpiringBatchDetails> unitBatchDetails, Map<Batch, Long> batchDetailsConsolidated, List<Tbunit> pendCloseQuarterUnits, List<Tbunit> unitsNotInitialized){
		int iSheets = 0;
		excel = new ExcelCreator();
		excel.setFileName(messages.get("quarter.expiringbatchlist") + " " + (messages.get(quarter.getQuarter().getKey()) + "-" + quarter.getYear()));
		excel.createWorkbook();
		
		excel.addSheet(messages.get("QBER")+" - pg. " + iSheets+1, iSheets);
		excel.setRow(TITLE_ROW);
		
		//Add Super Tittle
		excel.addGroupHeaderFromResource("quarter.expiringbatchlist", 4, "title");
		excel.lineBreak();
		
		//Add Filters Used
		excel.addText(messages.get("Source") + ":");
		excel.addText(source == null ? messages.get("form.noselection") : source.getName().getName1());
		excel.lineBreak();
		
		excel.addText(messages.get("Quarter") + ":");
		excel.addText(messages.get(quarter.getQuarter().getKey()) + "/" + quarter.getYear());
		excel.lineBreak();
		
		if (unit != null){
			excel.addText(messages.get("Tbunit") + ":");
			excel.addText(unit.getName().getName1() + " - " + unit.getAdminUnit().getFullDisplayName2());
			excel.lineBreak();
		}else if(adminunit != null){
			excel.addText(messages.get("AdministrativeUnit") + ":");
			excel.addText(adminunit.getFullDisplayName2());
			excel.lineBreak();
		}else if(unit == null && adminunit == null){
			excel.addText(messages.get("AdministrativeUnit") + ":");
			excel.addText(messages.get("form.noselection"));
			excel.lineBreak();
		}
		
		excel.lineBreak();
		
		//Add units information
		for(ExpiringBatchDetails d : unitBatchDetails){
			excel.lineBreak();
			addUnitQuarterlyBatchExpiringReport(d);
			
			if(excel.getRow() == MAX_CASES_PER_SHEET){
				iSheets += 1;
				excel.addSheet(messages.get("cases")+" - pg. " + iSheets+1, iSheets);
				addTitlesQuarterlyConsolidatedSP();
			}
		}
		
		excel.lineBreak();
		
		//add Consolidated content
		addUnitQuarterlyBatchExpiringConsolidatedReport(batchDetailsConsolidated);
		
		//Add List of units
		includePendAndNotInitializedUnits(pendCloseQuarterUnits, unitsNotInitialized);
		
		excel.sendResponse();
	}
	
	public void addUnitQuarterlyBatchExpiringReport(ExpiringBatchDetails d){
		excel.addText(d.getUnit().getName().getName1() + " (" + d.getUnit().getAdminUnit().getFullDisplayName2() + ")");
		
		excel.lineBreak();
		
		if(d.getBatchDetailsKeySet().size() > 0){
			excel.addTextFromResource("Medicine", "title");
			excel.addTextFromResource("Batch", "title");
			excel.addTextFromResource("Batch.expiryDate", "title");
			excel.addTextFromResource("Movement.quantity", "title");
		
			excel.lineBreak();
			
			for(Batch b : d.getBatchDetailsKeySet()){
				excel.addText(b.getMedicine().toString());
				excel.addText(b.getBatchNumber() + (b.getManufacturer() != null ? " - " : "") + b.getManufacturer());
				excel.addDate(b.getExpiryDate());
				excel.addNumber(d.getBatchInfo().get(b));
				excel.lineBreak();
			}
		}else{
			excel.addTextFromResource("quarter.expiringbatchlist.noresultmsg");
			excel.lineBreak();
		}
		excel.lineBreak();
	}
	
	public void addUnitQuarterlyBatchExpiringConsolidatedReport(Map<Batch, Long> batchDetailsConsolidated){
		excel.addText("Consolidated");
		
		excel.lineBreak();
		
		if(batchDetailsConsolidated != null && batchDetailsConsolidated.size() > 0){
			excel.addTextFromResource("Medicine", "title");
			excel.addTextFromResource("Batch", "title");
			excel.addTextFromResource("Batch.expiryDate", "title");
			excel.addTextFromResource("Movement.quantity", "title");
		
			excel.lineBreak();
			
			for(Batch b : batchDetailsConsolidated.keySet()){
				excel.addText(b.getMedicine().toString());
				excel.addText(b.getBatchNumber() + (b.getManufacturer() != null ? " - " : "") + b.getManufacturer());
				excel.addDate(b.getExpiryDate());
				excel.addNumber(batchDetailsConsolidated.get(b));
				excel.lineBreak();
			}
		}else{
			excel.addTextFromResource("quarter.expiringbatchlist.noresultmsg");
			excel.lineBreak();
		}
		excel.lineBreak();
	}
	
	private void includePendAndNotInitializedUnits(List<Tbunit> pendCloseQuarterUnits, List<Tbunit> unitsNotInitialized){
		excel.lineBreak();
		excel.lineBreak();
		excel.lineBreak();
		
		if(pendCloseQuarterUnits != null && pendCloseQuarterUnits.size() > 0){
			String m = messages.get("quarter.openquarterunit");
			m = m.replace("{0}", new Integer(pendCloseQuarterUnits.size()).toString());
			
			excel.addTextFromResource(m,"title");
			excel.lineBreak();
			for(Tbunit u : pendCloseQuarterUnits){
				excel.addText(u.getName().getName1());
				excel.lineBreak();
			}
			excel.lineBreak();
			excel.lineBreak();
		}
		
		if(unitsNotInitialized != null && unitsNotInitialized.size() > 0){
			String m = messages.get("quarter.notinitializedunit");
			m = m.replace("{0}", new Integer(unitsNotInitialized.size()).toString());
			
			excel.addTextFromResource(m,"title");
			excel.lineBreak();
			for(Tbunit u : unitsNotInitialized){
				excel.addText(u.getName().getName1());
				excel.lineBreak();
			}
			excel.lineBreak();
			excel.lineBreak();
		}
	}

}
