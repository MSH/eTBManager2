package org.msh.tb.export.newone;


public class SuperTitle {
	private String titleName;
	private int cellQuantity;
	
	public SuperTitle(String titleName){
		this.titleName = titleName;
	}
	
	/**
	 * @return the titleName
	 */
	public String getTitleName() {
		return titleName;
	}
	/**
	 * @param titleName the titleName to set
	 */
	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}
	/**
	 * @return the cellQuantity
	 */
	public int getCellQuantity() {
		return cellQuantity;
	}
	/**
	 * @param cellQuantity the cellQuantity to set
	 */
	public void setCellQuantity(int cellQuantity) {
		this.cellQuantity = cellQuantity;
	}
}
