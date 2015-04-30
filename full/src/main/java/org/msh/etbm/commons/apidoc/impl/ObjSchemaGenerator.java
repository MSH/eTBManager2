package org.msh.etbm.commons.apidoc.impl;


import org.msh.etbm.commons.apidoc.annotations.ApiDocField;
import org.msh.etbm.commons.apidoc.model.ApiObject;
import org.msh.tb.application.SysStartup;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by rmemoria on 30/4/15.
 */
public class ObjSchemaGenerator {

    private List<Class> scannedClasses = new ArrayList<Class>();

    /**
     * Generate api documentation meta-data about the class type
     * @param clazz
     * @return
     */
    public ApiObject generate(Class clazz) {
        scannedClasses.clear();

        ApiObject schema = new ApiObject();

        // check if it's a primitive type
        if (!isComplexObject(clazz)) {
            schema.setType(clazz.getSimpleName());
            return schema;
        }

        List<ApiObject> properties = new ArrayList<ApiObject>();

        scanClass(clazz, properties, true);

        schema.setType("Object");
        if (properties.size() > 0) {
            schema.setProperties(properties);
        }

        return schema;
    }

    /**
     * Return true if the class is a complex object type, or false if it's a
     * primitive, string or date type
     * @param clazz the type to be tested
     * @return true if is a complex type
     */
    public static boolean isComplexObject(Class clazz) {
        if (clazz.isPrimitive() ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                String.class.isAssignableFrom(clazz) ||
                Boolean.class.isAssignableFrom(clazz)) {
            return false;
        }

        return true;
    }

    /**
     * Scan the class
     * @param clazz
     * @param properties
     */
    protected void scanClass(Class clazz, List<ApiObject> properties, boolean root) {
        if (!isComplexObject(clazz)) {
            return;
        }

        // avoid infinite loop
        if (root) {
            if (scannedClasses.contains(clazz)) {
                return;
            }
            scannedClasses.add(clazz);
        }

        // just to avoid NPE
        if (clazz == Object.class) {
            return;
        }

        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(clazz);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        PropertyDescriptor[] props = info.getPropertyDescriptors();

        for (PropertyDescriptor prop: props) {
            String pname = prop.getName();

            if ("class".equals(pname)) {
                continue;
            }

            // get the field
            Field field = null;
            try {
                field = clazz.getDeclaredField(pname);
            } catch (NoSuchFieldException e) {
                System.out.println("No field found for property " + pname);
            }

            String type = prop.getPropertyType().getSimpleName();

            ApiObject doc = new ApiObject();
            doc.setName(pname);
            doc.setType(type);

            // try to find information about the field
            if (field != null) {
                checkFieldAnnotations(field, doc);

                // check if type is list, map or another object
                checkComplexTypes(prop, field, doc);
            }

            properties.add(doc);
        }

        Class superclass = clazz.getSuperclass();
        if (superclass != Object.class) {
            scanClass(superclass, properties, false);
        }
    }

    /**
     * Search for information contained in the field by using annotations
     * @param field
     * @param schema
     */
    protected void checkFieldAnnotations(Field field, ApiObject schema) {
        ApiDocField docfield = (ApiDocField) field.getAnnotation(ApiDocField.class);

        if (docfield != null) {
            String desc = docfield.description();
            schema.setDescription(desc);
        }

        for (Annotation anot: field.getDeclaredAnnotations()) {
            if ("NotNull".equals(anot.getClass().getSimpleName())) {
                schema.setNotNull(true);
                break;
            }
        }
    }

    /**
     * Check if property is a list, a map or another object. If so, scan the properties of the
     * parametrized value as well
     * @param prop
     * @param field
     * @param schema
     * @return
     */
    protected boolean checkComplexTypes(PropertyDescriptor prop, Field field, ApiObject schema) {
        Class ptype = prop.getPropertyType();

        if (Collection.class.isAssignableFrom(ptype)) {
            scanCollection(prop, field, schema);
            return true;
        }

        if (Map.class.isAssignableFrom(ptype)) {
            scanMap(prop, field, schema);
            return true;
        }

        // if is not a complext type, so finish it
        if (!isComplexObject(ptype)) {
            return false;
        }

        // is an object type

        schema.setType("Object");
        List<ApiObject> props = new ArrayList<ApiObject>();
        schema.setProperties(props);

        scanClass(ptype, props, true);

        return true;
    }

    protected void scanCollection(PropertyDescriptor prop, Field field, ApiObject schema) {
        // try to find the type handled by the collection
        Type tp = field.getGenericType();
        if (!(tp instanceof ParameterizedType)) {
            schema.setType("Unknown");
            return;
        }

        ParameterizedType ptype = (ParameterizedType)tp;
        Class listType = (Class)ptype.getActualTypeArguments()[0];

        schema.setType("Array");

        List<ApiObject> props = new ArrayList<ApiObject>();
        schema.setProperties(props);

        scanClass(listType, props, true);
    }


    protected void scanMap(PropertyDescriptor prop, Field field, ApiObject schema) {
        schema.setType("Map");
    }
}
