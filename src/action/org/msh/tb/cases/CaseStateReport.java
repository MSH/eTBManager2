package org.msh.tb.cases;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.misc.GlobalLists;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.Workspace;
import org.msh.tb.entities.Tag.TagType;
import org.msh.tb.entities.enums.CaseClassification;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.DiagnosisType;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.login.UserSession;

/**
 * Generate report by case state to be displayed in the main page
 * @author Ricardo Memoria
 *
 */
@Name("caseStateReport")
public class CaseStateReport {

	@In
	protected EntityManager entityManager;
	@In Workspace defaultWorkspace;
	@In(create=true) UserSession userSession;
	@In(create=true) UserWorkspace userWorkspace;
	
	
	protected List<Item> items;
	private List<ValidationItem> validationItems;
	private Item total;
	protected Map<String, String> messages;
	private List<Item> tags;


	/**
	 * Return list of consolidated values by case state
	 * @return
	 */
	public List<Item> getItems() {
		if (items == null)
			createItems();
		return items;
	}


	/**
	 * Create items of the report
	 */
	public void createItems() {
		messages = Messages.instance();

		items = new ArrayList<Item>();
		validationItems = new ArrayList<ValidationItem>();
		
		String aucond;
		if (userWorkspace.getView() == UserView.ADMINUNIT)
			 aucond = "inner join administrativeunit a on a.id = u.adminunit_id ";
		else aucond = "";

		String cond = generateSQLConditionByUserView();

		String condByCase = generateSQLConditionByCase();

		Integer hsID = null;
		if (userWorkspace.getHealthSystem() != null)
			hsID = userWorkspace.getHealthSystem().getId();
		
		/*String sql = "select c.state, c.validationState, c.diagnosisType, count(*) " +
				"from tbcase c " +
				"inner join tbunit u on u.id = c.notification_unit_id " + aucond +
				"where c.state not in (" + CaseState.ONTREATMENT.ordinal() + ',' + CaseState.TRANSFERRING.ordinal() + ") " +
				(hsID != null? "and u.healthSystem_id = " + hsID.toString(): "") +
				" and u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase +
				" group by c.state, c.validationState, c.diagnosisType " +
				"union " +
				"select c.state, c.validationState, 1, count(*) " +
				"from tbcase c " +
				"inner join tbunit u on u.id = c.treatment_unit_id " + aucond + 
				"where c.state in (" + CaseState.ONTREATMENT.ordinal() + ',' + CaseState.TRANSFERRING.ordinal() + ") " + 
				" and u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase +
				(hsID != null? " and u.healthSystem_id = " + hsID.toString(): "") +
				" group by c.state, c.validationState";*/
		
		String sql = "select c.state, c.validationState, c.diagnosisType, count(*) " +
		"from tbcase c " +
		"inner join tbunit u on u.id = c.notification_unit_id " + aucond +
		"where c.state not in (" + CaseState.ONTREATMENT.ordinal() + ',' + CaseState.TRANSFERRING.ordinal() + ") " +
		(hsID != null? "and u.healthSystem_id = " + hsID.toString(): "") +
		" and u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase +
		" group by c.state, c.validationState, c.diagnosisType " +
		"union " +
		"select c.state, c.validationState, c.diagnosisType, count(*) " +
		"from tbcase c " +
		"inner join tbunit u on u.id = c.treatment_unit_id " + aucond + 
		"where c.state in (" + CaseState.ONTREATMENT.ordinal() + ',' + CaseState.TRANSFERRING.ordinal() + ")"+
		" and u.workspace_id = " + defaultWorkspace.getId() + cond + condByCase +
		(hsID != null? " and u.healthSystem_id = " + hsID.toString(): "") +
		" group by c.state, c.validationState";
		
		List<Object[]> lst = entityManager.createNativeQuery(sql).getResultList();
		
		total = new Item();
		total.setDescription(messages.get("global.total"));
		
		for (Object[] val: lst) {
			int qty = ((BigInteger)val[3]).intValue();
			
			DiagnosisType diagType;
			if (val[2] != null)
				diagType = DiagnosisType.values()[(Integer)val[2]];
			else diagType = DiagnosisType.CONFIRMED;
			ValidationState vs = ValidationState.values()[(Integer)val[1]];

			Item item = findItem(CaseState.values()[(Integer)val[0]], diagType);
			item.add(qty);
			total.add(qty);
			
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
	}


	/**
	 * Generate SQL condition to filter cases by user view
	 * @return
	 */
	protected String generateSQLConditionByUserView() {
		if (userWorkspace.getView() == null)
			return "";

		switch (userWorkspace.getView()) {
		case ADMINUNIT: 
			return " and (a.code like '" + userWorkspace.getAdminUnit().getCode() + "%')"; 
		case TBUNIT: 
			return " and u.id = " + userWorkspace.getTbunit().getId();
		default: return "";
		}
	}
	
	/**
	 * Generate SQL condition to filter cases
	 * @return SQL condition to be used in a where clause
	 */
	protected String generateSQLConditionByCase() {
		CaseClassification[] classifs = ((GlobalLists)Component.getInstance("globalLists")).getCaseClassifications();
		
		String caseCondition = "";

		for (CaseClassification cla: classifs) {
			boolean hasClassif = userSession.isCanOpenCaseByClassification(cla);
			if (hasClassif) {
				if (!caseCondition.isEmpty())
					caseCondition += ",";
				 caseCondition += cla.ordinal();
			}
		}
		
		if (!caseCondition.isEmpty())
			 return " and c.classification in (" + caseCondition + ")";
		else return caseCondition;
		
/*		boolean tbcases = userSession.isCanOpenCaseByClassification(CaseClassification.TB);
		boolean mdrcases = userSession.isCanOpenMDRTBCases();
		
		if (tbcases && mdrcases) {
			return "";
		}
		
		String caseCondition = "";
		
		if (tbcases)
			 caseCondition = " and (c.classification = " + CaseClassification.TB.ordinal() + ")";
		else
		if (mdrcases)
			caseCondition = " and (c.classification = " + CaseClassification.DRTB.ordinal() + ")";
		
		return caseCondition;
*/
	}

	
	/**
	 * Return report by tags
	 * @return
	 */
	public List<Item> getTags() {
		if (tags == null)
			createTagsReport();
		return tags;
	}
	
	
	/**
	 * Generate the consolidated tag report displayed at the left side of the home page in the case management module
	 */
	protected void createTagsReport() {
		Workspace workspace = (Workspace)Component.getInstance("defaultWorkspace");

		String s;
		switch (userWorkspace.getView()) {
		case TBUNIT: s = "inner join tbcase c on c.id=tc.case_id inner join tbunit u on u.id = c.treatment_unit_id ";
			break;
		case ADMINUNIT: s = "inner join tbcase c on c.id=tc.case_id inner join tbunit u on u.id = c.treatment_unit_id inner join administrativeunit a on a.id = u.adminunit_id";
			break;
		default: s = "";
		}
		
		String sql = "select t.id, t.tag_name, t.sqlCondition is null, t.consistencyCheck, count(*) " +
			"from tags_case tc inner join tag t on t.id = tc.tag_id " + s +
			" where t.workspace_id = :id " + generateSQLConditionByUserView() + 
			" group by t.id, t.tag_name order by t.tag_name";
		
		List<Object[]> lst = entityManager.createNativeQuery(sql)
				.setParameter("id", workspace.getId())
				.getResultList();

		tags = new ArrayList<Item>();
		for (Object[] vals: lst) {
			Item item = new Item();
			item.setStateIndex((Integer)vals[0]);
			item.setDescription(vals[1].toString());
			if ((Integer)vals[2] == 1) 
				item.setTagType(TagType.MANUAL);
			else {
				if ((Boolean)vals[3] == Boolean.TRUE)
					 item.setTagType(TagType.AUTOGEN_CONSISTENCY);
				else item.setTagType(TagType.AUTOGEN);
			}
			item.setCases(((BigInteger)vals[4]).intValue());
			tags.add(item);
		}
	}
	

	/**
	 * Return an item from the validation list from its validation state
	 * @param state
	 * @return Instance of {@link ValidationState}
	 */
	private ValidationItem findValidationItem(ValidationState state) {
		for (ValidationItem item: validationItems) {
			if (item.getValidationState().equals(state)) {
				return item;
			}
		}
		
		ValidationItem item = new ValidationItem();
		item.setValidationState(state);
		validationItems.add(item);
		return item;
	}


	/**
	 * Search for a specific item based on the state
	 * @param state
	 * @return
	 */
	private Item findItem(CaseState state, DiagnosisType diagType) {
		int stateIndex = state.ordinal();
		String desc = null;
		
		if (state.ordinal() >= CaseState.CURED.ordinal()) {
			stateIndex = 100;
			desc = messages.get("cases.closed");
		}
		else
		if ((state == CaseState.WAITING_TREATMENT) && (diagType == DiagnosisType.SUSPECT)) {
			stateIndex = 200;
			desc = messages.get("CaseState.NOT_ON_TREATMENT");
		}
		//VR: additional registered-cases categories
		else
			if((state == CaseState.ONTREATMENT) && (diagType == DiagnosisType.SUSPECT)){
				stateIndex = 300;
				desc = messages.get("cases.suspectOnTreatment");
			}
		else
			if ((state == CaseState.ONTREATMENT) && (diagType == DiagnosisType.CONFIRMED)){
				stateIndex = 400;	
				desc = messages.get("cases.confirmedOnTreatment");
				}
		else
			if ((state == CaseState.WAITING_TREATMENT) && (diagType == DiagnosisType.CONFIRMED)){
				stateIndex = 500;	
				desc = messages.get("cases.confirmedNotOnTreatment");
				}
		else
			if (state == CaseState.TRANSFERRED_OUT){
				stateIndex = 600;	
				desc = messages.get("CaseState.TRANSFERRING");
					}
		// END
		else {
			desc = messages.get(state.getKey());
			stateIndex = state.ordinal();
		}

		for (Item item: items) {
			if (item.getStateIndex() == stateIndex)
				return item;
		}
		
		Item item = new Item();
		item.setState(state);
		item.setDescription(desc);
		item.setStateIndex(stateIndex);
		
		items.add(item);
		
		return item;
	}

	/**
	 * @return the total
	 */
	public Item getTotal() {
		if (total == null)
			createItems();
		return total;
	}

	
	/**
	 * Return list of items to be Validated
	 * @return List of {@link ValidationItem} instances
	 */
	public List<ValidationItem> getValidationItems() {
		if (validationItems == null)
			createItems();
		return validationItems;
	}
	

	/**
	 * Store consolidated information about cases under validation
	 * @author Ricardo Memoria
	 *
	 */
	public class ValidationItem {
		private ValidationState validationState;
		private long cases;

		public ValidationState getValidationState() {
			return validationState;
		}
		public void setValidationState(ValidationState validationState) {
			this.validationState = validationState;
		}
		public long getCases() {
			return cases;
		}
		public void setCases(long cases) {
			this.cases = cases;
		}
		public void addCases(long num) {
			cases += num;
		}
	}


	/**
	 * Store consolidated information about a case state
	 * @author Ricardo Memoria
	 *
	 */
	public class Item {
		private String description;
		private CaseState state;
		private int cases;
		private int stateIndex;
		private TagType tagType;

		public void add(int val) {
			cases += val;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		/**
		 * @return the state
		 */
		public CaseState getState() {
			return state;
		}
		/**
		 * @param state the state to set
		 */
		public void setState(CaseState state) {
			this.state = state;
		}
		/**
		 * @return the cases
		 */
		public int getCases() {
			return cases;
		}
		/**
		 * @param cases the cases to set
		 */
		public void setCases(int cases) {
			this.cases = cases;
		}
		/**
		 * @return the stateIndex
		 */
		public int getStateIndex() {
			return stateIndex;
		}
		/**
		 * @param stateIndex the stateIndex to set
		 */
		public void setStateIndex(int stateIndex) {
			this.stateIndex = stateIndex;
		}
		/**
		 * @return the autoGenerated
		 */
		/**
		 * @return the tagType
		 */
		public TagType getTagType() {
			return tagType;
		}
		/**
		 * @param tagType the tagType to set
		 */
		public void setTagType(TagType tagType) {
			this.tagType = tagType;
		}
	}
}
