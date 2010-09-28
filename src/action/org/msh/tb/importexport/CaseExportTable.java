package org.msh.tb.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.msh.mdrtb.entities.ExamCulture;
import org.msh.mdrtb.entities.ExamHIV;
import org.msh.mdrtb.entities.ExamSputumSmear;
import org.msh.mdrtb.entities.TbCase;


/**
 * Mantains a table of cases to be exported in a table format
 * @author Ricardo Memoria
 *
 */
public class CaseExportTable {

	private List<CaseRow> rows;
	private List<CaseRowGroup> groups;
	
	/**
	 * Update table, deleting all rows
	 */
	public void refresh() {
		rows = null;
	}


	/**
	 * @param tbcase
	 * @return
	 */
	public CaseRowGroup addTbcase(TbCase tbcase, Object data) {
		CaseRowGroup rowGroup = new CaseRowGroup(this);
		rowGroup.setTbcase(tbcase);
		if (groups == null)
			groups = new ArrayList<CaseRowGroup>();
		groups.add(rowGroup);
		
		CaseRow row = new CaseRow(rowGroup);
		row.setTbcase(tbcase);
		row.setData(data);
		if (rows == null)
			rows = new ArrayList<CaseRow>();
		rows.add(row);
		rowGroup.getRows().add(row);
		
		return rowGroup;
	}

	
	/**
	 * @param caseId
	 * @param examHIV
	 * @return
	 */
	public CaseRow addExamHIV(Integer caseId, ExamHIV examHIV) {
		CaseRowGroup rowGroup = findCaseRowGroup(caseId);
		if (rowGroup == null)
			return null;
		
		return rowGroup.addExamHIV(examHIV);
	}

	
	/**
	 * @param caseId
	 * @param examCulture
	 * @return
	 */
	public CaseRow addExamCulture(Integer caseId, ExamCulture examCulture) {
		CaseRowGroup rowGroup = findCaseRowGroup(caseId);
		if (rowGroup == null)
			return null;
		
		return rowGroup.addExamCulture(examCulture);
	}


	/**
	 * @param caseId
	 * @param examSputumSmear
	 * @return
	 */
	public CaseRow addExamSputumSmear(Integer caseId, ExamSputumSmear examSputumSmear) {
		CaseRowGroup rowGroup = findCaseRowGroup(caseId);
		if (rowGroup == null)
			return null;
		
		return rowGroup.addExamSputumSmear(examSputumSmear);
	}
	
	/**
	 * Search for group of rows of a case
	 * @param caseId
	 * @return
	 */
	public CaseRowGroup findCaseRowGroup(Integer caseId) {
		if (groups == null)
			return null;
		
		for (CaseRowGroup group: groups) {
			if (group.getTbcase().getId().equals(caseId))
				return group;
		}
		return null;
	}

	
	public List<CaseRow> getRows() {
		return rows;
	}
	
	public List<CaseRowGroup> getGroups() {
		return groups;
	}


	/**
	 * Organize rows sorting them by case name and date
	 */
	public void organize() {
		Collections.sort(rows, new Comparator<CaseRow>() {
			public int compare(CaseRow row1, CaseRow row2) {
				return row1.getTbcase().getPatient().getFullName().compareTo(row2.getTbcase().getPatient().getFullName());
			}
		});
		
		Collections.sort(groups, new Comparator<CaseRowGroup>() {
			public int compare(CaseRowGroup grp1, CaseRowGroup grp2) {
				return grp1.getTbcase().getPatient().getFullName().compareTo(grp2.getTbcase().getPatient().getFullName());
			}
		});
	}
}
