package org.msh.tb.ua;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Histology;
import org.msh.tb.cases.ExamHome;

@Name("histologyHome")
public class HistologyHome extends ExamHome<Histology> {
	private static final long serialVersionUID = -9166350112323604726L;

	@Factory("histology")
	public Histology getHistology() {
		return getInstance();
	}
}
