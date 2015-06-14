package org.msh.tb.ng.cases;

import org.jboss.seam.annotations.Name;
import org.msh.etbm.services.cases.exams.MicroscopyServices;

/**
 * Created by rmemoria on 13/6/15.
 */
@Name("microscopyServicesNG")
public class MicroscopyServicesNG extends MicroscopyServices {
    @Override
    protected boolean isLaboratoryRequired() {
        return false;
    }
}
