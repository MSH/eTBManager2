package org.msh.tb.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.international.Messages;
import org.msh.tb.entities.TbCase;

public class ExcelCreator {
	
	private String fileName;
	private WritableWorkbook workbook;
	private File excelFile;
	private int row;
	private int column;
	private WritableSheet sheet;
	private Map<String, String> messages;
	private Map<String, WritableCellFormat> cellFormats;
	private WritableCellFormat dateFormat;
	private WritableCellFormat dateTimeFormat;
	private int colIdent;
	private int maxRow;
	private Map<String, Integer> columnMarks = new HashMap<String, Integer>();

	
	/**
	 * Add a new sheet to the Excel workbook
	 * @param sheetName
	 * @param index
	 * @return instance of {@link WritableSheet} class
	 */
	public WritableSheet addSheet(String sheetName, int index) {
		sheet = getWorkbook().createSheet(sheetName, index);
		return sheet;
	}
	
	
	public void setColumnsAutoside(int colIni, int colEnd) {
		for (int i = colIni; i < colEnd; i++) {
			CellView view = new CellView();
			view.setAutosize(true);
			sheet.setColumnView(i, view);
		}
	}


	/**
	 * Return the reference to the current sheet
	 * @return instance of {@link WritableSheet} class
	 */
	public WritableSheet getCurrentSheet() {
		return sheet;
	}


	/**
	 * Execute a line break operation
	 */
	public void lineBreak() {
		column = colIdent;
		row++;
		updateMaxRow();
	}

	
	public Label addText(String text, int aColumn, int aRow, String cellFormatName) {
		if (text == null)
			return null;
		Label label = new Label(aColumn, aRow, text);
		addCell(label, cellFormatName);
		return label;
	}


	/**
	 * Add a text to the current cell and moves cursor to the next cell
	 * @param text
	 */
	public void addText(String text) {
		addText(text, null);
	}

	
	/**
	 * Add a text to the current cell and moves cursor to the next cell
	 * @param text
	 */
	public void addText(String text, String cellFormatName) {
		if (text == null) {
			column++;
			return;
		}
		addText(text, column, row, cellFormatName);
		column++;
	}

		
	/**
	 * Add a text from the message file
	 * @param resourceKey is the key in the resource file
	 */
	public void addTextFromResource(String resourceKey) {
		if (resourceKey == null) {
			column++;
			return;
		}
		addText( getMessages().get(resourceKey) );
	}

	
	/**
	 * Add a text from the resource message file and its formatting
	 * @param resourceKey
	 * @param cellFormatName
	 */
	public void addTextFromResource(String resourceKey, String cellFormatName) {
		if (resourceKey == null) {
			column++;
			return;
		}
		addText( getMessages().get(resourceKey), cellFormatName );
	}
	

	
	/**
	 * Create a new cell format
	 * @param formatName
	 * @return
	 */
	public WritableCellFormat createCellFormat(String formatName) {
		WritableFont font = new WritableFont(WritableFont.TAHOMA, 10);
		WritableCellFormat cellFormat = new WritableCellFormat(font);
		
		if (cellFormats == null)
			cellFormats = new HashMap<String, WritableCellFormat>();
		
		cellFormats.put(formatName, cellFormat);
		
		return cellFormat;
	}
	

	public void addNumber(Double value, int aColumn, int aRow, String cellFormatName) {
		if (value == null)
			return;
		Number number = new Number(aColumn, aRow, value);
		addCell(number, cellFormatName);
	}


	/**
	 * Add a number to the current cell and moves cursor to the next cell
	 * @param value
	 */
	public void addNumber(Double value) {
		addNumber(value, column, row, null);
		column++;
	}


	/**
	 * Add a number to the current cell and moves cursor to the next cell
	 * @param value
	 */
	public void addNumber(Integer value) {
		Double dval = (value != null? value.doubleValue(): null);
		addNumber(dval, column, row, null);
		column++;
	}
	
