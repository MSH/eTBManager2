package org.msh.tb;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.etbm.commons.transactionlog.mapping.LogInfo;
import org.msh.tb.entities.Source;
import org.msh.utils.EntityQuery;



@Name("sourceHome")
@LogInfo(roleName="SOURCES", entityClass=Source.class)
public class SourceHome extends EntityHomeEx<Source> {
	private static final long serialVersionUID = 8838166214931110292L;

	@Factory("source")
	public Source getSource() {
		return (Source)getInstance();
	}

	
	@Override
	public EntityQuery<Source> getEntityQuery() {
		return (SourcesQuery)Component.getInstance("sources", false);
	}


	public void setSource(Source source) {
		if (source == null)
			 setId(null);
		else setId(source.getId());
	}
}
