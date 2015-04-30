package org.msh.etbm.commons.apidoc.impl;

import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.commons.apidoc.annotations.ApiDocReturn;
import org.msh.etbm.commons.apidoc.model.ApiGroup;
import org.msh.etbm.commons.apidoc.model.ApiReturn;
import org.msh.etbm.commons.apidoc.model.ApiRoute;

import javax.ws.rs.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Scan the class methods and extract information about the declared routes
 * Created by rmemoria on 29/4/15.
 */
public class RestEasyRoute {

    private ApiDocBuilder builder;
    private String classPath;

    public void scan(ApiDocBuilder builder, Class clazz) {
        this.builder = builder;

        Path path = (Path)clazz.getAnnotation(Path.class);

        if (path == null) {
            return;
        }

        classPath = path.value();
        ApiGroup grp = getGroup(clazz, classPath);

        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null) {
            for (Method met: methods) {
                getRoute(grp, met);
            }
        }
    }

    /**
     * Get information about the group
     * @param clazz
     * @param path
     * @return
     */
    protected ApiGroup getGroup(Class clazz, String path) {
        String groupname;
        String description;

        // get group information
        ApiDoc doc = (ApiDoc)clazz.getAnnotation(ApiDoc.class);
        if (doc != null) {
            groupname = doc.group();
            description = doc.description();
        }
        else {
            groupname = "Unknown";
            description = null;
        }

        ApiGroup grp = builder.addGroup(groupname);

        if (description != null) {
            grp.setDescription(description);
        }

        // get default return codes
        if (doc != null && doc.returnCodes() != null) {

            for (ApiDocReturn ret: doc.returnCodes()) {
                ApiReturn resp = new ApiReturn();
                resp.setDescription(ret.description());
                resp.setStatusCode(ret.statusCode());

                grp.getReturnCodes().add(resp);
            }
        }

        return grp;
    }


    /**
     * Add route based on the method annotations
     * @param grp
     * @param method
     * @return
     */
    public ApiRoute getRoute(ApiGroup grp, Method method) {
        Path path = (Path)method.getAnnotation(Path.class);

        if (path == null) {
            return null;
        }

        String methodPath = classPath + path.value();

        ApiRoute.MethodType type = getMethodType(method);

        ApiRoute route = builder.addRoute(grp, methodPath, type);

        // get method documentation
        ApiDocMethod doc = (ApiDocMethod)method.getAnnotation(ApiDocMethod.class);
        if (doc != null) {
            route.setDescription(doc.description());

            if (doc.returnCodes() != null) {
                for (ApiDocReturn it: doc.returnCodes()) {
                    ApiReturn ret = new ApiReturn();
                    ret.setStatusCode(it.statusCode());
                    ret.setDescription(it.description());

                    route.getReturnCodes().add(ret);
                }
            }
        }

        Consumes consumes = (Consumes) method.getAnnotation(Consumes.class);
        if (consumes != null) {
            if (route.getConsumes() == null) {
                route.setConsumes(new ArrayList<String>());
            }

            for (String s: consumes.value()) {
                route.getConsumes().add(s);
            }
        }

        Produces prod = (Produces) method.getAnnotation(Produces.class);
        if (prod != null) {
            if (route.getProduces() == null) {
                route.setProduces( new ArrayList<String>() );
            }

            for (String s: prod.value()) {
                route.getProduces().add(s);
            }
        }

        return route;
    }

    /**
     * Return the method type of the route declared in the method as JSR annotation
     * @param method
     * @return
     */
    private ApiRoute.MethodType getMethodType(Method method) {
        GET getMethod = (GET)method.getAnnotation(GET.class);
        if (getMethod != null) {
            return ApiRoute.MethodType.GET;
        }

        POST postmet = (POST)method.getAnnotation(POST.class);
        if (postmet != null) {
            return ApiRoute.MethodType.POST;
        }

        PUT put = (PUT)method.getAnnotation(PUT.class);
        if (put != null) {
            return ApiRoute.MethodType.PUT;
        }

        DELETE del = (DELETE)method.getAnnotation(DELETE.class);
        if (del != null) {
            return ApiRoute.MethodType.DELETE;
        }

        return null;
    }
}
