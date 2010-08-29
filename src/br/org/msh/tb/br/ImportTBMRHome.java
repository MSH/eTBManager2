package org.msh.tb.br;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.UserLogin;
import org.msh.mdrtb.entities.Workspace;

@Name("importTBMRHome")
public class ImportTBMRHome {

	@In EntityManager entityManager;
	@In(required=true) Workspace defaultWorkspace;
	@In(create=true) ImportTBMR_DB importTBMRDB;
	@In(required=true) UserLogin userLogin;

	private List<SelectItem> ufList;
	private String uf;

	/**
	 * Call assyncronous component to import cases
	 */
	public void execute() {
		try {
			importTBMRDB.importCases(uf, defaultWorkspace, userLogin);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
