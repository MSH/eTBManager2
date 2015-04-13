package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.transactionlog.mapping.LogInfo;
import org.msh.tb.SubstancesQuery;
import org.msh.tb.entities.ExamDST;
import org.msh.tb.entities.ExamDSTResult;
import org.msh.tb.entities.Substance;
import org.msh.tb.entities.enums.DstResult;
import org.msh.tb.misc.EntityActions;
import org.msh.validators.FacesMessagesBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle actions of a DST exams in a JSF page
 *
 * Created by rmemoria on 11/4/15.
 */
@Name("dstActions")
@LogInfo(roleName="EXAM_DST", entityClass=ExamDST.class)
public class DstActions extends LabExamActions<ExamDST> {

    private List<ExamDSTResult> items;

    @In(create=true)
    SubstancesQuery substances;

    @Override
    public String save() {
        ExamDST exam = getInstance();

        // remove NOTDONE and include new results
        for (ExamDSTResult ms: items) {
            ExamDSTResult res = exam.findResultBySubstance(ms.getSubstance());

            // add new results
            if (ms.getResult() != DstResult.NOTDONE) {
                if (res == null) {
                    exam.getResults().add(ms);
                }
                else {
                    res.setResult(ms.getResult());
                }
            }
            else {
                // remove undone results
                if (res != null) {
                    exam.getResults().remove(res);
                }
            }
        }

        // save the results
        return super.save();
    }


    @Override
    public String getControlPrefix() {
        return "dst";
    }

    @Override
    public FacesMessagesBinder bindFields() {
        return super.bindFields();
    }
    /**
     * Return the list of items of DST results to be edited
     * @return List of {@link ExamDSTResult} to post results
     */
    public List<ExamDSTResult> getItems() {
        if (items == null)
            createItems();

        return items;
    }


    /**
     * Create the items of the DST result to be edited
     */
    public void createItems() {
        items = new ArrayList<ExamDSTResult>();

        for (Substance s: substances.getDstSubstances()) {
            ExamDSTResult res = findResult(s);
            if (res == null) {
                res = new ExamDSTResult();
                res.setSubstance(s);
            }

            if (res.getResult() == null)
                res.setResult(DstResult.NOTDONE);

            items.add(res);
            res.setExam(getInstance());
        }
    }


    /**
     * Find an instance of the {@link ExamDSTResult} of the current case
     * by its {@link Substance} instance
     * @param s
     * @return
     */
    protected ExamDSTResult findResult(Substance s) {
        for (ExamDSTResult mr: getInstance().getResults()) {
            if (mr.getSubstance().equals(s)) {
                return mr;
            }
        }
        return null;
    }
}
