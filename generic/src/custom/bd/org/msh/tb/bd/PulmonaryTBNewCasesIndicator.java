package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.*;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.*;


/**
 * Generate indicator for new cases pulmonary for Bangladesh - TB 12 form (first table)
 * @author Mauricio Santos
 *
 */
@Name("pulmonaryTBNewCasesIndicator")
public class PulmonaryTBNewCasesIndicator extends Indicator2D implements TB12Indicator{

	@In(create=true) EntityManager entityManager;
    @In(create=true) Map<String, String> messages;

    //vars that will alocate the result. name pattern is: row_colum_subcolumn. This class generates only the result for the new case table
    float smearPositive_total_M, smearPositive_total_F,
            smearPositive_smearNegative_M, smearPositive_smearNegative_F,
            smearPositive_smearPositive_M, smearPositive_smearPositive_F,
            smearPositive_died_M, smearPositive_died_F,
            smearPositive_failure_M, smearPositive_failure_F,
            smearPositive_defaulted_M, smearPositive_defaulted_F,
            smearPositive_transfOut_M, smearPositive_transfOut_F,
            smearPositive_notEvaluated_M, smearPositive_notEvaluated_F,
            smearPositive_grandTotal_M, smearPositive_grandTotal_F, smearPositive_grandTotal_T,
            /*Suspended by bangldesh team
            xpert_total_M, xpert_total_F,
            xpert_smearNegative_M, xpert_smearNegative_F,
            xpert_smearPositive_M, xpert_smearPositive_F,
            xpert_died_M, xpert_died_F,
            xpert_failure_M, xpert_failure_F,
            xpert_defaulted_M, xpert_defaulted_F,
            xpert_transfOut_M, xpert_transfOut_F,
            xpert_notEvaluated_M, xpert_notEvaluated_F,
            xpert_grandTotal_M, xpert_grandTotal_F, xpert_grandTotal_T,*/
            smearNegative_total_M, smearNegative_total_F,
            smearNegative_smearNegative_M, smearNegative_smearNegative_F,
            smearNegative_smearPositive_M, smearNegative_smearPositive_F,
            smearNegative_died_M, smearNegative_died_F,
            smearNegative_failure_M, smearNegative_failure_F,
            smearNegative_defaulted_M, smearNegative_defaulted_F,
            smearNegative_transfOut_M, smearNegative_transfOut_F,
            smearNegative_notEvaluated_M, smearNegative_notEvaluated_F,
            smearNegative_grandTotal_M, smearNegative_grandTotal_F, smearNegative_grandTotal_T,
            notEvaluated_total_M, notEvaluated_total_F,
            notEvaluated_smearNegative_M, notEvaluated_smearNegative_F,
            notEvaluated_smearPositive_M, notEvaluated_smearPositive_F,
            notEvaluated_died_M, notEvaluated_died_F,
            notEvaluated_failure_M, notEvaluated_failure_F,
            notEvaluated_defaulted_M, notEvaluated_defaulted_F,
            notEvaluated_transfOut_M, notEvaluated_transfOut_F,
            notEvaluated_notEvaluated_M, notEvaluated_notEvaluated_F,
            notEvaluated_grandTotal_M, notEvaluated_grandTotal_F, notEvaluated_grandTotal_T;

    public void initialize(){
        this.getIndicatorFilters().setInfectionSite(InfectionSite.PULMONARY);
        this.getIndicatorFilters().setUseIniTreatmentDate(false);
        this.getIndicatorFilters().setUseRegistrationDate(true);
    }

    //remove after done

    @Override
    protected void createIndicators() {
        calculateSmearPositiveRowIndicators();
        calculateSmearNegativeRowIndicators();
        calculateNotEvaluatedRowIndicators();
        populateTableFields();
    }

