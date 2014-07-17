package org.msh.utils;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.msh.tb.application.App;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@Name("dateValidator")
@org.jboss.seam.annotations.faces.Validator(id="dateValidator")
@BypassInterceptors
public class DateValidator implements Validator {

	public void validate(FacesContext facesContext, UIComponent comp, Object val) throws ValidatorException {
		//Checks if it is an acceptable date (after 01/01/1900)
		MinDateValidator minDateValidator = App.getComponent(MinDateValidator.class);
		minDateValidator.validate(facesContext, comp, val);
		
		//Checks if has to validate future
		UIParameter canBeFuture = findParam(comp.getParent(), "canBeFuture");
		if (canBeFuture != null) {
			Object p = canBeFuture.getValue();
			
			if (p != null && p.equals("false")){
				FutureDateValidator futureDateValidator = App.getComponent(FutureDateValidator.class);
				futureDateValidator.validate(facesContext, comp, val);
			}
				
		}
		
		//Checks if has to validate patient death date
		UIParameter validatePatientDeathDate = findParam(comp.getParent(), "validatePatientDeathDate");
		if (validatePatientDeathDate != null) {
			Object p = validatePatientDeathDate.getValue();
			if (p != null && p.equals("true")){
				PatientDateValidator patientDateValidator = App.getComponent(PatientDateValidator.class);
				patientDateValidator.validate(facesContext, comp, val);
			}	
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
