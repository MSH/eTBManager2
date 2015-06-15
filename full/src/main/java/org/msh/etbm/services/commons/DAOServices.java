package org.msh.etbm.services.commons;

import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.App;
import org.msh.validators.BeanValidator;
import org.msh.validators.MessagesList;

import javax.persistence.EntityManager;

/**
 * Created by rmemoria on 6/4/15.
 */
public abstract class DAOServices<E> {


    private Class<E> entityClass;

    /**
     * Validae an object entity
     * @param entity
     * @return
     */
    public MessagesList validate(E entity) {
        MessagesList lst = BeanValidator.validate(entity);

        return lst;
    }

    /**
     * Save an entity. If the entity is not managed, it'll be inserted, otherwise its
     * information will be updated. The entity is validated, and in case of errors, the
     * messages are returned and the object is not saved
     * @param entity the entity to be saved
     * @return list of validation messages
     */
    @Transactional
    public MessagesList save(E entity) {
        MessagesList msgs = validate(entity);

        if (msgs.size() > 0) {
            return msgs;
        }

        EntityManager em = App.getEntityManager();

        em.persist(entity);
        em.flush();

        afterSaved(entity);

        return null;
    }

    /**
     * Called after the entity is successfully saved
     * @param entity
     */
    protected void afterSaved(E entity) {
        // do nothing
    }

    /**
     * Called after the entity is successfully deleted
     * @param entity
     */
    protected void afterDeleted(E entity) {
        // do nothing
    }

    /**
     * Delete an entity from the database
     * @param entity
     */
    @Transactional
    public void delete(E entity) {
        EntityManager em = App.getEntityManager();
        entity = em.merge(entity);
        em.remove(entity);
        em.flush();

        afterDeleted(entity);
    }

    /**
     * Delete an entity by its ID
     * @param id
     */
    @Transactional
    protected void deleteById(Object id) {
        EntityManager em = App.getEntityManager();
        E entity = em.find(getEntityClass(), id);
        delete(entity);
    }


    /**
     * Return the entity class in use
     * @return
     */
    protected Class<E> getEntityClass() {
        if (entityClass == null) {
            entityClass = EntityUtils.getDeclaredGenericType(getClass());
        }
        return entityClass;
    }

    /**
     * Return the entity by its ID
     * @param id the entity ID
     * @return the entity
     */
    public E find(Object id) {
        return App.getEntityManager().find(getEntityClass(), id);
    }

    /**
     * Create query object to return a list of entities
     * @return instance of {@link EntityQuery}
     */
    public EntityQuery createQuery() {
        return new EntityQuery(getEntityClass());
    }
}
