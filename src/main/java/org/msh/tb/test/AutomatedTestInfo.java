package org.msh.tb.test;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.login.UserSession;

import javax.persistence.EntityManager;

/**
 * Created by ricardo on 04/09/14.
 */
@Name("automatedTestInfo")
public class AutomatedTestInfo {
    private Integer caseId;
    private Integer userId;
    private Integer labId;
    private Integer unitId;
    private Integer adminUnitId;

    private static final String[] restrictions = {
            "exists(select id from ExamMicroscopy where tbcase.id=a.id)",
            "exists(select id from ExamCulture where tbcase.id=a.id)",
            "exists(select id from ExamDST where tbcase.id=a.id)",
            "exists(select id from MedicalExamination where tbcase.id=a.id)",
            "exists(select id from ExamHIV where tbcase.id=a.id)",
            "exists(select id from ExamXRay where tbcase.id=a.id)"
    };


    @Create
    public void initialize() {
        UserWorkspace uw = UserSession.getUserWorkspace();

        userId = uw.getUser().getId();
        unitId = uw.getTbunit().getId();
        labId = uw.getLaboratory() != null? uw.getLaboratory().getId(): null;
        adminUnitId = uw.getTbunit().getAdminUnit().getId();

        EntityManager em = App.getEntityManager();

        int count = restrictions.length;

        Number id = null;
        while ((count >= 0) && (id == null)) {
            String sql = "select max(a.id) from TbCase a where a.patient.workspace.id = :wsid ";
            for (int i = 0; i < count; i++) {
                String restr = restrictions[i];
                sql += " and " + restr;
            }
            id = (Number)em.createQuery(sql)
                    .setParameter("wsid", uw.getWorkspace().getId())
                    .getSingleResult();
            count--;
        }

        // search for the case
        // first, search for a case with culture and microscopy exam
/*
        Number id = (Number)em.createQuery("select max(a.id) from TbCase a where exists (select id from ExamCulture where tbcase.id = a.id)" +
                " and exists(select id from ExamMicroscopy where tbcase.id = a.id) " +
                " and exists(select id from ExamD" +
                "and a.patient.workspace.id = :wsid")
                .setParameter("wsid", uw.getWorkspace().getId())
                .getSingleResult();
*/

        // case was found ?
/*
        if (id == null) {
            // so search for any case
            id = (Number)em.createQuery("select max(a.id) from TbCase a where a.patient.workspace.id = :wsid")
                    .setParameter("wsid", uw.getWorkspace().getId())
                    .getSingleResult();
        }
*/

        caseId = id != null? id.intValue(): null;
    }


    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public Integer getAdminUnitId() {
        return adminUnitId;
    }

    public void setAdminUnitId(Integer adminUnitId) {
        this.adminUnitId = adminUnitId;
    }
}
