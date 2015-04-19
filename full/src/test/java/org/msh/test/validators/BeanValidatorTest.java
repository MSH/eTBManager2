package org.msh.test.validators;

import org.junit.Test;
import org.msh.test.validators.fixtures.Container;
import org.msh.test.validators.fixtures.TestForm;
import org.msh.utils.date.DateUtils;
import org.msh.validators.BeanValidator;
import org.msh.validators.MessagesList;
import org.msh.validators.ValidationMessage;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by rmemoria on 6/4/15.
 */
public class BeanValidatorTest {

    @Test
    public void test() {
        BeanValidator b = new BeanValidator();

        // no errors must be found
        TestForm frm = new TestForm();
        frm.setEmail("rmemoria@gmail.com");
        frm.setFutureDate(DateUtils.incMonths(new Date(), 1));
        frm.setPastDate(DateUtils.incMonths(new Date(), -1));
        frm.setName("Ricardo");

        MessagesList lst = b.validate(frm);
        assertEquals(0, lst.size());

        // email is required, but its value is null
        frm.setEmail(null);
        lst = b.validate(frm);
        assertEquals(1, lst.size());
        assertEquals("email", lst.get(0).getField());
        assertEquals("javax.faces.component.UIInput.REQUIRED", lst.get(0).getMessage());
        frm.setEmail("rmem@test");

        // a date must be in the future, but it's a past date
        frm.setFutureDate(frm.getPastDate());
        lst = b.validate(frm);
        assertNotNull(lst);
        assertEquals(1, lst.size());
        assertEquals("futureDate", lst.get(0).getField());
        assertEquals("validator.future", lst.get(0).getMessage());

        // and now the past date is in the future
        frm.setPastDate(DateUtils.incMonths(new Date(), 1));
        lst = b.validate(frm);
        assertNotNull(lst);
        assertEquals(2, lst.size());
        ValidationMessage msg = lst.getByField("pastDate");
        assertNotNull(msg);
        assertEquals("validator.past", msg.getMessage());


        frm.setName("This is a long name that must generate an error");
        lst = b.validate(frm);
        assertEquals(3, lst.size());
        msg = lst.getByField("name");
        assertNotNull(msg);
        assertEquals("name", msg.getField());
        assertEquals("javax.faces.validator.LengthValidator.MAXIMUM", msg.getMessage());
    }


    /**
     * Test validation of objects inside objects using the @InnerValidation annotation
     */
    @Test
    public void innerTest() {
        Container c = new Container();
        c.setId("myid");
        MessagesList lst = BeanValidator.validate(c);
        assertNotNull(lst);
        assertEquals(1, lst.size());
        ValidationMessage msg = lst.getByField("form");
        assertNotNull(msg);

        TestForm frm = new TestForm();
        c.setForm(frm);
        lst = BeanValidator.validate(c);
        assertNotNull(lst);
        assertEquals(1, lst.size());

        msg = lst.getByField("form.email");
        assertNotNull(msg);
        assertEquals("form.email", msg.getField());

        frm.setFutureDate(new Date());
        lst = BeanValidator.validate(c);
        assertNotNull(lst);
        assertEquals(2, lst.size());
        msg = lst.getByField("form.futureDate");
        assertEquals("form.futureDate", msg.getField());
    }
}
