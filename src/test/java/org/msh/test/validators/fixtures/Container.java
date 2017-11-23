package org.msh.test.validators.fixtures;

import org.hibernate.validator.NotNull;
import org.msh.validators.InnerValidation;

/**
 * Simple POJO to be validated in test cases
 * Created by rmemoria on 19/4/15.
 */
public class Container {
    @NotNull
    private String id;

    @NotNull
    @InnerValidation
    private TestForm form;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TestForm getForm() {
        return form;
    }

    public void setForm(TestForm form) {
        this.form = form;
    }
}
