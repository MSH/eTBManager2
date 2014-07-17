package org.msh.tb.application;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;

import java.util.List;

@Scope(ScopeType.CONVERSATION)
@Name(StatusMessages.COMPONENT_NAME)
@BypassInterceptors
public class AppFacesMessages extends FacesMessages {
	private static final long serialVersionUID = 6471597935804058609L;

	public List<StatusMessage> getStatusMessages() {
		return getMessages();
	}
}
