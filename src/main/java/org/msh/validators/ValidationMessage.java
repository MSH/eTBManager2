package org.msh.validators;

/**
 * Store information about a validation message. Usually generated
 * by the class {@link BeanValidator}
 *
 * Created by rmemoria on 6/4/15.
 */
public class ValidationMessage {
    // the field assigned to the message
    private String field;
    // the message key
    private String message;
    // arguments used to construct the final message
    private Object[] args;

    /**
     * Default constructor
     */
    public ValidationMessage() {
    }

    /**
     * Constructor with null field
     * @param message the validation message
     */
    public ValidationMessage(String message) {
        this.message = message;
    }

    public ValidationMessage(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public ValidationMessage(String field, String message, Object[] args) {
        this.field = field;
        this.message = message;
        this.args = args;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return (field != null? field + ": ": "") + message;
    }

    public Object[] getArgs() {
        return args;
    }
}
