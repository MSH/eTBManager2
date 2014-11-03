package org.msh.tb.bd;

/**
 * Created by Mauricio on 15/10/2014.
 */
public interface TB12Indicator {

    //Name pattern - typeOfHQL_Table_Row(and/or)Column
    //param followupExamLimit = DateUtils.incDays(getIndicatorFilters().getEndDate(),90)

    static final String HQLFrom_New_NotEval2SmearColumns = " from ExamMicroscopy m, ExamMicroscopy followup join m.tbcase c ";

    static final String HQLFrom_New_OutcomeNotEvalColumns = " from ExamMicroscopy m join m.tbcase c ";

    static final String HQLFrom_Retreat_SmearNotEval2Columns = " from ExamMicroscopy followup join followup.tbcase c ";

    static final String HQLFrom_Retreat_OutcomeNotEvalColumns = " from TbCase c ";

    static final String HQLFrom_New_NotEval2RowSmearNotEval2Columns = " from ExamMicroscopy m, ExamMicroscopy followup right join m.tbcase c ";

    static final String HQLFrom_New_NotEval2RowOutcomeNotEvalColumns = " from ExamMicroscopy m right join m.tbcase c ";

    static final String HQLWhere_New_SmearPositiveRow = " and m.id = (select max(m2.id) from ExamMicroscopy m2 where m2.tbcase.id = m.tbcase.id " +
                                                                    "and m2.dateRelease = (select max(m3.dateRelease) from ExamMicroscopy m3 where m3.tbcase.id = m2.tbcase.id and m3.dateRelease <= c.treatmentPeriod.iniDate and m3.dateRelease <= #{indicatorFilters.endDate}))" +
                                                    " and m.result in (1,2,3,4,5) ";

    static final String HQLWhere_New_SmearNegativeRow = " and m.id = (select max(m2.id) from ExamMicroscopy m2 where m2.tbcase.id = m.tbcase.id " +
                                                                    "and m2.dateRelease = (select max(m3.dateRelease) from ExamMicroscopy m3 where m3.tbcase.id = m2.tbcase.id and m3.dateRelease <= c.treatmentPeriod.iniDate and m3.dateRelease <= #{indicatorFilters.endDate}))" +
                                                    " and m.result = 0 ";

    //HQLWhere_New_NotEvaluatedRow1 - for cases with registered exams but those exams result is pending or null
    static final String HQLWhere_New_NotEvaluatedRow1 = " and m.id = (select max(m2.id) from ExamMicroscopy m2 where m2.tbcase.id = m.tbcase.id " +
                                                                        "and m2.dateRelease = (select max(m3.dateRelease) from ExamMicroscopy m3 where m3.tbcase.id = m2.tbcase.id and m3.dateRelease <= c.treatmentPeriod.iniDate and m3.dateRelease <= #{indicatorFilters.endDate}))" +
                                                        " and (m.result in (6,7) or m.result is null)";

    //HQLWhere_New_NotEvaluatedRow2 - for cases with no registered exams
    static final String HQLWhere_New_NotEvaluatedRow2 = " and (select max(m2.id) from ExamMicroscopy m2 where m2.tbcase.id = m.tbcase.id " +
                                                                "and m2.dateRelease = (select max(m3.dateRelease) from ExamMicroscopy m3 where m3.tbcase.id = m2.tbcase.id and m3.dateRelease <= c.treatmentPeriod.iniDate and m3.dateRelease <= #{indicatorFilters.endDate})) is null ";

    static final String HQLWhere_Both_SmearColumns = " and followup.id = (select max(m4.id) from ExamMicroscopy m4 where m4.tbcase.id = followup.tbcase.id " +
                                                                        "and m4.dateRelease = (select min(m5.dateRelease) from ExamMicroscopy m5 where m5.tbcase.id = m4.tbcase.id and m5.dateRelease > c.treatmentPeriod.iniDate and m5.dateRelease <= :followupExamLimit)) ";

    static final String HQLWhere_New_OutcomeColumns = " and c.state in (5,6,7,8) and (select max(m4.id) from ExamMicroscopy m4 where m4.tbcase.id = c.id " +
                                                  " and m4.dateRelease = (select min(m5.dateRelease) from ExamMicroscopy m5 where m5.tbcase.id = m4.tbcase.id and m5.dateRelease > c.treatmentPeriod.iniDate and m5.dateRelease <= :followupExamLimit)) is null ";

    static final String HQLWhere_Both_NotEvaluatedColumn = " and c.state not in (5,6,7,8) and (select max(m4.id) from ExamMicroscopy m4 where m4.tbcase.id = c.id " +
                                                      " and m4.dateRelease = (select min(m5.dateRelease) from ExamMicroscopy m5 where m5.tbcase.id = m4.tbcase.id and m5.dateRelease > c.treatmentPeriod.iniDate and m5.dateRelease <= :followupExamLimit)) is null ";

    static final String HQLWhere_Both_NotEvaluatedColumn2 = " and c.state not in (5,6,7,8) and followup.tbcase.id = c.id " +
                                                           " and followup.id = (select max(m4.id) from ExamMicroscopy m4 where m4.tbcase.id = followup.tbcase.id " +
                                                                        "and m4.dateRelease = (select min(m5.dateRelease) from ExamMicroscopy m5 where m5.tbcase.id = m4.tbcase.id and m5.dateRelease > c.treatmentPeriod.iniDate and m5.dateRelease <= :followupExamLimit)) " +
                                                           " and (followup.result in (6,7) or followup.result is null) ";

    static final String HQLGroupBy_New_SmearColumns = " group by c.patient.gender, followup.result ";

    static final String HQLGroupBy_New_OutcomeColumns =  " group by c.patient.gender, c.state ";

    static final String HQLGroupBy_New_NotEvaluatedColumn = " group by c.patient.gender ";

    static final String HQLGroupBy_Retreat_SmearColumns = " group by c.patientType, c.patient.gender, followup.result ";

    static final String HQLGroupBy_Retreat_OutcomeColumns =  " group by c.patientType, c.patient.gender, c.state ";

    static final String HQLGroupBy_Retreat_NotEvaluatedColumn =  " group by c.patientType, c.patient.gender ";

}
