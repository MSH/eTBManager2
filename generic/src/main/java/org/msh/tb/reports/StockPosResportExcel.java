package org.msh.tb.reports;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.adminunits.InfoCountryLevels;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.export.ExcelCreator;
@Name("stockPosResportExcel")
public class StockPosResportExcel {
	@In(create=true) Map<String, String> messages;
	@In(create=true) StockPosReport stockPosReport;
	@In(create=true) InfoCountryLevels levelInfo;
	
	private ExcelCreator excel;
	private final static int MAX_ROWS_PER_SHEET = 40000; // max: 65536 in excel 2003 or later
	private final static int TITLE_ROW = 1;
	
	public void downloadExcel(List<Medicine> medicines, ReportSelection reportSelection, StockPosReportItem root){
		int iSheets = 0;
		excel = new ExcelCreator();
		excel.setFileName(messages.get("manag.rel1"));
		excel.createWorkbook();
		
		excel.addSheet(messages.get("manag.rel1")+" - pg. " + iSheets+1, iSheets);
		excel.setRow(TITLE_ROW);
		
		//Add Super Tittle
		excel.addGroupHeaderFromResource("manag.rel1", medicines.size()+1, "title");
		excel.lineBreak();
		
		//Add Filters Used
		excel.addText(messages.get("Source") + ":");
		excel.addText(reportSelection.getSource() == null ? messages.get("form.noselection") : reportSelection.getSource().getName().getName1());
		excel.lineBreak();
		
		excel.addText(levelInfo.getNameLevel1() + ":");
		excel.addText(reportSelection.getAuselection().getUnitLevel1() == null ? messages.get("form.noselection") : reportSelection.getAuselection().getUnitLevel1().toString());
		excel.lineBreak();
		
		excel.addText(messages.get("MedicineLine") + ":");
		excel.addText(reportSelection.getMedicineLine() == null ? messages.get("form.noselection") : messages.get(reportSelection.getMedicineLine().getKey()));
		excel.lineBreak();
		
		excel.lineBreak();
		
		addTitles(medicines);
		
		//Add Content
		for(StockPosReportItem item: root.getChildren()){
			excel.lineBreak();
			if(item.getItem() instanceof Tbunit)
				excel.addText(((Tbunit)item.getItem()).getName().toString(), "subtitle");
			else if(item.getItem() instanceof AdministrativeUnit)
				excel.addText(((AdministrativeUnit)item.getItem()).getName().toString(), "subtitle");
			else
				excel.addText("ERRO", "subtitle");
			
			for(int i = 0; i <= medicines.size()-1; i++){
				excel.addNumber(item.getQuantities()[i], "subtitle");
			}
			
			for(StockPosReportItem item2: item.getChildren()){
				excel.lineBreak();
				if(item2.getItem() instanceof Tbunit)
					excel.addText(((Tbunit)item2.getItem()).getName().toString());
				else if(item2.getItem() instanceof AdministrativeUnit)
					excel.addText(((AdministrativeUnit)item2.getItem()).getName().toString());
				else
					excel.addText("ERRO");
				
				for(int i = 0; i <= medicines.size()-1; i++){
					excel.addNumber(item2.getQuantities()[i]);
				}
			}
			
			//check max rows per sheet
			if(excel.getRow() == MAX_ROWS_PER_SHEET){
				iSheets += 1;
				excel.addSheet(messages.get("cases")+" - pg. " + iSheets+1, iSheets);
				addTitles(medicines);
			}
		}
		
		//add total
		excel.lineBreak();
		excel.addTextFromResource("global.total", "title");
		for(int i = 0; i <= medicines.size()-1; i++){
			Integer number = root.getQuantities()[i];
			excel.addNumber(number, "title");
		}
		
		excel.sendResponse();
	}
	
	public void addTitles(List<Medicine> medicines){
		excel.addText(levelInfo.getNameLevel1() + " / " + messages.get("Tbunit"), "title");
		for(Medicine m : medicines){
			excel.addText(m.getAbbrevName() + " " + m.getStrength() + " " + m.getStrengthUnit(), "title");
		}
	}

}
