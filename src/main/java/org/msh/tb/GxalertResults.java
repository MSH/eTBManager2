package org.msh.tb;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.GxalertData;
import org.msh.utils.EntityQuery;

/**
 * Created by ricardo on 17/12/14.
 */
@Name("gxalertResults")
public class GxalertResults extends EntityQuery<GxalertData> {
    @Override
    protected String getCountEjbql() {
        return "select count(*) from GxalertData";
    }

    @Override
    public String getEjbql() {
        return "from GxalertData";
    }

    @Override
    public Integer getMaxResults() {
        return 50;
    }
}
