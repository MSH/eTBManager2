package org.msh.tb.reports;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.MedicineLine;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;


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

    /**
     * Reference date to calculate report inventory
     */
    private Date date;

	private boolean consolidated;
	private boolean executing;

	@In(create=true)
	private ReportSelection reportSelection;
	
	@In(create=true)
	EntityManager entityManager;

	@In(create=true) 
	StockPosResportExcel stockPosResportExcel;
	
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
		MedicineLine medLine = reportSelection.getMedicineLine();

		if ((adminUnits == null) && (region == null))
			return;

        String hql = "from Movement m join fetch m.medicine " +
                "join fetch m.tbunit u join fetch u.adminUnit a " +
                "where u.workspace.id = #{defaultWorkspace.id} " +
                "and m.date = (select max(m2.date) from Movement m2 " +
                "where m2.tbunit.id=m.tbunit.id and m2.source.id=m.source.id " +
                "and m2.medicine.id=m.medicine.id " +
                (date != null? " and m2.date <= :dt": "") + ") ";

        if (region != null) {
            hql += " and a.code like :aucode";
        }

        if (medLine != null) {
            hql += " and m.medicine.line = " + medLine.ordinal();
        }

        hql += "  order by a.name.name1, u.name.name1";

/*
		String hql = "from StockPosition sp join fetch sp.medicine " +
			"join fetch sp.tbunit ds join fetch ds.adminUnit a1 " +
			"where ds.medManStartDate is not null " +
			"and sp.tbunit.workspace.id = #{defaultWorkspace.id} " +
			(source == null? "": "and sp.source.id = " + source.getId());
		
		if (region != null)
			hql = hql.concat(" and sp.tbunit.adminUnit.code like '" + region.getCode() + "%'");
		
		if(medLine != null)
			hql = hql.concat(" and sp.medicine.line = " + medLine.ordinal());
		
		hql = hql.concat(" order by sp.tbunit.name");
*/

        Query qry = entityManager.createQuery(hql);
        if (region != null) {
            qry.setParameter("aucode", region.getCode() + "%");
        }

        if (date != null) {
            qry.setParameter("dt", date);
        }

		List<Movement> lst = qry.getResultList();

        // create the columns of the report
		mountMedicineList(lst);

        removeDuplicatedRecords(lst);

		root = new StockPosReportItem();
		root.redimArray(medicines.size());
		
		// generate report content
		for (Movement mov: lst) {
			Tbunit ds = mov.getTbunit();
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
			if ((admRoot != null) && (mov.getAvailableQuantity() > 0)) {
				StockPosReportItem regItem = root.findChild(admRoot);
				if (regItem == null)
					regItem = root.addChild(admRoot);
				
				StockPosReportItem dsItem = regItem.findChild(ds);
				if (dsItem == null)
					dsItem = regItem.addChild(ds);
				
				int index = medicines.indexOf(mov.getMedicine());
				dsItem.setQuantity(index, mov.getAvailableQuantity());
			}
		}

        Comparator comparator = new Comparator<StockPosReportItem>() {
            @Override
            public int compare(StockPosReportItem o1, StockPosReportItem o2) {
                return o1.getItem().toString().compareTo(o2.getItem().toString());
            }
        };

        // sort results
        Collections.sort(root.getChildren(), comparator);

        for (StockPosReportItem item: root.getChildren()) {
            Collections.sort(item.getChildren(), comparator);
        }
	}

    /**
     * Remove all movements that are duplicated, i.e, the same unit, source and medicine in the same date
     * The record with older record date will remain
     * @param lst list of {@link org.msh.tb.entities.Movement} objects returned by the query
     */
    private void removeDuplicatedRecords(List<Movement> lst) {
        int index = 0;
        Movement prev = null;

        while (index < lst.size()) {
            Movement mov = lst.get(index);
            // check if previous movement is the same as before
            if ((prev != null)
                && (prev.getSource().getId().equals(mov.getSource().getId()))
                && (prev.getTbunit().getId().equals(mov.getTbunit().getId()))
                && (prev.getMedicine().getId().equals(mov.getMedicine().getId())))
            {
                // check who is the older one
                if (prev.getRecordDate().after(mov.getRecordDate())) {
                    lst.remove(prev);
                    prev = mov;
                }
                else {
                    lst.remove(mov);
                }
            }
            else {
                prev = mov;
                index++;
            }
        }
    }


    /**
	 * Generate the list of medicines that will be used to create the report columns
	 * @param lst list of {@link org.msh.tb.entities.Movement} objects returned by the query
	 */
	private void mountMedicineList(List<Movement> lst) {
		medicines = new ArrayList<Medicine>();
		
		for (Movement mov: lst) {
			Medicine prod = mov.getMedicine();
			if (!medicines.contains(prod)) {
				medicines.add(prod);
			}
		}
	}
	
	/**
	 * Generates Excel and sends it
	 * @return
	 */
	public void downloadExcel(){
		StockPosReportItem root = getRoot();
		if(this.getMedicines() != null && this.getMedicines().size() > 0){
			stockPosResportExcel.downloadExcel(medicines, reportSelection, root);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
