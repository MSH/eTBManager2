package org.msh.validators;

import org.msh.validators.impl.BeanValidatorImpl;


/**
 * Created by rmemoria on 6/4/15.
 */
public class BeanValidator {

    /**
     * Validate the given object data using its constraints defined
     * in the object class
     * @param data the object to be validated
     * @return list of validation messages
     */
    public static MessagesList validate(Object data) {
        BeanValidatorImpl beanValidator = new BeanValidatorImpl();
        return beanValidator.validate(data);
    }

}
