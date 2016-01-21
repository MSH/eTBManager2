package org.msh.tb.medicines.movs;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.Tbunit;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

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
	public List<StockPosition> generate(Tbunit unit, Source source) {
		String hql = "from StockPosition sp " + 
			"join fetch sp.medicine join fetch sp.source " + 
			"where sp.tbunit.id = :unit";
		
		if (source != null)
			hql = hql + " and sp.source.id = :source ";

		hql = hql + " order by sp.medicine.genericName";
		
		Query qry = entityManager.createQuery(hql);
		
		qry.setParameter("unit", unit.getId());
		if (source != null)
			qry.setParameter("source", source.getId());
		
		return qry.getResultList();
	}


	/**
	 * Return the list of batches available grouped by medicine
	 * @param unit
	 * @param source
	 * @return
	 */
	public List<BatchQuantity> getBatchAvailable(Tbunit unit, Source source) {
		String hql = "from BatchQuantity b " + 
			"join fetch b.batch bt join fetch bt.medicine join fetch b.source " + 
			"where b.tbunit.id = :unit "; 
	
		if (source != null)
			hql = hql + " and b.source.id = :source";

		hql = hql + " order by b.source.name.name1, bt.medicine.genericName.name1, bt.expiryDate";
	
		Query qry = entityManager.createQuery(hql);
	
		qry.setParameter("unit", unit.getId());
		if (source != null)
			qry.setParameter("source", source.getId());
		
		List<BatchQuantity> batches = qry.getResultList();

		return batches;
	}

}
