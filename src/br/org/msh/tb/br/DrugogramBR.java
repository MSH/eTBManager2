package org.msh.tb.br;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.drugogram.Drugogram;

@Name("drugogramBR")
public class DrugogramBR extends Drugogram{

	@Create
	public void initialize() {
		setIncludeMedicalExamination(true);
		setIncludeXRay(true);
	}
}
