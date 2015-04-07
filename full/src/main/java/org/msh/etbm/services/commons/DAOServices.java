package org.msh.etbm.services.commons;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.App;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.validators.BeanValidator;
import org.msh.validators.MessagesList;

import javax.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

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

        return null;
    }

    /**
     * Delete an entity from the database
     * @param entity
     */
    @Transactional
    public void delete(Object entity) {
        EntityManager em = App.getEntityManager();
        entity = em.merge(entity);
        em.remove(entity);
        em.flush();
    }

    @Transactional
    protected void deleteById(Object id) {
        EntityManager em = App.getEntityManager();
        E entity = em.find(getEntityClass(), id);
        em.remove(entity);
        em.flush();
    }


    /**
     * Return the entity class in use
     * @return
     */
    protected Class<E> getEntityClass() {
        if (entityClass == null) {
            entityClass = EntityUtils.getGenericEntityClass(getClass());
        }
        return entityClass;
    }

    /**
     * Return the entity by its ID
     * @param id the entity ID
     * @return the entity
     */
    public E getEntity(Object id) {
        return App.getEntityManager().find(getEntityClass(), id);
    }
}
