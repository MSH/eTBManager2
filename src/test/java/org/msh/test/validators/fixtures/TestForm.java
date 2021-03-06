package org.msh.test.validators.fixtures;


import org.hibernate.validator.Future;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Past;
import org.hibernate.validator.Size;

import javax.persistence.Column;
import java.util.Date;

/**
 * Created by rmemoria on 6/4/15.
 */
public class TestForm {

    private static final long serialVersionUID = 1514632458011926044L;

    @NotNull
    @Column(length = 20)
    private String email;

    @Size(min = 2, max = 10)
    private String name;

    @Past
    private Date pastDate;

    @Future
    private Date futureDate;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getPastDate() {
        return pastDate;
    }

    public void setPastDate(Date pastDate) {
        this.pastDate = pastDate;
    }

    public Date getFutureDate() {
        return futureDate;
    }

    public void setFutureDate(Date futureDate) {
        this.futureDate = futureDate;
    }
}
