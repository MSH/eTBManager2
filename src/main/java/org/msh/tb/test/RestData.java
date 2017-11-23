package org.msh.tb.test;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by ricardo on 23/11/14.
 */
@XmlRootElement(name="data")
public class RestData {
    private String name;
    private String middleName;
    private int age;
    private Integer prop1;
    private Date birthDate;

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @XmlElement
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @XmlElement
    public Integer getProp1() {
        return prop1;
    }

    public void setProp1(Integer prop1) {
        this.prop1 = prop1;
    }

    @XmlElement
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
