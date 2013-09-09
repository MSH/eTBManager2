package org.msh.tb.ua.cases.exams;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.msh.tb.cases.exams.ExamHome;
import org.msh.tb.ua.entities.Histology;

@Name("histologyHome")
public class HistologyHome extends ExamHome<Histology> {
	private static final long serialVersionUID = -9166350112323604726L;

	@Factory("histology")
	public Histology getHistology() {
		return getInstance();
	}
}
