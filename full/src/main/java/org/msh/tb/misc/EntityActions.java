package org.msh.tb.misc;

import org.msh.etbm.services.commons.EntityUtils;

/**
 * Abstract class used by many action classes to handle entity operations in the
 * JSF used interface
 *
 * Created by rmemoria on 6/4/15.
 */
public class EntityActions<E> {

    private E instance;
    private Class<E> entityClass;

    /**
     * Get the current instance managed by the action class
     * @return
     */
    public E getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    /**
     * Create a new instance to be handled by the action class
     * @return
     */
    protected E createInstance() {
        Class clazz = getEntityClass();
        if (clazz != null) {
            try {
                return (E)clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Clear the current instance and create a new one
     */
    public void clear() {
        instance = null;
    }

    /**
     * Return the entity class handled by the action
     * @return
     */
    public Class<E> getEntityClass() {
        if (entityClass == null) {
            entityClass = EntityUtils.getGenericEntityClass(getClass());
        }
        return entityClass;
    }
}
