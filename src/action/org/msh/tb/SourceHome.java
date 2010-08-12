package org.msh.tb;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Source;
import org.msh.tb.log.LogInfo;


@Name("sourceHome")
@LogInfo(roleName="SOURCES")
public class SourceHome extends EntityHomeEx<Source> {
	private static final long serialVersionUID = 8838166214931110292L;

	@Factory("source")
	public Source getSource() {
		return (Source)getInstance();
	}

	@Override
	public String persist() {
		return super.persist();
	}

	@Override
	public String remove() {
		return super.remove();
	}

	
	public void setSource(Source source) {
		if (source == null)
			 setId(null);
		else setId(source.getId());
	}
}
