package org.msh.tb.ua.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.msh.utils.date.DateUtils;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
/**
 * Class for generating PDF-files. For generating call {@link #create(fileName)} 
 * @author A.M.
 */
public abstract class PDFCreator {
	/**
	 * specials symbol of selected checkbox
	 */
	protected static final String checkBoxEmpty="\u2610";
	/**
	 * specials symbol of empty checkbox
	 */
	protected static final String checkBoxSelected="\u2611";
	
	//fonts
	protected BaseFont bCyrFont;
	private BaseFont font_unic;
	protected Font times6;
	protected Font times7;
	protected Font times8;
	protected Font times8b;
	protected Font times12b;
	protected Font times10bi;
	protected Font times10i;
	protected Font times10b;
	protected Font times10;
	
	protected PdfPTable tab; //temp current table
	private float curLeadTab; //temp current leading of the cells in table
	private float curPaddingBottom; //temp current padding bottom of the cells in table
	protected PdfPCell cell; //temp current cell
	
	/**
	 * current document
	 */
	private Document document;
	
	/**
	 * Create the PDF-file with stated file name
	 * @param fileName
	 */
	public void create(String fileName) {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return;
		HttpServletResponse response = (HttpServletResponse)fc.getExternalContext().getResponse();  
	    response.setContentType("application/pdf");  
	    response.setHeader("Content-disposition",  "attachment; filename="+fileName+".pdf");
		
		document = new Document();
		try 
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, baos);
			
			formatDocument();
			
			document.open();
			document.newPage();
			
			initFonts();
			initData();
			fullDocument();
			
			document.close();
			
    		// setting some response headers
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            // setting the content type
            response.setContentType("application/pdf");
            // the contentlength
            response.setContentLength(baos.size());
            // write ByteArrayOutputStream to the ServletOutputStream
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();
            os.close();
            fc.responseComplete();
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Method, where must be set all necessary formatting for current document: page size, margins, orientation etc. Call before document.open() 
	 */
	protected void formatDocument() {
		document.setPageSize(PageSize.A4.rotate());
		document.setMargins(15F, 15F, 15F, 15F);
	}

	/**
	 * Method, where must be initialized all necessary data for generating document. Call before {@link #fullDocument()}
	 */
	protected void initData(){}
	
	/**
	 * Method, where must be generating content of document. Call before document.close()
	 */
	protected abstract void fullDocument();

