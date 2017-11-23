package org.msh.tb.medicines.orders;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.OrderComment;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

@Name("orderCommentsHome")
public class OrderCommentHome extends EntityHomeEx<OrderComment>{

	private static final long serialVersionUID = -5419639883224418406L;
	
	@In(create=true) OrderHome orderHome;
	List<OrderComment> comments;
	
	@Factory("orderComment")
	public OrderComment getOrderComment() {
		return getInstance();
	}
	
	/**
	 * Return list of comments of the selected case in {@link OrderHome} instance
	 * @return
	 */
	public List<OrderComment> getComments() {
		if (comments == null)
			comments = createComments();
		return comments;
	}
	
	/**
	 * Add a new comment. The section property must be informed  
	 * @return
	 */
	public String addComment() {
		OrderComment comment = getInstance();
		
		if(comment.getComment().isEmpty() || comment.getComment().trim().isEmpty())
			return "";
		
		comment.setUser(getUser());
		comment.setDate(new Date());
		comment.setOrder(orderHome.getInstance());
		comment.setStatusOnComment(orderHome.getInstance().getStatus());
		
		persist();
		
		if (comments != null) {
			comments.add(0, getInstance());
		}

		// clear instance to avoid comment being displayed after included
		clearInstance();
		Contexts.getEventContext().set("orderComment", getInstance());
		
		return "comment-added";
	}
	
	/**
	 * Create a list of comments from. The first time the list is requested, it's loaded from
	 * the database, but it's them saved in memory to be reused during request
	 * @param section
	 * @return
	 */
	public List<OrderComment> createComments() {
		EntityManager em = getEntityManager();
		List<OrderComment> lst = em.createQuery("from OrderComment c " +
				"join fetch c.user u " +
				"where c.order.id = #{order.id} order by c.date desc")
			.getResultList();

		return lst;
	}
	
	public String remove(){
		super.remove();
		
		// clear instance to avoid comment being displayed after included
		clearInstance();
		Contexts.getEventContext().set("orderComment", getInstance());
		
		return "comment-removed";
	}
	
}
