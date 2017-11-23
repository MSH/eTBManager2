package org.msh.etbm.services.gxalert;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.entities.GxalertData;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Created by ricardo on 12/12/14.
 */
@Name("gxalertService")
public class GxalertService {

    @In
    EntityManager entityManager;

    @Transactional
    public void saveData(GxalertData data) {
        Workspace ws = (Workspace) Component.getInstance("defaultWorkspace");
        data.setWorkspace(ws);
        data.setRecordDate(new Date());

        entityManager.persist(data);
        entityManager.flush();
        System.out.println(data.getId());
    }
}
