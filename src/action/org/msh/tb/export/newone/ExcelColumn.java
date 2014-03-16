package org.msh.tb.export.newone;

public class ExcelColumn {
	private String excelColName;
	private String dbColName;
	private int excelColOrder;
	private int parentId;
	/**
	 * @return the excelColName
	 */
	public String getExcelColName() {
		return excelColName;
	}
	/**
	 * @param excelColName the excelColName to set
	 */
	public void setExcelColName(String excelColName) {
		this.excelColName = excelColName;
	}
	/**
	 * @return the dbColName
	 */
	public String getDbColName() {
		return dbColName;
	}
	/**
	 * @param dbColName the dbColName to set
	 */
	public void setDbColName(String dbColName) {
		this.dbColName = dbColName;
	}
	/**
	 * @return the excelColOrder
	 */
	public int getExcelColOrder() {
		return excelColOrder;
	}
	/**
	 * @param excelColOrder the excelColOrder to set
	 */
	public void setExcelColOrder(int excelColOrder) {
		this.excelColOrder = excelColOrder;
	}
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
}
