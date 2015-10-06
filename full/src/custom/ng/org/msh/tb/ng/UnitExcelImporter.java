package org.msh.tb.ng;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 25/5/15.
 */
@Name("unitExcelImporter")
public class UnitExcelImporter {
    // Data to be imported. Up to now, just csv files are supported
    private InputStream file;

    private boolean executed;

    private int total;
    private int totalSaved;

    private Integer hsid;

    private List<String> auNotFound = new ArrayList<String>();

    public void execute() throws IOException, BiffException {
        executed = false;
        if (file == null) {
            return;
        }

        EntityManager em = App.getEntityManager();

        // get the first health system available
        String hql = "select max(id) from HealthSystem where workspace.id = #{defaultWorkspace.id}";
        Object val = em.createQuery(hql).getSingleResult();
        if (val instanceof Number) {
            hsid = ((Number)val).intValue();
        }

        Workbook wb = Workbook.getWorkbook(file);

        total = 0;
        totalSaved = 0;
        Workspace ws = UserSession.getWorkspace();

        String[] sheets = wb.getSheetNames();

        auNotFound.clear();

        for (String sname: sheets) {
            Sheet sheet = wb.getSheet(sname);
            int rows = sheet.getRows();
            String prevau = null;
            Integer previd = null;
            for (int i = 1; i < rows; i++) {
                String admunit = sheet.getCell(1, i).getContents();

                if (admunit.indexOf(" ") == 2) {
                    admunit = admunit.substring(3);
                }

                if (admunit != null) {
                    admunit = admunit.trim();
                }

                if (admunit != null && admunit.isEmpty()) {
                    admunit = null;
                }

                if (admunit == null) {
                    admunit = prevau;
                }

                String name = sheet.getCell(2, i).getContents();
                if (name != null && name.trim().isEmpty()) {
                    name = null;
                }

                // admin unit was defined ?
                if (admunit != null) {
                    Integer auid = null;

                    // adm unit is the same as previous one ?
                    if (admunit.equals(prevau)) {
                        auid = previd;
                    }
                    else {
                        List<Integer> lst = em.createNativeQuery("select id from administrativeunit where upper(name1) = :name" +
                                " and workspace_id = :ws")
                                .setParameter("name", admunit.trim())
                                .setParameter("ws", ws.getId())
                                .getResultList();

                        // admin unit was found?
                        if (lst.size() == 0) {
                            auNotFound.add(sname + ", " + admunit);
                            auid = null;
                        }
                        else {
                            auid = lst.get(0);
                        }
                    }

                    if (auid != null && name != null) {
                        insertUnit(auid, name);
                        totalSaved++;
                    }

                    total++;
                    prevau = admunit;
                    previd = auid;
//                    System.out.println("adminunit = " + admunit + ", unit = " + name);
                }
            }
        }

        System.out.println("Total = " + total);
    }

    /**
     * Save the unit in the system
     * @param auid
     * @param name
     */
    public void insertUnit(Integer auid, String name) {
        Workspace ws = UserSession.getWorkspace();

        EntityManager em = App.getEntityManager();
        Number val = (Number)em.createQuery("select max(id) from Tbunit where upper(name.name1) = :name" +
                " and workspace.id = :wsid")
                .setParameter("name", name)
                .setParameter("wsid", ws.getId() )
                .getSingleResult();

        if (val != null) {
            return;
        }

        String sql = "insert into tbunit (batchControl, changeEstimatedQuantity, dispensingFrequency, \n" +
                "mdrHealthUnit, medicineStorage, medicineSupplier, name1, numDAysOrder, orderOverMinimum, \n" +
                "receivingFromSource, tbhealthunit, treatmenthealthunit, workspace_id, adminunit_id, \n" +
                "authorizerunit_id, firstline_supplier_id, secondline_supplier_id, notifhealthunit, \n" +
                "active, patientDispensing, ntmHealthUnit, legacyid, healthsystem_id) values \n" +
                "(:batchControl, :changeestqtd, :dispfreq, :mdrhealthunit, :medstorage, :medsupplier, \n" +
                ":name, :numdays, :orderovermin, :recfromsource, :tbhealthunit, :treathu, :ws_id, \n" +
                ":auid, :authunit_id, :firstsup, :secsup, :nothu, :active, :patdisp, :ntmhu, :legid, :hsid)";

        em.createNativeQuery(sql)
                .setParameter("batchControl", true)
                .setParameter("changeestqtd", true)
                .setParameter("dispfreq", 1)
                .setParameter("mdrhealthunit", false)
                .setParameter("medstorage", true)
                .setParameter("medsupplier", true)
                .setParameter("name", name)
                .setParameter("numdays", 120)
                .setParameter("orderovermin", true)
                .setParameter("recfromsource", false)
                .setParameter("tbhealthunit", true)
                .setParameter("treathu", true)
                .setParameter("ws_id", ws.getId())
                .setParameter("auid", auid)
                .setParameter("authunit_id", null)
                .setParameter("firstsup", null)
                .setParameter("secsup", null)
                .setParameter("nothu", true)
                .setParameter("active", true)
                .setParameter("patdisp", true)
                .setParameter("ntmhu", false)
                .setParameter("legid", "IMP")
                .setParameter("hsid", hsid)
                .executeUpdate();
    }

    public boolean isExecuted() {
        return executed;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    public List<String> getAuNotFound() {
        return auNotFound;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalSaved() {
        return totalSaved;
    }
}
