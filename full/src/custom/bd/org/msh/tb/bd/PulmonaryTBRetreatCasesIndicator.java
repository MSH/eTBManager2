package org.msh.tb.bd;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.Gender;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.PatientType;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.utils.date.DateUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;


/**
 * Generate indicator for re-treatment cases pulmonary for Bangladesh - TB 12 form (second table)
 * @author Mauricio Santos
 *
 */
@Name("pulmonaryTBRetreatCasesIndicator")
public class PulmonaryTBRetreatCasesIndicator extends Indicator2D implements TB12Indicator{

	@In(create=true) EntityManager entityManager;
    @In(create=true) Map<String, String> messages;

    //vars that will alocate the result. name pattern is: row_colum_subcolumn. This class generates only the result for the new case table
    float relapse_total_M, relapse_total_F, relapse_total_T,
            relapse_smearNegative_M, relapse_smearNegative_F,
            relapse_smearPositive_M, relapse_smearPositive_F,
            relapse_died_M, relapse_died_F,
            relapse_failure_M, relapse_failure_F,
            relapse_defaulted_M, relapse_defaulted_F,
            relapse_transfOut_M, relapse_transfOut_F,
            relapse_notEvaluated_M, relapse_notEvaluated_F,
            relapse_otherOutcomes_M, relapse_otherOutcomes_F,

            failure_total_M, failure_total_F, failure_total_T,
            failure_smearNegative_M, failure_smearNegative_F,
            failure_smearPositive_M, failure_smearPositive_F,
            failure_died_M, failure_died_F,
            failure_failure_M, failure_failure_F,
            failure_defaulted_M, failure_defaulted_F,
            failure_transfOut_M, failure_transfOut_F,
            failure_notEvaluated_M, failure_notEvaluated_F,
            failure_otherOutcomes_M, failure_otherOutcomes_F,

            default_total_M, default_total_F, default_total_T,
            default_smearNegative_M, default_smearNegative_F,
            default_smearPositive_M, default_smearPositive_F,
            default_died_M, default_died_F,
            default_failure_M, default_failure_F,
            default_defaulted_M, default_defaulted_F,
            default_transfOut_M, default_transfOut_F,
            default_notEvaluated_M, default_notEvaluated_F,
            default_otherOutcomes_M, default_otherOutcomes_F,

            //otherp = cases with patient type as other
            otherp_total_M, otherp_total_F, otherp_total_T,
            otherp_smearNegative_M, otherp_smearNegative_F,
            otherp_smearPositive_M, otherp_smearPositive_F,
            otherp_died_M, otherp_died_F,
            otherp_failure_M, otherp_failure_F,
            otherp_defaulted_M, otherp_defaulted_F,
            otherp_transfOut_M, otherp_transfOut_F,
            otherp_otherOutcomes_M, otherp_otherOutcomes_F,
            otherp_notEvaluated_M, otherp_notEvaluated_F,

            /*Task Suspended. Waiting definition from BD team
            //othern = cases with patient type as other with diag exam as smear negative.
            othern_total_M, othern_total_F, othern_total_T,
            othern_smearNegative_M, othern_smearNegative_F,
            othern_smearPositive_M, othern_smearPositive_F,
            othern_died_M, othern_died_F,
            othern_failure_M, othern_failure_F,
            othern_defaulted_M, othern_defaulted_F,
            othern_transfOut_M, othern_transfOut_F,
            othern_otherOutcomes_M, othern_otherOutcomes_F,
            othern_notEvaluated_M, othern_notEvaluated_F;*/

            total_total_M, total_total_F, total_total_T,
            total_smearNegative_M, total_smearNegative_F,
            total_smearPositive_M, total_smearPositive_F,
            total_died_M, total_died_F,
            total_failure_M, total_failure_F,
            total_defaulted_M, total_defaulted_F,
            total_transfOut_M, total_transfOut_F,
            total_otherOutcomes_M, total_otherOutcomes_F,
            total_notEvaluated_M, total_notEvaluated_F;

    @Override
    protected void createIndicators() {
        calculateSmearsColumnsIndicators();
        calculateOutcomeColumnsIndicators();
        calculateNotEvaluatedColumnsIndicators();

        calculateTotalColumn();
        calculateTotalRow();

        populateTableFields();
    }

