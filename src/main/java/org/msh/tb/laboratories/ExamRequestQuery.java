package org.msh.tb.laboratories;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.ExamRequest;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.DisplayCaseNumber;
import org.msh.tb.entities.enums.ExamStatus;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;

/**
 * Query the list of requests based on the filters in the {@link org.msh.tb.laboratories.SampleFilters} component
 *
 * Created by ricardo on 18/08/14.
 */
@Name("examRequestQuery")
public class ExamRequestQuery extends EntityQuery<ExamRequest>{

    private boolean updating;

    /* (non-Javadoc)
     * @see org.jboss.seam.framework.Query#getCountEjbql()
     */
    @Override
    protected String getCountEjbql() {
        String hql = "select count(*) from ExamRequest req " +
                " where req.tbcase.patient.workspace.id = #{defaultWorkspace.id} " + getTableConditions();

        return hql;
    }

    /**
     * Force a list refresh and signal that it's being updated
     */
    public void update() {
        updating = true;
        refresh();
    }

    /* (non-Javadoc)
     * @see org.jboss.seam.framework.Query#getEjbql()
     */
    @Override
    public String getEjbql() {
        String hql = "from ExamRequest req " +
                " join fetch req.tbcase c join fetch c.patient p " +
                "where p.workspace.id = #{defaultWorkspace.id} " + getTableConditions();

        return hql;
    }


    /**
     * Return HQL conditions of the query to be applied based on filters for table results
     * @return
     */
    protected String getTableConditions() {
        SampleFilters filters = SampleFilters.instance();

        String hql;

        if (filters.getExamType() != null) {
            String s = "select exam.id from " + getExamTable() +
                    " exam where exam.tbcase.id = c.id ";

            s += " and exam.laboratory.id = #{userWorkspace.laboratory.id}";

            if (filters.getExamStatus() != null) {
                s += " and exam.status = #{sampleFilters.examStatus}";
                if (filters.getExamStatus() == ExamStatus.PERFORMED) {
                    s += " and month(exam.dateCollected) = (#{sampleFilters.month} + 1) and year(exam.dateCollected) = #{sampleFilters.year} ";
                }
            }
            hql = " and exists(" + s + ") ";
        }
        else {
            hql = "";
        }

        hql += " and req.laboratory.id = #{userWorkspace.laboratory.id}";

        if ((filters.getPatient() != null) && (!filters.getPatient().isEmpty())) {
            String[] names = filters.getPatient().split(" ");
            if (names.length > 0) {
                String hqlNumber = generateHQLPatientNumber(filters.getPatient());

                String s="(";
                for (String name: names) {
                    if (!name.isEmpty()) {
                        name = name.replaceAll("'", "''");
                        if (s.length() > 1)
                            s += " and ";
                        s += " ((upper(p.name) like '%" + name.toUpperCase() +
                                "%') or (upper(p.middleName) like '%" + name.toUpperCase() +
                                "%') or (upper(p.lastName) like '%" + name.toUpperCase() + "%')) ";
                    }
                }

                s = s + (hqlNumber.isEmpty() ? ")": " or (" + hqlNumber + "))");

                hql += " and (" + s + "))";
            }
        }

        return hql;
    }


    /**
     * Convert the number entered by the user and separates patient number and case number
     * @return
     */
    protected String generateHQLPatientNumber(String key) {
        if (key == null || key.equals(""))
            return "";

        String hql = "";

        // check if search is by case ID
        Workspace ws = UserSession.getWorkspace();
        if ((ws.getSuspectCaseNumber() == DisplayCaseNumber.CASE_ID) || (ws.getConfirmedCaseNumber() == DisplayCaseNumber.CASE_ID)) {
            Integer caseId = stringToNumber(key);
            if (caseId != null)
                hql += "c.id = " + caseId;
        }

        // check if search is by record number
        if ((ws.getSuspectCaseNumber() == DisplayCaseNumber.VALIDATION_NUMBER) || (ws.getConfirmedCaseNumber() == DisplayCaseNumber.VALIDATION_NUMBER)) {
            String[] s = key.split("-");
            if (s.length == 1) {
                Integer patnum = stringToNumber(s[0]);
                if (patnum != null) {
                    if (!hql.isEmpty())
                        hql += " or ";
                    hql += "p.recordNumber = " + patnum;
                }
            }
            else {
                if (s.length == 2) {
                    Integer patnum = stringToNumber(s[0]);
                    Integer digit = stringToNumber(s[1]);
                    if ((patnum != null) && (digit != null)) {
                        if (!hql.isEmpty())
                            hql += " or ";
                        hql += "(p.recordNumber = " + patnum + " and c.caseNumber = " + digit + ")";
                    }
                }
            }
        }

        // add filters by suspect registration code
        if (ws.getSuspectCaseNumber() == DisplayCaseNumber.USER_DEFINED) {
            if (!hql.isEmpty())
                hql += " or ";
            hql += "c.suspectRegistrationCode = '" + key + "'";
        }

        // add filters by registration code
        if (ws.getSuspectCaseNumber() == DisplayCaseNumber.USER_DEFINED) {
            if (!hql.isEmpty())
                hql += " or ";
            hql += "c.registrationCode = '" + key + "'";
        }

        if (!hql.isEmpty())
            hql = "(" + hql + ")";

        return hql;
    }

    /**
     * Convert a number from string to integer. If the number is not a valid number,
     * it'll return null
     * @param s the string representation of the number
     * @return Integer number or null
     */
    protected Integer stringToNumber(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Return the exam property used in the search based on user filters
     * @return String value
     */
    protected String getExamTable() {
        SampleFilters filters = SampleFilters.instance();
        if ((filters == null) || (filters.getExamType() == null)) {
            return null;
        }
        switch (filters.getExamType()) {
            case CULTURE: return "ExamCulture";
            case MICROSCOPY: return "ExamMicroscopy";
            case DST: return "ExamDST";
            case XPERT: return "ExamXpert";
            default: return null;
        }
    }

    @Override
    public Integer getMaxResults() {
        return 30;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }
}
