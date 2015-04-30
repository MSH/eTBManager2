package org.msh.etbm.commons.apidoc.model;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by rmemoria on 29/4/15.
 */
public class ApiObject {
    private String name;
    private String type;
    private boolean notNull;
    private String description;
    private List<ApiObject> properties;
    private Class clazz;
    private Field field;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ApiObject> getProperties() {
        return properties;
    }

    public void setProperties(List<ApiObject> properties) {
        this.properties = properties;
    }
}
