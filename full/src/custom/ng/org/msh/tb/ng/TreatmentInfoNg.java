package org.msh.tb.ng;

import org.msh.tb.cases.treatment.TreatmentInfo;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.XpertResult;
import org.msh.tb.entities.enums.XpertRifResult;

/**
 * Created by rmemoria on 25/3/15.
 */
public class TreatmentInfoNg extends TreatmentInfo {

    private XpertResult xpertResult;
    private XpertRifResult xpertRifResult;
    private CultureResult cultureResult;
    private MicroscopyResult microscopyResult;

    public XpertResult getXpertResult() {
        return xpertResult;
    }

    public void setXpertResult(XpertResult xpertResult) {
        this.xpertResult = xpertResult;
    }

    public XpertRifResult getXpertRifResult() {
        return xpertRifResult;
    }

    public void setXpertRifResult(XpertRifResult xpertRifResult) {
        this.xpertRifResult = xpertRifResult;
    }

    public CultureResult getCultureResult() {
        return cultureResult;
    }

    public void setCultureResult(CultureResult cultureResult) {
        this.cultureResult = cultureResult;
    }

    public MicroscopyResult getMicroscopyResult() {
        return microscopyResult;
    }

    public void setMicroscopyResult(MicroscopyResult microscopyResult) {
        this.microscopyResult = microscopyResult;
    }
}
