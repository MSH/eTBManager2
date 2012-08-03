package org.msh.tb.br;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.application.tasks.AsyncTask;
import org.msh.tb.application.tasks.TaskManager;

@Name("importTBMRHome")
public class ImportTBMRHome {

	@In EntityManager entityManager;
//	@In(required=true) Workspace defaultWorkspace;
//	@In(create=true) ImportTBMR_DB importTBMRDB;
//	@In(required=true) UserLogin userLogin;
	@In(create=true) TaskManager taskManager;

	private List<SelectItem> ufList;
	private String uf;

	/**
	 * Call assyncronous component to import cases
	 */
	public void execute() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uf", uf);
		params.put("userLogin", Component.getInstance("userLogin"));
		
		taskManager.runTask(ImportTBMRTask.class, params);
		FacesMessages.instance().add("Importação está em andamento... Você pode continuar navegando no sistema");
/*		try {
			importTBMRDB.importCases(uf, defaultWorkspace, userLogin);
		} catch (Exception e) {
			e.printStackTrace();
		}
*/	}


	/**
	 * Check if task is running
	 * @return true if task is running, otherwise return false
	 */
	public boolean isTaskRunning() {
		return taskManager.findTaskByClass(ImportTBMRTask.class) != null; 
	}
	
	
	
	/**
	 * Return the task
	 * @return
	 */
	public AsyncTask getTask() {
		return taskManager.findTaskByClass(ImportTBMRTask.class);
	}
	
	public String getUf() {
		return uf;
	}


	public void setUf(String uf) {
		this.uf = uf;
	}


	public List<SelectItem> getUfList() {
		if (ufList == null) {
			ufList = new ArrayList<SelectItem>();
			
			List<Object[]> lst = entityManager.createQuery("select a.legacyId, a.name.name1 " +
					"from AdministrativeUnit a where a.parent is null " +
					"and a.workspace.id = #{defaultWorkspace.id}")
					.getResultList();

			ufList.add(new SelectItem(null, "-"));
			for (Object[] vals: lst) {
				ufList.add(new SelectItem(vals[0].toString(), vals[1].toString()));
			}
		}
		return ufList;
	}
	

}
