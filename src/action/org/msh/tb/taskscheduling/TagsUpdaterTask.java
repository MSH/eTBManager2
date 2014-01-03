package org.msh.tb.taskscheduling;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.msh.tb.entities.Tag;
 
@Name("tagsUpdaterTask")
@AutoCreate
@Scope(ScopeType.APPLICATION)
public class TagsUpdaterTask extends Task{

	private final String EVENT_NAME = "Cases Tag Update";
	private final String EVENT_DESC = "Update all cases checking if they attend the conditions to have the tag in question.";
    
  
    @Asynchronous
    @Transactional
    public QuartzTriggerHandle createQuartzTestTimer(
                @Expiration Date when, @IntervalCron String interval) {
    	
		List<Tag> tags = (List<Tag>) entityManager.createQuery("from Tag where dailyUpdate = true and active = true").getResultList();
		
		for(Tag tag : tags){
			if(updateCases(tag))
				saveTransactionLog(tag, tag.getWorkspace().getId());
		}
		
        return null;
    }
    
	/**
	 * Update the cases of an auto generated tag
	 * @param tag
	 */
	@Transactional
	public boolean updateCases(Tag tag) {
		if (!tag.isAutogenerated())
			return false;

		// remove previous tags
		String sql = "delete from tags_case where tag_id = :id";
		entityManager.createNativeQuery(sql).setParameter("id", tag.getId()).executeUpdate();

		Integer wsid = tag.getWorkspace().getId();

		// is tag active ?
		if (tag.isActive()) {
			// include new tags
			sql = "insert into tags_case (case_id, tag_id) " +
					"select a.id, " + tag.getId() + " from tbcase a join patient p on p.id=a.patient_id " +
					"where " + tag.getSqlCondition() + " and p.workspace_id = :id";
			entityManager.createNativeQuery(sql).setParameter("id", wsid).executeUpdate();
		}

		return true;
	}

	@Override
	protected String getEventName() {
		return EVENT_NAME;
	}

	@Override
	protected String getLogDescription() {
		return EVENT_DESC;
	}

}