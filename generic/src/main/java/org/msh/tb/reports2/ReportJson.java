/**
 * 
 */
package org.msh.tb.reports2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msh.tb.client.shared.model.CIndicator;
import org.msh.tb.client.shared.model.CReport;
import org.msh.tb.entities.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
		ObjectMapper mapper = new ObjectMapper();
        mapper.addMixInAnnotations(CIndicator.class, MixInIndicator.class);
		try {
            String jsondata = mapper.writer().writeValueAsString(report);
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
		
/*
		rep.setId(report.getId());
		rep.setTitle(report.getTitle());
		rep.setDashboard(report.isDashboard());
		rep.setPublished(report.isPublished());
*/

		// read json data
		if ((report.getData() != null) && (!report.getData().isEmpty())) {
			ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
			try {
                CReport rep2 = mapper.readValue(report.getData(), CReport.class);
                return rep2;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return rep;
	}

    abstract class MixInIndicator {
        @JsonIgnore
        abstract int getColVariablesCount();

        @JsonIgnore
        abstract int getRowVariablesCount();
    }

}
