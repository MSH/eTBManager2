package org.msh.etbm.services.commons;

import javax.persistence.Entity;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Created by rmemoria on 6/4/15.
 */
public class EntityUtils {

    /**
     * Return the class of the type declared in a generic class (example, entity class
     * declared in an EntityHome or EntityAction)
     *
     * @return the type declared in the given generic class
     */
    public static Class getDeclaredGenericType(Class clazz) {
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            if (paramType.getActualTypeArguments().length == 2) {
                if (paramType.getActualTypeArguments()[1] instanceof TypeVariable) {
                    throw new IllegalArgumentException("Could not guess entity class by reflection");
                } else {
                    return (Class)paramType.getActualTypeArguments()[1];
                }
            } else {
                return (Class) paramType.getActualTypeArguments()[0];
            }
        } else {
            throw new IllegalArgumentException("Could not guess entity class by reflection");
        }
    }


    /**
     * Given an entity class, it will return the root entity, i.e, the concrete entity class
     * used in the generic version of the system that the given class was specialized to.
     * Example: If a TbCaseNG is passed, the method returns TbCase
     * @param customClass the customized class to a country
     * @return the concrete entity class
     */
    public static Class getRootEntityClass(Class customClass) {
        Class entityClass = customClass;
        Class p = customClass.getSuperclass();
        if (p.getAnnotation(Entity.class) != null) {
            return getRootEntityClass(p);
        }
        else {
            return entityClass;
        }
    }

}