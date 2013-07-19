package org.msh.tb.az.indicators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.az.entities.TbCaseAZ;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.IndicatorVerify;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableCell;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;
import org.msh.tb.indicators.core.IndicatorTable.TableRow;

@Name("dstProfileIndicatorAZ")
@Scope(ScopeType.CONVERSATION)
public class DSTProfileIndicatorAZ extends IndicatorVerify<TbCaseAZ>{
	private static final long serialVersionUID = -8679402070131416164L;

	private IndicatorTable table_any_resist;
	private IndicatorTable table_monoresist;
	private IndicatorTable table_mlu_tb;
	private IndicatorTable table_poly_resist;
	private IndicatorTable table_prevtreat;

	private String newp;
	private String newPer;
	private String oldp;
	private String oldPer;
	private String total;

	private String colkey;
	
	@Override
	protected void createIndicators() {
		setGroupFields(null);
		setConsolidated(false);
		createTable();
		generateTables();
	}

	@Override
	protected void createTable() {
		newp = getMessage("manag.ind.dstprofile.nevertreated");
		newPer = getMessage("manag.ind.dstprofile.nevertreatedpercent");
		oldp = getMessage("manag.ind.dstprofile.prevtreated");
		oldPer = getMessage("manag.ind.dstprofile.prevtreatedpercent");
		total = getMessage("global.total");
		
		table_any_resist = new IndicatorTable();
		initTable(table_any_resist);
		table_any_resist.addRow(getMessage("manag.ind.dstprofile.anyresistto")+" H", null);
		table_any_resist.addRow(getMessage("manag.ind.dstprofile.anyresistto")+" R", null);
		table_any_resist.addRow(getMessage("manag.ind.dstprofile.anyresistto")+" E", null);
		table_any_resist.addRow(getMessage("manag.ind.dstprofile.anyresistto")+" S", null);
		
		table_monoresist = new IndicatorTable();
		initTable(table_monoresist);
		table_monoresist.addRow(getMessage("manag.ind.dstprofile.onlyresistto")+" H", null);
		table_monoresist.addRow(getMessage("manag.ind.dstprofile.onlyresistto")+" R", null);
		table_monoresist.addRow(getMessage("manag.ind.dstprofile.onlyresistto")+" E", null);
		table_monoresist.addRow(getMessage("manag.ind.dstprofile.onlyresistto")+" S", null);
		
		table_mlu_tb = new IndicatorTable();
		initTable(table_mlu_tb);
		table_mlu_tb.addRow("H+R", null);
		table_mlu_tb.addRow("H+R+E", null);
		table_mlu_tb.addRow("H+R+S", null);
		table_mlu_tb.addRow("H+R+E+S", null);
		
		table_poly_resist = new IndicatorTable();
		initTable(table_poly_resist);
		table_poly_resist.addRow("H+E", null);
		table_poly_resist.addRow("H+S", null);
		table_poly_resist.addRow("H+E+S", null);
		table_poly_resist.addRow("R+E", null);
		table_poly_resist.addRow("R+S", null);
		table_poly_resist.addRow("R+E+S", null);
		table_poly_resist.addRow("E+S", null);
		
		table_prevtreat = new IndicatorTable();
		TableColumn totalCell = table_prevtreat.addColumn(total, null);
		totalCell.setRowTotal(false);
		table_prevtreat.addColumn(getMessage("manag.ind.dstprofile.prevtreatedRelpase"), null);
		table_prevtreat.addColumn(getMessage("manag.ind.dstprofile.prevtreatedFail1"), null);
		table_prevtreat.addColumn(getMessage("manag.ind.dstprofile.prevtreatedReTreat"), null);
		table_prevtreat.addColumn(getMessage("manag.ind.dstprofile.failureDefaultOthers"), null);
		table_prevtreat.addRow(getMessage("manag.ind.dstprofile.anyresistto")+" H", null);
		table_prevtreat.addRow("H+R", null);
		table_prevtreat.addRow("H+R+E", null);
		table_prevtreat.addRow("H+R+S", null);
		table_prevtreat.addRow("H+R+E+S", null);
	}
	
