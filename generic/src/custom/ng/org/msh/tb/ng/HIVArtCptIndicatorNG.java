package org.msh.tb.ng;

import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.indicators.core.Indicator2D;

/**
 * @author vani rao
 *
 */
@Name("hivArtCptIndicatorNG")
public class HIVArtCptIndicatorNG extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6872640495293874422L;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		Map<String, String> messages = Messages.instance();

		// calculate number of cases with hiv positive
		int total = calcNumberOfCases("exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id = c.id)");
		
		// calculate number of HIV +ve cases who started on ART
		int artCount = calcNumberOfCases("exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id = c.id and hiv.startedARTdate is not null)");
		
			
		// calculate number of HIV +ve cases who started on CPT
		int cptCount = calcNumberOfCases("exists(select hiv.id from ExamHIV hiv where hiv.tbcase.id = c.id and hiv.startedCPTdate is not null)");
		
		
		float artperc = (float)artCount/(float)total*100;
		float cptperc = (float)cptCount/(float)total*100;
		
		int nartcount = total - artCount;
		int ncptcount = total - cptCount;
		
		float nartperc = (float)nartcount/(float)total*100;
		float ncptperc = (float)ncptcount/(float)total*100;
		
		addValue(messages.get("cases.examhiv.art"), messages.get("global.yes"), new Float(artCount));
		addValue(messages.get("cases.examhiv.art"), messages.get("global.no"), new Float(total-artCount));
		
		addValue(messages.get("cases.examhiv.artperc"),messages.get("global.yes"),new Float(artperc));
		addValue(messages.get("cases.examhiv.artperc"),messages.get("global.no"), new Float(nartperc));
		
		addValue(messages.get("cases.examhiv.cpt"), messages.get("global.yes"), new Float(cptCount));
		addValue(messages.get("cases.examhiv.cpt"), messages.get("global.no"), new Float(total-cptCount));
		
		addValue(messages.get("cases.examhiv.cptperc"),messages.get("global.yes"), cptperc);
		addValue(messages.get("cases.examhiv.cptperc"),messages.get("global.no"), ncptperc);
		
	}
	@Override
	protected String getHQLFrom() {
		return "from TbCase c";
	}

}
