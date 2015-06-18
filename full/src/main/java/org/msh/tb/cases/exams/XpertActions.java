package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;
import org.msh.validators.FacesMessagesBinder;

/**
 * Created by rmemoria on 10/4/15.
 */
@Name("xpertActions")
@LogInfo(roleName="EXAM_XPERT", entityClass=ExamXpert.class)
public class XpertActions extends LabExamActions<ExamXpert> {

    private static final XpertResult results[] = {
            XpertResult.TB_DETECTED,
            XpertResult.TB_NOT_DETECTED,
            XpertResult.INVALID_NORESULT_ERROR
    };

    @Override
    public String getControlPrefix() {
        return "xp";
    }


    /**
     * Bind fields to UI components
     * @return
     */
    public FacesMessagesBinder bindFields() {
        return super.bindFields()
                .bind("xprifres", "rifResult")
                .bind("edtxpreason", "reasonTest");
    }

    /**
     * @return
     */
    public XpertResult[] getGenexpertResults() {
        return results;
    }

    /**
     * @return
     */
    public XpertRifResult[] getRifResults() {
        return XpertRifResult.values();
    }
}
