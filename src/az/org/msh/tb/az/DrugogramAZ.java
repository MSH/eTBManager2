package org.msh.tb.az;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.drugogram.Drugogram;

@Name("drugogramAZ")
public class DrugogramAZ extends Drugogram{

	@Create
	public void initialize() {
		setIncludeMedicalExamination(true);
		setIncludeXRay(true);
	}
}
