package org.msh.etbm.commons.apidoc.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rmemoria on 29/4/15.
 */
public class DataStore {
    private static final Map<String, Object> values = new HashMap<String, Object>();

    public static Object get(String key) {
        return values.get(key);
    }

    public static void put(String key, Object data) {
        values.put(key, data);
    }
}
