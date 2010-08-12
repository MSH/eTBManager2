package org.msh.tb.test;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Asynchronous;
import org.msh.mdrtb.entities.TbCase;

@Name("executeAsyncAction")
public class ExecuteAsyncAction {

	@In(create=true) AdjustDaysPlannedAction adjustDaysPlannedAction;

	/**
	 * Initialize the field {@link TbCase}.numDaysPlanned
	 */
	@Asynchronous
	public void adjustDaysPlanned() {
		int ini = 0;
		int max = 50;
		
		while (true) {
			if (!adjustDaysPlannedAction.adjust(ini, max))
				break;
			ini += 50;
		}
	}


}
