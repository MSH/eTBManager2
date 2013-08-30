package org.msh.tb.ua;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Transfer;
import org.msh.utils.EntityQuery;


/**
 * Maintain a list of transfers that are not open
 * @author A.M.
 *
 */
@Name("transfersReceived")
public class TransfersReceived extends EntityQuery<Transfer> {
	private static final long serialVersionUID = 7320080806913999857L;
	
	private boolean executing;
	
	public void execute() {
		executing = true;
		refresh();
	}

	@Override
	public String getEjbql() {
		return "from Transfer tr ".concat(conditions());
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Transfer tr join fetch tr.unitFrom join fetch tr.unitTo ".concat(conditions());
	}

	protected String conditions() {
		return "where tr.status = 1 and (tr.unitTo.id = #{userSession.tbunit.id} or tr.unitFrom.id = #{userSession.tbunit.id})";
	}
	
	@Override
	public Integer getMaxResults() {
		return 30;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();

		if (s == null)
			 return "tr.shippingDate desc";
		else return s;
	}

	public boolean isExecuting() {
		return executing;
	}
}
