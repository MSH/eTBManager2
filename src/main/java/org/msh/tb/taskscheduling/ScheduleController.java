package org.msh.tb.taskscheduling;

import org.jboss.seam.annotations.*;

import java.io.Serializable;
import java.util.Date;

import static org.jboss.seam.ScopeType.APPLICATION;

@Name("taskSchedulerController")
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