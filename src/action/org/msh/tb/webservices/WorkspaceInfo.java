package org.msh.tb.webservices;

public class WorkspaceInfo {

	private Integer id;
	private String name1;
	private String name2;
	private String healthUnitName;
	
	public WorkspaceInfo() {
		super();
	}

	public WorkspaceInfo(Integer id, String name1, String name2, String healthUnit) {
		super();
		this.id = id;
		this.name1 = name1;
		this.name2 = name2;
		this.healthUnitName = healthUnit;
	}
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the name1
	 */
	public String getName1() {
		return name1;
	}
	/**
	 * @param name1 the name1 to set
	 */
	public void setName1(String name1) {
		this.name1 = name1;
	}
	/**
	 * @return the name2
	 */
	public String getName2() {
		return name2;
	}
	/**
	 * @param name2 the name2 to set
	 */
	public void setName2(String name2) {
		this.name2 = name2;
	}

	/**
	 * @return the healthUnitName
	 */
	public String getHealthUnitName() {
		return healthUnitName;
	}

	/**
	 * @param healthUnitName the healthUnitName to set
	 */
	public void setHealthUnitName(String healthUnitName) {
		this.healthUnitName = healthUnitName;
	}
}
