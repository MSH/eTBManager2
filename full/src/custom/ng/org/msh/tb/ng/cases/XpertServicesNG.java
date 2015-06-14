package org.msh.tb.ng.cases;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.services.cases.exams.XpertServices;

/**
 * Created by rmemoria on 13/6/15.
 */
@Name("xpertServicesNG")
public class XpertServicesNG extends XpertServices {
    @Override
    protected boolean isLaboratoryRequired() {
        return false;
    }
}
