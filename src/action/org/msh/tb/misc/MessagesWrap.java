package org.msh.tb.misc;

import static org.jboss.seam.ScopeType.EVENT;

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.international.Messages;
import org.msh.tb.login.UserSession;

@Name("org.jboss.seam.international.messagesFactory")
@BypassInterceptors
public class MessagesWrap extends Messages {

    @Factory(value = "messages", autoCreate = true, scope = EVENT)
	public Map<String, String> getMessages() {
		UserSession userSession = (UserSession)Component.getInstance("userSession", false);
		if ((userSession == null) || (!userSession.isDisplayMessagesKeys()))
			return createMap();
		
		return new AbstractMap<String, String>() {
            @Override
            public String get(Object key) {
            	return key.toString();
            }

            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                java.util.ResourceBundle bundle = SeamResourceBundle.getBundle();
                Set<Map.Entry<String, String>> entrySet = new HashSet<Map.Entry<String, String>>();

                Enumeration<String> keys = bundle.getKeys();

                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();

                    entrySet.add(new Map.Entry<String, String>() {

                        public String getKey() {
                            return key;
                        }

                        public String getValue() {
                            return get(key);
                        }

                        public String setValue(String arg0) {
                            throw new UnsupportedOperationException("not implemented");
                        }
                    });
                }

                return entrySet;
            }
		};
	}
}
