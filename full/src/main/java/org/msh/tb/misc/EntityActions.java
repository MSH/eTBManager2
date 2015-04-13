package org.msh.tb.misc;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.msh.etbm.services.ServiceDiscovery;
import org.msh.etbm.services.commons.DAOServices;
import org.msh.etbm.services.commons.EntityUtils;
import org.msh.etbm.transactionlog.ActionTX;
import org.msh.etbm.transactionlog.mapping.LogInfo;
import org.msh.tb.application.App;
import org.msh.tb.entities.enums.RoleAction;
import org.msh.validators.FacesMessagesBinder;
import org.msh.validators.MessagesList;

import javax.persistence.EntityManager;

/**
 * Abstract class used by many action classes to handle entity operations in the
 * JSF used interface
 *
 * Created by rmemoria on 6/4/15.
 */
public abstract class EntityActions<E> {

    // store the instance
    private E instance;

    // store the class of the instance (for performance issues)
    private Class<E> entityClass;

    // the DAO service related to the action class
    private DAOServices services;

    // responsible for storing transaction logs
    private ActionTX atx;

    // transaction log status (enabled or disabled)
    private boolean txLogEnabled = true;

    private boolean showMessages = true;


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
        beginTxLog();
    }


    /**
     * Return the entity ID
     * @return the ID of the entity, or null if no entity is managed
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


    /**
     * Return true if the instance is an existing entity managed by
     * the entity manager
     * @return boolean value
     */
    public boolean isManaged() {
        if (instance == null) {
            return false;
        }

        return App.getEntityManager().contains(instance);
    }


    /**
     * Delete the entity
     */
    @Transactional
    public String delete() {
        if (!isManaged()) {
            throw new RuntimeException("Not possible to delete an unmaneged entity");
        }

        // start the transaction log
        beginTxLog(getEventName(), RoleAction.DELETE);

        // delete the entity
        getServices().delete(getInstance());

        // save the transaction log
        endTxLog();

        showDeleteMessage();

        return "deleted";
    }


    /**
     * Save the changes to the entity
     * @return
     */
    @Transactional
    public String save() {
        boolean bnew = !isManaged();

        // if not created, it'll be created automatically (example, new entities)
        beginTxLog();

        MessagesList lst = getServices().save(getInstance());

        if (lst != null) {
            bindFields().publish(lst.getMessages());
            return "validation-error";
        }

        // save the transaction log
        endTxLog();

        // display success message
        if (bnew) {
            showCreateMessage();
        }
        else {
            showUpdateMessage();
        }

        return "saved";
    }

    /**
     * Create an instance of the FacesMessagesBinder containing the links
     * between the control names and the fields
     * @return
     */
    public abstract FacesMessagesBinder bindFields();

    /**
     * Start a new transaction log under a specific event name and role action
     * @param eventName
     * @param action
     * @return
     */
    public ActionTX beginTxLog(String eventName, RoleAction action) {
        // is disabled ?
        if (!txLogEnabled) {
            return null;
        }

        atx = ActionTX.begin(eventName, getInstance(), action);

        return atx;
    }


    /**
     * Start transaction recording of the entity being edited
     */
    public ActionTX beginTxLog() {
        // transaction is already active ?
        if (atx != null) {
            return atx;
        }

        RoleAction ra = isManaged()? RoleAction.EDIT: RoleAction.NEW;
        return beginTxLog(getEventName(), ra);
    }


    /**
     * Save the transaction log
     */
    public void endTxLog() {
        if (atx != null) {
            atx.end();
            atx = null;
        }
    }

    /**
     * Get the action tx related to the transaction log
     * @return
     */
    public ActionTX getActionTX() {
        return atx;
    }

    /**
     * Return the event name being recorded by the action in the transaction log table.
     * The event name is taken from the {@link LogInfo} declared in this action class
     * @return
     */
    protected String getEventName() {
        Class clazz = getClass();

        while (clazz != null) {
            LogInfo logInfo = (LogInfo)clazz.getAnnotation(LogInfo.class);
            if (logInfo != null) {
                return logInfo.roleName();
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Check if log is enabled or disabled
     * @return
     */
    public boolean isTxLogEnabled() {
        return txLogEnabled;
    }

    /**
     * Enable or disable the recording of transaction in the action. Default is true
     * @param enableTxLog
     */
    public void setTxLogEnabled(boolean enableTxLog) {
        this.txLogEnabled = enableTxLog;
        if (enableTxLog) {
            beginTxLog();
        }
        else {
            atx = null;
        }
    }

    /**
     * Show in the rendered page a message that the entity was successfully deleted
     */
    protected void showDeleteMessage() {
        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_deleted");
    }

    /**
     * Show in the rendered page a message that the entity was successfully created
     */
    protected void showCreateMessage() {
        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_created");
    }

    /**
     * Show in the rendered page a message that the entity was successfully updated
     */
    protected void showUpdateMessage() {
        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "default.entity_updated");
    }

    public boolean isShowMessages() {
        return showMessages;
    }

    public void setShowMessages(boolean showMessages) {
        this.showMessages = showMessages;
    }
}
