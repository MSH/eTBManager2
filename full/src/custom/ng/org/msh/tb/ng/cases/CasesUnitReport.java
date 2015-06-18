package org.msh.tb.ng.cases;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Create consolidated report of number of cases by different views (suspects, TB, DR-TB, NTM) of a
 * specific region of the country
 *
 * Created by rmemoria on 17/6/15.
 */
@Name("casesUnitReport")
public class CasesUnitReport {

    @In EntityManager entityManager;

    private Integer admunitId;
    private List<CaseUnitItem> items;
    private int level;
    private boolean executing;

    public void execute() {
        executing = true;
    }

    /**
     * Return the items of the report
     * @return
     */
    public List<CaseUnitItem> getItems() {
        if (items == null) {
            createReport();
        }
        return items;
    }


    /**
     * Return the admin unit level
     * @return
     */
    public int getLevel() {
        return level;
    }


    /**
     * Create the report
     */
    protected void createReport() {
        if (!executing) {
            return;
        }

        if (admunitId == 0) {
            admunitId = null;
        }

        UserWorkspace uw = UserSession.getUserWorkspace();

        // if view is by TB facility so does nothing
        if (uw.getView() == UserView.TBUNIT) {
            return;
        }

        AdministrativeUnit au;
        // value to be decremented from the level
        int dl = 0;

        if (uw.getView() == UserView.ADMINUNIT && admunitId == null) {
            admunitId = uw.getAdminUnit().getId();
            dl = uw.getAdminUnit().getLevel();
        }

        if (admunitId != null) {
            au = entityManager.find(AdministrativeUnit.class, admunitId);
            level = au.getLevel() - dl;
        }
        else {
            au = null;
        }

        mountAdminUnits();
        mountHealthFacilities();
    }

    /**
     * Mount the list of administrative units based on the parent unit
     */
    protected void mountAdminUnits() {
        String sql = "select a.id, a.name1, a.unitsCount, k.diagnosisType, k.classification, sum(k.count)\n" +
                "from administrativeunit a\n" +
                "left join (select a2.code, c.diagnosisType, c.classification, count(*) as count\n" +
                "from tbcase c \n" +
                "inner join tbunit b on b.id=c.owner_unit_id\n" +
                "inner join administrativeunit a2 on a2.id = b.adminunit_id\n" +
                "where a2.workspace_id=:wsid and c.state < 3 " +
                "and c.diagnosisType is not null and c.classification is not null\n" +
                "group by a2.code, c.diagnosisType, c.classification) as k\n" +
                "  on k.code like concat(a.code, '%')\n" +
                "where " + (admunitId == null? "a.parent_id is null\n": "a.parent_id = :id\n") +
                "and a.workspace_id=:wsid\n" +
                "group by a.id, a.name1, k.diagnosisType, k.classification\n" +
                "order by a.name1";

        Query qry = entityManager.createNativeQuery(sql)
                .setParameter("wsid", UserSession.getWorkspace().getId());

        if (admunitId != null) {
            qry.setParameter("id", admunitId);
        }

        List<Object[]> lst = qry.getResultList();

        items = new ArrayList<CaseUnitItem>();

        mountList(lst, CaseUnitItem.UnitType.ADMINUNIT);
    }


    /**
     * Mount the list of health facilities based on the administrative unit selected
     */
    protected void mountHealthFacilities() {
        String sql = "select a.id, a.name1, 0, b.diagnosisType, b.classification, count(*)\n" +
                "from tbunit a " +
                "left join tbcase b on b.owner_unit_id=a.id\n" +
                "where " + (admunitId == null? "a.adminunit_id is null" : "a.adminunit_id = " + admunitId) +
                " and b.state < 3\n" +
                "group by a.id, a.name1, b.diagnosisType, b.classification\n" +
                "order by a.name1";

        List<Object[]> lst = entityManager
                .createNativeQuery(sql)
                .getResultList();

        mountList(lst, CaseUnitItem.UnitType.HEALTHFACILITY);
    }

    /**
     * Mount the list of items from information retrieved from the database
     * @param lst list of values
     * @param type the type of list (admin unit or TB unit)
     */
    protected void mountList(List<Object[]> lst, CaseUnitItem.UnitType type) {
        for (Object[] val: lst) {
            Integer id = (Integer)val[0];
            String name = (String)val[1];

            CaseUnitItem item = findItemByUnitId(id, type);

            if (item == null) {
                item = new CaseUnitItem();
                item.setId(id);
                item.setName(name);
                item.setType(type);

                int count = ((Number)val[2]).intValue();
                item.setNode(count > 0);

                items.add(item);
            }

            if (val[3] != null) {
                DiagnosisType dtype = DiagnosisType.values()[(Integer)val[3]];
                CaseClassification cla = CaseClassification.values()[(Integer)val[4]];
                int count = ((Number)val[5]).intValue();

                // force item as a node if there are cases in an admin unit node
                if (count > 0 && type == CaseUnitItem.UnitType.ADMINUNIT) {
                    item.setNode(true);
                }

                if (dtype == DiagnosisType.SUSPECT) {
                    item.setNumSuspects( item.getNumSuspects() + count );
                }
                else {
                    switch (cla) {
                        case TB: item.setNumTB(count);
                            break;
                        case DRTB: item.setNumDRTB(count);
                            break;
                        case NTM: item.setNumDRTB(count);
                            break;
                        default:
                            throw new RuntimeException("Value not supported");
                    }
                }
            }
        }
    }

    /**
     * Find the unit by ID and type
     * @param id the unit ID
     * @param type type of unit (admin unit or facility)
     * @return
     */
    private CaseUnitItem findItemByUnitId(int id, CaseUnitItem.UnitType type) {
        for (CaseUnitItem it: items) {
            if ((it.getId() == id) && (it.getType() == type)) {
                return it;
            }
        }

        return null;
    }

    /**
     * Return the admin unit ID to be used in the report
     * @return
     */
    public Integer getAdmunitId() {
        return admunitId;
    }

    /**
     * Set the admin unit ID to be used in the report
     * @param admunitId the ID
     */
    public void setAdmunitId(Integer admunitId) {
        this.admunitId = admunitId;
    }

}
