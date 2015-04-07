package org.msh.validators.impl;

import org.msh.validators.MessagesList;
import org.msh.validators.ValidationMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmemoria on 6/4/15.
 */
public class MessagesListImpl implements MessagesList {

    private List<ValidationMessage> messages;

    @Override
    public ValidationMessage add(String field, String msg) {
        return add(field, msg, null);
    }


    @Override
    public ValidationMessage add(String msg) {
        return add(null, msg);
    }


    @Override
    public ValidationMessage add(String field, String keymsg, Object[] args) {
        if (messages == null) {
            messages = new ArrayList<ValidationMessage>();
        }
        ValidationMessage msg = new ValidationMessage(field, keymsg, args);
        messages.add(msg);
        return msg;
    }


    @Override
    public List<ValidationMessage> getMessages() {
        return messages;
    }

    @Override
    public int size() {
        return messages != null? messages.size(): 0;
    }

    @Override
    public void clear() {
        messages = null;
    }

    @Override
    public ValidationMessage get(int index) {
        return messages != null? messages.get(index): null;
    }

    @Override
    public ValidationMessage getByField(String field) {
        if (messages == null) {
            return null;
        }

        for (ValidationMessage msg: messages) {
            if (field.equals(msg.getField())) {
                return msg;
            }
        }
        return null;
    }
}
