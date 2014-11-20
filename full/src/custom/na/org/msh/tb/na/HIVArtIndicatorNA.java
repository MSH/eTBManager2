package org.msh.tb.na;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.indicators.core.Indicator2D;

import java.util.Map;


/**
 * @author usrivastava
 *
 */
@Name("hivArtIndicatorNA")
public class HIVArtIndicatorNA extends Indicator2D{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2131975637861672159L;

	@Override
	protected void createIndicators() {
		Map<String, String> messages = Messages.instance();

		// calculate number of cases with hiv positive
		int total = calcNumberOfCases("hiv.tbcase.id = c.id and exists(select hiv1.id from ExamHIV_NA hiv1 where hiv1.tbcase.id = c.id and hiv1.result = 0)");
				
		int artCount = calcNumberOfCases(" hiv.tbcase.id = c.id "+
		"and hiv.startedARTdate is not null ");
		addValue(messages.get("cases.examhiv.art"), messages.get("global.yes"), new Float(artCount));
		addValue(messages.get("cases.examhiv.art"), messages.get("global.no"), new Float(total-artCount));
		
		int cptCount = calcNumberOfCases(" hiv.tbcase.id = c.id "+
		"and hiv.startedCPTdate is not null ");
		addValue(messages.get("cases.examhiv.cpt"), messages.get("global.yes"), new Float(cptCount));
		addValue(messages.get("cases.examhiv.cpt"), messages.get("global.no"), new Float(total-cptCount));
		
		
	}

	@Override
	protected String getHQLFrom() {
		return "from TbCase c, ExamHIV_NA hiv";
	}


	
	
}
