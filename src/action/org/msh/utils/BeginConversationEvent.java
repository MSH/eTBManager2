package org.msh.utils;

import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Conversation;

@Name("beginConversationEvent")
public class BeginConversationEvent {

	@In
	private Conversation conversation;
	
	public void switchFlushModeManual() {
		conversation.changeFlushMode(FlushModeType.MANUAL);
	}
}