	/**
	 * Add a number to the current cell and moves cursor to the next cell
	 * @param value
	 */
	public void addNumber(Integer value, String style) {
		Double dval = (value != null? value.doubleValue(): null);
		addNumber(dval, column, row, style);
		column++;
	}
	
	/**
	 * Add a number to the current cell and moves cursor to the next cell
	 * @param value
	 */
	public void addNumber(Long value) {
		Double dval = (value != null? value.doubleValue(): null);
		addNumber(dval, column, row, null);
		column++;
	}
	

	public void addDate(Date date, int aColumn, int aRow) {
		if (date == null)
			return;
		DateTime dateCell = new DateTime(aColumn, aRow, date, dateFormat);
		addCell(dateCell, null);
	}
	
	/**
	 * Add a date to the current cell and moves cursor to the cell at its right position
	 * @param date
	 */
	public void addDate(Date date) {
		addDate(date, column, row);
		column++;
	}


	/**
	 * Add a date to the current cell and moves cursor to the cell at its right position
	 * @param date
	 */
	public void addDateTime(Date date, int aColumn, int aRow) {
		if (date == null) {
			column++;
			return;
		}
		DateTime dateCell = new DateTime(aColumn, aRow, date, dateTimeFormat);
		addCell(dateCell, null);
		column++;
	}


	/**
	 * Add a date to the current cell and moves cursor to the cell at its right position
	 * @param date
	 */
	public void addDateTime(Date date) {
		addDateTime(date, column, row);
		column++;
	}

	
	public void addGroupHeader(String text, int colspan, String cellFormatName) {
		addText(text, column, row - 1, cellFormatName);
	}
	
