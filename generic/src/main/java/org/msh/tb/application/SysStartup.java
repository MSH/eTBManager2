package org.msh.tb.application;

import java.util.Calendar;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;

@Name("sysStartup")
public class SysStartup {
	@In(create=true) SystemTimer systemTimer; 
	
	static final long halfDayDelay = 12*60*60*1000L;
	
	@Observer("org.jboss.seam.postInitialization")
	public void initTimerChecking() {
		EtbmanagerApp.instance().initializeInstance();
		
		System.out.println("Initializing shedulled tasks...");
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2100);
		
		systemTimer.trigger(0, halfDayDelay);
	}

}
