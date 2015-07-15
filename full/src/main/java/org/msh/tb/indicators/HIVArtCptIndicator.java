package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.Messages;
import org.msh.tb.indicators.core.Indicator2D;
import org.msh.tb.indicators.core.IndicatorTable;
import org.msh.tb.indicators.core.IndicatorTable.TableColumn;

import java.util.Map;

/**
 * @author vani rao
 *
 */
@Name("hivArtCptIndicator")
public class HIVArtCptIndicator extends Indicator2D{

	private String artcount;
	private String artper;
	private String cptcount;
	private String cptper;

	@Override
	protected void createIndicators() {
		// TODO Auto-generated method stub
		Map<String, String> messages = Messages.instance();
		
		artcount 		= getMessage("cases.examhiv.art");
		artper 		= getMessage("cases.examhiv.artperc");
		cptcount 		= getMessage("cases.examhiv.cpt");
		cptper 		= getMessage("cases.examhiv.cptperc");
		
		IndicatorTable table = getTable();
		table.addColumn(artcount, null);
		TableColumn colartper = table.addColumn(artper, null);
		colartper.setHighlight(true);
		
		table.addColumn(cptcount, null);
		TableColumn colcptper = table.addColumn(cptper, null);
		colcptper.setHighlight(true);
		
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
		
		addValue(artcount, messages.get("global.yes"), new Float(artCount));
		addValue(artcount, messages.get("global.no"), new Float(total-artCount));
		
		addValue(artper,messages.get("global.yes"),new Float(artperc));
		addValue(artper,messages.get("global.no"), new Float(nartperc));
		
		addValue(cptcount, messages.get("global.yes"), new Float(cptCount));
		addValue(cptcount, messages.get("global.no"), new Float(total-cptCount));
		
		addValue(cptper,messages.get("global.yes"), cptperc);
		addValue(cptper,messages.get("global.no"), ncptperc);
	}


	@Override
	protected String getHQLFrom() {
		return "from TbCase c";
	}

}
