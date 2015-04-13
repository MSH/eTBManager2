package org.msh.tb.cases.exams;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.ExamCulture;
import org.msh.tb.misc.EntityActions;
import org.msh.validators.FacesMessagesBinder;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 10/4/15.
 */
@Name("cultureActions")
@LogInfo(roleName="EXAM_CULTURE", entityClass=ExamCulture.class)
public class CultureActions extends LabExamActions<ExamCulture> {

    private List<SelectItem> numColonies;

    @Override
    public String getControlPrefix() {
        return "cul";
    }


    /**
     * Bind fields to UI components
     * @return
     */
    public FacesMessagesBinder bindFields() {
        return super.bindFields()
                .bind("culres", "result")
                .bind("culnoc", "numberOfColonies");
    }

    /**
     * Create list of NumColonies of required count
     * */
    public List<SelectItem> getNumColonies(int count) {
        if (numColonies == null) {
            numColonies = new ArrayList<SelectItem>();

            SelectItem item = new SelectItem();
            item.setLabel("-");
            numColonies.add(item);

            for (int i = 1; i <= count; i++) {
                item = new SelectItem();
                item.setLabel(Integer.toString(i));
                item.setValue(i);
                numColonies.add(item);
            }
        }
        return numColonies;
    }

}
