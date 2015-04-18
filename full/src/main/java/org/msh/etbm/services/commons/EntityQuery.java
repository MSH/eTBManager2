package org.msh.etbm.services.commons;

import org.msh.tb.application.App;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Define a filter to be applied to the entities to be returned
 * Created by rmemoria on 18/4/15.
 */
public class EntityQuery {

    private String _orderBy;
    private Integer _maxResults;
    private Integer _firstResult;
    private List<String> restrictions;
    private List<String> joins;
    private List<Object> ids;
    private Map<String, Object> params;
    private Class entityClass;

    /**
     * COnstructor used to specify the entity class
     * @param entityClass
     */
    public EntityQuery(Class entityClass) {
        this.entityClass = entityClass;
    }

    public EntityQuery orderBy(String orderBy) {
        _orderBy = orderBy;
        return this;
    }

    public EntityQuery maxResults(Integer max) {
        _maxResults = max;
        return this;
    }

    public EntityQuery firstResult(Integer value) {
        _firstResult = value;
        return this;
    }

    public EntityQuery addId(Object id) {
        if (ids == null) {
            ids = new ArrayList<Object>();
        }
        ids.add(id);

        return this;
    }

    public EntityQuery addRestriction(String hql) {
        if (restrictions == null) {
            restrictions = new ArrayList<String>();
        }
        restrictions.add(hql);
        return this;
    }

    public EntityQuery setParam(String param, Object value) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(param, value);
        return this;
    }


    public EntityQuery addJoin(String hqlJoin) {
        if (joins == null) {
            joins = new ArrayList<String>();
        }
        joins.add(hqlJoin);

        return this;
    }


    /**
     * Execute the query using the parameters
     * @return
     */
    public List exec() {
        String hql = createHQL();
        Query qry = App.getEntityManager().createQuery(hql);

        if (params != null) {
            for (String p: params.keySet()) {
                Object value = params.get(p);
                qry.setParameter(p, value);
            }
        }

        if (_maxResults != null) {
            qry.setMaxResults(_maxResults);
        }

        if (_firstResult != null) {
            qry.setFirstResult(_firstResult);
        }

        return qry.getResultList();
    }

    /**
     * Create the HQL based on the class content
     * @return
     */
    private String createHQL() {
        StringBuilder hql = new StringBuilder();

        hql.append("from " + entityClass.getName());

        // add joins
        if (joins != null) {
            for (String s: joins) {
                hql.append('\n');
                hql.append(s);
            }
        }

        boolean bWhere = false;

        if (restrictions != null) {
            for (String s: restrictions) {
                if (!bWhere) {
                    hql.append("\nwhere ");
                    bWhere = true;
                }
                else {
                    hql.append("\nand ");
                }
                hql.append(s);
            }
        }

        if (ids != null) {
            hql.append("\nand id in (");
            for (Object id: ids) {
                hql.append(id.toString());
            }
            hql.append(")");
        }

        if (_orderBy != null) {
            hql.append('\n');
            hql.append("order by ");
            hql.append(_orderBy);
        }

        return hql.toString();
    }
}
