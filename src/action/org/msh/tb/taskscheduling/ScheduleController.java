package org.msh.tb.taskscheduling;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.Serializable;
import java.util.Date;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("controller")
@Scope(APPLICATION)
@AutoCreate
@Startup
public class ScheduleController implements Serializable {
 
    private static final long serialVersionUID = 7609983147081676186L;

    private static String CRON_INTERVAL_1 = "0 0 3 * * ?"; //every day at 3 am
    
    @In
    TagsUpdaterTask tagsUpdaterTask;
 
    @Create
    public void scheduleTimer() {
        tagsUpdaterTask.createQuartzTestTimer(new Date(), CRON_INTERVAL_1);
    }
}