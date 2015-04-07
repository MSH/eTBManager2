package org.msh.etbm.services;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.etbm.services.cases.exams.ExamMicroscopyServices;
import org.msh.etbm.services.commons.DAOServices;
import org.msh.tb.application.App;
import org.msh.tb.entities.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Component that allows to return a DAO service component of a given entity.
 * Usefull when you want to abstract of the entity class you're handling
 *
 * Created by rmemoria on 7/4/15.
 */
@Name("serviceDiscovery")
@Scope(ScopeType.APPLICATION)
public class ServiceDiscovery {

    private Map<Class, String> daoservices;

    @Create
    public void init() {
        initDaoServices();
    }

    /**
     * Register all DAO services available
     */
    protected void initDaoServices() {
        // already initialized ?
        if (daoservices != null) {
            return;
        }
        daoservices = new HashMap<Class, String>();

        // register the services by entity
        daoservices.put(TbCase.class, "caseServices");
        daoservices.put(ExamMicroscopy.class, "examMicroscopyServices");
        daoservices.put(ExamCulture.class, "examCultureServices");
        daoservices.put(ExamDST.class, "examDSTServices");
        daoservices.put(ExamXpert.class, "examXpertServices");
    }

    /**
     * Get DAO services by the entity class. If service is not available,
     * an exception is thrown
     * @param entityClass entity class
     * @return instance of DAO service
     */
    DAOServices getByEntity(Class entityClass) {
        String compname = daoservices.get(ExamMicroscopy.class);

        if (compname == null) {
            throw new RuntimeException("No DAO service registered for class " + compname);
        }

        return (DAOServices) App.getComponent(compname);
    }
}
