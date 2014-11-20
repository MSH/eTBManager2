/**
 * 
 */
package org.msh.tb.na.dataanalysis;

import org.jboss.seam.annotations.Name;
import org.msh.tb.reports2.ReportGroup;
import org.msh.tb.reports2.ReportResources;

/**
 * @author Ricardo Memoria
 *
 */
@Name("reportResources.na")
public class ReportResourcesNA extends ReportResources {

	/** {@inheritDoc}
	 */
	@Override
	protected ReportGroup addCaseDataVariables() {
		ReportGroup grp = super.addCaseDataVariables();
		
		// add specific variable for namibia
		add(grp, new SocialSupportNAVariable());
		return grp;
	}

}
