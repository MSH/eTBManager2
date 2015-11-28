package org.msh.tb.client.commons;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * Standard callback handler
 * @author Ricardo Memoria
 *
 * @param <T>
 */
public abstract class StandardCallback<T> implements AsyncCallback<T>{

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
	 */
	public void onFailure(Throwable except) {
        int code = -1;
        if (except instanceof StatusCodeException) {
            code = ((StatusCodeException) except).getStatusCode();
        }

        if (code != 0) {
            Window.alert("Error : " + except.toString());
        }
	}

}
