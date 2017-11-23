package org.msh.tb.adminunits;

import org.msh.tb.entities.LocalizedNameComp;

/**
 * Store information about the country structure of a workspace. This information is kept in memory in application level
 * by the class {@link CountryLevelInfo} and can be access directly by the component name "levelInfo", which is sensitive by workspace
 * @author Ricardo Memoria
 *
 */
public class InfoCountryLevels {
	
	public InfoCountryLevels(int workspaceId) {
		super();
		this.workspaceId = workspaceId;
	}

	private LocalizedNameComp[] names = new LocalizedNameComp[5];
	private int workspaceId;
	private int maxLevel;

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
		
		if (level > maxLevel)
			maxLevel = level;
	}
	
	public LocalizedNameComp getNameLevel1() {
		return names[0];
	}
	
	public LocalizedNameComp getNameLevel2() {
		return names[1];
	}
	
	public LocalizedNameComp getNameLevel3() {
		return names[2];
	}
	
	public LocalizedNameComp getNameLevel4() {
		return names[3];
	}
	
	public LocalizedNameComp getNameLevel5() {
		return names[4];
	}
	
	public int getMaxLevel() {
		return maxLevel;
	}
	
	/**
	 * Check if workspace has the first level of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel1() {
		return (maxLevel >= 1);
	}


	/**
	 * Check if workspace has the second level of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel2() {
		return (maxLevel >= 2);
	}


	/**
	 * Check if workspace has the 3rd level of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel3() {
		return (maxLevel >= 3);
	}


	/**
	 * Check if workspace has the 4th level of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel4() {
		return (maxLevel >= 4);
	}


	/**
	 * Check if workspace has the 5th level of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel5() {
		return (maxLevel >= 5);
	}
	
	/**
	 * Check if workspace has the level passed as a parameter of the country structure
	 * @return true if so, false if the country structure doesn't have this level
	 */
	public boolean isHasLevel(int level) {
		return (maxLevel >= level);
	}
}
