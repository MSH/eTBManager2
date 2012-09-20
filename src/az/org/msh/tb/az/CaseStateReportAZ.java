package org.msh.tb.az;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.cases.CaseStateReport;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;

/**
 * Refine standard report by case state to be displayed in the main page.<br>
 * Add EIDSS import item section
 * @author Alexey Kurasoff
 *
 */
@Name("caseStateReportAZ")
public class CaseStateReportAZ extends CaseStateReport{
	@In Workspace defaultWorkspace;
	@In(create=true) UserWorkspace userWorkspace;
	
	public static final int EIDSS_BINDED = 1616;
	public static final int EIDSS_NOT_BINDED = 1515;
	public static final int thirdCat = 700;
	protected List<Item> itemsEIDSS = null;
	

	/**
	 * Return list of consolidated values by case state
	 * @return
	 */
	@Override
	public List<Item> getItems() {
		if (items == null)
			createItems();
		if (itemsEIDSS == null)
			createEIDSSItems();
		return items;
	}
	
	/**
	 * Create items of the report
	 */
	@Override
	public void createItems() {
			messages = Messages.instance();

			items = new ArrayList<Item>();
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
			
			List<Object[]> lst = entityManager.createNativeQuery(sql).getResultList();
			
			setTotal(new Item());
			getTotal().setDescription(messages.get("global.total"));
			
			for (Object[] val: lst) {
				int qty = ((BigInteger)val[3]).intValue();
				
				DiagnosisType diagType;
				if (val[2] != null)
					diagType = DiagnosisType.values()[(Integer)val[2]];
				else continue;
				ValidationState vs = ValidationState.values()[(Integer)val[1]];

				Item item = findItem(CaseState.values()[(Integer)val[0]], diagType);
				item.add(qty);
				getTotal().add(qty);
				
				if (!ValidationState.VALIDATED.equals(vs)) {
					ValidationItem valItem = findValidationItem(vs);
					valItem.addCases(qty);
				}
			}
			
			Collections.sort(items, new Comparator<Item>() {

				public int compare(Item o1, Item o2) {
					CaseState cs1 = o1.getState();
					CaseState cs2 = o2.getState();
					if (cs1 == null)
						return 1;
					if (cs2 == null)
						return -1;
					
					if (cs1.ordinal() > cs2.ordinal())
						return 1;
					if (cs1.ordinal() < cs2.ordinal())
						return -1;
					return 0;
				}
				
			});
			
		//3rd category
		Item it = new Item();
		it.setDescription(messages.get("TbCase.toThirdCategory"));
		String querySQL = getSQLSelect()+" where cAZ.toThirdCategory = 1";
		BigInteger col3cat = (BigInteger) entityManager.createNativeQuery(querySQL).getSingleResult();
		it.add(col3cat.intValue());
		it.setStateIndex(thirdCat);
		getItems().add(it);
	}
	

	protected String generateSQL(String aucond, String cond, String condByCase, Integer hsID) {
		String res = "select c.state, c.validationState, c.diagnosisType, count(*) " +
		"from tbcase c " +
		"inner join tbunit u on u.id = c.notification_unit_id " + aucond +
		" where c.diagnosisType in (0,1) "+(hsID != null? " and u.healthSystem_id = " + hsID.toString(): "") +
		" and u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase +
		" group by c.state, c.validationState, c.diagnosisType";
		return res;
	}
	
	/**
	 * Create additional items related to the EIDSS import
	 */
	private void createEIDSSItems() {
		if (messages == null) 
			messages = Messages.instance();
		itemsEIDSS = new ArrayList<Item>();
		BigInteger importedEmpty = getImportedNotBinded();
		BigInteger importedBusy = getImportedBinded();
		Item it = new Item();
		it.setDescription(messages.get("az_EIDSS_Not_Binded"));
		it.add(importedEmpty.intValue());
		it.setStateIndex(EIDSS_NOT_BINDED);
		itemsEIDSS.add(it);
		Item it1 = new Item();
		it1.setDescription(messages.get("az_EIDSS_Binded"));
		it1.add(importedBusy.intValue());
		it1.setStateIndex(EIDSS_BINDED);
		itemsEIDSS.add(it1);
	}
	/**
	 * Get quantity of EIDSS imported cases related to the notification or cure unit or both
	 * @return
	 */
	private BigInteger getImportedBinded() {
		String querySQL = getSQLSelect()+ " " + getSQLEIDSSOnly() + " and " + getSQLHasUnit();
		return (BigInteger) entityManager.createNativeQuery(querySQL).getSingleResult();
	}



	/**
	 * Get quantity of EIDSS imported cases not related to any health unit
	 * @return
	 */
	private BigInteger getImportedNotBinded() {
		String querySQL = getSQLSelect()+" " + getSQLEIDSSOnly() + " and " + getSQLNoUNITS();
		return (BigInteger) entityManager.createNativeQuery(querySQL).getSingleResult();
	}
	/**
	 * no units in this case, so imported, but not used
	 * @return
	 */
	private String getSQLNoUNITS() {
		return "(c.OWNER_UNIT_ID is null) and (c.NOTIFICATION_UNIT_ID is null)";//"(c.CURR_ADMINUNIT_ID is null) and (c.NOTIF_ADMINUNIT_ID is null) and (c.NOTIFICATION_UNIT_ID is null)";
	}
	/**
	 * this case binded to at least one unit
	 * @return
	 */
	private String getSQLHasUnit() {
		return "((not c.OWNER_UNIT_ID is null) or (not c.NOTIFICATION_UNIT_ID is null))";//"((not c.CURR_ADMINUNIT_ID is null) or (not c.NOTIF_ADMINUNIT_ID is null) or (not c.NOTIFICATION_UNIT_ID is null))";
	}
	/**
	 * Restrict to imported from the EIDSS
	 * @return
	 */
	private String getSQLEIDSSOnly() {
		return "where (not c.legacyId is null)";
	}
	/**
	 * select only azeri cases
	 * @return
	 */
	private String getSQLSelect() {
		return "select count(*) from tbcaseAZ cAZ inner join tbcase c on cAZ.id=c.id";
	}

	public List<Item> getItemsEIDSS() {
		return itemsEIDSS;
	}
	

}
