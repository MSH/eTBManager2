package org.msh.etbm.services.gxalert;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.GxalertData;

import javax.persistence.EntityManager;

/**
 * Created by ricardo on 12/12/14.
 */
@Name("gxalertService")
public class GxalertService {

    @In
    EntityManager entityManager;

    @Transactional
    public void saveData(GxalertData data) {
        entityManager.persist(data);
        entityManager.flush();
    }
}
