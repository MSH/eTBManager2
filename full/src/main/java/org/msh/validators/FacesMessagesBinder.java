package org.msh.validators;

import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bind validation messages to its UI controls in a JSF application
 *
 * Created by rmemoria on 6/4/15.
 */
public class FacesMessagesBinder {

    private Map<String, String> controls = new HashMap<String, String>();

    /**
     * Bind a control name to a field
     * @param ctrlname
     * @param field
     * @return
     */
    public FacesMessagesBinder bind(String ctrlname, String field) {
        controls.put(ctrlname, field);
        return this;
    }

    /**
     * Publish the respective message to the control related to a field.
     * The message will be displayed to the control, if found
     * @param msgs
     */
    public void publish(List<ValidationMessage> msgs) {
        for (int i = 0; i < msgs.size(); i++) {
            ValidationMessage msg = msgs.get(i);

            if (msg.getField() != null) {
                String ctrlname = controlByField(msg.getField());

                String txt = Messages.instance().get(msg.getMessage());

                // check if there are extra arguments to be formated
                if ((msg.getArgs() != null) && (msg.getArgs().length > 0)) {
                    MessageFormat mf = new MessageFormat(txt);
                    txt = mf.format(msg.getArgs());
                }

                FacesMessages.instance().addToControl(ctrlname, txt);
            }
        }
    }

    /**
     * Return the name of the control assgined to the field
     * @param field
     * @return
     */
    public String controlByField(String field) {
        for (String ctrl: controls.keySet()) {
            String f = controls.get(ctrl);
            if (f.equals(field)) {
                return ctrl;
            }
        }
        return null;
    }
}