    private void calculateSmearsColumnsIndicators(){
        List<Object[]> result;

        //Smears columns - Male and female
        result = getEntityManager().createQuery(" select c.patientType, c.patient.gender, followup.result, count(followup.result) " + HQLFrom_Retreat_SmearNotEval2Columns + getHQLWhere() +
                HQLWhere_Both_SmearColumns + HQLGroupBy_Retreat_SmearColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(), 90))
                .getResultList();

        for(Object[] val : result) {
            PatientType patientType = (PatientType) val[0];
            Gender gender = (Gender) val[1];
            MicroscopyResult micResult = (MicroscopyResult) val[2];
            Long qtd = (Long) val[3];

            if(patientType.equals(PatientType.RELAPSE)){
                if(gender.equals(Gender.FEMALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        relapse_smearNegative_F += qtd;
                    else if (isSmearPositive(micResult))
                        relapse_smearPositive_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        relapse_smearNegative_M += qtd;
                    else if (isSmearPositive(micResult))
                        relapse_smearPositive_M += qtd;
                }
            }else if(patientType.equals(PatientType.FAILURE_FT) || patientType.equals(PatientType.FAILURE_RT)){
                if(gender.equals(Gender.FEMALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        failure_smearNegative_F += qtd;
                    else if (isSmearPositive(micResult))
                        failure_smearPositive_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        failure_smearNegative_M += qtd;
                    else if (isSmearPositive(micResult))
                        failure_smearPositive_M += qtd;
                }
            }else if(patientType.equals(PatientType.AFTER_DEFAULT)){
                if(gender.equals(Gender.FEMALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        default_smearNegative_F += qtd;
                    else if (isSmearPositive(micResult))
                        default_smearPositive_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        default_smearNegative_M += qtd;
                    else if (isSmearPositive(micResult))
                        default_smearPositive_M += qtd;
                }
            }else{ //others - types not counted on the options above
                if(gender.equals(Gender.FEMALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        otherp_smearNegative_F += qtd;
                    else if (isSmearPositive(micResult))
                        otherp_smearPositive_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    if(micResult.equals(MicroscopyResult.NEGATIVE))
                        otherp_smearNegative_M += qtd;
                    else if (isSmearPositive(micResult))
                        otherp_smearPositive_M += qtd;
                }
            }
        }
    }

    private void calculateOutcomeColumnsIndicators(){
        List<Object[]> result;

        //Outcome columns - Male and female
        result = getEntityManager().createQuery(" select c.patientType, c.patient.gender, c.state, count(c.id) " + HQLFrom_Retreat_OutcomeNotEvalColumns + getHQLWhere() +
                HQLWhere_Both_OutcomeColumns + HQLGroupBy_Retreat_OutcomeColumns)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(), 90))
                .getResultList();

        for(Object[] val : result) {
            PatientType patientType = (PatientType) val[0];
            Gender gender = (Gender) val[1];
            CaseState outcome = (CaseState) val[2];
            Long qtd = (Long) val[3];

            if(patientType.equals(PatientType.RELAPSE)){
                if(gender.equals(Gender.FEMALE)){
                    if(outcome.equals(CaseState.DIED)){
                        relapse_died_F += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        relapse_failure_F += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        relapse_defaulted_F += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        relapse_transfOut_F += qtd;
                    }else{
                        relapse_otherOutcomes_F += qtd;
                    }
                }else if(gender.equals(Gender.MALE)){
                    if(outcome.equals(CaseState.DIED)){
                        relapse_died_M += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        relapse_failure_M += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        relapse_defaulted_M += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        relapse_transfOut_M += qtd;
                    }else{
                        relapse_otherOutcomes_M += qtd;
                    }
                }
            }else if(patientType.equals(PatientType.FAILURE_FT) || patientType.equals(PatientType.FAILURE_RT)){
                if(gender.equals(Gender.FEMALE)){
                    if(outcome.equals(CaseState.DIED)){
                        failure_died_F += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        failure_failure_F += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        failure_defaulted_F += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        failure_transfOut_F += qtd;
                    }else{
                        failure_otherOutcomes_F += qtd;
                    }
                }else if(gender.equals(Gender.MALE)){
                    if(outcome.equals(CaseState.DIED)){
                        failure_died_M += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        failure_failure_M += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        failure_defaulted_M += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        failure_transfOut_M += qtd;
                    }else{
                        failure_otherOutcomes_M += qtd;
                    }
                }
            }else if(patientType.equals(PatientType.AFTER_DEFAULT)){
                if(gender.equals(Gender.FEMALE)){
                    if(outcome.equals(CaseState.DIED)){
                        default_died_F += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        default_failure_F += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        default_defaulted_F += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        default_transfOut_F += qtd;
                    }else{
                        default_otherOutcomes_F += qtd;
                    }
                }else if(gender.equals(Gender.MALE)){
                    if(outcome.equals(CaseState.DIED)){
                        default_died_M += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        default_failure_M += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        default_defaulted_M += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        default_transfOut_M += qtd;
                    }else{
                        default_otherOutcomes_M += qtd;
                    }
                }
            }else{ //others - types not counted on the options above
                if(gender.equals(Gender.FEMALE)){
                    if(outcome.equals(CaseState.DIED)){
                        otherp_died_F += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        otherp_failure_F += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        otherp_defaulted_F += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        otherp_transfOut_F += qtd;
                    }else{
                        otherp_otherOutcomes_F += qtd;
                    }
                }else if(gender.equals(Gender.MALE)){
                    if(outcome.equals(CaseState.DIED)){
                        otherp_died_M += qtd;
                    }else if(outcome.equals(CaseState.FAILED)){
                        otherp_failure_M += qtd;
                    }else if(outcome.equals(CaseState.DEFAULTED)){
                        otherp_defaulted_M += qtd;
                    }else if(outcome.equals(CaseState.TRANSFERRED_OUT)){
                        otherp_transfOut_M += qtd;
                    }else{
                        otherp_otherOutcomes_M += qtd;
                    }
                }
            }
        }
    }

    private void calculateNotEvaluatedColumnsIndicators() {
        List<Object[]> result;

        //Not evaluated columns - Male and female - when a case doesn't have a followup exam registered.
        result = getEntityManager().createQuery(" select c.patientType, c.patient.gender, count(c.id) " + HQLFrom_Retreat_OutcomeNotEvalColumns + getHQLWhere() +
                HQLWhere_Both_NotEvaluatedColumn + HQLGroupBy_Retreat_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(), 90))
                .getResultList();
        calcNotEvalColumn(result);

        result = getEntityManager().createQuery(" select c.patientType, c.patient.gender, count(c.id) " + HQLFrom_Retreat_SmearNotEval2Columns + getHQLWhere() +
                HQLWhere_Both_NotEvaluatedColumn2 + HQLGroupBy_Retreat_NotEvaluatedColumn)
                .setParameter("followupExamLimit", DateUtils.incDays(getIndicatorFilters().getEndDate(), 90))
                .getResultList();
        calcNotEvalColumn(result);
    }

