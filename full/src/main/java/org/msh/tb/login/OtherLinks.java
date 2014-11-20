package org.msh.tb.login;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.SystemConfigHome;
import org.msh.tb.entities.SystemConfig;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Return a list of links to be displayed in the footer of e-TB Manager in the useful links section
 *
 * Created by ricardo on 22/09/14.
 */
@Name("otherLinks")
@BypassInterceptors
public class OtherLinks {

    private List<SelectItem> links;

    /**
     * Return the links
     * @return
     */
    public List<SelectItem> getLinks() {
        if (links == null) {
            links = createLinks();
        }
        return links;
    }


    /**
     * Create the links to be displayed
     * @return
     */
    protected List<SelectItem> createLinks() {
        SystemConfigHome home = (SystemConfigHome) Component.getInstance(SystemConfigHome.class);
        SystemConfig cfg = home.getSystemConfig();

        if (cfg.getOtherLinks() == null) {
            return null;
        }

        String s = cfg.getOtherLinks().trim();

        String[] lst = s.split("\\r?\\n");

        List<SelectItem> lnks = new ArrayList<SelectItem>();
        for (String item: lst) {
            int pos = item.indexOf('=');
            if (pos > 0) {
                String label = item.substring(0, pos).trim();
                String link = item.substring(pos + 1, item.length()).trim();
                lnks.add(new SelectItem(link, label));
            }
        }

        return lnks;
    }
}
