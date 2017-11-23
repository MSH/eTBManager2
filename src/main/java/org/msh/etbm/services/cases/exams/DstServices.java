package org.msh.etbm.services.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.enums.DstResult;
import org.msh.validators.MessagesList;

/**
 * Exposes services to handle DST exam results
 * Created by rmemoria on 10/4/15.
 */
@Name("dstServices")
public class DstServices extends LabExamServices<ExamDST> {

    @Override
    public MessagesList save(ExamDST entity) {
        prepareFields(entity);

        MessagesList lst = super.save(entity);

        // successfully saved ?
        if (lst == null) {
            // delete NOT DONE results
            App.getEntityManager().createQuery("delete from ExamDSTResult where result = 0 and exam.id = :id")
                    .setParameter("id", entity.getId())
                    .executeUpdate();
        }

        return lst;
    }


    @Override
    public MessagesList validate(ExamDST exam) {
        MessagesList lst = super.validate(exam);

        // check if there is any result
        boolean hasResult = false;
        if (exam.getResults() != null) {
            for (ExamDSTResult res: exam.getResults()) {
                if ((res.getResult() != null) && (res.getResult() != DstResult.NOTDONE)) {
                    hasResult = true;
                    break;
                }
            }
        }

        if (!hasResult) {
            lst.add("DSTExam.msg03");
        }

        return lst;
    }

    /**
     * Prepare the results to be saved, removing NOT DONE results and counting
     * the number of results
     * @param exam
     */
    protected void prepareFields(ExamDST exam) {
        // count the results
        int numResistants = 0;
        int numContaminated = 0;
        int numSusceptibles = 0;

        if (exam.getResults() != null) {
            int index = 0;
            while (index < exam.getResults().size()) {
                ExamDSTResult res = exam.getResults().get(index);
                if (res.getResult() == DstResult.NOTDONE) {
                    exam.getResults().remove(res);
                }
                else {
                    switch (res.getResult()) {
                        case RESISTANT:
                            numResistants++;
                            break;
                        case CONTAMINATED:
                            numContaminated++;
                            break;
                        case SUSCEPTIBLE:
                            numSusceptibles++;
                            break;
                        default:
                    }
                    index++;
                }
            }
        }

        exam.setNumContaminated(numContaminated);
        exam.setNumResistant(numResistants);
        exam.setNumSusceptible(numSusceptibles);
    }
}
