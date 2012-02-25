package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.msh.tb.application.mail.MailService;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserLogin;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.entities.enums.UserView;

/**
 * Component responsible to send asynchronous messages to the users based on order events
 * @author Ricardo Memoria
 *
 */
@Name("orderMsgDispatcher")
public class OrderMsgDispatcher {

	@In EntityManager entityManager;
	@In(required=true) OrderHome orderHome;
	
	/**
	 * Send a message to the users notifying about a new order created
	 */
	@Observer("new-medicine-order")
	public void notifyNewOrder() {
		Order order = orderHome.getInstance();

		if (order.getStatus() == OrderStatus.WAITAUTHORIZING) {
			List<User> users = getAuthorizerUsers();
			if (!users.contains(order.getUserCreator()))
				users.add(order.getUserCreator());

			sendMessage(users, "/mail/neworder.xhtml");
		}

		if (order.getStatus() == OrderStatus.WAITSHIPMENT) {
			List<User> users = getUsersToShip();
			sendMessage(users, "/mail/ordertoship.xhtml");
			
			users = new ArrayList<User>();
			users.add(order.getUserCreator());
			sendMessage(users, "/mail/neworder.xhtml");
		}
		MailService.instance().dispatchQueue();
	}

	
	/**
	 * Send a new e-mail message when the order is authorized. The message is sent to the user
	 * that created the order and the user that authorized the order
	 */
	@Observer("medicine-order-authorized")
	public void notifyAuthorizedOrder() {
		Order order = orderHome.getInstance();

		// notify users about order approved
		List<User> users = new ArrayList<User>();
		users.add( order.getUserCreator() );
		
		User user = getCurrentUser();
		if ((user != null) && (!users.contains(user)))
			users.add( user );
		
		sendMessage(users, "/mail/orderauthorized.xhtml");

		// notify users that are in charge of shipment
		users = getUsersToShip();
		sendMessage(users, "/mail/ordertoship.xhtml");

		MailService.instance().dispatchQueue();
	}

	
	/**
	 * Notify order that was shipped
	 */
	@Observer("medicine-order-shipped")
	public void notifyShippedOrder() {
		Order order = orderHome.getInstance();

		MailService srv = MailService.instance();
		
		srv.addComponent("order", order);
		srv.addComponent("orderHome", orderHome);

		List<User> users = new ArrayList<User>();
		users.add(order.getUserCreator());

		User user = getCurrentUser();
		if ((user != null) && (!users.contains(user)))
			users.add(user);
		
		sendMessage(users, "/mail/ordershipped.xhtml");
		MailService.instance().dispatchQueue();
	}

	
	/**
	 * Return the current user logged into the system
	 * @return
	 */
	public User getCurrentUser() {
		UserLogin userLogin = (UserLogin)Component.getInstance("userLogin");
		return (userLogin != null? userLogin.getUser(): null);
	}


	/**
	 * Return a list of users in charge of authorizing the order
	 * @return
	 */
	protected List<User> getAuthorizerUsers() {
		Tbunit unit = orderHome.getInstance().getUnitTo().getAuthorizerUnit();
		if (unit == null)
			return null;

		return getUsersByRoleAndUnit(13, unit);
	}
	
	
	/**
	 * Return the list of users that have permission to a particular role in a unit
	 * @param roleid
	 * @param unit
	 * @return
	 */
	protected List<User> getUsersByRoleAndUnit(Integer roleid, Tbunit unit) {
		String code = unit.getAdminUnit().getCode();
		String s = "";
		while (code != null) {
			if (!s.isEmpty())
				s += ",";
			s += "'" + code + "'";
			code = AdministrativeUnit.getParentCode(code);
		}
		
		String hql = "select u.user from UserWorkspace u where u.profile.id in " +
				"(select distinct p.id from UserProfile p join p.permissions perm where perm.userRole.id=13 " +
				"and p.workspace.id = #{defaultWorkspace.id} and perm.canExecute=:p) " +
				"and (u.view = :viewcountry or (u.view = :viewunit and u.tbunit.id = :unitid) " +
				"or (u.view = :viewadm and u.tbunit.adminUnit.code in (" + s + ")))";

		List<User> lst = entityManager.createQuery(hql)
			.setParameter("p", true)
			.setParameter("viewcountry", UserView.COUNTRY)
			.setParameter("viewunit", UserView.TBUNIT)
			.setParameter("unitid", unit.getId())
			.setParameter("viewadm", UserView.ADMINUNIT)
			.getResultList();
		
		return lst;
	}


	/**
	 * Return a list of users in charge of authorizing the order
	 * @return
	 */
	public List<User> getUsersToShip() {
		return getUsersByRoleAndUnit(14, orderHome.getInstance().getUnitTo());
	}

	
	/**
	 * Send a message to a list of users using its default language and time zone
	 * @param users
	 * @param mailpage
	 */
	public void sendMessage(List<User> users, String mailpage) {
		MailService srv = MailService.instance();
		
		// avoid lazy initialization problem
		orderHome.getInstance().getUnitFrom().getAdminUnit().getParents();
		orderHome.getSources();
		
		for (User user: users) {
			srv.addComponent("user", user);
			srv.addComponent("order", orderHome.getInstance());
			srv.addComponent("orderHome", orderHome);
			srv.addMessageToQueue(mailpage, user.getTimeZone(), user.getLanguage(), true);
		}
	}


	/**
	 * Return the instance of the OrderMsgDispatcher in the current context
	 * @return
	 */
	public static OrderMsgDispatcher instance() {
		return (OrderMsgDispatcher)Component.getInstance("orderMsgDispatcher");
	}
}