	private void initTable(IndicatorTable table) {
		table.addColumn(newp, null);
		TableColumn newPercent = table.addColumn(newPer, null);
		newPercent.setHighlight(true);
		table.addColumn(oldp, null);
		TableColumn oldPercent = table.addColumn(oldPer, null);
		oldPercent.setHighlight(true);
		TableColumn totalCell = table.addColumn(total, null);
		totalCell.setRowTotal(false);
	}

	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		hql += " and c.patientType <> 12";
		/*hql += " and (select res.result " +
				"from ExamDSTResult res " +
				"where res.exam.id=(select dst.id from ExamDST dst where dst.tbcase.id=c.id) " +
				"and res.substance.abbrevName.name1 in ('H','R','E','S'))="+DstResult.RESISTANT.ordinal();*/
		return hql;
	}

	@Override
	protected void addToAllowing(TbCase tc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void generateTables() {
		setCounting(true);
		int count = ((Long)createQuery().getSingleResult()).intValue();
		if (count<=4000){
			initVerifList("manag.ind.dstprofile",6,4,1);
			setCounting(false);
			setOverflow(false);
			List<TbCase> lst = createQuery().getResultList();
			Iterator<TbCase> it = lst.iterator();
			int rowid;
			int colid;
			while(it.hasNext()){
				TbCase tc = it.next();
				
				//set key of column 
				switch (tc.getPatientType()) {
				case NEW: 
					colkey = newp;
					break;
				default: 
					colkey = oldp;
					break;
				}
				
				//get results of dst-tests for H,R,E,S
				int resH=0;
				int resR=0;
				int resE=0;
				int resS=0;
				
				ExamDST ex = rightDSTTestContains(tc,"H");
				if (ex!=null)
					for (ExamDSTResult exr: ex.getResults())
						if (exr.getSubstance().getAbbrevName().getName1().equals("H")) resH = exr.getResult().ordinal();	
				
				ex = rightDSTTestContains(tc,"R");
				if (ex!=null)
					for (ExamDSTResult exr: ex.getResults())
						if (exr.getSubstance().getAbbrevName().getName1().equals("R")) resR = exr.getResult().ordinal();	
				
				ex = rightDSTTestContains(tc,"E");
				if (ex!=null)
					for (ExamDSTResult exr: ex.getResults())
					if (exr.getSubstance().getAbbrevName().getName1().equals("E")) resE = exr.getResult().ordinal();	
				
				ex = rightDSTTestContains(tc,"S");
				if (ex!=null)
					for (ExamDSTResult exr: ex.getResults())
					if (exr.getSubstance().getAbbrevName().getName1().equals("S")) resS = exr.getResult().ordinal();	
					
				//add to 1st table - any resistanse
				String anyres = getMessage("manag.ind.dstprofile.anyresistto");
				String monores = getMessage("manag.ind.dstprofile.onlyresistto");
				if (resH==1){
					addToTable(table_any_resist, anyres+" H");
					addToTablePrevTreat(tc,anyres+" H");
					//check to monoresist (2nd table)
					if (resR!=1 && resE!=1 && resS!=1)
						addToTable(table_monoresist, monores+" H");
				}
				if (resR==1){
					addToTable(table_any_resist, anyres+" R");
					if (resH!=1 && resE!=1 && resS!=1)
						addToTable(table_monoresist, monores+" R");
				}
				if (resE==1){
					addToTable(table_any_resist, anyres+" E");
					if (resH!=1 && resR!=1 && resS!=1)
						addToTable(table_monoresist, monores+" E");
				}
				if (resS==1){
					addToTable(table_any_resist, anyres+" S");
					if (resH!=1 && resR!=1 && resE!=1)
						addToTable(table_monoresist, monores+" S");
				}
				//split to 3rd and 4th tables (DR-TB/simple TB)
				if (CaseClassification.DRTB.equals(tc.getClassification())){ //to 3rd table
					if (resH==1 && resR==1 && resE==1 && resS==1){
						addToTable(table_mlu_tb, "H+R+E+S");
						addToTablePrevTreat(tc,"H+R+E+S");
					}
					
					if (resH==1 && resR==1 && resE!=1 && resS==1){
						addToTable(table_mlu_tb, "H+R+S");
						addToTablePrevTreat(tc,"H+R+S");
					}
					
					if (resH==1 && resR==1 && resE==1 && resS!=1){
						addToTable(table_mlu_tb, "H+R+E");
						addToTablePrevTreat(tc,"H+R+E");
					}
					
					if (resH==1 && resR==1 && resE!=1 && resS!=1){
						addToTable(table_mlu_tb, "H+R");
						addToTablePrevTreat(tc,"H+R");
					}
					
				}
				else{//to 4th table
					if (resH==1 && resR!=1 && resE==1 && resS!=1)
						addToTable(table_poly_resist, "H+E");
					if (resH==1 && resR!=1 && resE!=1 && resS==1)
						addToTable(table_poly_resist, "H+S");
					if (resH==1 && resR!=1 && resE==1 && resS==1)
						addToTable(table_poly_resist, "H+E+S");
					if (resH!=1 && resR==1 && resE==1 && resS!=1)
						addToTable(table_poly_resist, "R+E");
					if (resH!=1 && resR==1 && resE!=1 && resS==1)
						addToTable(table_poly_resist, "R+S");
					if (resH!=1 && resR==1 && resE==1 && resS==1)
						addToTable(table_poly_resist, "R+E+S");
					if (resH!=1 && resR!=1 && resE==1 && resS==1)
						addToTable(table_poly_resist, "E+S");
					
				}
			}
			countPercentage(table_any_resist);
			countPercentage(table_monoresist);
			countPercentage(table_mlu_tb);
			countPercentage(table_poly_resist);
		}
	}

	private void countPercentage(IndicatorTable table) {
		for (TableRow tr:table.getRows()){
			Float val = 0F;
			TableCell tc = tr.findCell(table.findColumnByTitle(newp));
			if (tc!=null)
				val = tc.getValue();
			else
				table.addValue(newp, tr.getTitle(), 0F);
			Float tot = 0F;
			tc = tr.findCell(table.findColumnByTitle(total));
			if (tc!=null)
				tot = tc.getValue();
			else
				table.addValue(total, tr.getTitle(), 0F);
			float res = 0;
			if (tot!=0)
				res = val*100/tot;
			table.addValue(newPer, tr.getTitle(), res);
			if (val==0 && tot==0)
				res = 100;
			table.addValue(oldPer, tr.getTitle(), 100-res);
			if (res==100)
				table.addValue(oldp, tr.getTitle(), 0F);
		}
	}

	private void addToTablePrevTreat(TbCase tc, String rowTitle) {
		if (PatientType.NEW.equals(tc.getPatientType()) || PatientType.TRANSFER_IN.equals(tc.getPatientType())) 
			return;
		
		String col;
		switch (tc.getPatientType()) {
		case RELAPSE:
			col = getMessage("manag.ind.dstprofile.prevtreatedRelpase");
			break;
		case FAILURE_FT:
			col = getMessage("manag.ind.dstprofile.prevtreatedFail1");
			break;
		case FAILURE_RT:
			col = getMessage("manag.ind.dstprofile.prevtreatedReTreat");
			break;
		default:
			col = getMessage("manag.ind.dstprofile.failureDefaultOthers");
			break;
		}
		
		table_prevtreat.addValue(total, rowTitle, 1F);
		table_prevtreat.addValue(col, rowTitle, 1F);
	}

	private void addToTable(IndicatorTable table,String rowTitle) {
		table.addValue(total, rowTitle, 1F);
		table.addValue(colkey, rowTitle, 1F);
	}
	
	private ExamDST rightDSTTestContains(TbCase tc, String sub) {
		List<ExamDST> lst = new ArrayList<ExamDST>();
		for (ExamDST dst:tc.getExamsDST()){
			for (ExamDSTResult res: dst.getResults()){
				if (sub.equals(res.getSubstance().getAbbrevName().getName1())){
					lst.add(dst);
				}
			}
		}
		switch (lst.size()) {
		case 0: 
			return null;
		case 1: 
			return lst.get(0);
		default:
			return WorstDSTRes(lst);
		}
	}

	public IndicatorTable getTable_any_resist() {
		if (table_any_resist==null)
			createTable();
		return table_any_resist;
	}

	public IndicatorTable getTable_monoresist() {
		if (table_monoresist==null)
			createTable();
		return table_monoresist;
	}

	public IndicatorTable getTable_mlu_tb() {
		if (table_mlu_tb==null)
			createTable();
		return table_mlu_tb;
	}

	public IndicatorTable getTable_poly_resist() {
		if (table_poly_resist==null)
			createTable();
		return table_poly_resist;
	}

	public IndicatorTable getTable_prevtreat() {
		if (table_prevtreat==null)
			createTable();
		return table_prevtreat;
	}
	
	/**
	 * Clear all tables and verifyList
	 * */
	public void clear(){
		table_any_resist = null;
		table_monoresist = null;
		table_mlu_tb = null;
		table_poly_resist = null;
		table_prevtreat = null;
		setVerifyList(null);
	}
	
}
