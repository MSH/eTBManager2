package org.msh.etbm.web;

import freemarker.ext.servlet.AllHttpScopesHashModel;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import org.jboss.seam.core.Locale;
import org.jboss.seam.international.Messages;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.msh.tb.application.EtbmanagerApp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extension of the FreeMarker template to implement SEAM context in order to
 * have translation support
 *
 * Created by ricardo on 04/12/14.
 */
public class FreeMarkerTemplateServlet extends FreemarkerServlet {

    private static final String SEAM_CONTEXT_TEST = "seam-context";

    /**
     * Implement seam context only on GET request
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // when doGet is called for the first time on the same page request, a test is performed
        // to check if the SEAM context was already created. If not, create it and add an attribute
        // to the request and call doGet again
        if (request.getAttribute(SEAM_CONTEXT_TEST) == null) {
            final HttpServletRequest req = request;
            final HttpServletResponse resp = response;
            final FreeMarkerTemplateServlet instance = this;

            // create a SEAM context around the request
            ContextualHttpServletRequest context = new ContextualHttpServletRequest(request) {
                @Override
                public void process() throws Exception {
                    // set the attribute to indicate the context was created
                    req.setAttribute(SEAM_CONTEXT_TEST, true);
                    // call doGet again in order to run the parent implementation (not possible in here)
                    doGet(req, resp);
                }
            };
            context.run();
        }
        else {
            // SEAM context is available, process the template here
            super.doGet(request, response);
        }
    }


    /**
     * Include the components available during template processing
     * @param request
     * @param response
     * @param template
     * @param data
     * @return
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected boolean preTemplateProcess(HttpServletRequest request, HttpServletResponse response, Template template, TemplateModel data) throws ServletException, IOException {
        boolean result = false;

        AllHttpScopesHashModel model = (AllHttpScopesHashModel)data;
        try {
            model.put("messages", Messages.instance());
            model.put("request", request);
            model.put("locale", Locale.instance());
            model.put("etbmanagerApp", EtbmanagerApp.instance());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        result = super.preTemplateProcess(request, response, template, data);
        return true;
    }

    /**
     * Transform the request URL in the right file in the template path
     * @param request instance of HttpServletRequest
     * @return the template path
     */
    @Override
    protected String requestUrlToTemplatePath(HttpServletRequest request) {
        String s = super.requestUrlToTemplatePath(request);
        if ("/index.html".equals(s)) {
            return s;
        }
        s = s.replace(".html", ".template.html");
        return s;
    }
}
