package org.msh.tb.misc;

import java.util.Calendar;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;

@Name("sysStartup")
public class SysStartup {
//	@In(create=true) StockChecking stockChecking;
	
	@Observer("org.jboss.seam.postInitialization")
	public void initTimerChecking() {
		System.out.println("Initializing shedulled tasks...");
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2100);
		
//		stockChecking.asyncCheckUnits(new Date(), 60*60*1000L, c.getTime());
	}

}
