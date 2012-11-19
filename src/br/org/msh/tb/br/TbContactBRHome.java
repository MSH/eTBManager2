package org.msh.tb.br;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.br.entities.TbContactBR;
import org.msh.tb.cases.TbContactHome;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;

/**
 * Handle specific rules for the TB contacts of the Brazilian version
 * 
 * @author Ricardo Memoria
 *
 */
@Name("tbContactBRHome")
public class TbContactBRHome  {
	
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
	

	@In(create=true) TbContactHome tbContactHome;
	
	/**
	 * Factory that returns an instance of the {@link TbContactBR} entity managed by the {@link TbContactHome} component
	 * @return
	 */
	@Factory("tbContactBR")
	public TbContactBR getTbContact() {
		return (TbContactBR)tbContactHome.getInstance();
	}


	/**
	 * Validate and save a TB contact of the Brazilian version
	 * @return "persisted" if successfully saved
	 */
	public String persist() {
		TbContactBR con = getTbContact();
		
		if ((con.getConduct() == null) || (con.getConduct().getCustomId().equals("1"))) {
			con.setDateEndTreat(null);
			con.setDateIniTreat(null);
		}

		if ((con.getDateEndTreat() != null) && (con.getDateIniTreat() != null) && 
			(con.getDateEndTreat().before(con.getDateIniTreat()))) 
		{
			FacesMessages.instance().add("A data final não pode ser anterior ou igual a data inicial");
			return "error";
		}
		
		return tbContactHome.persist();
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
