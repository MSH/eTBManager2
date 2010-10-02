package org.msh.tb.br;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.enums.CultureResult;
import org.msh.mdrtb.entities.enums.MicroscopyResult;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.br.entities.TbContactBR;
import org.msh.tb.cases.CaseHome;

@Name("tbContactBRHome")
public class TbContactBRHome extends EntityHomeEx<TbContactBR> {
	private static final long serialVersionUID = 1515193100467041594L;

	private static final MicroscopyResult[] microscopyResults = {
		MicroscopyResult.NOTDONE,
		MicroscopyResult.NEGATIVE,
		MicroscopyResult.PLUS,
		MicroscopyResult.PLUS2,
		MicroscopyResult.PLUS3
	};
	
	private static final CultureResult[] cultureResult = {
		CultureResult.NOTDONE,
		CultureResult.NEGATIVE,
		CultureResult.PLUS,
		CultureResult.PLUS2,
		CultureResult.PLUS3
	};
	
	@In(required=true) CaseHome caseHome;
	@In(create=true) FacesMessages facesMessages;
	
	@Factory("tbContactBR")
	public TbContactBR getTbContact() {
		return getInstance();
	}
	
	@Override
	public String persist() {
		TbContactBR con = getInstance();

		if ((con.getDateEndTreat() != null) && (con.getDateIniTreat() != null) && 
			(con.getDateEndTreat().before(con.getDateIniTreat()))) 
		{
			facesMessages.add("A data final n�o pode ser anterior ou igual a data inicial");
			return "error";
		}
		
		TbCase tbcase = caseHome.getInstance();
		
		con.setTbcase(tbcase);
		tbcase.getContacts().add(con);
		
		return super.persist();
	}


	@Override
	public String remove() {
		TbCase tbcase = caseHome.getInstance();
		tbcase.getContacts().remove(getInstance());
		
		if (super.remove().equals("removed"))
			 return "contact-removed";
		else return "error";
	}


	/**
	 * Return options to the Brazilian version of the contact form
	 * @return
	 */
	public MicroscopyResult[] getMicroscopyResults() {
		return microscopyResults;
	}


	/**
	 * Return field options to the Brazilian version of the contact form
	 * @return
	 */
	public CultureResult[] getCultureResults() {
		return cultureResult;
	}
}
