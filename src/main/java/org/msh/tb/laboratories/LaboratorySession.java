package org.msh.tb.laboratories;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.App;
import org.msh.tb.entities.Laboratory;
import org.msh.tb.login.SessionData;

/**
 * Store user session data (in-memory data available along user session) about
 * the laboratory
 * Created by ricardo on 14/08/14.
 */
@Name("laboratorySession")
public class LaboratorySession {

    private Laboratory laboratory;

    /**
     * Return the laboratory ID selected in the user session
     * @return Integer value
     */
    public Integer getLaboratoryId() {
        return (Integer) SessionData.instance().getValue("labid");
    }

    /**
     * Change the laboratory ID of the user session
     * @param id new ID
     */
    public void setLaboratoryId(Integer id) {
        SessionData.instance().setValue("labid", id);
        laboratory = null;
    }

    /**
     * Return the laboratory according to the selected laboratory ID
     * @return instance of {@link org.msh.tb.entities.Laboratory}
     */
    public Laboratory getLaboratory() {
        if (laboratory == null) {
            Integer id = getLaboratoryId();
            if (id == null) {
                laboratory = App.getEntityManager().find(Laboratory.class, id);
            }
        }
        return laboratory;
    }

    /**
     * Return the instance of the component
     * @return instance LaboratorySession
     */
    public static LaboratorySession instance() {
        return (LaboratorySession) Component.getInstance("laboratorySession");
    }
}
