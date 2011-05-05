package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.StockPosition;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("estimatedStockReport")
public class EstimatedStockReport {

	@In(create=true) ReportSelection reportSelection;
	@In(create=true) EntityManager entityManager;

	public class Item {
		private Medicine medicine;
		private int quantity;
		private Date lastUpdate;
		private long numDays;
		private long consumption;
		private int numPatients;

		public void addConsumption(long qtd, int numpats) {
			consumption += qtd;
			numPatients += numpats;
		}
		public long getConsumption() {
			return consumption;
		}
		public int getNumPatients() {
			return numPatients;
		}
		public long getNumDays() {
			return numDays;
		}
		public void setNumDays(long numDays) {
			this.numDays = numDays;
		}
		public long getEstimatedQuantity() {
			return (quantity - consumption < 0? 0: quantity - consumption);
		}
		public Date getLastUpdate() {
			return lastUpdate;
		}
		public void setLastUpdate(Date lastUpdate) {
			this.lastUpdate = lastUpdate;
		}
		public Medicine getMedicine() {
			return medicine;
		}
		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
		}
		public int getQuantity() {
			return quantity;
		}
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	private List<Item> itens;
	private List<PrescribedMedicine> prescs;


	/**
	 * Execute the report
	 */
	public void execute() {
		itens = null;
		prescs = null;
	}


	/**
	 * Generate report
	 */
	public void generate() {
		itens = new ArrayList<Item>();
		
		List<StockPosition> lst = entityManager.createQuery("from StockPosition sp join fetch sp.medicine " +
				"where sp.tbunit.id = #{userSession.tbunit.id} " + 
				"and sp.date = (select max(aux.date) from StockPosition aux " +
				"where aux.tbunit.id = sp.tbunit.id " +
				"and aux.source.id = sp.source.id " +
				"and aux.medicine.id = sp.medicine.id) " +
				"and sp.tbunit.id = #{userSession.tbunit.id} " +
				(reportSelection.getSource() != null? "and sp.source.id = #{reportSelection.source.id} ": "") +
				"order by sp.medicine.genericName")
				.getResultList();

		Date dtNow = DateUtils.getDate();
		
		for (StockPosition sp: lst) {
			Item item = itemByMedicine(sp.getMedicine());
			if (item == null) {
				item = new Item();
				itens.add(item);
				item.setMedicine(sp.getMedicine());
			}
			item.setQuantity(item.getQuantity() + sp.getQuantity());
			
			if ((item.getLastUpdate() == null) || (item.getLastUpdate().before(sp.getDate())))
				item.setLastUpdate(sp.getDate());
			
			if (sp.getDate().before(dtNow))
				item.setNumDays(DateUtils.daysBetween(sp.getDate(), dtNow));
		}
		
		loadPrescriptions();

		calcConsumption();
	}

	
	protected int calcConsumption() {
		Date dt = DateUtils.getDate();

		Integer caseid = null;
		Integer medid = null;
		for (PrescribedMedicine p: prescs) {
			Item item = itemByMedicine(p.getMedicine());
			
			if (item.getMedicine().getId() != medid)
				caseid = null;
			medid = item.getMedicine().getId();
			
			int qtd = 0;
			
			if ((dt.after(item.getLastUpdate())) && (p.getPeriod().isDateInside(item.getLastUpdate()))) {
				Period per = new Period( DateUtils.incDays( item.getLastUpdate(), 1) , dt);
				qtd = p.calcEstimatedDispensing(per);
				item.addConsumption(qtd, (caseid != p.getTbcase().getId() ? 1 : 0));
				caseid = p.getTbcase().getId();
			}

		}
		return 0;
	}

	
	protected Item itemByMedicine(Medicine m) {
		for (Item item: itens) {
			if (item.getMedicine().equals(m)) {
				return item;
			}
		}
		return null;
	}
		
	
	protected void loadPrescriptions() {
		prescs = entityManager.createQuery("from PrescribedMedicine d join fetch d.medicine " +
					"join fetch d.tbcase " + 
					"where (d.period.endDate is null or d.period.endDate >= (select max(sp.date) " + 
					"from StockPosition sp where sp.source.id=d.source.id " + 
					"and sp.tbunit.id=#{userSession.tbunit.id} " + 
					"and sp.medicine.id=d.medicine.id)) " +
					(reportSelection.getSource() != null? "and d.source.id = #{reportSelection.source.id} ": "") +
					"and exists(select aux.id from TreatmentHealthUnit aux where aux.tbcase.id=d.tbcase.id and aux.tbunit.id = #{userSession.tbunit.id}) " +
					"order by d.tbcase.id")
					.getResultList();
	}

	public List<Item> getItens() {
		if (itens == null)
			generate();

		return itens;
	}
}
