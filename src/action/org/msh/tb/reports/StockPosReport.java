package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;



/**
 * Generates data to be displayed in the "stock position by region" report.
 * This report displays a table with medicines in columns and regions/health units in rows 
 * @author Ricardo Memoria
 *
 */
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
		List<AdministrativeUnit> adminUnits = reportSelection.getAuselection().getOptionsLevel1();

		if ((adminUnits == null) && (region == null))
			return;
		
		String hql = "from StockPosition sp join fetch sp.medicine " +
			"join fetch sp.tbunit ds join fetch ds.adminUnit a1 " +
			"where ds.medManStartDate is not null " +
			"and sp.tbunit.workspace.id = #{defaultWorkspace.id} " +
			(source == null? "": "and sp.source.id = " + source.getId());
		
		if (region != null)
			hql = hql.concat(" and sp.tbunit.adminUnit.code like '" + region.getCode() + "%'");
		
		hql = hql.concat(" order by sp.tbunit.name");
		
		List<StockPosition> lst = entityManager.createQuery(hql).getResultList();
		
		mountMedicineList(lst);
		
		root = new StockPosReportItem();
		root.redimArray(medicines.size());
		
		// monta relatorio
		for (StockPosition sp: lst) {
			Tbunit ds = sp.getTbunit();
			AdministrativeUnit adm = ds.getAdminUnit();
			
			// find 1st level administrative unit
			AdministrativeUnit admRoot = region;
			if (admRoot == null) {
				for (AdministrativeUnit aux: adminUnits) {
					if (aux.isSameOrChildCode(adm.getCode())) {
						admRoot = aux;
						break;
					}
				}
			}

			// administrative unit was found ?
			if ((admRoot != null) && (sp.getQuantity() > 0)) {
				StockPosReportItem regItem = root.findChild(admRoot);
				if (regItem == null)
					regItem = root.addChild(admRoot);
				
				StockPosReportItem dsItem = regItem.findChild(ds);
				if (dsItem == null)
					dsItem = regItem.addChild(ds);
				
				int index = medicines.indexOf(sp.getMedicine());
				dsItem.setQuantity(index, sp.getQuantity());
			}
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