	/**
	 * Initialize necessary fonts and base fonts. Format [FontFamily|Size|b=bold|i=italic].
	 * Call before {@link #initData()}
	 */
	protected void initFonts() {
		try {
			font_unic = BaseFont.createFont("c:/windows/fonts/arialuni.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			bCyrFont = BaseFont.createFont("c:/windows/fonts/times.ttf", "Cp1251", BaseFont.EMBEDDED);
			
			times6 = new Font(bCyrFont,6);  
			times7 = new Font(bCyrFont,7);  
			times8 = new Font(bCyrFont,8);  
			times8b = new Font(bCyrFont,8,Font.BOLD);  
			times10 = new Font(bCyrFont,10);  
			times10b = new Font(bCyrFont,10,Font.BOLD);  
			times10bi = new Font(bCyrFont,10,Font.BOLDITALIC);  
			times10i = new Font(bCyrFont,10,Font.ITALIC);  
			times12b = new Font(bCyrFont,12,Font.BOLD);  
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Generate cell with formatting in method {@link #generateTableCell()} and such view:
	 * [preText+checkbox+postText]
	 * @param f - font for preText and postText 
	 * @param preText - text before checkbox
	 * @param condition - value of checkbox selection
	 * @param postText - text after checkbox. Optional parameters shared by space
	 * @return cell
	 */
	protected PdfPCell generateCheckBoxTableCell(Font f, String preText, boolean condition, String... postText) {
		Phrase p = new Phrase(new Chunk(preText+" ",f));
		p.add(getCheckBox(condition));
		for (String ss:postText)
			p.add(new Chunk(" "+ss,f));
		cell = generateTableCell(p);
		return cell;
	}

	/**
	 * Generate cell with formatting in method {@link #generateTableCell()}
	 * @param f - font
	 * @param s - text in cell. Optional parameters shared by space
	 * @return cell
	 */
	protected PdfPCell generateTableCell(Font f,String... s) {
		Phrase p = new Phrase();
		for (String ss:s)
			p.add(new Chunk(ss+" ",f));
		cell = generateTableCell(p);
		return cell;
	}
	
	/**
	 * Generate cell with formatting in method {@link #generateTableCell()}, 
	 * where alternate records from map with stated font for every text-component.
	 * Shared by spaces.
	 * @param map - text-components for cell with stated fonts for every text-component
	 * @return cell
	 */
	protected PdfPCell generateTableCell(LinkedHashMap<String,Font> map) {
		Phrase p = new Phrase();
		Iterator<Entry<String, Font>> it = map.entrySet().iterator();
		while (it.hasNext()){
			Entry<String, Font> e = it.next();
			p.add(new Chunk(e.getKey()+" ",e.getValue()));
		}
		cell = generateTableCell(p);
		return cell;
	}
	
	/**
	 * Generate cell with formatting in method {@link #generateTableCell()} 
	 * with phrase p in content. It is recommended used this method, instead of 
	 * {@link new PdfPCell(p)} or {@link cell.add(p)}. Otherwise will be problem 
	 * with horizontal alignment in cell. 
	 * @param p - cell content
	 * @return cell
	 */
	protected PdfPCell generateTableCell(Phrase p){
		PdfPCell cell = generateTableCell();
		cell.setPhrase(p);
		if (p.getContent().isEmpty())
			cell.setFixedHeight(14);
		return cell;
	}
	
	/**
	 * Generate cell with formatting by default: 
	 * without borders, with leading {@link #curLeadTab} 
	 * and padding bottom {@link #curPaddingBottom}
	 * @return cell
	 */
	protected PdfPCell generateTableCell(){
		PdfPCell cell = new PdfPCell();
		cell.setLeading(getCurLeadTab(), 1);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPaddingBottom(curPaddingBottom);
		//cell.setFixedHeight(14);
		//cell.setPaddingTop(4);
		//cell.setExtraParagraphSpace(0.5F);
		return cell;
	}
	
	/**
	 * Return date in local format. If date is NULL return field for manual enter date
	 * @param d - date
	 * @return
	 */
	protected String getFieldDate(Date d) {
		String s = getFieldForFill(13);
		if (d!=null)
			s = DateUtils.formatAsLocale(d, false);
		return s;
	}

	/**
	 * Add for current table {@link #tab} cell with centered horizontal and vertical alignment 
	 * and stated formatting
	 * @param text - text in cell content
	 * @param font - font of text
	 * @param border - true for all 4 borders, false for no border
	 * @param rowspan - row span of cell
	 * @param colspan - column span of cell
	 * @param rotate - true for vertical text, false for horizontal text
	 */
	protected void addCellMiddleCenter(String text, Font font, boolean border, int rowspan, int colspan, boolean rotate) {
		cell = generateTableCell(new Phrase(text,font));
		if (border)
			cell.setBorder(Rectangle.BOX);
		formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
		cell.setRowspan(rowspan);
		cell.setColspan(colspan);
		if (rotate)
			cell.setRotation(90);
		tab.addCell(cell);
	}
	
	/**
	 * Add for current table {@link #tab} cell with centered horizontal and vertical alignment 
	 * and stated formatting
	 * @param text - text in cell content
	 * @param font - font of text
	 * @param border - true for all 4 borders, false for no border
	 */
	protected void addCellMiddleCenter(String text, Font font, boolean border) {
		//if (text==null) text = "";
		cell = generateTableCell(new Phrase(text,font));
		if (border)
			cell.setBorder(Rectangle.BOX);
		formatCell(Element.ALIGN_MIDDLE, Element.ALIGN_CENTER);
		tab.addCell(cell);
	}

	/**
	 * Add empty cell without border with stated row span
	 * @param rowspan
	 */
	protected void addEmptyCell(int rowspan) {
		PdfPCell empCell = new PdfPCell();
		empCell.setRowspan(rowspan);
		empCell.setBorder(Rectangle.NO_BORDER);
		tab.addCell(empCell);
	}
	
	/**
	 * Return field for manual enter data
	 * @param size - quantity of sign in field (sign - '_')
	 * @return
	 */
	protected String getFieldForFill(int size){
		String s = "";
		for (int i = 0; i < size; i++) {
			s+="_";
		}
		return s;
	}
	
	/**
	 * Return no-selected or selected checkbox according to stated condition
	 * @param condition - value of selection in checkbox
	 * @return
	 */
	protected Chunk getCheckBox(boolean condition) {
		Chunk res;
		if (condition)
			res = new Chunk(checkBoxSelected,new Font(font_unic,10));
		else
			res = new Chunk(checkBoxEmpty,new Font(font_unic,10));
		return res;
	}

	/**
	 * Set for current cell {@link #cell} content alignment inside cell
	 * @param vAling - vertical alignment (values: {@link Element#ALIGN_TOP}(default), {@link Element#ALIGN_MIDDLE}, {@link Element#ALIGN_BOTTOM})
	 * @param hAlign - horizontal alignment (values: {@link Element#ALIGN_LEFT}(default), {@link Element#ALIGN_CENTER}, {@link Element#ALIGN_RIGHT}, {@link Element#ALIGN_JUSTIFIED}, {@link Element#ALIGN_JUSTIFIED_ALL})
	 */
	protected void formatCell(int vAling, int hAlign) {
		cell.setVerticalAlignment(vAling);
		cell.setHorizontalAlignment(hAlign);
		//cell.setUseAscender(true); 
        //cell.setUseDescender(true); 
	}
	
	/**
	 * Set the variable borders for current cell {@link #cell}
	 * @param left - true if left border is visible
	 * @param top - true if top border is visible
	 * @param right - true if right border is visible
	 * @param bottom - true if bottom border is visible
	 */
	protected void borderCell(boolean left,boolean top, boolean right,boolean bottom) {
		if (left && top && right && bottom)
			cell.setBorder(Rectangle.BOX);
		
		cell.setBorder(Rectangle.NO_BORDER);
		
		if (left)
			cell.setBorderWidthLeft(0.5f);
		if (top)
			cell.setBorderWidthTop(0.5f);
		if (right)
			cell.setBorderWidthRight(0.5f);
		if (bottom)
			cell.setBorderWidthBottom(0.5f);

	}
	
	/**
	 * Return current document
	 * @return
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Set default leading for generated cells 
	 * @param curLeadTab
	 */
	public void setCurLeadTab(float curLeadTab) {
		this.curLeadTab = curLeadTab;
	}

	/**
	 * Get current default leading for generated cells 
	 * @param curLeadTab
	 */
	public float getCurLeadTab() {
		return curLeadTab;
	}
	/**
	 * Set default padding bottom for generated cells 
	 * @param curPaddingBottom
	 */
	public void setCurPaddingBottom(float curPaddingBottom) {
		this.curPaddingBottom = curPaddingBottom;
	}

	/**
	 * Event for dotted bottom border. Use by {@link cell.setCellEvent(new DottedLine())}
	 */
	protected class DottedLine implements PdfPCellEvent { 
		public DottedLine() {}
		
		@Override
		public void cellLayout(PdfPCell cell, Rectangle position, 
				PdfContentByte[] canvases) { 
			PdfContentByte cb = canvases[PdfPTable.LINECANVAS]; 
			cb.saveState(); 
			cb.setLineCap(PdfContentByte.LINE_CAP_ROUND); 
			cb.setLineDash(2, 2, 0); 
			cb.moveTo(position.getLeft(), position.getBottom()); 
			cb.lineTo(position.getRight(), position.getBottom()); 
			cb.stroke(); 
			cb.restoreState(); 
		} 
	} 
	
	/**
	 * Event for diagonal cross border in cell (left-top angle to right-bottom angle). Use by {@link cell.setCellEvent(new DottedDiagLine())}
	 */
	protected class DottedDiagLine implements PdfPCellEvent { 
		public DottedDiagLine() {}

		@Override
		public void cellLayout(PdfPCell cell, Rectangle position, 
				PdfContentByte[] canvases) {
			PdfContentByte cb = canvases[PdfPTable.LINECANVAS]; 
			cb.saveState(); 
			cb.setLineCap(PdfContentByte.LINE_CAP_ROUND); 
			cb.setLineDash(8, 3, 0); 
			cb.moveTo(position.getLeft(), position.getTop()); 
			cb.lineTo(position.getRight(), position.getBottom()); 
			cb.stroke(); 
			cb.restoreState(); 
		}
	}
	
	/**
	 * Event for diagonal cross out cell (view 'X'). Use by {@link cell.setCellEvent(new NoDataCell())}
	 */
	protected class NoDataCell implements PdfPCellEvent { 
		public NoDataCell() {}

		@Override
		public void cellLayout(PdfPCell cell, Rectangle position, 
				PdfContentByte[] canvases) {
			PdfContentByte cb = canvases[PdfPTable.LINECANVAS]; 
			cb.saveState(); 
			cb.setLineCap(PdfContentByte.LINE_CAP_ROUND); 
			//cb.setLineDash(8, 3, 0); 
			cb.moveTo(position.getLeft(), position.getTop()); 
			cb.lineTo(position.getRight(), position.getBottom()); 
			cb.moveTo(position.getLeft(), position.getBottom()); 
			cb.lineTo(position.getRight(), position.getTop()); 
			cb.stroke(); 
			cb.restoreState(); 
		}
	}
		

}
