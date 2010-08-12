package org.msh.tb.adminunits;

import org.msh.mdrtb.entities.LocalizedNameComp;

/**
 * @author Ricardo
 *
 */
public class InfoCountryLevels {
	
	public InfoCountryLevels(int workspaceId) {
		super();
		this.workspaceId = workspaceId;
	}

	private LocalizedNameComp[] names = new LocalizedNameComp[5];
	private int workspaceId;

	/**
	 * @return the names
	 */
	public LocalizedNameComp[] getNames() {
		return names;
	}
	/**
	 * @param names the names to set
	 */
	public void setNames(LocalizedNameComp[] names) {
		this.names = names;
	}
	/**
	 * @return the workspaceId
	 */
	public int getWorkspaceId() {
		return workspaceId;
	}
	/**
	 * @param workspaceId the workspaceId to set
	 */
	public void setWorkspaceId(int workspaceId) {
		this.workspaceId = workspaceId;
	}


	public void addName(int level, String name1, String name2) {
		LocalizedNameComp name = names[level - 1];

		if (name == null) {
			name = new LocalizedNameComp();
			name.setName1(name1);
			name.setName2(name2);
			names[level - 1] = name;
		}
		else {
			name.setName1(name.getName1() + "/" + name1);
			name.setName2(name.getName2() + "/" + name2);
		}
	}
	
	public LocalizedNameComp getInfoLevel1() {
		return names[0];
	}
	
	public LocalizedNameComp getInfoLevel2() {
		return names[1];
	}
	
	public LocalizedNameComp getInfoLevel3() {
		return names[2];
	}
	
	public LocalizedNameComp getInfoLevel4() {
		return names[3];
	}
	
	public LocalizedNameComp getInfoLevel5() {
		return names[4];
	}
}
