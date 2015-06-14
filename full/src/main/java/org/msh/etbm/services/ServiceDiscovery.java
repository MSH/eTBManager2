package org.msh.etbm.services;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.etbm.services.commons.DAOServices;
import org.msh.tb.application.App;
import org.msh.tb.entities.*;
import org.msh.tb.login.SessionData;
import org.msh.tb.login.UserSession;

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
        daoservices.put(ExamMicroscopy.class, "microscopyServices");
        daoservices.put(ExamCulture.class, "cultureServices");
        daoservices.put(ExamDST.class, "dstServices");
        daoservices.put(ExamXpert.class, "xpertServices");
    }

    /**
     * Get DAO services by the entity class. If service is not available,
     * an exception is thrown
     * @param entityClass entity class
     * @return instance of DAO service
     */
    public DAOServices getByEntityClass(Class entityClass) {
        String compname = daoservices.get(entityClass);

        // component was found ?
        if (compname == null) {

            // try to find by service implemented to super class
            for (Class clazz: daoservices.keySet()) {
                if (clazz.isAssignableFrom(entityClass)) {
                    compname = daoservices.get(clazz);
                    break;
                }
            }

            if (compname == null) {
                throw new RuntimeException("No DAO service registered for class " + compname);
            }
        }

        // get DAO services component
        DAOServices comp = null;

        Workspace ws = UserSession.getWorkspace();
        String ext = ws.getExtension();

        // extension was defined?
        if (ext != null && !ext.isEmpty()) {
            String customcomp = compname + ext.toUpperCase();

            comp = (DAOServices) App.getComponent(customcomp);
        }

        if (comp == null) {
            comp = (DAOServices) App.getComponent(compname);
        }

        if (comp == null) {
            throw new RuntimeException("No component found with name " + comp);
        }

        return comp;
    }
}
