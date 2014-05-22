package org.msh.utils;

import java.util.Date;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.CaseState;
import org.msh.utils.date.DateUtils;

@Name("patientDateValidator")
@org.jboss.seam.annotations.faces.Validator(id="patientDateValidator")
@BypassInterceptors
public class PatientDateValidator implements Validator {

	public void validate(FacesContext facesContext, UIComponent comp, Object val) throws ValidatorException {
		CaseHome caseHome = App.getComponent(CaseHome.class);
		
		if(caseHome == null)
			return;
		
		Date dt = (Date)val;
		TbCase tbcase = caseHome.getInstance();

		if (dt == null || tbcase == null)
			return;
		
		//Validates consistency if patient is already registered as dead
		Date deathDate = null;
		for(TbCase c : tbcase.getPatient().getCases()){
			if(c.getState().equals(CaseState.DIED) || c.getState().equals(CaseState.DIED_NOTTB)){
				deathDate = c.getOutcomeDate();
				break;
			}
		}
		
		if(deathDate != null && dt != null && deathDate.compareTo(dt) < 1){
			Map<String,String> messages = Messages.instance();
			
			FacesMessage message = new FacesMessage();
			message.setDetail(messages.get("cases.deadbefore") + " " + DateUtils.formatAsLocale(deathDate, false));
			message.setSummary(messages.get("cases.deadbefore") + " " + DateUtils.formatAsLocale(deathDate, false));
			message.setSeverity(FacesMessage.SEVERITY_ERROR); 
			throw new ValidatorException(message);
		}
	}

	public UIParameter findParam(UIComponent comp, String pname) {
		for (UIComponent c: comp.getChildren()) {
			if ((c instanceof UIParameter) && (((UIParameter)c).getName().equals(pname))) {
				return (UIParameter)c;
			}
		}
		return null;
	}

}
