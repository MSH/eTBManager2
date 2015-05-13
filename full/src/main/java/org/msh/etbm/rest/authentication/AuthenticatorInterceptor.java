package org.msh.etbm.rest.authentication;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.msh.etbm.services.auth.AuthenticationService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Authenticator interceptor, called when a rest call is made to check if user is authenticated
 *
 * Created by ricardo on 03/12/14.
 */
@Provider
@ServerInterceptor
public class AuthenticatorInterceptor implements PreProcessInterceptor {

    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<Object>());;


    @Override
    public ServerResponse preProcess(HttpRequest req, ResourceMethod resourceMethod) throws Failure, WebApplicationException {
        // check if route is authenticated
        Method method = resourceMethod.getMethod();
        boolean authenticated = method.getAnnotation(Authenticated.class) != null;

        if (!authenticated) {
            authenticated = method.getDeclaringClass().getAnnotation(Authenticated.class) != null;
            if (!authenticated) {
                return null;
            }
        }

        // check if authentication is required
/*
        if ((Identity.instance().isLoggedIn()) || (path.startsWith("/pub/"))) {
            return null;
        }
*/

        // get auth token
        String authToken = null;

        Cookie cookie = req.getHttpHeaders().getCookies().get("authToken");

        if (cookie != null) {
            authToken = cookie.getValue();
        }
        else {
            List<String> vals = req.getUri().getQueryParameters().get("authToken");
            if ((vals != null) && (vals.size() > 0)) {
                authToken = vals.get(0);
            }
        }

        if (authToken == null) {
            return ACCESS_DENIED;
        }

        AuthenticationService srv = (AuthenticationService) Component.getInstance("authenticationService");
        if (!srv.loginWithToken(authToken)) {
            return ACCESS_DENIED;
        }
        return null;
    }
}
