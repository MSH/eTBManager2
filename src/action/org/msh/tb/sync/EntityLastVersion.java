/**
 * 
 */
package org.msh.tb.sync;

/**
 * Store temporary information to be serialized to the initialization file
 * about the last version send of each entity class in the file
 * @author Ricardo Memoria
 *
 */
public class EntityLastVersion {

	private String entityClass;
	private Integer lastVersion;

	/**
	 * @return the entityClass
	 */
	public String getEntityClass() {
		return entityClass;
	}
	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}
	/**
	 * @return the lastVersion
	 */
	public Integer getLastVersion() {
		return lastVersion;
	}
	/**
	 * @param lastVersion the lastVersion to set
	 */
	public void setLastVersion(Integer lastVersion) {
		this.lastVersion = lastVersion;
	}
}
