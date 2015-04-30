package org.msh.etbm.commons.apidoc;

import org.msh.etbm.commons.apidoc.impl.ApiDocScannerImpl;
import org.msh.etbm.commons.apidoc.model.ApiDocument;

/**
 * Generate API documentation in a structured way about the REST API
 *
 * Created by rmemoria on 28/4/15.
 */
public class ApiDocGenerator {

    public ApiDocument doc;

    /**
     * Generate information containing all documentation about the REST APIs
     * @param packagename
     * @param basepath
     * @param version
     * @return
     */
    public ApiDocument generate(String packagename, String basepath, String version) {
        ApiDocScannerImpl scanner = new ApiDocScannerImpl();

        if (doc == null) {
            doc = scanner.scan("org.msh.etbm.rest");
        }

        doc.setBasePath(basepath);
        doc.setVersion(version);

        return doc;
    }
}
