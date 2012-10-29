package org.msh.tb.ua;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.msh.tb.adminunits.AdminUnitGroup;
import org.msh.tb.adminunits.AdminUnitSelection;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseStateReport;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;

/**
 * Generate report by case state to be displayed in the main page
 * @author Ricardo Memoria
 * Changed by Alex Kurasoff, because proposed case classification refused by UA users
 *
 */
@Name("caseStateReportUA")
@Scope(ScopeType.SESSION)
@Synchronized(timeout=20000)
public class CaseStateReportUA extends CaseStateReport{

	@In Workspace defaultWorkspace;
	@In(create=true) UserWorkspace userWorkspace;
	
	private AdminUnitSelection selectedAdmUnitSel;


	public List<AdminUnitGroup<HealthUnitInfoUA>> getAdminUnitUA(){
		HealthUnitsQueryUA healthUnitsQuery = (HealthUnitsQueryUA)Component.getInstance("healthUnitsQueryUA", true);
		if (getSelectedAdmUnit()!=null){
			List<AdminUnitGroup<HealthUnitInfoUA>> lst2 = new ArrayList<AdminUnitGroup<HealthUnitInfoUA>>();
			for (AdminUnitGroup<HealthUnitInfoUA> adm: healthUnitsQuery.getAdminUnitsUA()) {
				//if (getSelectedAdmUnitSel().getParentUnits().size()==1){
					if (adm.getAdminUnit().equals(getSelectedAdmUnit()))
						lst2.add(adm);
					/*}else if (getSelectedAdmUnitSel().getParentUnits().size()==2)
						if (findUnit(adm)!=-1){
							if (lst2.isEmpty())	
								{
								AdminUnitGroup<HealthUnitInfo> ad = new AdminUnitGroup<HealthUnitInfo>();
								ad.setAdminUnit(adm.getAdminUnit());adm.getAdminUnit().getCountryStructure();
								lst2.add(ad);
								}
							lst2.get(0).getItems().add(adm.getItems().get(findUnit(adm)));
						}*/
			}
			return lst2;
		}
		return healthUnitsQuery.getAdminUnitsUA();
	}
	
	/*private void addUnit(List<AdminUnitGroup<HealthUnitInfo>> lst2,adm){
		for (int i = lst2.size(); i < array.length; i++) {
			
		}
	}*/
	
	/*private int findUnit(AdminUnitGroup<HealthUnitInfo> adm){
		for (int i = 0; i < adm.getItems().size(); i++) {
			EntityManager em = (EntityManager) Component.getInstance("entityManager");
			Tbunit tu = (Tbunit) em.find(Tbunit.class, adm.getItems().get(i).getUnitId());
			if (tu.getAdminUnit().equals(getSelectedAdmUnit()))
				return i;
		}
		return -1;
	}*/
	

	/**
	 * Generate SQL condition to filter cases by user view
	 * @return
	 */
	protected String generateSQLConditionByUserView() {
		switch (userWorkspace.getView()) {
		case ADMINUNIT: 
			return " and (a.code like '" + getSelectedAdmUnit().getCode() + "%')"; 
		case TBUNIT: 
			return " and u.id = " + userWorkspace.getTbunit().getId();
		default: return "";
		}
	}
	
	public AdministrativeUnit getSelectedAdmUnit() {
		if (userWorkspace.getView().equals(UserView.ADMINUNIT))
				userWorkspace.getAdminUnit();
		return getSelectedAdmUnitSel().getSelectedUnit();
	}


	public void setSelectedAdmUnitSel(AdminUnitSelection selectedAdmUnitSel) {
		this.selectedAdmUnitSel = selectedAdmUnitSel;
	}


	public AdminUnitSelection getSelectedAdmUnitSel() {
		if (selectedAdmUnitSel==null)
			//if (!selectedAdmUnitSel.isAlreadySelected())
			{
				selectedAdmUnitSel = new AdminUnitSelection(true);
				selectedAdmUnitSel.setSelectedUnit(userWorkspace.getAdminUnit());
			}
			/*else {
				selectedAdmUnitSel = new AdminUnitSelection();
			}*/
		return selectedAdmUnitSel;
	}
	
	/**
	 * Create items of the report
	 */
	@Override
	public void createItems() {
		items = new ArrayList<CaseStateItem>();
		setValidationItems(new ArrayList<ValidationItem>());

		String aucond;
		if (userWorkspace.getView() == UserView.ADMINUNIT)
			aucond = "inner join administrativeunit a on a.id = u.adminunit_id ";
		else aucond = "";

		String cond = generateSQLConditionByUserView();

		String condByCase = generateSQLConditionByCase();

		Integer hsID = null;
		if (userWorkspace.getHealthSystem() != null)
			hsID = userWorkspace.getHealthSystem().getId();

		String sql = generateSQL(aucond, cond, condByCase, hsID);

		List<Object[]> lst = App.getEntityManager().createNativeQuery(sql).getResultList();

		setTotal(new Item(App.getMessage("global.total"), 0));

		for (Object[] val: lst) {
			int qty = ((BigInteger)val[3]).intValue();

			DiagnosisType diagType;
			if (val[2] != null)
				diagType = DiagnosisType.values()[(Integer)val[2]];
			else diagType = DiagnosisType.CONFIRMED;
			ValidationState vs = ValidationState.values()[(Integer)val[1]];

			Item item = findItem(CaseState.values()[(Integer)val[0]], diagType);
			item.add(qty);
			getTotal().add(qty);

			if (!ValidationState.VALIDATED.equals(vs)) {
				ValidationItem valItem = findValidationItem(vs);
				valItem.add(qty);
			}
		}

		Collections.sort(items, new Comparator<CaseStateItem>() {

			public int compare(CaseStateItem o1, CaseStateItem o2) {
				Integer cs1 = o1.getStateIndex();
				Integer cs2 = o2.getStateIndex();
				if (cs1 == null)
					return 1;
				if (cs2 == null)
					return -1;

				return cs1.compareTo(cs2);
			}

		});
	}


	protected String generateSQL(String aucond, String cond, String condByCase, Integer hsID) {
		String res = "select c.state, c.validationState, c.diagnosisType, count(*) " +
		"from tbcase c " +
		"inner join tbunit u on u.id = c.notification_unit_id " + aucond +
		" where u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase + // c.diagnosisType in (0,1) and 
		(hsID != null? "and u.healthSystem_id = " + hsID.toString(): "") +
		" group by c.state, c.validationState, c.diagnosisType";
		return res;
	}
}
