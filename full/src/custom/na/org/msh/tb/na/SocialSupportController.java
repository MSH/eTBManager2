/**
 * 
 */
package org.msh.tb.na;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.na.entities.TbCaseNA;

/**
 * Handle the operations in the social support page. It saves the changes
 * done by the user in the social support page, validating the data before
 * 
 * @author Ricardo Memoria
 *
 */
@Name("socialSupportController")
public class SocialSupportController {

	@In CaseHome caseHome;
	
	/**
	 * Save the changes in the social support page
	 * @return
	 */
	public String save() {
		TbCaseNA tbcase = (TbCaseNA)caseHome.getInstance();
		
		if (!tbcase.isSocialDisabilityAwarded()) {
			tbcase.setStartDateSocialAward(null);
			tbcase.setCommentSocialAward(null);
		}
		
		if (!tbcase.isFoodPackageAwarded()) {
			tbcase.setStartDateFoodPackageAward(null);
			tbcase.setCommentFoodPackageAward(null);
		}
		
		if (!tbcase.isTransportAssistProvided()) {
			tbcase.setStartDateTransportAssist(null);
			tbcase.setCommentTransportAssist(null);
		}

		caseHome.persist();
		
		return "persisted";
	}
}
