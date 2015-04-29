package org.msh.etbm.services.cases.exams;

import org.msh.etbm.services.commons.DAOServices;
import org.msh.tb.application.App;
import org.msh.tb.entities.LaboratoryExam;
import org.msh.tb.entities.TbCase;
import org.msh.validators.MessagesList;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Asbtract class that exposes common services to laboratory exams, like CRUD operations
 * Created by rmemoria on 6/4/15.
 */
public abstract class LabExamServices<E> extends DAOServices<E> {

    /**
     * Validae a laboratory exam
     * @param entity
     * @return
     */
    @Override
    public MessagesList validate(E entity) {
        MessagesList lst = super.validate(entity);

        LaboratoryExam exam = (LaboratoryExam) entity;
        // check if release date is after the date collected
        if ((exam.getDateCollected() != null) && (exam.getDateRelease() != null) &&
            (exam.getDateRelease().before(exam.getDateCollected())))
        {
            lst.add("dateRelease", "cases.exams.datereleasebeforecol");
        }

        // by default, laboratory is required, but not marked in the model since
        // it may not be desirable in some exams
        if (isLaboratoryRequired() && exam.getLaboratory() == null) {
            lst.addRequired("laboratory");
        }

        return lst;
    }


    /**
     * Return true if the laboratory is a required field and must be validated
     * @return
     */
    protected boolean isLaboratoryRequired() {
        return true;
    }

    /**
     * Return the results of the laboratory exam of a given case
     * @param tbcase instance representing the case
     * @param inverseOrder if true, the list will bring the last collected, otherwise, the first results collected
     * @return list of laboratory exams, if available
     */
    public List<LaboratoryExam> getResults(TbCase tbcase, boolean inverseOrder) {
        String entityClass = getEntityClass().getSimpleName();
        String hql = "from " + entityClass + " exam " +
                "left join fecth exam.laboratory lab " +
                " where exam.tbcase.id = #{tbcase.id}";

        if (inverseOrder) {
            hql = hql.concat(" order by exam.dateCollected desc");
        }
        else {
            hql = hql.concat(" order by exam.dateCollected");
        }

        EntityManager em = App.getEntityManager();
        return em
                .createQuery(hql)
                .getResultList();
    }
}
