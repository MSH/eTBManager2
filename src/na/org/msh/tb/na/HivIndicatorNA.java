package org.msh.tb.na;

import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.Indicator;

/**
 * Generates case outcome indicator report
 * @author Utkarsh Srivastava
 *
 */
@Name("hivIndicatorNA")
public class HivIndicatorNA extends Indicator {

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
		return "from ExamHIV_NA hiv";
	}

	@Override
	public boolean isHasTotal() {
		return false;
	}
}
