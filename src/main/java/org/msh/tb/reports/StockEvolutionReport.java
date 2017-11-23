package org.msh.tb.reports;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;



@Name("stockEvolutionReport")
public class StockEvolutionReport {

	public class Item {
		private Medicine medicine;
		private long initialQuantity;
		private long inQuantity;
		private long outQuantity;
		private long finalQuantity;
		public long getFinalQuantity() {
			return finalQuantity;
		}
		public void setFinalQuantity(long finalQuantity) {
			this.finalQuantity = finalQuantity;
		}
		public long getInitialQuantity() {
			return initialQuantity;
		}
		public void setInitialQuantity(long initialQuantity) {
			this.initialQuantity = initialQuantity;
		}
		public long getInQuantity() {
			return inQuantity;
		}
		public void setInQuantity(long inQuantity) {
			this.inQuantity = inQuantity;
		}
		public long getOutQuantity() {
			return outQuantity;
		}
		public void setOutQuantity(long outQuantity) {
			this.outQuantity = outQuantity;
		}
		public Medicine getMedicine() {
			return medicine;
		}
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
	}

	private List<Item> itens;

	@In(create=true) ReportSelection reportSelection;
	@In(create=true) EntityManager entityManager;
	
	public void execute() {
		itens = null;
	}
	
	/**
	 * Generates the report
	 */
	public void createReport() {
		String hql = "select m.medicine," +
				"(select sum(a.quantity * a.oper) " +  // stock position at the beginning of the period
				"from Movement a where a.tbunit.id=m.tbunit.id and a.source.id=m.source.id and a.medicine.id=m.medicine.id and a.date<:dt1), " +
				"(select sum(b.quantity * b.oper) " +  // stock position at the end of the period 
				"from Movement b where b.tbunit.id=m.tbunit.id and b.source.id=m.source.id and b.medicine.id=m.medicine.id and b.date<:dt2), " +
				"(select sum(c.quantity) from Movement c where c.oper = 1 and c.quantity > 0 and c.date between :dt1 and :dt2 " +
				"and c.tbunit.id=m.tbunit.id and c.source.id=m.source.id and c.medicine.id=m.medicine.id) " +
				"from StockPosition m " +
				"where m.tbunit.id = #{userSession.tbunit.id} " +
				(reportSelection.getSource() != null? " and m.source.id = #{reportSelection.source.id} ": "") + 
				"order by m.medicine.genericName";
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.setParameter("dt1", reportSelection.getIniDate())
			.setParameter("dt2", reportSelection.getEndDate())
			.getResultList();
		
		itens = new ArrayList<Item>();
		for (Object[] vals: lst) {
			Medicine med = (Medicine)vals[0];
			Long qtdIni = vals[1] == null? 0: (Long)vals[1];
			Long qtdEnd = vals[2] == null? 0: (Long)vals[2];
			Long movIn = vals[3] == null? 0: (Long)vals[3];
			Long movOut = qtdIni + movIn - qtdEnd;
			
			Item it = new Item();
			itens.add(it);
			
			it.setMedicine(med);
			it.setInitialQuantity(qtdIni);
			it.setFinalQuantity(qtdEnd);
			it.setInQuantity(movIn);
			it.setOutQuantity(movOut);
		}
		
/*		// calcula estoque inicial
		List<StockPosition> lst =  entityManager
				.createQuery("from StockPosition sp join fetch sp.medicine " + 
				"where sp.date = (select max(s.date) from StockPosition s where s.medicine.id = sp.medicine.id " +
				"and s.tbunit.id = sp.tbunit.id and s.source.id = sp.source.id " + 
				"and s.date <= :dt) " +
				"and sp.tbunit.id = #{userSession.tbunit.id} " + 
				(reportSelection.getSource() != null? " and sp.source.id = #{reportSelection.source.id} ": "") + 
				"order by sp.medicine.genericName")
				.setParameter("dt", reportSelection.getIniDate())
				.getResultList();
		
		itens = new ArrayList<Item>();

		for (StockPosition sp: lst) {
			Item it = new Item();
			itens.add(it);
			it.setMedicine(sp.getMedicine());
			it.setInitialQuantity(sp.getQuantity());
		}

		// calcula movimentos de entrada no per�odo
		List<Object[]> res = entityManager.createQuery("select m.medicine.id, sum(m.quantity) from Movement m " + 
				"where m.date >= :dtini and m.date < :dtfim " + 
				"and m.oper = 1 and m.quantity > 0 " +
				"and m.tbunit.id = #{userSession.tbunit.id} " +
				(reportSelection.getSource() != null? " and m.source.id = #{reportSelection.source.id} ": "") + 
				"group by m.medicine.id")
				.setParameter("dtini", reportSelection.getIniDate())
				.setParameter("dtfim", reportSelection.getEndDate())
				.getResultList();
		for (Object[] vals: res) {
			Item it = itemByMedicine((Integer)vals[0]);
			it.setInQuantity((Long)vals[1]);
		}

		// calcula movimentos de sa�da no per�odo
		res = entityManager.createQuery("select m.medicine.id, sum(-m.oper * m.quantity) from Movement m " + 
				"where m.date >= :dtini and m.date < :dtfim " + 
				"and ((m.oper = -1) or (m.quantity < 0)) " +
				"and m.tbunit.id = #{userSession.tbunit.id} " +
				(reportSelection.getSource() != null? " and m.source.id = #{reportSelection.source.id} ": "") + 
				"group by m.medicine.id")
				.setParameter("dtini", reportSelection.getIniDate())
				.setParameter("dtfim", reportSelection.getDayAfterEndDate())
				.getResultList();
		for (Object[] vals: res) {
			Item it = itemByMedicine((Integer)vals[0]);
			it.setOutQuantity((Long)vals[1]);
		}
		
		// calcula estoque final
		for (Item it: itens) {
			it.setFinalQuantity(it.getInitialQuantity() + it.getInQuantity() - it.getOutQuantity());
		}
*/
/*		lst =  entityManager
				.createQuery("from StockPosition sp join fetch sp.medicine " + 
						"where sp.date = (select max(s.date) from StockPosition s where s.medicine.id = sp.medicine.id " +
						"and s.drugStorage.id = sp.drugStorage.id and s.source.id = sp.source.id " + 
						"and s.date < :dt) " +
						"and sp.drugStorage.id = :ds and sp.source.id = :source " +
				"order by sp.medicine.name")
				.setParameter("dt", reportSelection.getEndDate())
				.setParameter("ds", reportSelection.getDrugStorage().getId())
				.setParameter("source", reportSelection.getSource().getId())
				.getResultList();
		for (StockPosition sp: lst) {
			Item it = itemByMedicine(sp.getMedicine().getId());
			it.setFinalQuantity(sp.getQuantity());
		}
*/		
	}

	public List<Item> getItens() {
		if (itens == null)
			createReport();
		return itens;
	}


	/**
	 * Get item by medicine id
	 * @param id
	 * @return
	 */
	protected Item itemByMedicine(Integer id) {
		for (Item it: itens) {
			if (it.getMedicine().getId().equals(id)) {
				return it;
			}
		}
		
		Item it = new Item();
		
		Medicine p = entityManager.find(Medicine.class, id);
		it.setMedicine(p);
		itens.add(it);
		return it;
	}
}