    private void calcNotEvalColumn(List<Object[]> result){
        for(Object[] val : result) {
            PatientType patientType = (PatientType) val[0];
            Gender gender = (Gender) val[1];
            Long qtd = (Long) val[2];

            if(patientType.equals(PatientType.RELAPSE)){
                if(gender.equals(Gender.FEMALE)){
                    relapse_notEvaluated_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    relapse_notEvaluated_M += qtd;
                }
            }else if(patientType.equals(PatientType.FAILURE_FT) || patientType.equals(PatientType.FAILURE_RT)){
                if(gender.equals(Gender.FEMALE)){
                    failure_notEvaluated_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    failure_notEvaluated_M += qtd;
                }
            }else if(patientType.equals(PatientType.AFTER_DEFAULT)){
                if(gender.equals(Gender.FEMALE)){
                    default_notEvaluated_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    default_notEvaluated_M += qtd;
                }
            }else{//others - types not counted on the options above
                if(gender.equals(Gender.FEMALE)){
                    otherp_notEvaluated_F += qtd;
                }else if(gender.equals(Gender.MALE)){
                    otherp_notEvaluated_M += qtd;
                }
            }
        }
    }

    private void populateTableFields(){
        //The code bellow populates the retreat case table, relapse row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.relapse"), relapse_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.relapse"), relapse_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.relapse"), relapse_notEvaluated_F);

        //The code bellow populates the retreat case table, failures row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.failures"), failure_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.failures"), failure_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.failures"), failure_notEvaluated_F);

        //The code bellow populates the retreat case table, default negative row
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.default"), default_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.default"), default_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.default"), default_notEvaluated_F);

