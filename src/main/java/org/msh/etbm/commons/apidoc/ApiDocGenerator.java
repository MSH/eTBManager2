package org.msh.etbm.commons.apidoc;

import org.msh.etbm.commons.apidoc.impl.ApiDocScannerImpl;
import org.msh.etbm.commons.apidoc.model.ApiDocument;
import org.msh.etbm.commons.apidoc.model.ApiGroup;

/**
 * Generate API documentation in a structured way about the REST API
 *
 * Created by rmemoria on 28/4/15.
 */
public class ApiDocGenerator {

    private static final String REST_API_PACKAGE = "org.msh.etbm.rest";

    /**
     * Generate information containing all documentation about the REST APIs
     * @param packagename
     * @param basepath
     * @param version
     * @return
     */
    public ApiDocument generate(String packagename, String basepath, String version) {
        ApiDocScannerImpl scanner = new ApiDocScannerImpl();

        ApiDocument doc = scanner.scan(REST_API_PACKAGE, false, null);

        doc.setBasePath(basepath);
        doc.setVersion(version);

        return doc;
    }


    /**
     * Return information about an specific group
     * @param groupname
     * @return
     */
    public ApiGroup generateGroupInfo(String groupname) {
        ApiDocScannerImpl scanner = new ApiDocScannerImpl();

        ApiDocument doc = scanner.scan(REST_API_PACKAGE, true, groupname);
        if (doc.getGroups().size() == 0) {
            return null;
        }

        return doc.getGroups().get(0);
    }
}
