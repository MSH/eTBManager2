package org.msh.tb.misc;

import org.apache.commons.beanutils.PropertyUtils;
import org.msh.etbm.services.ServiceDiscovery;
import org.msh.etbm.services.commons.DAOServices;
import org.msh.etbm.services.commons.EntityUtils;
import org.msh.tb.application.App;

/**
 * Abstract class used by many action classes to handle entity operations in the
 * JSF used interface
 *
 * Created by rmemoria on 6/4/15.
 */
public class EntityActions<E> {

    private E instance;
    private Class<E> entityClass;
    private DAOServices services;

    /**
     * Get the current instance managed by the action class
     * @return
     */
    public E getInstance() {
        if (instance == null) {
            instance = createInstance();
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


    /**
     * Return the DAO services assigned to the action class
     * @return instance of DAOServices
     */
    public DAOServices getServices() {
        if (services == null) {
            ServiceDiscovery discovery = (ServiceDiscovery)App.getComponent("serviceDiscovery");
            services = discovery.getByEntityClass(getEntityClass());
        }
        return services;
    }


    /**
     * Set the ID of the entity being handled
     * @param id
     */
    public void setId(Object id) {
        instance = (E)getServices().find(id);
        if (instance == null) {
            throw new RuntimeException("Entity not found. ID=" + id + " class=" + getEntityClass());
        }
    }

    /**
     * Return the entity ID
     * @return
     */
    public Object getId() {
        if (instance == null) {
            return null;
        }

        try {
            return PropertyUtils.getProperty(instance, "id");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
