package org.msh.tb.medicines.movs;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;

@Name("stockPositionList")
public class StockPositionList {

	@In(create=true) EntityManager entityManager;
	
	/**
	 * Generates a stock position list based on the arguments.
	 * @param unit - Required
	 * @param source - Optional
	 * @param date - Optional - Reference date of the stock position
	 * @return
	 */
	public List<StockPosition> generate(Tbunit unit, Source source, Date date) {
		String hql = "from StockPosition sp " + 
			"join fetch sp.medicine join fetch sp.source " + 
			"where sp.tbunit.id = :unit " + 
			"and sp.date = (select max(aux.date) from StockPosition aux " +
			"where aux.tbunit.id = sp.tbunit.id and aux.source.id = sp.source.id " +
			"and aux.medicine.id = sp.medicine.id ";
		
		if (date != null)
			 hql = hql + "and aux.date <= :dt)";
		else hql = hql + ")";
		
		if (source != null)
			hql = hql + " and sp.source.id = :source";

		hql = hql + "order by sp.medicine.genericName";
		
		Query qry = entityManager.createQuery(hql);
		
		qry.setParameter("unit", unit.getId());
		if (date != null)
			qry.setParameter("dt", date);
		if (source != null)
			qry.setParameter("source", source.getId());
		
		return qry.getResultList();
	}
}
