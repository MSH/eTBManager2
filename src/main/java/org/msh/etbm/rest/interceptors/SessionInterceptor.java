package org.msh.etbm.rest.interceptors;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.seam.web.ServletContexts;
import org.jboss.seam.web.Session;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ext.Provider;

/**
 * Check if the server request contains a cookie called JSESSIONID. If so, it indicates that it is
 * a request from the browser with a web session supporting it. If the cookie is not available, destroy
 * the web session
 *
 * Created by ricardo on 03/12/14.
 */
@ServerInterceptor
@Provider
public class SessionInterceptor implements PostProcessInterceptor {
    @Override
    public void postProcess(ServerResponse serverResponse) {
        HttpServletRequest req = (HttpServletRequest) ServletContexts.instance().getRequest();

        Cookie[] cookies = req.getCookies();
        boolean sessionIdFound = false;
        if (cookies != null) {
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    sessionIdFound = true;
                    break;
                }
            }
        }

        // if session ID was not found, them destroy the session
        if (!sessionIdFound) {
            Session.instance().invalidate();
        }
    }
}
