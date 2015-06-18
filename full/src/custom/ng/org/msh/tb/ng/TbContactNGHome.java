package org.msh.tb.ng;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.ng.entities.TbContactNG;

/**
* @author Vani Rao
*
* 
*/
@Name("tbContactNGHome")
public class TbContactNGHome extends EntityHomeEx<TbContactNG> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1176239729083664259L;
	
	@In(required=true) CaseHome caseHome;
	
	private String contactFName;
	private String contactLName;
	private String contactOtherName;

	@Factory("tbContactNG")
	public TbContactNG getTbContact() {
		return getInstance();
	}
	
	@Override
	public String persist() {		
		TbContactNG con = getInstance();
		TbCase tbcase = caseHome.getInstance();
		
		con.setTbcase(tbcase);
		tbcase.getContacts().add(con);
		
		String s = super.persist();
		
		//if ("persisted".equals(s))
		//	TagsCasesHome.instance().updateTags(tbcase);
		
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
	
	
	public String getContactFName() {
		return contactFName;
	}
		
	public void setContactFName(String contactFName) {
		this.contactFName = contactFName;
	}
	
	public String getContactLName() {
		return contactLName;
	}

	public void setContactLName(String contactLName) {
		this.contactLName = contactLName;
	}


	public String getContactOtherName() {
		return contactOtherName;
	}
	
	public void setContactOtherName(String contactOtherName) {
		this.contactOtherName = contactOtherName;
	}	
}
