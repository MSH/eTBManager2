package org.msh.tb.workspaces;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.tasks.TaskManager;
import org.msh.tb.entities.*;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copy data from one workspace into another
 * @author Ricardo Memoria
 *
 */
@Name("workspaceCopy")
public class WorkspaceCopy {

	@In(required=true) WorkspaceHome workspaceHome;
	@In EntityManager entityManager;
	@In TaskManager taskManager;

	private Workspace sourceWorkspace;
	private boolean adminUnits;
	private boolean healthSystems;
	private boolean tbunits;
	private boolean sources;
	private boolean substances;
	private boolean medicines;
	private boolean regimens;
	private boolean laboratories;
	private boolean ageRanges;
	private boolean tbfields;
	private boolean users;
	private boolean profiles;
	
	private List<Workspace> options;
	

	/**
	 * Copy data from the workspace set in sourceWorkspace property to the workspace set in workspaceHome component
	 * @return
	 */
	public String copy() {
		if (sourceWorkspace == null) 
			return "error";

		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("sourceWorkspace", sourceWorkspace);
		params.put("targetWorkspace", workspaceHome.getInstance());
		
		if (adminUnits)
			params.put(AdministrativeUnit.class.getSimpleName(), Boolean.TRUE);
		
		if (healthSystems)
			params.put(HealthSystem.class.getSimpleName(), Boolean.TRUE);
		
		if (tbunits)
			params.put(Tbunit.class.getSimpleName(), Boolean.TRUE);
		
		if (sources)
			params.put(Source.class.getSimpleName(), Boolean.TRUE);
		
		if (substances)
			params.put(Substance.class.getSimpleName(), Boolean.TRUE);
		
		if (medicines)
			params.put(Medicine.class.getSimpleName(), Boolean.TRUE);
		
		if (regimens)
			params.put(Regimen.class.getSimpleName(), Boolean.TRUE);
		
		if (laboratories)
			params.put(Laboratory.class.getSimpleName(), Boolean.TRUE);
		
		if (users)
			params.put(UserWorkspace.class.getSimpleName(), Boolean.TRUE);
		
		if (profiles)
			params.put(UserProfile.class.getSimpleName(), Boolean.TRUE);
		
		if (tbfields)
			params.put(FieldValue.class.getSimpleName(), Boolean.TRUE);
		
		taskManager.runTask(WorkspaceCopyTask.class, params);
		return "copied";
	}

	
	public boolean isAdminUnits() {
		return adminUnits;
	}

	public void setAdminUnits(boolean adminUnits) {
		this.adminUnits = adminUnits;
	}

	public boolean isHealthSystems() {
		return healthSystems;
	}

	public void setHealthSystems(boolean healthSystems) {
		this.healthSystems = healthSystems;
	}

	public boolean isTbunits() {
		return tbunits;
	}

	public void setTbunits(boolean tbunits) {
		this.tbunits = tbunits;
	}

	public boolean isSources() {
		return sources;
	}

	public void setSources(boolean sources) {
		this.sources = sources;
	}

	public boolean isSubstances() {
		return substances;
	}

	public void setSubstances(boolean substances) {
		this.substances = substances;
	}

	public boolean isMedicines() {
		return medicines;
	}

	public void setMedicines(boolean medicines) {
		this.medicines = medicines;
	}

	public boolean isRegimens() {
		return regimens;
	}

	public void setRegimens(boolean regimens) {
		this.regimens = regimens;
	}

	public boolean isLaboratories() {
		return laboratories;
	}

	public void setLaboratories(boolean laboratories) {
		this.laboratories = laboratories;
	}

	public boolean isAgeRanges() {
		return ageRanges;
	}

	public void setAgeRanges(boolean ageRanges) {
		this.ageRanges = ageRanges;
	}

	public boolean isTbfields() {
		return tbfields;
	}

	public void setTbfields(boolean tbfields) {
		this.tbfields = tbfields;
	}

	public boolean isUsers() {
		return users;
	}

	public void setUsers(boolean users) {
		this.users = users;
	}

	public boolean isProfiles() {
		return profiles;
	}

	public void setProfiles(boolean profiles) {
		this.profiles = profiles;
	}

	public Workspace getSourceWorkspace() {
		return sourceWorkspace;
	}

	public void setSourceWorkspace(Workspace sourceWorkspace) {
		this.sourceWorkspace = sourceWorkspace;
	}


	/**
	 * Return list of workspaces to be selected by the user
	 * @return
	 */
	public List<Workspace> getOptions() {
		if (options == null) {
			options = entityManager.createQuery("from Workspace w where w.id != #{workspaceHome.id} " +
					"and exists(select uw.id from UserWorkspace uw where uw.user.id = #{userLogin.user.id} " +
					"and uw.workspace.id = w.id)")
					.getResultList();
		}
		return options;
	}
	
}
