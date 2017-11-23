package org.msh.tb.sync.actions;

import org.apache.commons.beanutils.PropertyUtils;
import org.msh.tb.application.App;
import org.msh.tb.sync.Sync;

import javax.persistence.ManyToMany;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Mauricio on 20/12/2015.
 */
public abstract class ImporterUtils {

    /**
     * It will look for duplicities comparing the object and params with the objects that already exists on the database
     * doing a search considering the keyParameters annotated.
     * @param o the object that will be searched
     * @param params the params used on the search
     * @return a similar object if find, null if don't find
     */
    public static Object findDuplicity(Object o, Map<String, Object> params){
        if(o==null)
            return null;

        Class clazz = o.getClass();
        List<String> keyAttributeLst = new ArrayList<String>();

        while(clazz != null){
            for(Field f : clazz.getDeclaredFields()) {
                //if the field is a keyAttribute stores this field on a list
                if (f.getAnnotation(Sync.class) != null && f.getAnnotation(Sync.class).keyAttribute()) {
                    String keyAttributeName = f.getName();
                    //if the field has an internal attribute that should be used, check it
                    if (!f.getAnnotation(Sync.class).internalKeyAttribute().isEmpty()) {
                        //if has more than one internalKeyAttributes run this list of atributtes adding to the list of key parameters
                        String[] internalAttributeList = f.getAnnotation(Sync.class).internalKeyAttribute().trim().split(",");
                        for (String s : internalAttributeList) {
                            String internalAttribute = keyAttributeName + "." + s;
                            if (params.get(internalAttribute) == null && params.get(keyAttributeName) == null) {
                                throw new RuntimeException("No parameter found for key attribute! " + internalAttributeList);
                            }
                            keyAttributeLst.add(internalAttribute);
                        }
                    } else {
                        //Check if keyAttribute exists on params list. It has to exist, can only be a keyAttibute required fields
                        if (params.get(keyAttributeName) == null) {
                            throw new RuntimeException("No parameter found for key attribute! " + keyAttributeName);
                        }
                        keyAttributeLst.add(keyAttributeName);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        if(keyAttributeLst == null || keyAttributeLst.size() < 1)
            return null;

        String queryString = "from " + o.getClass().getSimpleName() + " where ";
        for(String key : keyAttributeLst){
            queryString += key + " = :" + key.replace(".", "") + " and ";
        }
        queryString = queryString.substring(0, queryString.length()-4);

        Query query = App.getEntityManager().createQuery(queryString);
        for(String key : keyAttributeLst){
            Object value = params.get(key);
            if(value==null) {
                //if don't find the value as a param, look for it inside an object, eg Patient
                Object embeddedObject = params.get(key.substring(0, key.lastIndexOf(".")));
                try{
                    value = PropertyUtils.getProperty(embeddedObject, key.substring(key.lastIndexOf(".")+1, key.length()));
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException("value not found on parameters list:" + key.substring(key.lastIndexOf(".")+1, key.length()));
                }
            }
            query.setParameter(key.replace(".", ""),value);
        }

        List<Object> resultList = query.getResultList();

        if(resultList != null && resultList.size() > 0)
            return resultList.get(0);

        return null;
    }

    /**
     * The object will be checked if any field is annotated with @Sync, if the is any field anotated with this
     * type and if it is a list all objects from this list will be removed from the DB and the list will be cleared.
     * @param o object to have its params checked
     */
    public static void cleanObjectCollections(Object o){
        if(o==null)
            return;
        Class clazz = o.getClass();
        List<String> lst = new ArrayList<String>();

        while(clazz != null){
            for(Field f : clazz.getDeclaredFields()){
                if(f.getAnnotation(Sync.class) != null && f.getAnnotation(Sync.class).clearList()){
                    if(f.getAnnotation(ManyToMany.class) != null)
                        throw new RuntimeException("Sync Clear can not be assigned to a Many to Many field. Need to implement for those cases.");
                    lst.add(f.getName());
                }
            }

            clazz = clazz.getSuperclass();
        }

        for(String s : lst){
            try{
                Collection c = (Collection) PropertyUtils.getProperty(o, s);
                if(c!=null){
                    for(Object item : c){
                        App.getEntityManager().remove(item);
                    }
                    c.clear();
                }
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

}
