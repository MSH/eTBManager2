package org.msh.tb;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.Tag;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Workspace;

@Name("tagsCasesHome")
public class TagsCasesHome {

	@In(create=true) EntityManager entityManager;
	
	private boolean updated;
	
	/**
	 * Update the automatic tags of a case
	 * @param tbcase
	 */
	@Transactional
	@Observer("update-case-tags")
	public void updateTags(TbCase tbcase) {
		// avoid updating several times the case in the same request
		if (updated)
			return;

		// get tags
		List<Tag> tags = entityManager.createQuery("from Tag t where t.active = true " +
				"and t.sqlCondition is not null and t.workspace.id = #{defaultWorkspace.id}")
				.getResultList();

		if (tags.size() == 0)
			return;
		
		// erase all tags of the current case
		entityManager.createNativeQuery("delete from tags_case where case_id = :id " +
				"and tag_id in (select id from tag where sqlCondition is not null)")
				.setParameter("id", tbcase.getId())
				.executeUpdate();
		
		Integer wsid = ((Workspace)Component.getInstance("defaultWorkspace")).getId();
		// update tags
		String sql = "";
		for (Tag tag: tags) {
			if (!sql.isEmpty())
				sql += " union ";
			sql += "select a.id, " + tag.getId() + 
				" from tbcase a join patient p on p.id=a.patient_id " +
				" and p.workspace_id = " + wsid + 
				" and a.id = " + tbcase.getId() + 
				" and " + tag.getSqlCondition();
		}
		sql = "insert into tags_case (case_id, tag_id) " + sql;
		entityManager.createNativeQuery(sql).executeUpdate();

		updated = true;
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

		Integer wsid = ((Workspace)Component.getInstance("defaultWorkspace")).getId();

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
	
	/**
	 * Update the cases of an auto generated tag
	 * @param tag
	 */
	@Transactional
	public String fixTagsConsolidation() {
		List<Tag> tags = entityManager.createQuery("from Tag").getResultList();

		for(Tag tag : tags){
			if(tag.isAutogenerated()){
				// remove previous tags
				String sql = "delete from tags_case where tag_id = :id";
				entityManager.createNativeQuery(sql).setParameter("id", tag.getId()).executeUpdate();

				Integer wsid = tag.getWorkspace().getId();
				
				// include new tags
				sql = "insert into tags_case (case_id, tag_id) " +
						"select a.id, " + tag.getId() + " from tbcase a join patient p on p.id=a.patient_id " +
						"where " + tag.getSqlCondition() + " and p.workspace_id = :id";
				entityManager.createNativeQuery(sql).setParameter("id", wsid).executeUpdate();				
			}
		}
		FacesMessages facesMessages = (FacesMessages)Component.getInstance("facesMessages");
		facesMessages.addFromResourceBundle("default.entity_updated");
		return "fixed";
	}


	static public TagsCasesHome instance() { 
		return (TagsCasesHome)Component.getInstance("tagsCasesHome", true);
	}
}
