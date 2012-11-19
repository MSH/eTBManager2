package org.msh.tb.cases;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TbContact;
import org.msh.tb.transactionlog.LogInfo;

/**
 * Handle basic operations with a {@link TbContact} instance and its inherited entities
 * 
 * @author Ricardo Memoria
 *
 */
@Name("tbContactHome")
@LogInfo(roleName="TBCONTACT")
public class TbContactHome extends ExamHome<TbContact> {
	private static final long serialVersionUID = 1515193100467041594L;

	@In(required=true) CaseHome caseHome;

	/**
	 * Return the instance of the {@link TbContact} managed by this home class 
	 * @return
	 */
	@Factory("tbContact")
	public TbContact getTbContact() {
		return getInstance();
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.ExamHome#persist()
	 */
	@Override
	public String persist() {
		TbContact con = getInstance();
		TbCase tbcase = caseHome.getInstance();
		
		con.setTbcase(tbcase);
		tbcase.getContacts().add(con);
		
		String s = super.persist();
		
		if ("persisted".equals(s)) {
			TagsCasesHome.instance().updateTags(tbcase);
		}
		
		return s;
	}


	/* (non-Javadoc)
	 * @see org.msh.tb.cases.exams.ExamHome#remove()
	 */
	@Override
	public String remove() {
		TbCase tbcase = caseHome.getInstance();
		tbcase.getContacts().remove(getInstance());
		
		if (super.remove().equals("removed")) {
			TagsCasesHome.instance().updateTags(tbcase);
			return "contact-removed";
		}
		else return "error";
	}
}
