/**
 * 
 */
package org.msh.tb.reports2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.entities.Report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Convert report data from and to JSON data
 * 
 * @author Ricardo Memoria
 *
 */
public class ReportJson {

	/**
	 * Convert a report data from a client representation to its server entity data
	 * @param report
	 * @param res
	 * @return
	 */
	public static Report convertFromClient(CReport report, Report res) {
		// convert data to json
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("colVariables", report.getColumnVariables());
		data.put("rowVariables", report.getRowVariables());
		data.put("filters", report.getFilters());
		
		if (report.getChartType() != null) {
			data.put("chart", report.getChartType());
		}

		if (report.getTblSelectedCell() != null) {
			data.put("tblSelectedCell", report.getTblSelectedCell());
		}
		
		if (report.getTblSelection() != null) {
			data.put("tblSelection", report.getTblSelection());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsondata = mapper.writer().writeValueAsString(data);
			System.out.println(jsondata);
			res.setData(jsondata);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		res.setDashboard(report.isDashboard());
		res.setId(report.getId());
		res.setPublished(report.isPublished());
		res.setTitle(report.getTitle());

		return res;
	}
	
	
	/**
	 * Convert a report representation in the server to the client
	 * @param report
	 * @return
	 */
	public static CReport convertToClient(Report report) {
		CReport rep = new CReport();
		
		rep.setId(report.getId());
		rep.setTitle(report.getTitle());
		rep.setDashboard(report.isDashboard());
		rep.setPublished(report.isPublished());

		// read json data
		if ((report.getData() != null) && (!report.getData().isEmpty())) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				JsonNode root = mapper.readTree(report.getData());

				// set the chart type
				if (root.has("chart")) {
					rep.setChartType( root.get("chart").asInt() );
				}
				
				if (root.has("tblSelectedCell")) {
					rep.setTblSelectedCell( root.get("tblSelectedCell").asInt() );
				}
				
				if (root.has("tblSelection")) {
					rep.setTblSelectedCell( root.get("tblSelection").asInt() );
				}
				
				ArrayList<String> colVars = new ArrayList<String>();
				for (JsonNode node: root.get("colVariables")) {
					colVars.add(node.textValue());
				}
				
				ArrayList<String> rowVars = new ArrayList<String>();
				for (JsonNode node: root.get("rowVariables")) {
					rowVars.add(node.textValue());
				}
				
				Iterator<Entry<String, JsonNode>> vals = root.findValue("filters").fields();
				HashMap<String, String> filters = new HashMap<String, String>();
				while (vals.hasNext()) {
					Entry<String, JsonNode> val = vals.next();
					filters.put(val.getKey(), val.getValue().textValue());
				}

				if (colVars.size() > 0) {
					rep.setColumnVariables(colVars);
				}
				
				if (rowVars.size() > 0) {
					rep.setRowVariables(rowVars);
				}
				
				if (filters.size() > 0) {
					rep.setFilters(filters);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return rep;
	}
}
