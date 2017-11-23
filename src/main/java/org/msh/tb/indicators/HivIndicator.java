package org.msh.tb.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.enums.HIVResult;
import org.msh.tb.indicators.core.Indicator;

/**
 * Generates case outcome indicator report
 * @author Utkarsh Srivastava
 *
 */
@Name("hivIndicator")
public class HivIndicator extends Indicator {

	@Override
	protected void createIndicators() {
//		setGroupFields("hiv.result");
        setCondition("exists(select id from ExamHIV where tbcase.id=c.id and result=" + HIVResult.POSITIVE.ordinal() + ")");
        Number val = (Number)createQuery().getSingleResult();
        addValue(translateKey(HIVResult.POSITIVE), val.intValue());

        setCondition("exists(select id from ExamHIV where tbcase.id=c.id and result=" + HIVResult.NEGATIVE.ordinal() + ")");
        val = (Number)createQuery().getSingleResult();
        addValue(translateKey(HIVResult.NEGATIVE), val.intValue());

//		createItems(createQuery().getResultList());
	}

/*
	@Override
	protected String getHQLJoin() {
		String joinStr = "join hiv.tbcase c ";
		String s = super.getHQLJoin();

		if (s != null)
			joinStr = joinStr.concat(s);
		return joinStr;
	}
*/


/*
	@Override
	protected String getHQLFrom() {
		return "from ExamHIV hiv";
	}
*/

	@Override
	public boolean isHasTotal() {
		return false;
	}
}
