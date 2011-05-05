package org.msh.tb.indicators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.indicators.core.Indicator;
import org.msh.tb.indicators.core.IndicatorDate;

/**
 * Generates case outcome indicator report
 * @author Utkarsh Srivastava
 *
 */
@Name("hivIndicator")
public class HivIndicator extends Indicator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5752532950894149959L;

	@Override
	protected void createIndicators() {
		setGroupFields("hiv.result");
		createItems( createQuery().getResultList() );
	}

	@Override
	protected String getHQLJoin() {
		String joinStr = "join hiv.tbcase c ";
		String s = super.getHQLJoin();

		if (s != null)
			joinStr = joinStr.concat(s);
		return joinStr;
	}


	@Override
	protected String getHQLFrom() {
		return "from ExamHIV hiv";
	}

	@Override
	public boolean isHasTotal() {
		return false;
	}
}
