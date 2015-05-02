package org.msh.etbm.commons.apidoc.impl;

import org.msh.etbm.commons.apidoc.annotations.ApiDoc;
import org.msh.etbm.commons.apidoc.annotations.ApiDocMethod;
import org.msh.etbm.commons.apidoc.annotations.ApiDocQueryParam;
import org.msh.etbm.commons.apidoc.annotations.ApiDocReturn;
import org.msh.etbm.commons.apidoc.model.*;

import javax.management.Query;
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Scan the class methods and extract information about the declared routes
 * Created by rmemoria on 29/4/15.
 */
public class RestEasyRoute {

    private ApiDocBuilder builder;
    private String classPath;
    private boolean detailed;
    private String groupname;

    public void scan(ApiDocBuilder builder, Class clazz, String groupname, boolean detailed) {
        this.builder = builder;
        this.detailed = detailed;
        this.groupname = groupname;

        Path path = (Path)clazz.getAnnotation(Path.class);

        if (path == null) {
            return;
        }

        classPath = path.value();
        ApiGroup grp = getGroup(clazz, classPath, detailed);

        // group found ?
        if (grp == null) {
            return;
        }

        if (detailed) {
            grp.setRoutes( new ArrayList<ApiRoute>() );
            Method[] methods = clazz.getDeclaredMethods();
            if (methods != null) {
                for (Method met: methods) {
                    getRoute(grp, met);
                }
            }
        }
    }

    /**
     * Get information about the group
     * @param clazz
     * @param path
     * @return
     */
    protected ApiGroup getGroup(Class clazz, String path, boolean detailed) {
        String gname;
        String description;

        // get group information
        ApiDoc doc = (ApiDoc)clazz.getAnnotation(ApiDoc.class);
        if (doc != null) {
            gname = doc.group();
            description = doc.description();
        }
        else {
            gname = "Unknown";
            description = null;
        }

        // check if it's the group being searched
        if ((this.groupname != null) && (!this.groupname.equals(gname))) {
            return null;
        }

        ApiGroup grp = builder.addGroup(gname);

        if (description != null) {
            grp.setDescription(description);
        }

        if (detailed) {
            // get default return codes
            if (doc != null && doc.returnCodes() != null) {

                for (ApiDocReturn ret: doc.returnCodes()) {
                    grp.setReturnCodes( new ArrayList<ApiReturn>() );

                    ApiReturn resp = new ApiReturn();
                    resp.setDescription(ret.description());
                    resp.setStatusCode(ret.statusCode());

                    grp.getReturnCodes().add(resp);
                }
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

        checkParams(method, route);

        Type rtype = method.getReturnType();
        if (rtype != Void.TYPE) {
            if (Collection.class.isAssignableFrom((Class)rtype)) {
                rtype = method.getGenericReturnType();
            }
            ObjSchemaGenerator gen = new ObjSchemaGenerator();
            ApiObject obj = gen.generate(rtype);
            route.setReturnObject(obj);
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


    /**
     * Check if there are query parameters used in the route
     * @param met
     * @param route
     */
    protected void checkParams(Method met, ApiRoute route) {
        Class[] params = met.getParameterTypes();

        if (params == null || params.length == 0) {
            return;
        }

        Annotation[][] anots = met.getParameterAnnotations();

        List<ApiQueryParam> queries = new ArrayList<ApiQueryParam>();

        for (int i = 0; i < params.length; i++) {
            Annotation[] lst = anots[i];
            ApiQueryParam p = new ApiQueryParam();

            for (Annotation anot: lst) {
                if (anot instanceof QueryParam) {
                    QueryParam qparam = (QueryParam)anot;
                    p.setName( qparam.value() );
                    p.setType( params[i].getSimpleName() );
                    continue;
                }

                if (anot instanceof ApiDocQueryParam) {
                    ApiDocQueryParam doc = (ApiDocQueryParam)anot;
                    p.setDescription(doc.value());
                }
            }

            if (p.getName() != null) {
                queries.add(p);
            }
            else {
                // is a complex object
                ObjSchemaGenerator gen = new ObjSchemaGenerator();
                ApiObject objdoc = gen.generate(params[i]);
                if (objdoc != null) {
                    route.setInputObject(objdoc);
                }
            }
        }

        if (queries.size() > 0) {
            route.setQueryParams(queries);
        }
    }
}
