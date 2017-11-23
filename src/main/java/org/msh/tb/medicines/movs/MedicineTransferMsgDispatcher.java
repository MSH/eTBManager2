package org.msh.tb.medicines.movs;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.MsgDispatcher;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.User;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Component responsible to send asynchronous messages to the users based on order events
 * @author Ricardo Memoria
 *
 */
@Name("medicineTransferMsgDispatcher")
public class MedicineTransferMsgDispatcher extends MsgDispatcher{

	@In EntityManager entityManager;
	@In(required=true) TransferHome transferHome;
	
	/**
	 * Send a message to the users notifying about a new case transference
	 */
	@Observer("medicine-new-transfer")
	public void notifyTransference(){		
		Transfer transfer = transferHome.getInstance();
		
		List<User> users = getUsersByRoleAndUnit("TRANSF_REC", transfer.getUnitTo(), true);
				
		addComponent("transfer", transfer);
		addComponent("list", transferHome.getSources());
		
		sendMessage(users, "/mail/newmedtransfer.xhtml");
	}


	/**
	 * Return the instance of the OrderMsgDispatcher in the current context
	 * @return
	 */
	public static MedicineTransferMsgDispatcher instance() {
		return (MedicineTransferMsgDispatcher)Component.getInstance("medicineTransferMsgDispatcher");
	}
}
