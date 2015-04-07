package org.msh.etbm.services.commons;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Created by rmemoria on 6/4/15.
 */
public class EntityUtils {

    /**
     * Return the entity class in use
     *
     * @return
     */
    public static Class getGenericEntityClass(Class clazz) {
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
}