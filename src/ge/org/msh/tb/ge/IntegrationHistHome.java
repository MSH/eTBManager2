package org.msh.tb.ge;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.international.StatusMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.ge.entities.IntegrationHistory;

@Name("integrationHistHome")
public class IntegrationHistHome extends EntityHomeEx<IntegrationHistory>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 7058917432160414176L;

	@Logger private Log log;

    @In StatusMessages statusMessages;

    public void integrationHistHome()
    {
        // implement your business logic here
        log.info("integrationHistHome.integrationHistHome() action called");
        statusMessages.add("integrationHistHome");
    }

    // add additional action methods

}
