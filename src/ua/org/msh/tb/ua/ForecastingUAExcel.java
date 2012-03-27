package org.msh.tb.ua;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
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
import org.msh.tb.forecasting.ForecastingCalculation;
import org.msh.utils.date.DateUtils;

/**
 * Calculate a forecasting and export its results to Excel
 * @author alexxme
 *
 */
@Name("forecastingUAExcel")
public class ForecastingUAExcel{
	private static final long serialVersionUID = -3844270483725397280L;
	@In(create=true) ForecastingCalculation forecastingCalculation;

	/**
	 * Creator of the Excel file 
	 */
	
	private Forecasting forecasting;
	private WritableWorkbook workbook;
	private WritableSheet sheet;
	private WritableCellFormat times12ptBoldFormat;
	private WritableCellFormat times12ptBoldUnderlineFormat;
	private WritableCellFormat times12ptFormat;
	private WritableCellFormat times10ptItalicFormat;
	private WritableCellFormat floatFormat;
	private WritableCellFormat floatBoldFormat;
	
	private File excelFile;
	private int row;
	private int column;
	
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
		String fileName = "Medicines_"+DateUtils.getDate().getDate()+"."+DateUtils.getDate().getMonth()+"."+(DateUtils.getDate().getYear()-100);
		excelFile = File.createTempFile("etbmanager", "xls");
		try {
			workbook = Workbook.createWorkbook(excelFile, ws);		//имя и путь файла
			sheet = workbook.createSheet(fileName, 0);	//название листа
			setCellFormat();
			fillTable();
			
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
		WritableFont times12ptBold = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
		times12ptBoldFormat = new WritableCellFormat(times12ptBold);
		times12ptBoldFormat.setAlignment(Alignment.CENTRE);
		times12ptBoldFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		times12ptBoldFormat.setWrap(true);
		times12ptBoldFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		
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
		
		floatFormat = new WritableCellFormat(times12pt,NumberFormats.FLOAT);
		floatFormat.setAlignment(Alignment.CENTRE);
		floatFormat.setWrap(true);
		floatFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		floatFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		
		floatBoldFormat = new WritableCellFormat(times12ptBold,NumberFormats.FLOAT);
		floatBoldFormat.setAlignment(Alignment.CENTRE);
		floatBoldFormat.setWrap(true);
		floatBoldFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		floatBoldFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		
		
		WritableFont times10ptItalic = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD,true);
		times10ptItalicFormat = new WritableCellFormat(times10ptItalic);
		times10ptItalicFormat.setAlignment(Alignment.CENTRE);
		times10ptItalicFormat.setWrap(true);
		times10ptItalicFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		
		CellView cv = new CellView();
		cv.setFormat(times12ptFormat);
	}
	
	public void fillTable() throws RowsExceededException, WriteException 
	{
		sheet.setColumnView(1, 25); 
		for (int i = 2; i < 11; i++) {
			sheet.setColumnView(i, 15);
		}
		sheet.setColumnView(11, 18);
		
		addTitles();
		forecastingCalculation.execute();
		forecasting = forecastingCalculation.getForecasting();
		
		for (ForecastingMedicine fm : forecasting.getMedicines()) 
			fillRow(fm);
		
		Formula f;
		for (int j = 2; j < 12; j++) 
			if (j!=3 && j!=6 && j!=10) 	
			{
				WritableCellFormat tmp;
				if (j>9) tmp = floatBoldFormat;
				else tmp = times12ptBoldFormat;
				setColumn(j);
				workbook.addNameArea("col1_"+j, sheet, j, 5, j, 14);
				f = new Formula(j, 15, "SUM(col1_"+j+")",tmp);
				sheet.addCell(f);
			}
			
		for (int j = 2; j < 12; j++) 
			if (j!=3 && j!=6 && j!=10)	
			{
				WritableCellFormat tmp;
				if (j>9) tmp = floatBoldFormat;
				else tmp = times12ptBoldFormat;
				setColumn(j);
				workbook.addNameArea("col2_"+j, sheet, j, 18, j, 32);
				f = new Formula(j, 33, "SUM(col2_"+j+")",tmp);
				sheet.addCell(f);
				f = new Formula(j, 34, "SUM(col1_"+j+",col2_"+j+")",tmp);
				sheet.addCell(f);
			}
	}
	
	private void fillRow(ForecastingMedicine fm) throws RowsExceededException, WriteException{
		if (fm.getMedicine().getLegacyId()!=null)
		if (!fm.getMedicine().getLegacyId().equals("")){
			int i = Integer.parseInt(fm.getMedicine().getLegacyId());
			if (i<11) setRow(i+4);
				else if (i<14) setRow(i+7);
				else if (i<17) setRow(i+8);
				else if (i<21) setRow(i+9);
				else setRow(i+10);
			setColumn(0);
			addText(i+".",null);
			setColumn(1);
			addText(fm.getMedicine().getGenericName().getName2(),times12ptBoldFormat);
			setColumn(2);
			int soh = fm.getStockOnHand();
			addNumber(soh,null);
			setColumn(3);
			addNumber(fm.getForecasting().getLeadTime(),null);
			setColumn(4);
			int cc = fm.getConsumptionCases();
			addNumber(cc,null);
			setColumn(5);
			int cnc = fm.getConsumptionNewCases();
			addNumber(cnc,null);
			setColumn(6);
			addNumber(fm.getForecasting().getBufferStock(),null);
			setColumn(7);
			addNumber(cc+cnc,null);
			setColumn(8);
			addNumber(fm.getStockOnHandAfterLT(),null);
			setColumn(9);
			addNumber(cc+cnc-soh,null);
			setColumn(10);
			addNumber(fm.getUnitPrice(),floatFormat);
			setColumn(11);
			sheet.addCell(new Formula(11, row, "SUM(J"+(row+1)+"*K"+(row+1)+")",floatBoldFormat));
			setColumn(1);
		}
	}
	
	private void addTitles() throws RowsExceededException, WriteException {		
		for (int i = 0; i < 12; i++) 
			for (int j = 2; j < 35; j++) {
				setRow(j);
				setColumn(i);
				addText("", null);
			}
		
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
		
		setColumn(0);
		setRow(4);
		addTextFromResource("manag.forecast.excel.colspan1", times12ptBoldFormat);
		setRow(16);
		addTextFromResource("manag.forecast.excel.colspan2", times12ptBoldFormat);
		
		setColumn(1);
		setRow(15);
		addTextFromResource("manag.forecast.excel.total1", times12ptBoldFormat);
		setRow(33);
		addTextFromResource("manag.forecast.excel.total", times12ptBoldFormat);
		setRow(34);
		addTextFromResource("manag.forecast.excel.total1", times12ptBoldFormat);
		setRow(17);
		addTextFromResource("manag.forecast.excel.type1", times12ptBoldUnderlineFormat);
		setRow(21);
		addTextFromResource("manag.forecast.excel.type2", times12ptBoldUnderlineFormat);
		setRow(25);
		addTextFromResource("manag.forecast.excel.colspan3", times12ptBoldUnderlineFormat);
		setRow(30);
		addTextFromResource("manag.forecast.excel.colspan4", times12ptBoldUnderlineFormat);
		
		sheet.mergeCells(0,4,11,4);
		sheet.mergeCells(0,16,11,16);
		sheet.mergeCells(1,25,2,25);
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
			sheet.addCell(label);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		}
	}
	
	public void addNumber(float resourceKey, WritableCellFormat cellFormatName) throws RowsExceededException, WriteException {
		if (cellFormatName == null)	
			cellFormatName = times12ptFormat;
		
		Number label = new Number(column, row, resourceKey, cellFormatName);
		try {
			sheet.addCell(label);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		}
	}
	
	

	
}	
