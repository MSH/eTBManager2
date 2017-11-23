package org.msh.tb.adminunits;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.Workspace;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Keep information about the administrative unit levels of a workspace. It's in application scope, meaning that
 * all country structures of all workspaces are kept in memory in instances of {@link InfoCountryLevels}  
 * @author Ricardo Memoria
 *
 */
@Name("countryLevelInfo")
@Scope(ScopeType.APPLICATION)
public class CountryLevelInfo {

	@In EntityManager entityManager;
	
	private List<InfoCountryLevels> levels;
	
	@Factory(value="levelInfo", scope=ScopeType.EVENT)
	public  InfoCountryLevels getLevelsWorspace() {
		if (levels == null)
			loadLevels();

		Workspace ws = (Workspace)Component.getInstance("defaultWorkspace", true);
		return levelByWorkspace(ws.getId());
	}


	/**
	 * Update list of country level information 
	 */
	public void refresh() {
		levels = null;
	}


	/**
	 * Load from the database the list of country level information 
	 */
	protected void loadLevels() {
		levels = new ArrayList<InfoCountryLevels>();
		
		List<Object[]> lst = entityManager.createQuery("select ws.id, cs.level, cs.name.name1, cs.name.name2 " +
				"from CountryStructure cs " +
				"inner join cs.workspace ws")
				.getResultList();

		for (Object[] val: lst) {
			InfoCountryLevels levelws = levelByWorkspace((Integer)val[0]);
			
			int level = (Integer)val[1];
			
			levelws.addName(level, (String)val[2], (String)val[3]);
		}
	}


	/**
	 * Search a level info by its workspace id
	 * @param wsid
	 * @return
	 */
	public InfoCountryLevels levelByWorkspace(int wsid) {
		for (InfoCountryLevels level: levels) {
			if (level.getWorkspaceId() == wsid) {
				return level;
			}
		}
		
		InfoCountryLevels level = new InfoCountryLevels(wsid);
		levels.add(level);
		
		return level;
	}
}
