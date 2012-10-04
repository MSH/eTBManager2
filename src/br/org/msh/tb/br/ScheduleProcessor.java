package org.msh.tb.br;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.Tag;

@Name("processor")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class ScheduleProcessor {
	
	@In(create=true) EntityManager entityManager;
	
/*	@Asynchronous
    @Transactional
    public QuartzTriggerHandle updateTagsTrigger(@Expiration Date when, @IntervalCron String interval) {
		TagsCasesHome tagsCasesHome = (TagsCasesHome)Component.getInstance("tagsCasesHome", true);
		
		List<Tag> tags = (List<Tag>) entityManager.createQuery("from Tag").getResultList();
		
		for(Tag tag : tags)
			tagsCasesHome.updateCases(tag);
		
        return null;
    }
*/		
}
