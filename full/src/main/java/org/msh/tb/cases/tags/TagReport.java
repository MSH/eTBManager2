package org.msh.tb.cases.tags;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.cases.CasesViewController;
import org.msh.tb.entities.Tag;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.login.UserSession;
import org.msh.tb.na.TBUnitCaseNumber;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate a report of tags by cases. This report is primarily used (but not limited)
 * in the home page of the case module, to display a summary of number of cases by tag
 * Created by rmemoria on 17/6/15.
 */
@Name("tagReport")
public class TagReport {
    private TagCategory summary;
    private TagCategory tags;



    /**
     * Create the report
     */
    private void createReport() {
        String sql = createSQL();

        Workspace ws = UserSession.getWorkspace();

        List<Object[]> lst = App.getEntityManager().createNativeQuery(sql)
                .setParameter("id", ws.getId())
                .getResultList();

        summary = new TagCategory();
        tags = new TagCategory();

        for (Object[] vals: lst) {
            Integer tagid = (Integer)vals[0];
            String tagname = (String)vals[1];

            Tag.TagType type = null;
            if ((Integer)vals[2] == 1)
                type = Tag.TagType.MANUAL;
            else {
                if ((Boolean)vals[3] == Boolean.TRUE)
                    type = Tag.TagType.AUTOGEN_CONSISTENCY;
                else type = Tag.TagType.AUTOGEN;
            }

            Integer order = (Integer)vals[4];
            boolean tagsummary = Boolean.TRUE.equals(vals[5]);
            Number count = (Number)vals[6];

            TagItem it = new TagItem();
            it.setName(tagname);
            it.setTagId(tagid);
            it.setTotal(count.longValue());
            it.setType(type);

            if (tagsummary) {
                summary.getTags().add(it);
            }
            else {
                tags.getTags().add(it);
            }
        }
    }


    /**
     * Create the SQL to query the tag report
     * @return SQL statement
     */
    private String createSQL() {
        String sql = "select t.id, t.tag_name, t.sqlCondition is null, t.consistencyCheck, t.displayOrder, t.summary, count(*) " +
                "from tags_case tc " +
                "inner join tag t on t.id = tc.tag_id ";

        UserWorkspace uw = UserSession.getUserWorkspace();

        Tbunit unit = uw.getTbunit();
        if (unit == null) {
            CasesViewController view = (CasesViewController)App.getComponent("casesViewController");
            unit = view.getSelectedUnit();
        }

        // generate the joins
        if ((unit != null) || (uw.getView() != UserView.COUNTRY) || (uw.getHealthSystem() != null)) {
            sql += " inner join tbcase c on c.id=tc.case_id inner join tbunit u on u.id = c.owner_unit_id";

            if (uw.getView() == UserView.ADMINUNIT) {
                sql += " inner join administrativeunit a on a.id = u.adminunit_id ";
            }

            sql += "\nwhere\n" +
                    "t.workspace_id = :id and t.active = true\n";

            if (uw.getView() == UserView.ADMINUNIT) {
                if (uw.getHealthSystem() != null) {
                    sql += " and u.healthSystem_id = " + uw.getHealthSystem().getId();
                }
                sql += " and (a.code like '" + uw.getAdminUnit().getCode() + "%')";
            }
            else {
                sql += " and u.id = " + uw.getTbunit().getId();
            }
        }

        sql += "\ngroup by t.id, t.tag_name order by t.displayOrder, t.tag_name";

        return sql;
    }


    /**
     * Return the tags used in the summary of the tag report
     * @return instance of {@link TagCategory}
     */
    public TagCategory getSummary() {
        if (summary == null) {
            createReport();
        }
        return summary;
    }

    /**
     * Return the tags used in the tag report
     * @return instance of {@link TagCategory}
     */
    public TagCategory getTags() {
        if (tags == null) {
            createReport();
        }
        return tags;
    }
}
