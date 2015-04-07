package org.msh.validators;

import java.util.List;

/**
 * Implemented by classes that want to store validation messages
 *
 * Created by rmemoria on 6/4/15.
 */
public interface MessagesList {
    /**
     * Add a message to a specific field
     * @param field
     * @param msg
     */
    ValidationMessage add(String field, String msg);

    /**
     * Add a generic message
     * @param msg
     */
    ValidationMessage add(String msg);

    /**
     * the arguments to compound the message in the specific language, considering that
     * the message is just a key to be used in a list of messages
     * @param field name of the field
     * @param msg the message
     * @param args list of arguments in an object format
     * @return
     */
    ValidationMessage add(String field, String msg, Object[] args);

    /**
     * Return the list of messages in the container
     * @return list of messages, or null if there is no message
     */
    List<ValidationMessage> getMessages();

    /**
     * Return the number of messages
     * @return int value
     */
    int size();

    /**
     * Remove all messages in the list
     */
    void clear();

    /**
     * Return a specific message in a give position in the list
     * @param index 0-based index of the message in the list
     * @return
     */
    ValidationMessage get(int index);

    /**
     * Find a message by its fields
     * @param field
     * @return
     */
    ValidationMessage getByField(String field);
}
