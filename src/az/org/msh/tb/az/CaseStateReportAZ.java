package org.msh.tb.az;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.cases.CaseStateReport;

/**
 * Refine standard report by case state to be displayed in the main page.<br>
 * Add EIDSS import item section
 * @author Alexey Kurasoff
 *
 */
@Name("caseStateReportAZ")
public class CaseStateReportAZ extends CaseStateReport{
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
		super.createItems();
		Item it = new Item();
		it.setDescription(messages.get("TbCase.toThirdCategory"));
		String querySQL = getSQLSelect()+" where cAZ.toThirdCategory = 1";
		BigInteger col3cat = (BigInteger) entityManager.createNativeQuery(querySQL).getSingleResult();
		it.add(col3cat.intValue());
		it.setStateIndex(thirdCat);
		getItems().add(it);
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
		String querySQL = getSQLSelect()+" " + getSQLEIDSSOnly() + " and " + getSQLHasUnit();
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
		return "(c.CURR_ADMINUNIT_ID is null) and (c.NOTIF_ADMINUNIT_ID is null) and (c.NOTIFICATION_UNIT_ID is null)";
	}
	/**
	 * this case binded to at least one unit
	 * @return
	 */
	private String getSQLHasUnit() {
		return "((not c.CURR_ADMINUNIT_ID is null) or (not c.NOTIF_ADMINUNIT_ID is null) or (not c.NOTIFICATION_UNIT_ID is null))";
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
