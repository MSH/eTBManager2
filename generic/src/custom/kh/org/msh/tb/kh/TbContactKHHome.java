package org.msh.tb.kh;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.kh.entities.TbContact_Kh;


/**
* @author Vani Rao
*
* 
*/
@Name("tbContactKHHome")
public class TbContactKHHome extends EntityHomeEx<TbContact_Kh>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6297065693679681219L;
	@In(required=true) CaseHome caseHome;
	
	@Factory("tbContactKH")
	public TbContact_Kh getTbContact() {
		return getInstance();
	}
	
	@Override
	public String persist() {		
		TbContact_Kh con = getInstance();
		TbCase tbcase = caseHome.getInstance();
		
		con.setTbcase(tbcase);
		tbcase.getContacts().add(con);
		
		String s = super.persist();
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

