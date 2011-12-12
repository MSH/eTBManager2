package org.msh.tb.medicines.orders;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.application.MailService;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.User;

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
	public void notifyNewOrder() {
		Order order = orderHome.getInstance();
		
		List<User> users = getAuthorizerUsers();
		if (!users.contains(order.getUserCreator()))
			users.add(order.getUserCreator());

		MailService srv = MailService.instance();
		
		srv.addComponent("order", order);
		srv.addComponent("orderHome", orderHome);

		// avoid lazy initialization problem
		order.getUnitFrom().getAdminUnit().getParents();
		orderHome.getSources();

		for (User user: users) {
			srv.addComponent("user", user);
			srv.sendLocalizedMessage("/mail/neworder.xhtml", user.getTimeZone(), user.getLanguage());
		}
	}


	/**
	 * Return a list of users in charge of authorizing the order
	 * @return
	 */
	public List<User> getAuthorizerUsers() {
		String hql = "select u.user from UserWorkspace u where u.profile.id in " +
				"(select distinct p.id from UserProfile p join p.permissions perm where perm.userRole.id=13 " +
				"and p.workspace.id = #{defaultWorkspace.id} and perm.canExecute=:p)";

		List<User> lst = entityManager.createQuery(hql).setParameter("p", true).getResultList();
		
		return lst;
	}
	
	
	/**
	 * Return the instance of the OrderMsgDispatcher in the current context
	 * @return
	 */
	public static OrderMsgDispatcher instance() {
		return (OrderMsgDispatcher)Component.getInstance("orderMsgDispatcher");
	}
}
