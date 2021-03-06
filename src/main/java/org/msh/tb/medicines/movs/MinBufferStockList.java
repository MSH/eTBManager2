package org.msh.tb.medicines.movs;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineUnit;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.Tbunit;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Holds information about the minimum buffer stock of a TB unit
 * @author Ricardo
 *
 */
@Name("minBufferStockList")
public class MinBufferStockList {

	@In(create=true) EntityManager entityManager;
	
	private Tbunit unit;
	private Source source;
	private List<MedicineUnit> items;


	/**
	 * Initialize the list of minimum buffer stock quantities
	 * @param unit
	 * @param source
	 */
	public void initialize(Tbunit unit, Source source) {
		this.unit = unit;
		this.source = source;
		
		String hql = "from MedicineUnit mu where mu.tbunit.id = :unitid";
		
		if (source != null)
			hql += " and source.id = #{minBufferStockList.source}";
		
		items = entityManager.createQuery(hql)
			.setParameter("unitid", unit.getId())
			.getResultList();
	}
	
	public MedicineUnit findItem(Medicine med, Source source) {
		for (MedicineUnit mu: getItems()) {
			if ((mu.getMedicine().equals(med)) && (mu.getSource().equals(source))) {
				return mu;
			}
		}
		return null;
	}
	
	public List<MedicineUnit> getItems() {
		return items;
	}

	public Tbunit getTbunit() {
		return unit;
	}
	
	public Source getSource() {
		return source;
	}
}
