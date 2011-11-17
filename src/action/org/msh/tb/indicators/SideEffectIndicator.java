package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.core.Indicator;

@Name("sideEffectIndicator")
public class SideEffectIndicator extends Indicator {
	private static final long serialVersionUID = 8935765779908246423L;

	@Override
	protected void createIndicators() {
		setGroupFields("se.sideEffect.value.name1");
		createItems( createQuery().getResultList() );
	}

	@Override
	protected String getHQLJoin() {
		String joinStr = "join se.tbcase c ";
		String s = super.getHQLJoin();

		if (s != null)
			joinStr = joinStr.concat(s);
		return joinStr;
	}


	@Override
	protected String getHQLFrom() {
		return "from CaseSideEffect se";
	}

	@Override
	public boolean isHasTotal() {
		return false;
	}
}
