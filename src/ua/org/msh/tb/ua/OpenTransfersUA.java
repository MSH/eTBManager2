package org.msh.tb.ua;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.tb.login.UserSession;
//Inverse order in list
@Name("openTransfersUA")
public class OpenTransfersUA {

	@In EntityManager entityManager;

	private List<Transfer> receivedTransfers;
	private List<Transfer> sentTransfers;


	/**
	 * Create the list of open orders of the selected unit
	 */
	protected void createTransferLists() {
		String hql = "from Transfer a " +
			"join fetch a.unitFrom uf join fetch a.unitTo ut " +
			"join fetch uf.adminUnit join fetch ut.adminUnit " +
			"where (a.unitFrom.id = :unitid or a.unitTo.id = :unitid) " +
			"and a.status = :stWAITREC " +
			"order by a.shippingDate desc"; 
		
		UserSession userSession = (UserSession)Component.getInstance("userSession");
		
		Tbunit unit = userSession.getTbunit();
		
		List<Transfer> lst = entityManager
			.createQuery(hql)
			.setParameter("unitid", userSession.getTbunit().getId())
			.setParameter("stWAITREC", TransferStatus.WAITING_RECEIVING)
			.getResultList();
		
		receivedTransfers = new ArrayList<Transfer>();
		sentTransfers = new ArrayList<Transfer>();
		
		for (Transfer item: lst) {
			if (item.getUnitTo().equals(unit))
				receivedTransfers.add(item);
			else
			if (item.getUnitFrom().equals(unit))
				sentTransfers.add(item);
		}
	}


	/**
	 * @return the receivedTransfers
	 */
	public List<Transfer> getReceivedTransfers() {
		if (receivedTransfers == null)
			createTransferLists();
		return receivedTransfers;
	}


	/**
	 * @return the sentTransfers
	 */
	public List<Transfer> getSentTransfers() {
		if (sentTransfers == null)
			createTransferLists();
		return sentTransfers;
	}

}
