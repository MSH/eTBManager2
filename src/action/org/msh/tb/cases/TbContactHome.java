package org.msh.tb.cases;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.TagsCasesHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TbContact;

@Name("tbContactHome")
public class TbContactHome extends EntityHomeEx<TbContact> {
	private static final long serialVersionUID = 1515193100467041594L;

	@In(required=true) CaseHome caseHome;
	
	@Factory("tbContact")
	public TbContact getTbContact() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		TbContact con = getInstance();
		TbCase tbcase = caseHome.getInstance();
		
		con.setTbcase(tbcase);
		tbcase.getContacts().add(con);
		
		String s = super.persist();
		
		if ("persisted".equals(s))
			TagsCasesHome.instance().updateTags(tbcase);
		
		return s;
	}


	@Override
	public String remove() {
		TbCase tbcase = caseHome.getInstance();
		tbcase.getContacts().remove(getInstance());
		
		if (super.remove().equals("removed"))
			 return "contact-removed";
		else return "error";
	}
}