    private void calculateSmearPositiveRowIndicators(){
        List<Object[]> result;

        //Smears columns - Male and female
        result = getEntityManager().createQuery(" select c.patient.gender, followup.result, count(followup.result) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() +
                " and followup.tbcase.id = m.tbcase.id " + HQLWhere_New_SmearPositiveRow + HQLWhere_Both_SmearColumns + HQLGroupBy_New_SmearColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();

        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            MicroscopyResult micResult = (MicroscopyResult) val[1];
            Long qtd = (Long) val[2];

            if(micResult.equals(MicroscopyResult.NEGATIVE)){
                if(gender.equals(Gender.MALE))
                    smearPositive_smearNegative_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_smearNegative_F += qtd.longValue();
            }else if(micResult.equals(MicroscopyResult.PLUS) || micResult.equals(MicroscopyResult.PLUS2) || micResult.equals(MicroscopyResult.PLUS3) ||
                    micResult.equals(MicroscopyResult.PLUS4) || micResult.equals(MicroscopyResult.POSITIVE)){
                if(gender.equals(Gender.MALE))
                    smearPositive_smearPositive_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_smearPositive_F += qtd.longValue();
            }
        }

        //Outcome Columns - Male and female
        System.out.println("select c.patient.gender, c.state, count(c.state) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_SmearPositiveRow
                + HQLWhere_New_OutcomeColumns + HQLGroupBy_New_OutcomeColumns);
        result = getEntityManager().createQuery("select c.patient.gender, c.state, count(c.state) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_SmearPositiveRow
                + HQLWhere_New_OutcomeColumns + HQLGroupBy_New_OutcomeColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();

        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            CaseState outcome = (CaseState) val[1];
            Long qtd = (Long) val[2];

            if(outcome.equals(CaseState.DIED)){
                if(gender.equals(Gender.MALE))
                    smearPositive_died_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_died_F += qtd.longValue();
            }else if(outcome.equals(CaseState.FAILED)){
                if(gender.equals(Gender.MALE))
                    smearPositive_failure_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_failure_F += qtd.longValue();
            }else if(outcome.equals(CaseState.DEFAULTED)){
                if(gender.equals(Gender.MALE))
                    smearPositive_defaulted_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_defaulted_F += qtd.longValue();
            }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                if(gender.equals(Gender.MALE))
                    smearPositive_transfOut_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearPositive_transfOut_F += qtd.longValue();
            }

        }

        //Not Evaluated Column - Male and female
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_SmearPositiveRow
                + HQLWhere_Both_NotEvaluatedColumn + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcSmearPosRowNotEvalColumns(result);

        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() + HQLWhere_New_SmearPositiveRow
                + HQLWhere_Both_NotEvaluatedColumn2 + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcSmearPosRowNotEvalColumns(result);


        //grand total columns
        smearPositive_grandTotal_M = smearPositive_smearNegative_M + smearPositive_smearPositive_M + smearPositive_died_M +
                smearPositive_failure_M + smearPositive_defaulted_M + smearPositive_transfOut_M + smearPositive_notEvaluated_M;
        smearPositive_grandTotal_F = smearPositive_smearNegative_F + smearPositive_smearPositive_F + smearPositive_died_F +
                smearPositive_failure_F + smearPositive_defaulted_F + smearPositive_transfOut_F + smearPositive_notEvaluated_F;
        smearPositive_grandTotal_T = smearPositive_grandTotal_M + smearPositive_grandTotal_F;

