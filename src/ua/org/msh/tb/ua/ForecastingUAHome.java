package org.msh.tb.ua;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingMedicine;
import org.msh.tb.entities.enums.MedicineCategory;
import org.msh.tb.entities.enums.MedicineLine;
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
	
	private Forecasting forecasting;
	
	private WritableWorkbook workbook; // переменна€ рабочей книги
	public WritableSheet sheet;
	public WritableCellFormat times12ptBoldFormat;
	public WritableCellFormat times12ptBoldUnderlineFormat;
	public WritableCellFormat times12ptFormat;
	public WritableCellFormat times10ptItalicFormat;
	
	public Label label;
	private File excelFile;
	private int row;
	private int column;
	String fileName;
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void toExcel() throws WriteException, IOException // метод создает книгу с одной раб страницей
	{
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(new Locale("ru", "RU"));
		fileName = "Medicines_"+DateUtils.getDate().getDate()+"."+DateUtils.getDate().getMonth()+"."+(DateUtils.getDate().getYear()-100);
		excelFile = File.createTempFile("etbmanager", "xls");
		try {
			workbook = Workbook.createWorkbook(excelFile, ws);		//им€ и путь файла
			sheet = workbook.createSheet(fileName, 0);	//название листа
			setCellFormat();
			test();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return;

		if (excelFile == null)
			return;
		HttpServletResponse response = (HttpServletResponse)fc.getExternalContext().getResponse();
		response.reset();

		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(((Long)excelFile.length()).intValue());
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
		OutputStream os;
		try {
			FileInputStream in = new FileInputStream(excelFile);
			os = response.getOutputStream();
			byte[] buffer = new byte[2048];
			int size = 0;
			
			while ((size = in.read(buffer)) != -1)
				os.write(buffer, 0, size);

			os.flush();
	        os.close();
	        fc.responseComplete();
		} catch (IOException e) {
			e.printStackTrace();
			excelFile.delete();
		}
	}
	
	private void setCellFormat() throws WriteException{
		//установка шрифта
		WritableFont times12ptBold = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
		times12ptBoldFormat = new WritableCellFormat(times12ptBold);
		//выравнивание по центру
		times12ptBoldFormat.setAlignment(Alignment.CENTRE);
		times12ptBoldFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		
		//перенос по словам если не помещаетс€
		times12ptBoldFormat.setWrap(true);
		//установить цвет
		//times12ptBoldFormat.setBackground(Colour.GRAY_25);
		//рисуем рамку
		times12ptBoldFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		//поворот текста
		//times12ptBoldFormat.setOrientation(Orientation.PLUS_90);
		
		WritableFont times12ptBoldUnderline = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD,false,UnderlineStyle.SINGLE);
		times12ptBoldUnderlineFormat = new WritableCellFormat(times12ptBoldUnderline);
		times12ptBoldUnderlineFormat.setAlignment(Alignment.CENTRE);
		times12ptBoldUnderlineFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		
		WritableFont times12pt = new WritableFont(WritableFont.TIMES, 12, WritableFont.NO_BOLD);
		times12ptFormat = new WritableCellFormat(times12pt);
		times12ptFormat.setAlignment(Alignment.CENTRE);
		times12ptFormat.setWrap(true);
		times12ptFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		times12ptFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		
		
		WritableFont times10ptItalic = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD,true);
		times10ptItalicFormat = new WritableCellFormat(times10ptItalic);
		times10ptItalicFormat.setAlignment(Alignment.CENTRE);
		times10ptItalicFormat.setWrap(true);
		times10ptItalicFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	}
	
	public void test() throws RowsExceededException, WriteException 
	{
    	//пример добавлени€ в €чейки
    	
		sheet.setColumnView(1, 25); 
		for (int i = 2; i < 13; i++) {
			sheet.setColumnView(i, 15);
		}
		
		addTitles();
		execute();
		forecasting = getForecasting();
		int i = 0;
		for (ForecastingMedicine fm : forecasting.getMedicines()) 
			if (fm.getMedicine().getLine().ordinal() == MedicineLine.FIRST_LINE.ordinal())
				fillRow(fm,i++);
		row++;
		addTextFromResource("manag.forecast.excel.total1", times12ptBoldFormat);
		
		/*StringBuffer buf;
		Formula f;
		
		for (int j = 2; j < 11; j++) {
			setColumn(j);
			buf = new StringBuffer();
			buf.append("SUM(R[-"+i+"]C:R[-1]C)");
			f = new Formula(j, row, buf.toString(),times12ptBoldFormat);
			sheet.addCell(f);
		}*/
		
		row++;
		setColumn(0);
		addTextFromResource("manag.forecast.excel.colspan2", times12ptBoldFormat);
		sheet.mergeCells(0,row,11,row);
		row++;
		setColumn(1);
		addTextFromResource("manag.forecast.excel.type1", times12ptBoldUnderlineFormat);
		
		for (ForecastingMedicine fm : forecasting.getMedicines()) 
			if (fm.getMedicine().getLine().ordinal() == MedicineLine.SECOND_LINE.ordinal())
				if (fm.getMedicine().getCategory().ordinal() == MedicineCategory.INJECTABLE.ordinal())	
					fillRow(fm,i++);

		row++;
		addTextFromResource("manag.forecast.excel.colspan3", times12ptBoldUnderlineFormat);
		sheet.mergeCells(1,row,2,row);
		for (ForecastingMedicine fm : forecasting.getMedicines()) 
			if (fm.getMedicine().getLine().ordinal() == MedicineLine.SECOND_LINE.ordinal())
				if (fm.getMedicine().getCategory().ordinal() == MedicineCategory.ORAL.ordinal())	
					fillRow(fm,i++);
		
		row++;
		setColumn(0);
		addTextFromResource("manag.forecast.excel.colspan4", times12ptBoldFormat);
		sheet.mergeCells(0,row,11,row);
		for (ForecastingMedicine fm : forecasting.getMedicines()) 
			if (fm.getMedicine().getLine().ordinal() == MedicineLine.OTHER.ordinal())
				fillRow(fm,i++);
		setColumn(1);
		row++;
		addTextFromResource("manag.forecast.excel.total2", times12ptBoldFormat);
		row++;
		addTextFromResource("manag.forecast.excel.total", times12ptBoldFormat);
		
		
	}
	
	private void fillRow(ForecastingMedicine fm, int i) throws RowsExceededException, WriteException{
		i++;
		row++;
		setColumn(0);
		addText(i+".",null);
		setColumn(1);
		addText(fm.getMedicine().toString(),times12ptBoldFormat);
		setColumn(2);
		addNumber(fm.getStockOnHand(),null);
		setColumn(3);
		addNumber(fm.getForecasting().getNumMonths(),null);
		setColumn(4);
		addNumber(fm.getConsumptionCases(),null);
		setColumn(5);
		addNumber(fm.getConsumptionNewCases(),null);
		setColumn(6);
		addNumber(fm.getForecasting().getBufferStock(),null);
		setColumn(8);
		addNumber(fm.getStockOnHandAfterLT(),null);
		setColumn(10);
		addNumber(fm.getUnitPrice(),null);
		setColumn(11);
		addNumber(fm.getTotalPrice(),times12ptBoldFormat);
		setColumn(1);
	}
	
	private void addTitles() throws RowsExceededException, WriteException {		
		setRow(2);
		setColumn(0);
		addTextFromResource("manag.forecast.excel.n", times12ptBoldFormat);
		
		for (int i = 2; i < 13; i++) {
			setRow(2);
			setColumn(i-1);
			addTextFromResource("manag.forecast.excel.col"+i, times12ptBoldFormat);
		}
		setColumn(0);
		setRow(3);
		for (int i = 1; i < 13; i++) {
			setColumn(i-1);
			addTextFromResource(Integer.toString(i), times10ptItalicFormat);			
		}
		
		setRow(4);
		setColumn(0);
		addTextFromResource("manag.forecast.excel.colspan1", times12ptBoldFormat);
		sheet.mergeCells(0,4,11,4);
	}
	
	
	public void addTextFromResource(String resourceKey, WritableCellFormat cellFormatName) throws RowsExceededException, WriteException {
		if (resourceKey == null) {
			column++;
			return;
		}
		addText(Messages.instance().get(resourceKey),cellFormatName);
	}
	
	public void addText(String resourceKey, WritableCellFormat cellFormatName) throws RowsExceededException, WriteException {
		if (resourceKey == null) {
			column++;
			return;
		}
		if (cellFormatName == null)	
			cellFormatName = times12ptFormat;
		Label label = new Label(column, row, resourceKey, cellFormatName);
		try {
			sheet.addCell(label);	//добавление данных в лист sheet с обработкой исключений
		} catch (RowsExceededException e) {
			e.printStackTrace();
		}
	}
	
	public void addNumber(float resourceKey, WritableCellFormat cellFormatName) throws RowsExceededException, WriteException {
		if (cellFormatName == null)	
			cellFormatName = times12ptFormat;
		Number label = new Number(column, row, resourceKey, cellFormatName);
		try {
			sheet.addCell(label);	//добавление данных в лист sheet с обработкой исключений
		} catch (RowsExceededException e) {
			e.printStackTrace();
		}
	}
	
	

	
}	