        //The code bellow populates the retreat case table, patient type as others with smear positive diag exam
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.others"), otherp_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others"), otherp_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others"), otherp_notEvaluated_F);

        //The code bellow populates the new case table, patient type as others and smear negative diag exam.
        /*Task suspended because BDTEAM said that some changes may occour. Waiting definition.
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("manag.pulmonary.others")+"NEG", othern_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("manag.pulmonary.others")+"NEG", othern_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("manag.pulmonary.others")+"NEG", othern_notEvaluated_F);
        */

        //The code bellow populates the retreat case table, patient type as others with smear positive diag exam
        addValue(messages.get("manag.gender.male0"), messages.get("manag.gender.male"), messages.get("global.total"), total_total_M);
        addValue(messages.get("manag.gender.female0"), messages.get("manag.gender.female"), messages.get("global.total"), total_total_F);
        addValue(messages.get("manag.pulmonary.sum"), messages.get("global.total"), total_total_T);

        addValue(messages.get("manag.gender.male1"), messages.get("manag.gender.male"), messages.get("global.total"), total_smearNegative_M);
        addValue(messages.get("manag.gender.female1"), messages.get("manag.gender.female"), messages.get("global.total"), total_smearNegative_F);

        addValue(messages.get("manag.gender.male2"), messages.get("manag.gender.male"), messages.get("global.total"), total_smearPositive_M);
        addValue(messages.get("manag.gender.female2"), messages.get("manag.gender.female"), messages.get("global.total"), total_smearPositive_F);

        addValue(messages.get("manag.gender.male3"), messages.get("manag.gender.male"), messages.get("global.total"), total_died_M);
        addValue(messages.get("manag.gender.female3"), messages.get("manag.gender.female"), messages.get("global.total"), total_died_F);

        addValue(messages.get("manag.gender.male4"), messages.get("manag.gender.male"), messages.get("global.total"), total_failure_M);
        addValue(messages.get("manag.gender.female4"), messages.get("manag.gender.female"), messages.get("global.total"), total_failure_F);

        addValue(messages.get("manag.gender.male5"), messages.get("manag.gender.male"), messages.get("global.total"), total_defaulted_M);
        addValue(messages.get("manag.gender.female5"), messages.get("manag.gender.female"), messages.get("global.total"), total_defaulted_F);

        addValue(messages.get("manag.gender.male6"), messages.get("manag.gender.male"), messages.get("global.total"), total_transfOut_M);
        addValue(messages.get("manag.gender.female6"), messages.get("manag.gender.female"), messages.get("global.total"), total_transfOut_F);

        addValue(messages.get("manag.gender.male7"), messages.get("manag.gender.male"), messages.get("global.total"), total_otherOutcomes_M);
        addValue(messages.get("manag.gender.female7"), messages.get("manag.gender.female"), messages.get("global.total"), total_otherOutcomes_F);

        addValue(messages.get("manag.gender.male8"), messages.get("manag.gender.male"), messages.get("global.total"), total_notEvaluated_M);
        addValue(messages.get("manag.gender.female8"), messages.get("manag.gender.female"), messages.get("global.total"), total_notEvaluated_F);

    }

    private boolean isSmearPositive(MicroscopyResult micResult){
        return (micResult.equals(MicroscopyResult.PLUS) || micResult.equals(MicroscopyResult.PLUS2) || micResult.equals(MicroscopyResult.PLUS3) || micResult.equals(MicroscopyResult.PLUS4)
                || micResult.equals(MicroscopyResult.POSITIVE));
    }

    @Override
    protected String getHQLWhere(){
        return super.getHQLWhere() + " and c.patientType in (1,2,3,4,5) ";
    }

    public double getSputumConversionRate(){
        return 10000;
    }

    private void calculateTotalColumn(){
        //calculate total
        relapse_total_F = relapse_smearNegative_F + relapse_smearPositive_F + relapse_died_F + relapse_failure_F + relapse_defaulted_F +
                relapse_transfOut_F + relapse_otherOutcomes_F + relapse_notEvaluated_F;
        relapse_total_M = relapse_smearNegative_M + relapse_smearPositive_M + relapse_died_M + relapse_failure_M + relapse_defaulted_M +
                relapse_transfOut_M + relapse_otherOutcomes_M + relapse_notEvaluated_M;
        relapse_total_T = relapse_total_F + relapse_total_M;

        failure_total_F = failure_smearNegative_F + failure_smearPositive_F + failure_died_F + failure_failure_F + failure_defaulted_F +
                failure_transfOut_F + failure_otherOutcomes_F + failure_notEvaluated_F;
        failure_total_M = failure_smearNegative_M + failure_smearPositive_M + failure_died_M + failure_failure_M + failure_defaulted_M +
                failure_transfOut_M + failure_otherOutcomes_M + failure_notEvaluated_M;
        failure_total_T = failure_total_F + failure_total_M;

        default_total_F = default_smearNegative_F + default_smearPositive_F + default_died_F + default_failure_F + default_defaulted_F +
                default_transfOut_F + default_otherOutcomes_F + default_notEvaluated_F;
        default_total_M = default_smearNegative_M + default_smearPositive_M + default_died_M + default_failure_M + default_defaulted_M +
                default_transfOut_M + default_otherOutcomes_M + default_notEvaluated_M;
        default_total_T = default_total_F + default_total_M;

        otherp_total_F = otherp_smearNegative_F + otherp_smearPositive_F + otherp_died_F + otherp_failure_F + otherp_defaulted_F +
                otherp_transfOut_F + otherp_otherOutcomes_F + otherp_notEvaluated_F;
        otherp_total_M = otherp_smearNegative_M + otherp_smearPositive_M + otherp_died_M + otherp_failure_M + otherp_defaulted_M +
                otherp_transfOut_M + otherp_otherOutcomes_M + otherp_notEvaluated_M;
        otherp_total_T = otherp_total_F + otherp_total_M;

        /*Task Suspended. Waiting definition from BD team
        othern_total_F = othern_smearNegative_F + othern_smearPositive_F + othern_died_F + othern_failure_F + othern_defaulted_F +
                othern_transfOut_F + othern_otherOutcomes_F + othern_notEvaluated_F;
        othern_total_M = othern_smearNegative_M + othern_smearPositive_M + othern_died_M + othern_failure_M + othern_defaulted_M +
                othern_transfOut_M + othern_otherOutcomes_M + othern_notEvaluated_M;
        othern_total_T = othern_total_F + othern_total_M;
        */
    }

    private void calculateTotalRow(){
        total_total_M = relapse_total_M + failure_total_M + default_total_M + otherp_total_M;
        total_total_F = relapse_total_F + failure_total_F + default_total_F + otherp_total_F;
        total_total_T = relapse_total_T + failure_total_T + default_total_T + otherp_total_T;

        total_smearNegative_M = relapse_smearNegative_M + failure_smearNegative_M + default_smearNegative_M + otherp_smearNegative_M;
        total_smearNegative_F = relapse_smearNegative_F + failure_smearNegative_F + default_smearNegative_F + otherp_smearNegative_F;

        total_smearPositive_M = relapse_smearPositive_M + failure_smearPositive_M + default_smearPositive_M + otherp_smearPositive_M;
        total_smearPositive_F = relapse_smearPositive_F + failure_smearPositive_F + default_smearPositive_F + otherp_smearPositive_F;

        total_died_M = relapse_died_M + failure_died_M + default_died_M + otherp_died_M;
        total_died_F = relapse_died_F + failure_died_F + default_died_F + otherp_died_F;

        total_failure_M = relapse_failure_M + failure_failure_M + default_failure_M + otherp_failure_M;
        total_failure_F = relapse_failure_F + failure_failure_F + default_failure_F + otherp_failure_F;

        total_defaulted_M = relapse_defaulted_M + failure_defaulted_M + default_defaulted_M + otherp_defaulted_M;
        total_defaulted_F = relapse_defaulted_F + failure_defaulted_F + default_defaulted_F + otherp_defaulted_F;

        total_transfOut_M = relapse_transfOut_M + failure_transfOut_M + default_transfOut_M + otherp_transfOut_M;
        total_transfOut_F = relapse_transfOut_F + failure_transfOut_F + default_transfOut_F + otherp_transfOut_F;

        total_otherOutcomes_M = relapse_otherOutcomes_M + failure_otherOutcomes_M + default_otherOutcomes_M + otherp_otherOutcomes_M;
        total_otherOutcomes_F = relapse_otherOutcomes_F + failure_otherOutcomes_F + default_otherOutcomes_F + otherp_otherOutcomes_F;

        total_notEvaluated_M = relapse_notEvaluated_M + failure_notEvaluated_M + default_notEvaluated_M + otherp_notEvaluated_M;
        total_notEvaluated_F = relapse_notEvaluated_F + failure_notEvaluated_F + default_notEvaluated_F + otherp_notEvaluated_F;

    }
}
