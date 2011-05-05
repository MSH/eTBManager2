package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Tag;
import org.msh.utils.EntityQuery;

@Name("tagHome")
public class TagHome extends EntityHomeEx<Tag> {
	private static final long serialVersionUID = 2346498717179017533L;

	
	@Override
	public EntityQuery<Tag> getEntityQuery() {
		return (TagsQuery)Component.getInstance("tagsQuery", false);
	}

}
