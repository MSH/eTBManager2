package org.msh.tb.client.commons;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
		Window.alert("Error : " + except.toString());
	}

}
