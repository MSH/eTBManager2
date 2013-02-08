package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.entities.Tag;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.utils.EntityQuery;

@Name("tagHome")
@LogInfo(roleName="TAGS")
public class TagHome extends EntityHomeEx<Tag> {
	private static final long serialVersionUID = 2346498717179017533L;

	private boolean autogenerated;


	@Override
	public String persist() {
		Tag tag = getInstance();
		if ((autogenerated) && (!tag.isAutogenerated())) {
			FacesMessages.instance().addToControlFromResourceBundle("edtsql", "javax.faces.component.UIInput.REQUIRED");
			return "error";
		}
		
		if (!tag.isAutogenerated())
			tag.setSqlCondition(null); 
		else {
			// test tag condition
			if (!testTagCondition()) {
				FacesMessages.instance().addToControlFromResourceBundle("edtsql", "admin.tags.conderror");
				return "error";
			}
		}
		
		String ret = super.persist();
		if ((ret.equals("persisted")) && (tag.isAutogenerated())) {
			TagsCasesHome tagsCasesHome = (TagsCasesHome)Component.getInstance("tagsCasesHome", true);
			if (!tagsCasesHome.updateCases(tag))
				return "error";
		}
		return ret;
	}

	
	/**
	 * Check if the SQL condition given to the tag is correct
	 * @return
	 */
	public boolean testTagCondition() {
		try {
			String sql = "select count(*) from tbcase a inner join patient p on p.id=a.patient_id where " + getInstance().getSqlCondition();
			getEntityManager().createNativeQuery(sql).getSingleResult();
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public EntityQuery<Tag> getEntityQuery() {
		return (TagsQuery)Component.getInstance("tagsQuery", false);
	}


	/**
	 * @return the autogenerated
	 */
	public boolean isAutogenerated() {
		return getInstance().isAutogenerated();
	}

	/**
	 * @param autogenerated the autogenerated to set
	 */
	public void setAutogenerated(boolean autogenerated) {
		this.autogenerated = autogenerated;
	}

}
