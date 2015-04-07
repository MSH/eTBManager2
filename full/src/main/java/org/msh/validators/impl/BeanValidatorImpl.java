package org.msh.validators.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.international.Messages;
import org.msh.validators.*;

import javax.persistence.Column;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the bean validator
 *
 * Created by rmemoria on 6/4/15.
 */
public class BeanValidatorImpl {

    /**
     * Validate the given object by its validation rules defined
     * in its class definition
     * @param obj the object to be validated
     * @return the list of ValidationMessages found, or null if no message was found
     */
    public MessagesList validate(Object obj) {
        if (obj == null) {
            return null;
        }

        Class clazz = obj.getClass();

        MessagesList msgs = new MessagesListImpl();

        validateFields(clazz, obj, msgs);

        return msgs;
    }

    /**
     * Validate all fields in the class and its parent classes
     * @param clazz the class to
     * @param obj the object containing the data to validate
     * @param msgs the list of validation messages
     */
    private void validateFields(Class clazz, Object obj, MessagesList msgs) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            validateField(field, obj, msgs);
        }
        clazz = clazz.getSuperclass();

        // if there are parent classes, then check constraints there
        if ((clazz != null) && (clazz != Object.class)) {
            validateFields(clazz, obj, msgs);
        }
    }

    /**
     * Validate a specific field of the object
     * @param field
     * @param obj
     * @param msgs
     */
    private void validateField(Field field, Object obj, MessagesList msgs) {
        String fname = field.getName();
        Object value = null;
        try {
            value = PropertyUtils.getProperty(obj, fname);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!validateNotNull(field, value, msgs)) {
            return;
        }

        if (!validateSize(field, value, msgs)) {
            return;
        }

        if (!validateColumn(field, value, msgs)) {
            return;
        }

        if (!validatePast(field, value, msgs)) {
            return;
        }
        validateFuture(field, value, msgs);
    }

    /**
     * Validate not null fields
     * @param field
     * @param value
     * @param msgs
     * @return
     */
    protected boolean validateNotNull(Field field, Object value, MessagesList msgs) {
        NotNull notNull = field.getAnnotation(NotNull.class);
        if (notNull == null) {
            return true;
        }

        if (value == null) {
            msgs.add(field.getName(), "javax.faces.component.UIInput.REQUIRED");
            return false;
        }
        return true;
    }

    /**
     * Validate the size of a string
     * @param field
     * @param value
     * @param msgs
     * @return
     */
    protected boolean validateSize(Field field, Object value, MessagesList msgs) {
        if (value == null) {
            return true;
        }

        Size size = field.getAnnotation(Size.class);
        if (size == null) {
            return true;
        }

        if (value instanceof String) {
            String s = value.toString();
            // check if value is under the minimum length
            if (s.length() < size.min()) {
                String msg = "javax.faces.validator.LengthValidator.MINIMUM";
                Object[] args = new Object[1];
                args[0] = size.min();
                msgs.add(field.getName(), msg, args);
                return false;
            }

            // check if message is over the maximum length
            if (s.length() > size.max()) {
                String msg = "javax.faces.validator.LengthValidator.MAXIMUM";
                Object[] args = new Object[1];
                args[0] = size.max();
                msgs.add(field.getName(), msg, args);
                return false;
            }
        }

        return true;
    }

    /**
     * Validate fields with the column annotation
     * @param field
     * @param value
     * @param msgs
     * @return
     */
    protected boolean validateColumn(Field field, Object value, MessagesList msgs) {
        if (value == null) {
            return true;
        }

        Column col = field.getAnnotation(Column.class);
        if (col == null) {
            return true;
        }

        if ((col.length() > 0) && (value instanceof String)) {
            if (value.toString().length() > col.length()) {
                String msg = "javax.faces.validator.LengthValidator.MAXIMUM";
                Object[] args = new Object[1];
                args[0] = col.length();
                msgs.add(field.getName(), msg, args);
                return false;
            }
        }

        return true;
    }


    /**
     * Validate date fields to make sure that it's a past date
     * @param field
     * @param value
     * @param msgs
     * @return
     */
    protected boolean validatePast(Field field, Object value, MessagesList msgs) {
        if (value == null) {
            return true;
        }

        Past past = field.getAnnotation(Past.class);
        if (past == null) {
            return true;
        }

        if (!(value instanceof Date)) {
            throw new RuntimeException("Field is not of Date type");
        }

        Date dt = (Date) value;

        if (!(dt.before(new Date()))) {
            msgs.add(field.getName(), "validator.past");
            return false;
        }

        return true;
    }

    /**
     * Validate date fields to make sure that it's a future date
     * @param field
     * @param value
     * @param msgs
     * @return
     */
    protected boolean validateFuture(Field field, Object value, MessagesList msgs) {
        if (value == null) {
            return true;
        }

        Future future = field.getAnnotation(Future.class);
        if (future == null) {
            return true;
        }

        if (!(value instanceof Date)) {
            throw new RuntimeException("Field is not of Date type");
        }

        Date dt = (Date) value;

        if (!(dt.after(new Date()))) {
            msgs.add(field.getName(), "validator.future");
            return false;
        }

        return dt.after(new Date());
    }

}
