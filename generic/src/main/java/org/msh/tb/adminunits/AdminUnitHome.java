package org.msh.tb.adminunits;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.CountryStructure;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.login.UserSession;
import org.msh.tb.transactionlog.LogInfo;



@Name("adminUnitHome")
@LogInfo(roleName="ADMINUNITS", entityClass=AdministrativeUnit.class)
@Scope(ScopeType.CONVERSATION)
public class AdminUnitHome extends EntityHomeEx<AdministrativeUnit> {
	private static final long serialVersionUID = -2648934215804041377L;

	private AdminUnitSelection auselection;
	private AdminUnitSelection auselectionparent;
	private List<CountryStructure> structures;
	
	@In(create=true) InfoCountryLevels levelInfo;
	@In(required=false) AdminUnitsQuery adminUnits;
	@In(create=true) UserSession userSession;
	@In(required=true) FacesMessages facesMessages;
	
	@Factory("adminUnit")
	public AdministrativeUnit getAdminUnit() {
		return getInstance();
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {
		AdministrativeUnit adminUnit = getInstance();
		//AdministrativeUnit parent = adminUnit.getParent();
		AdministrativeUnit parent = getAuselectionparent().getSelectedUnit();
		String ret = null;
		
		if ((parent != null) && (parent.getLevel() == 5)) {
			facesMessages.add("M�ximum level reached");
			return "error";
		}
		
		if (!isManaged()) {
			if (parent != null) {
				parent.setUnitsCount(parent.getUnitsCount() + 1);
				getEntityManager().persist(parent);
			}

			String code = generateNewCode(parent);
			if (code == null)
				return "error";
			adminUnit.setCode(code);
		}
		
		//If parent is changed
		if(isManaged() && ((parent == null && adminUnit.getParent() != null) 
				|| (parent != null && adminUnit.getParent() == null) ||  (parent != null && adminUnit != null && !parent.getId().equals(adminUnit.getParent().getId())))){
			
			if(parent != null && adminUnit.getId().equals(parent.getId())){
				facesMessages.addFromResourceBundle("admin.adminunits.sameasparent");
				return "error";				
			}
			
			//Validates Levels
			Integer maxSonLevel = (Integer)getEntityManager().createQuery("SELECT max(au.countryStructure.level) FROM AdministrativeUnit au where au.code like :code and au.id <> :auId and au.workspace.id = :workspaceId")
											.setParameter("code", adminUnit.getCode()+"%")
											.setParameter("auId", adminUnit.getId())
											.setParameter("workspaceId", adminUnit.getWorkspace().getId())
											.getSingleResult();
			
			maxSonLevel = (maxSonLevel == null ? new Integer(0) : maxSonLevel);
			int parentLevel = (parent == null ? 0 : parent.getLevel());
			
			if((maxSonLevel.intValue() - adminUnit.getLevel() + 1 + parentLevel) > levelInfo.getMaxLevel()){
				facesMessages.addFromResourceBundle("admin.adminunits.maxlevel");
				return "error";
			}
			
			//Update parent and code and persists
			String oldCode = adminUnit.getCode();
			adminUnit.setParent(parent);
			adminUnit.setCode(generateNewCode(parent));
			ret = super.persist();
			
			//Update code of possible sons
			List<AdministrativeUnit> sons = getEntityManager().createQuery("from AdministrativeUnit where code like :oldCode and workspace.id = :workspaceId")
											.setParameter("oldCode", oldCode+"%")
											.setParameter("workspaceId", adminUnit.getWorkspace().getId())
											.getResultList();
			
			for(AdministrativeUnit au : sons){
				au.setCode(generateNewCode(au.getParent()));
				getEntityManager().merge(au);
				getEntityManager().flush();
			}
			
			

		}else{
			ret = super.persist();
		}
		
		return ret;
	}


	
	/* (non-Javadoc)
	 * @see org.msh.tb.EntityHomeEx#setId(java.lang.Object)
	 */
	@Override
	public void setId(Object id) {
		if(super.getId() == null || !super.getId().equals(id))
			getAuselectionparent().setSelectedUnit(getInstance().getParent());
		super.setId(id);
		structures = null;
		auselection = null;
	}

	
	/**
	 * Return available structures for the selected country based on its parent level
	 * @return List of {@link CountryStructure} instances
	 */
	public List<CountryStructure> getStructures() {
		if (structures == null) {
			int level;
			//AdministrativeUnit parent = getInstance().getParent();
			AdministrativeUnit parent = getAuselectionparent().getSelectedUnit();
			if (parent == null)
				 level = 1;
			else level = parent.getLevel() + 1;

			structures = getEntityManager().createQuery("from CountryStructure c where c.level = :level " +
					"and c.workspace.id = #{defaultWorkspace.id}")
					.setParameter("level", level)
					.getResultList();
		}
		return structures;
	}


	/**
	 * Generate a new code for the new created administrative unit
	 * @param parent the parent administrative unit
	 * @return new code
	 */
	public String generateNewCode(AdministrativeUnit parent) {
		String cond;
		if (parent == null)
			 cond = "where aux.parent is null";
		else cond = "where aux.parent.id = " + parent.getId();
		
		String code = (String)getEntityManager().createQuery("select max(aux.code) from AdministrativeUnit aux " + cond)
			.getSingleResult();

		if (code != null) {
			if (code.length() > 3) {
				int len = code.length();
				code = code.substring(len-3, len);
			}
			code = incCode(code);
			if (code.length() > 3) {
				facesMessages.add("Maximum code length reached for " + getInstance().getName().toString());
				return null;
			}
			
			if (parent != null)
				code = parent.getCode() + code;
		}
		else {
			if (parent == null)
				 code = "001";
			else code = parent.getCode() + "001";
		}
		
		return code;
	}


	protected String incCode(String code) {
		if (code.length() > 3)
			throw new RuntimeException("incCode cannot generate value bigger than 3 digits");
		// transform code to int
		int value = codeToInt(code);
		
		// inc value
		value++;
		
		return intToCode(value);
	}
	
	protected int codeToInt(String code) {
		int index = code.length()-1;
		int value = 0;
		int mult = 1;
		for (int i = index; i >= 0; i--) {
			char c = code.charAt(i);
			int val = 0;
			if ((c >= '0') && (c <= '9'))
				 val = ((int)c) - 48;
			else val = ((int)c) - 65 + 10;

			value += val * mult;
			mult *= 36;
		}

		return value;
	}


	@Override
	public String remove() {
		// check if administrative unit can be removed
		UserWorkspace userWorkspace = getUserWorkspace();
		String code = userWorkspace.getTbunit().getAdminUnit().getCode();

		if (getInstance().isSameOrChildCode(code)) {
			facesMessages.addFromResourceBundle("admin.adminunits.delerror1");
			return "error";
		}
		
		if (isManaged()) {
			AdministrativeUnit aux = getInstance().getParent();
			if (aux != null)
				aux.setUnitsCount(aux.getUnitsCount() - 1);
		}

		if (adminUnits != null) 
			adminUnits.getResultList().remove(getInstance());
		return super.remove();
	}


	/**
	 * Get the administrative unit parent id
	 * @return
	 */
	public Integer getParentId() {
		AdministrativeUnit parent = getInstance().getParent();
		return (parent != null? parent.getId(): null);
	}


	/**
	 * Change the administrative unit parent id 
	 * @param id
	 */
	public void setParentId(Integer id) {
		if (id != null) {
			AdministrativeUnit parent = getEntityManager().find(AdministrativeUnit.class, id);
			getInstance().setParent(parent);
			getAuselectionparent().setSelectedUnit(parent);
		}
		else {
			getInstance().setParent(null);
		}
		
		// update the parent selection
		if (auselection != null)
			auselection.setSelectedUnit(getInstance().getParent());
	}

	
	/**
	 * Return the selection with information about the parent unit
	 * @return
	 */
	public AdminUnitSelection getAuselection() {
		if (auselection == null) {
			auselection = new AdminUnitSelection();
			auselection.setSelectedUnit(getInstance().getParent());
		}
		return auselection;
	}
	
	public void updateCode() {
		//List<Workspace> wslst = getEntityManager().createQuery("from Workspace").getResultList();
		//for (Workspace ws: wslst) {
		//	updateCodeWorkspace(ws);
		//}
		updateCodeWorkspace(UserSession.getUserWorkspace().getWorkspace());
	}
	
	protected void updateCodeWorkspace(Workspace ws) {
		getEntityManager().clear();
		List<AdministrativeUnit> lst = getEntityManager()
			.createQuery("from AdministrativeUnit a where a.workspace.id = :id and a.parent is null")
			.setParameter("id", ws.getId())
			.getResultList();

		int i = 1;
		for (AdministrativeUnit adm: lst) {
			String code = intToCode(i);
			adm.setCode(code);
			updateCodeAdminUnit(adm);
			getEntityManager().persist(adm);
			getEntityManager().flush();
			i++;
		}
	}
	
	protected String intToCode(int val) {
		String result = "";
		while (true) {
			char c;
			int digit = val % 36;
			val = val / 36;
			if (digit <= 9)
				c = Integer.toString(digit).charAt(0);
			else c = (char)(digit + 65 - 10);
			result = c + result;
			if (val == 0)
				break;
		}
		
		result = "000" + result;
		result = result.substring(result.length() - 3);
		return result;
	}
	
	protected void updateCodeAdminUnit(AdministrativeUnit adm) {
		String codeprefix = adm.getCode();
		
		int counter = 1;  
		for (AdministrativeUnit aux: adm.getUnits()) {
			String code = codeprefix + intToCode(counter);
			aux.setCode(code);
			getEntityManager().persist(aux);
			getEntityManager().flush();
			updateCodeAdminUnit(aux);
			counter++;
		}
	}

	public AdminUnitSelection getAuselectionparent() {
		if (auselectionparent == null) {
			auselectionparent = new AdminUnitSelection();
			auselectionparent.setSelectedUnit(getInstance().getParent());
		}
		return auselectionparent;
	}
	
}
