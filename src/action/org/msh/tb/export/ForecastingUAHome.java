package org.msh.tb.export;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.forecasting.ForecastingCalculation;
import org.msh.tb.forecasting.ForecastingHome;
import org.msh.utils.date.DateUtils;

/**
 * Calculate a forecasting and export its results to Excel
 * @author alexxme
 *
 */
@Name("forecastingUAHome")
public class ForecastingUAHome extends ForecastingCalculation{
	private static final long serialVersionUID = -3844270483725397280L;
	@In(create=true) ForecastingHome forecastingHome;
	@In(create=true) ForecastingCalculation forecastingCalculation;
	private Date endForecasting;
	private int numMonths;
	/**
	 * Creator of the Excel file 
	 */
	private ExcelCreator excel;
	
	private Forecasting forecasting;
	
	public void toExcel() {
		excel = new ExcelCreator();
		excel.setFileName("Medicines_"+DateUtils.getDate().getDate()+"."+DateUtils.getDate().getMonth()+"."+(DateUtils.getDate().getYear()-100));
		excel.createWorkbook();
		excel.addSheet(Messages.instance().get("MedicineReceiving.items"), 0);
		
		addTitles();
		execute();
		
		excel.sendResponse();	
	}
	
	private void addTitles() {		
		excel.setRow(2);
		excel.addTextFromResource("manag.forecast.excel.n", "title");
		excel.addTextFromResource("manag.forecast.excel.col2", "title");
		excel.addTextFromResource("manag.forecast.excel.col3", "title");
		excel.addTextFromResource("manag.forecast.excel.col4", "title");
		excel.addTextFromResource("manag.forecast.excel.col5", "title");
		excel.addTextFromResource("manag.forecast.excel.col6", "title");
		excel.addTextFromResource("manag.forecast.excel.col7", "title");
		excel.addTextFromResource("manag.forecast.excel.col8", "title");
		excel.addTextFromResource("manag.forecast.excel.col9", "title");
		excel.addTextFromResource("manag.forecast.excel.col10", "title");
		excel.addTextFromResource("manag.forecast.excel.col11", "title");
		excel.addTextFromResource("manag.forecast.excel.col12", "title");
		excel.setColumn(0);
		excel.setRow(3);
		for (int i = 1; i < 13; i++) {
			excel.addTextFromResource(Integer.toString(i));			
		}
		excel.setRow(5);
		excel.setColumn(0);
		excel.addGroupHeaderFromResource("manag.forecast.excel.colspan1", 12, "cell");
		
		/*if (levelInfo.isHasLevel1()) 
			excel.addText(levelInfo.getNameLevel1().toString(), "title");
		if (levelInfo.isHasLevel2()) 
			excel.addText(levelInfo.getNameLevel2().toString(), "title");
		if (levelInfo.isHasLevel3()) 
			excel.addText(levelInfo.getNameLevel3().toString(), "title");
		if (levelInfo.isHasLevel4()) 
			excel.addText(levelInfo.getNameLevel4().toString(), "title");
		if (levelInfo.isHasLevel5()) 
			excel.addText(levelInfo.getNameLevel5().toString(), "title");
		 */
	}
	
	
	public void addTextFromResource(String resourceKey, String cellFormatName) {
		if (resourceKey == null) {
			excel.setColumn(excel.getColumn()+1);
			return;
		}
		excel.addText(resourceKey, cellFormatName );
	}

	
}	