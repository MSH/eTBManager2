package org.msh.tb.az.indicators;

import org.jboss.seam.annotations.Name;
import org.msh.tb.indicators.HivIndicator;

/**
 * Generates case outcome indicator report
 * @author Utkarsh Srivastava
 *
 */
@Name("hivIndicatorAZ")
public class HivIndicatorAZ extends HivIndicator {
	private static final long serialVersionUID = 7098843524696236139L;

	@Override
	protected String getHQLWhere() {
		String hql = super.getHQLWhere();
		hql += " and hiv.date = (select max(ex.date) from ExamHIV ex where ex.tbcase.id = c.id)";
		return hql;
	}
}
