/**
 * 
 */
package org.msh.tb.tbunits;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.tbunits.UnitServices.StatisticItem;
import org.msh.tb.transactionlog.TransactionLogService;

/**
 * General controller used in UI for TB unit operations
 * 
 * @author Ricardo Memoria
 *
 */
@Name("unitController")
@Scope(ScopeType.CONVERSATION)
public class UnitController {

	public enum UnitTransferType { CASE, USER };
	
	@In UnitServices unitServices;
	@In TbUnitHome tbunitHome;
	
	private TBUnitSelection unitSelection;
	private List<UserWorkspace> users;
	private Map<StatisticItem, Long> caseStatistics;
	
	private boolean executed;
	private UnitTransferType transferType;

	
	/**
	 * Start the case transfer
	 */
	@Begin
	public void startCaseTransfer() {
		unitSelection = null;
		transferType = UnitTransferType.CASE;
	}
	
	
	/**
	 * Start the user transfer
	 */
	@Begin
	public void startUserTransfer() {
		unitSelection = null;
		transferType = UnitTransferType.USER;
	}

	/**
	 * Transfer all cases from one unit to another
	 * @return
	 */
	public String executeTransfer() {
		Tbunit destUnit = getUnitSelection().getTbunit();

		if ((transferType == null) || (destUnit == null)) {
			return "error";
		}
		
		if (destUnit.getId().equals( tbunitHome.getInstance() )) {
			return "error";
		}


		String roleAlias;
		
		if (transferType == UnitTransferType.CASE) {
			unitServices.transferCases(tbunitHome.getInstance(), unitSelection.getTbunit());
			roleAlias = "UNIT_CASESTRANS";
		}
		else {
			unitServices.transferUsers(tbunitHome.getInstance(), unitSelection.getTbunit());
			roleAlias = "UNIT_USERSTRANS";
		}
		executed = true;
		
		TransactionLogService log = TransactionLogService.instance();
		log.addTableRow("meds.movs.from", tbunitHome.getInstance().getName().toString());
		log.addTableRow("meds.movs.to", destUnit.getName().toString());
		log.saveExecuteTransaction(roleAlias, tbunitHome.getInstance());
		Conversation.instance().end();
		
		return "success";
	}

	
	/**
	 * Return the object containing the unit selected by the user 
	 * @return instance of {@link TBUnitSelection}
	 */
	public TBUnitSelection getUnitSelection() {
		if (unitSelection == null) {
			unitSelection = new TBUnitSelection("unitId");
		}
		return unitSelection;
	}

	/**
	 * Return the list of users assigned to this unit
	 * @return the users
	 */
	public List<UserWorkspace> getUsers() {
		if (users == null) {
			users = unitServices.getUsers(tbunitHome.getInstance());
		}
		return users;
	}
	
	
	/**
	 * Return the number of cases owned or notified by this unit
	 * @return long value
	 */
	public Long getNumCases() {
		return getCaseStatistics().get(StatisticItem.UNIT_CASES);
	}
	
	
	/**
	 * Return the number of cases treated or on treatment in this unit
	 * @return long value
	 */
	public Long getTreatedCases() {
		return getCaseStatistics().get(StatisticItem.TREATED_CASES);
	}
	
	/**
	 * Return the statistics about the case
	 * @return map containing the {@link StatisticItem} and the quantity in long type
	 */
	public Map<StatisticItem, Long> getCaseStatistics() {
		if (caseStatistics == null) {
			caseStatistics = unitServices.getCaseStatistics(tbunitHome.getInstance());
		}
		return caseStatistics;
	}

	/**
	 * @return the executed
	 */
	public boolean isExecuted() {
		return executed;
	}

	/**
	 * @param executed the executed to set
	 */
	public void setExecuted(boolean executed) {
		this.executed = executed;
	}


	/**
	 * @return the transferType
	 */
	public UnitTransferType getTransferType() {
		return transferType;
	}
}
