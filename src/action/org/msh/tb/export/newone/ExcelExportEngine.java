package org.msh.tb.export.newone;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.tb.export.ExcelCreator;

public abstract class ExcelExportEngine {
	private ExcelCreator excel;
	private final static int MAX_ROWS_PER_SHEET = 65530; // max: 65536 in excel 2003 or later

	private ArrayList<ExcelColumn> rows;
		
	private String createSelectStateHQL() throws Exception{
		if(rows == null || rows.size() == 0){
			throw new Exception("You must add at least one Excel column.");
		}
		
		String selectState = "select ";
		
		for(ExcelColumn e : rows)
			selectState += e.getDbColName() + ", ";
		
		selectState = selectState.substring(0, selectState.length() - 2);
		
		return selectState;
	}
	
	protected abstract String getFromStateHQL();
	
	protected String getWhereStateHQL(){
		return "";
	}
	
	protected String getJoinStateHQL(){
		return "";
	}
	
	protected String getOrderByStateHQL(){
		return "";
	}
	
	protected String getGroupByStateHQL(){
		return "";
	}
	
	protected String getHavingStateHQL(){
		return "";
	}
	
	private String getQuery() throws Exception{
		String query = createSelectStateHQL();
		query = query + " " + getFromStateHQL();
		query = query + " " + getJoinStateHQL();
		query = query + " " + getWhereStateHQL();
		query = query + " " + getOrderByStateHQL();
		query = query + " " + getGroupByStateHQL();
		query = query + " " + getHavingStateHQL();
		
		return query;
	}
	
	public void download() throws Exception{
		List<Object> content = getContent();
		excel.sendResponse();
	}
	
	private List<Object> getContent() throws Exception{
		EntityManager em = (EntityManager)Component.getInstance("entityManager", true);
		return em.createQuery(getQuery()).getResultList();
	}
	
	public abstract void addExcelColumns();
	
	public void addExcelColumn(){
		//continue here
	}
	
	public void addExcelSuperTitles(){
		
	}
	
	public void addExcelSuperTitle(String stText, int stCellsMerge){
		//continue here
	}
}