	public void addGroupHeaderFromResource(String key, int colspan, String cellFormatName) {
		String text = getMessages().get(key);
		addText(text, column, row - 1, cellFormatName);
		try {
			sheet.mergeCells(column, row-1, column -1 + colspan, row - 1);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a cell including formatting information
	 * @param cell
	 * @param cellFormatName
	 */
	protected void addCell(WritableCell cell, String cellFormatName) {
		try {
			WritableCellFormat cellFormat = cellFormats.get(cellFormatName);
			if (cellFormat != null)
				cell.setCellFormat(cellFormat);
			
			sheet.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public void addValue(Object obj, String property) {
		try {
			Object value = PropertyUtils.getProperty(obj, property);
			addValue(value);
		} catch (NestedNullException e) {
			addValue(null);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void addValue(Object[] objs, int position) {
		try {
			Object value = objs[position];
			addValue(value);
		} catch (NestedNullException e) {
			addValue(null);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void addValue(Object[] objs, int position, String property) {
		try {
			Object value = PropertyUtils.getProperty(objs[position], property);
			addValue(value);
		} catch (NestedNullException e) {
			addValue(null);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Add value to the current cell. The value is interpreted according to its right type
	 * @param value
	 */
	public void addValue(Object value) {
		if (value == null) {
			column++;
			return;
		}
		
		if (value instanceof String) {
			addText((String)value);
			return;
		}
		
		if (value instanceof java.lang.Number) {
			addNumber(((java.lang.Number)value).doubleValue());
			return;
		}
		
		if (value instanceof Date) {
			addDate((Date)value);
			return;
		}
		
		if (value instanceof Enum) {
			String key;
			try {
				key = (String)PropertyUtils.getProperty(value, "key");
			} catch (Exception e) {
				key = value.toString();
			}
			addTextFromResource(key);
			return;
		}
		
		addText(value.toString());		
	}
	
	/**
	 * Send response to the client using the FacesContext instance class. The response is the Excel file stored in the excelFile property
	 */
	public void sendResponse() {
		
		try {
			workbook.write();
			workbook.close();
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
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
/*		response.setHeader("Cache-Control","no-store");
		response.setHeader("Pragma","no-cache");
		response.setDateHeader("Expires",0);
*/
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

	

	/**
	 * Create a new Excel workbook
	 */
	public void createWorkbook() {
		try {
			if (excelFile != null)
				excelFile.delete();

			excelFile = File.createTempFile("etbmanager", "xls");

			createTitleFormat();
			createSubTitleFormat();
			createDateFormat();
			
			workbook = Workbook.createWorkbook(excelFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public WritableCellFormat createTitleFormat() throws WriteException {
		WritableCellFormat cf = createCellFormat("title");
		WritableFont font = (WritableFont)cf.getFont();
//		font.setBoldStyle(WritableFont.BOLD);
		font.setColour(Colour.WHITE);
		cf.setBorder(Border.ALL, BorderLineStyle.THIN);
		cf.setBackground(Colour.GREY_40_PERCENT);
		return cf;
	}
	
	public WritableCellFormat createSubTitleFormat() throws WriteException {
		WritableCellFormat cf = createCellFormat("subtitle");
		WritableFont font = (WritableFont)cf.getFont();
//		font.setBoldStyle(WritableFont.BOLD);
		font.setColour(Colour.BLACK);
		cf.setBorder(Border.ALL, BorderLineStyle.THIN);
		cf.setBackground(Colour.GREY_25_PERCENT);
		return cf;
	}


	/**
	 * Create the standard date and date-time pattern to display date and time
	 */
	public void createDateFormat() {
		DateFormat df = new DateFormat(getMessages().get("locale.outputDatePattern"));
		dateFormat = new WritableCellFormat(df);

		DateFormat dtf = new DateFormat(getMessages().get("locale.outputDatePattern") + " HH:mm:ss");
		dateTimeFormat = new WritableCellFormat(dtf);
	}


	/**
	 * Return the current workbook 
	 * @return
	 */
	public WritableWorkbook getWorkbook() {
		if (workbook == null)
			createWorkbook();
		return workbook;
	}


	/**
	 * Add a column mark, saving the currrent column position
	 * @param markName
	 */
	public void addColumnMark(String markName) {
		columnMarks.put(markName, column);
	}


	/**
	 * Add an array of values to the excel file. The first value of the array is the case id
	 * and is not included in the Excel file, 
	 * and just array of same id as the id of the tbcase parameters are included in the file.
	 * @param tbcase
	 * @param lst
	 */
	protected void addArrayValues(TbCase tbcase, List<Object[]> lst, String columnMark) {
		if (!gotoMark(columnMark))
			return;
	
		setColIdent(column);
		
		boolean bFirst = true;
		for (Object values[]: lst) {
			int id = (Integer)values[0];
			if (tbcase.getId().equals(id)) {
				// if it's first line, doesn't break it
				if (!bFirst)
					lineBreak();
				else bFirst = false;

				for (int i = 1; i < values.length; i++) {
					addValue(values[i]);
				}
			}
		}		
	}


	/**
	 * Moves the cursor to the column indicated in the column mark of name markName
	 * @param markName
	 * @return
	 */
	public boolean gotoMark(String markName) {
		Integer pos = columnMarks.get(markName);
		if (pos == null)
			return false;
		
		column = pos;
		return true;
	}


	/**
	 * Update the value of the maximum row based on the row variable
	 */
	protected void updateMaxRow() {
		if (maxRow < row)
			maxRow = row;
	}
	
	/**
	 * Return the current resource message file
	 * @return
	 */
	protected Map<String, String> getMessages() {
		if (messages == null)
			messages = Messages.instance();
		return messages;
	}
	
	
	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


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


	public void setColIdent(int colIdent) {
		this.colIdent = colIdent;
	}


	public int getColIdent() {
		return colIdent;
	}


	/**
	 * @param maxRow the maxRow to set
	 */
	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}


	/**
	 * @return the maxRow
	 */
	public int getMaxRow() {
		return maxRow;
	}

}