        //total columns
        smearPositive_total_M = smearPositive_grandTotal_M;
        smearPositive_total_F = smearPositive_grandTotal_F;
    }

    private void calcSmearPosRowNotEvalColumns(List<Object[]> result){
        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            Long qtd = (Long) val[1];

            if(gender.equals(Gender.MALE))
                smearPositive_notEvaluated_M += qtd.longValue();
            else if(gender.equals(Gender.FEMALE))
                smearPositive_notEvaluated_F += qtd.longValue();
        }
    }

    private void calculateSmearNegativeRowIndicators(){
        List<Object[]> result;

        //Smears columns - Male and female
        result = getEntityManager().createQuery(" select c.patient.gender, followup.result, count(followup.result) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() +
                " and followup.tbcase.id = m.tbcase.id " + HQLWhere_New_SmearNegativeRow + HQLWhere_Both_SmearColumns + HQLGroupBy_New_SmearColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();

        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            MicroscopyResult micResult = (MicroscopyResult) val[1];
            Long qtd = (Long) val[2];

            if(micResult.equals(MicroscopyResult.NEGATIVE)){
                if(gender.equals(Gender.MALE))
                    smearNegative_smearNegative_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_smearNegative_F += qtd.longValue();
            }else if(micResult.equals(MicroscopyResult.PLUS) || micResult.equals(MicroscopyResult.PLUS2) || micResult.equals(MicroscopyResult.PLUS3) ||
                    micResult.equals(MicroscopyResult.PLUS4) || micResult.equals(MicroscopyResult.POSITIVE)){
                if(gender.equals(Gender.MALE))
                    smearNegative_smearPositive_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_smearPositive_F += qtd.longValue();
            }
        }

        //Outcome Columns - Male and female
        result = getEntityManager().createQuery("select c.patient.gender, c.state, count(c.state) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_SmearNegativeRow
                + HQLWhere_New_OutcomeColumns + HQLGroupBy_New_OutcomeColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();

        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            CaseState outcome = (CaseState) val[1];
            Long qtd = (Long) val[2];

            if(outcome.equals(CaseState.DIED)){
                if(gender.equals(Gender.MALE))
                    smearNegative_died_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_died_F += qtd.longValue();
            }else if(outcome.equals(CaseState.FAILED)){
                if(gender.equals(Gender.MALE))
                    smearNegative_failure_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_failure_F += qtd.longValue();
            }else if(outcome.equals(CaseState.DEFAULTED)){
                if(gender.equals(Gender.MALE))
                    smearNegative_defaulted_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_defaulted_F += qtd.longValue();
            }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                if(gender.equals(Gender.MALE))
                    smearNegative_transfOut_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    smearNegative_transfOut_F += qtd.longValue();
            }

        }

        //Not Evaluated Column - Male and female
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_SmearNegativeRow
                + HQLWhere_Both_NotEvaluatedColumn + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcSmearNegRowNotEvalColumns(result);

        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() + HQLWhere_New_SmearNegativeRow
                + HQLWhere_Both_NotEvaluatedColumn2 + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcSmearNegRowNotEvalColumns(result);


        //grand total columns
        smearNegative_grandTotal_M = smearNegative_smearNegative_M + smearNegative_smearPositive_M + smearNegative_died_M +
                smearNegative_failure_M + smearNegative_defaulted_M + smearNegative_transfOut_M + smearNegative_notEvaluated_M;
        smearNegative_grandTotal_F = smearNegative_smearNegative_F + smearNegative_smearPositive_F + smearNegative_died_F +
                smearNegative_failure_F + smearNegative_defaulted_F + smearNegative_transfOut_F + smearNegative_notEvaluated_F;
        smearNegative_grandTotal_T = smearNegative_grandTotal_M + smearNegative_grandTotal_F;

        //total columns
        smearNegative_total_M = smearNegative_grandTotal_M;
        smearNegative_total_F = smearNegative_grandTotal_F;
    }

    private void calcSmearNegRowNotEvalColumns(List<Object[]> result){
        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            Long qtd = (Long) val[1];

            if(gender.equals(Gender.MALE))
                smearNegative_notEvaluated_M += qtd.longValue();
            else if(gender.equals(Gender.FEMALE))
                smearNegative_notEvaluated_F += qtd.longValue();
        }
    }

    private void calculateNotEvaluatedRowIndicators(){
        List<Object[]> result;

        //Smears columns - Male and female
        result = getEntityManager().createQuery(" select c.patient.gender, followup.result, count(followup.result) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() +
                " and followup.tbcase.id = m.tbcase.id " + HQLWhere_New_NotEvaluatedRow1 + HQLWhere_Both_SmearColumns + HQLGroupBy_New_SmearColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowSmearColumns(result);

        result = getEntityManager().createQuery(" select c.patient.gender, followup.result, count(followup.result) " + HQLFrom_New_NotEval2RowSmearNotEval2Columns + getHQLWhere() +
                " and followup.tbcase.id = m.tbcase.id " + HQLWhere_New_NotEvaluatedRow2 + HQLWhere_Both_SmearColumns + HQLGroupBy_New_SmearColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowSmearColumns(result);


        //Outcome Columns - Male and female
        result = getEntityManager().createQuery("select c.patient.gender, c.state, count(c.state) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_NotEvaluatedRow1
                + HQLWhere_New_OutcomeColumns + HQLGroupBy_New_OutcomeColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowOutcomeColumns(result);

        result = getEntityManager().createQuery("select c.patient.gender, c.state, count(c.state) " + HQLFrom_New_NotEval2RowOutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_NotEvaluatedRow2
                + HQLWhere_New_OutcomeColumns + HQLGroupBy_New_OutcomeColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowOutcomeColumns(result);


        //Not Evaluated Column - Male and female
        //Not eval row, when case has diag exam registered as pending. Not eval col when case has no follow up exam registered.
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_OutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_NotEvaluatedRow1
                + HQLWhere_Both_NotEvaluatedColumn + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowNotEvalColumns(result);

        //Not eval row, when case has diag exam registered as pending. Not eval col when case has follow up exam registered as pending.
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_NotEval2SmearColumns + getHQLWhere() + HQLWhere_New_NotEvaluatedRow1
                + HQLWhere_Both_NotEvaluatedColumn2 + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowNotEvalColumns(result);

        //Not eval row, when case has no diag exam registered. Not eval col when case has no follow up exam registered.
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + HQLFrom_New_NotEval2RowOutcomeNotEvalColumns + getHQLWhere() + HQLWhere_New_NotEvaluatedRow2
                + HQLWhere_Both_NotEvaluatedColumn + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowNotEvalColumns(result);

        //Not eval row, when case has no diag exam registered. Not eval col when case has follow up exam registered as pending.
        result = getEntityManager().createQuery("select c.patient.gender, count(c.id) " + " from ExamMicroscopy m, ExamMicroscopy followup join m.tbcase c " + getHQLWhere() + HQLWhere_New_NotEvaluatedRow2
                + HQLWhere_Both_NotEvaluatedColumn2 + HQLGroupBy_New_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(),90))
                .getResultList();
        calcNotEvalRowNotEvalColumns(result);

        //grand total columns
        notEvaluated_grandTotal_M = notEvaluated_smearNegative_M + notEvaluated_smearPositive_M + notEvaluated_died_M +
                notEvaluated_failure_M + notEvaluated_defaulted_M + notEvaluated_transfOut_M + notEvaluated_notEvaluated_M;
        notEvaluated_grandTotal_F = notEvaluated_smearNegative_F + notEvaluated_smearPositive_F + notEvaluated_died_F +
                notEvaluated_failure_F + notEvaluated_defaulted_F + notEvaluated_transfOut_F + notEvaluated_notEvaluated_F;
        notEvaluated_grandTotal_T = notEvaluated_grandTotal_M + notEvaluated_grandTotal_F;

        //total columns
        notEvaluated_total_M = notEvaluated_grandTotal_M;
        notEvaluated_total_F = notEvaluated_grandTotal_F;
    }

    private void calcNotEvalRowSmearColumns(List<Object[]> result){
        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            MicroscopyResult micResult = (MicroscopyResult) val[1];
            Long qtd = (Long) val[2];

            if(micResult.equals(MicroscopyResult.NEGATIVE)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_smearNegative_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_smearNegative_F += qtd.longValue();
            }else if(micResult.equals(MicroscopyResult.PLUS) || micResult.equals(MicroscopyResult.PLUS2) || micResult.equals(MicroscopyResult.PLUS3) ||
                    micResult.equals(MicroscopyResult.PLUS4) || micResult.equals(MicroscopyResult.POSITIVE)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_smearPositive_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_smearPositive_F += qtd.longValue();
            }
        }
    }

    private void calcNotEvalRowOutcomeColumns(List<Object[]> result){
        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            CaseState outcome = (CaseState) val[1];
            Long qtd = (Long) val[2];

            if(outcome.equals(CaseState.DIED)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_died_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_died_F += qtd.longValue();
            }else if(outcome.equals(CaseState.FAILED)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_failure_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_failure_F += qtd.longValue();
            }else if(outcome.equals(CaseState.DEFAULTED)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_defaulted_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_defaulted_F += qtd.longValue();
            }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                if(gender.equals(Gender.MALE))
                    notEvaluated_transfOut_M += qtd.longValue();
                else if(gender.equals(Gender.FEMALE))
                    notEvaluated_transfOut_F += qtd.longValue();
            }

        }
    }

    private void calcNotEvalRowNotEvalColumns(List<Object[]> result){
        for(Object[] val : result){
            Gender gender = (Gender) val[0];
            Long qtd = (Long) val[1];

            if(gender.equals(Gender.MALE))
                notEvaluated_notEvaluated_M += qtd.longValue();
            else if(gender.equals(Gender.FEMALE))
                notEvaluated_notEvaluated_F += qtd.longValue();
        }
    }

    private void populateTableFields(){
        //The code bellow populates the new case table, smear positive row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearpositive"), smearPositive_total_M + smearPositive_total_F);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_notEvaluated_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_notEvaluated_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearpositive"), smearPositive_grandTotal_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearpositive"), smearPositive_grandTotal_F);
        addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearpositive"), smearPositive_grandTotal_T);

        //The code bellow populates the new case table, xpert row
        /*suspended, Bangladesh team was not sure about the rules
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.xpert"), xpert_total_M + xpert_total_F);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_notEvaluated_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_notEvaluated_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.xpert"), xpert_grandTotal_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.xpert"), xpert_grandTotal_F);
        addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.xpert"), xpert_grandTotal_T);
        */
        //The code bellow populates the new case table, smear negative row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.smearnegative"), smearNegative_total_M + smearNegative_total_F);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_notEvaluated_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_notEvaluated_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.smearnegative"), smearNegative_grandTotal_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.smearnegative"), smearNegative_grandTotal_F);
        addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.smearnegative"), smearNegative_grandTotal_T);

        //The code bellow populates the new case table, not evaluated row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_total_M + notEvaluated_total_F);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_notEvaluated_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_notEvaluated_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_grandTotal_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_grandTotal_F);
        addValue(messages.get("manag.pulmonary.tot"), messages.get("manag.pulmonary.notevaluated"), notEvaluated_grandTotal_T);

    }

    @Override
    protected String getHQLWhere(){
        return super.getHQLWhere() + " " + " and c.patientType = " + PatientType.NEW.ordinal() + " ";
    }
}
