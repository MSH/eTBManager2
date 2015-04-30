package org.msh.etbm.commons.apidoc.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.msh.etbm.commons.apidoc.model.ApiDocument;
import org.msh.etbm.commons.apidoc.model.ApiGroup;
import org.msh.etbm.commons.apidoc.model.ApiRoute;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by rmemoria on 29/4/15.
 */
public class ApiDocScannerImpl implements ApiDocBuilder {

    private ApiDocument doc;

    public ApiDocument scan(String packagename) {
        ImmutableSet<ClassPath.ClassInfo> lst = getClasses(packagename);

        doc = new ApiDocument();

//        generateTestData();

        for (ClassPath.ClassInfo ci: lst) {
            scanClass(doc, ci);
        }


        return doc;
    }


    public void generateTestData() {
        ApiGroup grp = addGroup("TEST");
        ApiRoute route = addRoute(grp, "info", ApiRoute.MethodType.GET);
    }

    protected ImmutableSet<ClassPath.ClassInfo> getClasses(String packagename) {
        ImmutableSet<ClassPath.ClassInfo> lst = (ImmutableSet<ClassPath.ClassInfo>) DataStore.get("packages");
        if (lst != null) {
            return lst;
        }

        try {
            ClassPath cp = ClassPath.from(Thread.currentThread().getContextClassLoader());
            lst = cp.getTopLevelClassesRecursive(packagename);
            DataStore.put("packages", lst);
            return lst;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected void scanClass(ApiDocument doc, ClassPath.ClassInfo ci) {
        Class clazz = ci.load();

        RestEasyRoute routeScan = new RestEasyRoute();
        routeScan.scan(this, clazz);
    }


    /**
     * Add a new group to the api
     * @param name
     * @return
     */
    public ApiGroup addGroup(String name) {
        if (doc.getGroups() == null) {
            doc.setGroups( new ArrayList<ApiGroup>() );
        }

        for (ApiGroup grp: doc.getGroups()) {
            if (grp.getName().equals(name)) {
                return grp;
            }
        }

        ApiGroup grp = new ApiGroup();
        grp.setName(name);
        doc.getGroups().add(grp);
        return grp;
    }


    /**
     * Add a new route to the document
     * @param grp
     * @param path
     * @param type
     * @return
     */
    public ApiRoute addRoute(ApiGroup grp, String path, ApiRoute.MethodType type) {
        ApiRoute route = new ApiRoute();

        route.setPath(path);
        route.setType(type);

        grp.getRoutes().add(route);

        return route;
    }
}
