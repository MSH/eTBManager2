package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.AdministrativeUnit;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.StockPosition;
import org.msh.mdrtb.entities.Tbunit;



@Name("stockPosReport")
public class StockPosReport {

	private StockPosReportItem root;
	private List<Medicine> medicines;
	private boolean consolidated;
	private boolean executing;

	@In(create=true)
	private ReportSelection reportSelection;
	
	@In(create=true)
	EntityManager entityManager;

	/**
	 * Get the root element, i.e, the total quantity
	 * @return
	 */
	public StockPosReportItem getRoot() {
		if (root == null)
			createReport();
		
		return root;
	}


	/**
	 * Just to sign that report is being executed
	 */
	public void execute() {
		executing = true;
	}


	/**
	 * Create report
	 */
	public void createReport() {
		Source source = reportSelection.getSource();
		AdministrativeUnit region = reportSelection.getAuselection().getSelectedUnit();
		
		String hql = "from StockPosition sp join fetch sp.medicine " +
			"join fetch sp.tbunit ds join fetch ds.adminUnit a1 join fetch a1.parent " +
			"where ds.medManStartDate is not null " +
			"and sp.date = (select max(aux.date) from StockPosition aux " +
			"where " + (source == null? "": "aux.source.id=sp.source.id and ") +
			"aux.tbunit.id = sp.tbunit.id " +
			"and aux.medicine.id = sp.medicine.id) " +
			"and sp.tbunit.workspace.id = #{defaultWorkspace.id} " +
			(source == null? "": "and sp.source.id = " + source.getId());
		
		if (region != null)
			hql = hql.concat(" and sp.tbunit.adminUnit.parent.id = " + region.getId().toString());
		
		hql = hql.concat(" order by sp.tbunit.adminUnit.parent.name, sp.tbunit.name");
		
		List<StockPosition> lst = entityManager.createQuery(hql).getResultList();
		
		mountMedicineList(lst);
		
		root = new StockPosReportItem();
		root.redimArray(medicines.size());
		
		// monta relatorio
		for (StockPosition sp: lst) {
			Tbunit ds = sp.getTbunit();
			AdministrativeUnit adm = ds.getAdminUnit().getParent();
			
			StockPosReportItem regItem = root.findChild(adm);
			if (regItem == null)
				regItem = root.addChild(adm);
			
			StockPosReportItem dsItem = regItem.findChild(ds);
			if (dsItem == null)
				dsItem = regItem.addChild(ds);
			
			int index = medicines.indexOf(sp.getMedicine());
			dsItem.setQuantity(index, sp.getQuantity());
		}
	}


	/**
	 * Monta a lista de produtos
	 * @param lst
	 */
	private void mountMedicineList(List<StockPosition> lst) {
		medicines = new ArrayList<Medicine>();
		
		for (StockPosition sp: lst) {
			Medicine prod = sp.getMedicine();
			if (!medicines.contains(prod)) {
				medicines.add(prod);
			}
		}
	}
	
	public List<Medicine> getMedicines() {
		if (medicines == null)
			createReport();
		return medicines;
	}

	public String getWidths() {
		String s = "2.2";
		
		for (int i = 0; i < getMedicines().size(); i++) {
			s += " 1";
		}
		
		return s;
	}

	public boolean isConsolidated() {
		return consolidated;
	}

	public void setConsolidated(boolean consolidated) {
		this.consolidated = consolidated;
	}


	/**
	 * @return the executing
	 */
	public boolean isExecuting() {
		return executing;
	}

}
