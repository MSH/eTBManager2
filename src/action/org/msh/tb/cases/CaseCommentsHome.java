package org.msh.tb.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.CaseComment;
import org.msh.tb.entities.UserLogin;

@Name("caseCommentsHome")
public class CaseCommentsHome extends EntityHomeEx<CaseComment> {
	private static final long serialVersionUID = -7620376721300705176L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) CaseFilters caseFilters;
	
	private List<CommentWrapper> comments;

	@Factory("caseComment")
	public CaseComment getCaseComment() {
		return getInstance();
	}

	
	@Override
	public String remove() {
		if (comments != null) {
			for (CommentWrapper com: comments)
				if (com.getCaseComment().getId().equals(getId())) {
					comments.remove(com);
					break;
				}
		}

		super.remove();

		// clear instance to avoid comment being displayed after removed
		clearInstance();
		Contexts.getEventContext().set("caseComment", getInstance());

		return "comment-removed";
	}


	/**
	 * Add a new comment. The section property must be informed  
	 * @return
	 */
	public String addComment() {
		CaseView view = caseFilters.getCaseView();
		if (view == null)
			return "error";

		CaseComment comment = getInstance();
		
		comment.setUser(getUser());
		comment.setDate(new Date());
		comment.setView(view);
		comment.setTbcase(caseHome.getInstance());
		
		persist();
		
		if (comments != null) {
			comments.add(0, createCommentWrapper(getInstance()));
		}

		// clear instance to avoid comment being displayed after included
		clearInstance();
		Contexts.getEventContext().set("caseComment", getInstance());
		
		return "comment-added";
	}


	/**
	 * Create a list of comments from a specific section. The first time the list is requested, it's loaded from
	 * the database, but it's them saved in memory to be reused during request
	 * @param section
	 * @return
	 */
	public List<CommentWrapper> createComments(CaseView view) {
		EntityManager em = getEntityManager();
		List<CaseComment> lst = em.createQuery("from CaseComment c " +
				"join fetch c.user u " +
				"where c.tbcase.id = #{caseHome.id} " +
				"and c.view = :view order by c.date desc")
			.setParameter("view", view)
			.getResultList();
		
		List<CommentWrapper> res = new ArrayList<CommentWrapper>();
		for (CaseComment comment: lst) {
			res.add(createCommentWrapper(comment));
		}
		
		return res;
	}

	
	/**
	 * Return list of comments of the selected case in {@link CaseHome} instance and
	 * its selected view in the {@link CaseFilters} caseView property
	 * @return
	 */
	public List<CommentWrapper> getComments() {
		if (comments == null)
			comments = createComments(caseFilters.getCaseView());
		return comments;
	}


	/**
	 * Check if comments can be displayed
	 * @return
	 */
	public boolean isDisplayComments() {
		if (!caseHome.checkRoleBySuffix("CASE_COMMENTS"))
			return false;
		
		boolean canEdit;
		switch (caseFilters.getCaseView()) {
		case ADDINFO: canEdit = caseHome.isCanEditAditionalInfo();
			break;
		case EXAMS: canEdit = caseHome.isCanEditExams();
			break;
		case TREATMENT: canEdit =  caseHome.isCanEditTreatment();
			break;
		default: canEdit = caseHome.isCanEditCaseData();
		}
		return (canEdit) || (getComments().size() > 0);
	}


	/**
	 * Create a {@link CommentWrapper} containing the {@link CaseComment} object
	 * and the information if the user can remove the comment
	 * @return
	 */
	protected CommentWrapper createCommentWrapper(CaseComment caseComment) {
		boolean canRemove = Identity.instance().hasRole("REM_COMMENTS");

		// if user has no permission to remove global comments, check if it's its own comment
		if (!canRemove) {
			UserLogin userLogin = getUserLogin();
			canRemove = (userLogin.getUser().getId().equals(caseComment.getUser().getId()));
		}
		return new CommentWrapper(caseComment, canRemove);
	}


	public class CommentWrapper {
		private CaseComment caseComment;
		private boolean canRemove;

		public CommentWrapper(CaseComment caseComment, boolean canRemove) {
			super();
			this.caseComment = caseComment;
			this.canRemove = canRemove;
		}
		/**
		 * @return the caseComment
		 */
		public CaseComment getCaseComment() {
			return caseComment;
		}
		/**
		 * @param caseComment the caseComment to set
		 */
		public void setCaseComment(CaseComment caseComment) {
			this.caseComment = caseComment;
		}
		/**
		 * @return the canRemove
		 */
		public boolean isCanRemove() {
			return canRemove;
		}
		/**
		 * @param canRemove the canRemove to set
		 */
		public void setCanRemove(boolean canRemove) {
			this.canRemove = canRemove;
		}
	}
}
