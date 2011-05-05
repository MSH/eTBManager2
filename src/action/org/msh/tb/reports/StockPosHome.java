package org.msh.tb.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.ProductGroup;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.login.UserSession;



@Name("stockPosHome")
public class StockPosHome implements Comparator<StockPosItem>{

	@In(create=true)
	private EntityManager entityManager;
	
	@In(create=true)
	private UserSession userSession;
	
	private boolean showBatch;
	private String order;
	private Source source;
	private List<StockSource> sources;
	private StockSource total;
	private boolean executing;
	private ProductGroup productGroup;
	
	// extra info about the medicine in stock
	private StockPosition item;
	private Movement lastMovement;
	private Batch firstBatch;
	private int orderMult;

	public class StockSource {
		private Source source;
		private List<StockPosItem> items = new ArrayList<StockPosItem>();
		
		public StockPosItem findByMedicine(Medicine medicine) {
			for (StockPosItem item: items) {
				if (item.getMedicine().getId().equals(medicine.getId()))
					return item;
			}
			return null;
		}
		public Source getSource() {
			return source;
		}
		public void setSource(Source source) {
			this.source = source;
		}
		public List<StockPosItem> getItems() {
			return items;
		}
	}

	public void execute() {
		executing = true;
	}
	
	protected void createReport() {
		if ((showBatch) || (sources != null)) {
			return;
		}

		String hql = "from StockPosition sp join fetch sp.medicine " +
			"join fetch sp.source " +
			"where sp.date = (select max(aux.date) from StockPosition aux " +
			"where aux.source.id = sp.source.id " + 
			"and aux.tbunit.id = sp.tbunit.id " +
			"and aux.medicine.id = sp.medicine.id) " +
			"and sp.tbunit.id = #{userSession.tbunit.id}";
		
		if (source != null)
			hql = hql.concat(" and sp.source.id = " + source.getId());
		
		if (productGroup != null)
			hql = hql.concat(" and sp.medicine.group.id = " + productGroup.getId());
		
		if ((order == null) || (order.startsWith("neb")))
			 hql = hql.concat(" order by upper(sp.medicine.genericName.name1)");
		else hql = hql.concat(" order by " + order);
		
		List<StockPosition> items = entityManager.createQuery(hql)
			.getResultList();
		
		total = new StockSource();
		
		for (StockPosition sp: items) {
			StockSource ss = findSource(sp.getSource());
			StockPosItem it = new StockPosItem();
			it.setId(sp.getId());
			it.setLastMovement(sp.getDate());
			it.setMedicine(sp.getMedicine());
			it.setQuantity(sp.getQuantity());
			it.setTotalPrice(sp.getTotalPrice());

			calcTotal(it);
			
			ss.getItems().add(it);
		}
		
		// retorna informacoes sobre lote
		if (userSession.getTbunit().isBatchControl()) {
			hql = "select b.source.id, b.batch.medicine.id, min(b.batch.expiryDate) " +
				"from BatchQuantity b " +
				"where b.tbunit = #{userSession.tbunit} ";
			
			if (source != null)
				hql = hql.concat(" and b.source = #{stockPosHome.source}");
			
			hql = hql.concat(" group by b.source.id, b.batch.medicine.id");

			List<Object[]> batches = entityManager.createQuery(hql)
				.getResultList();
			
			for (Object[] obj: batches) {
				StockPosItem item = findItemByMedicineId((Integer)obj[0], (Integer)obj[1]);
				if (item != null) {
					Date dt = (Date)obj[2];
					item.setNextExpirationBatch(dt);
					
					StockPosItem it = total.findByMedicine(item.getMedicine());
					if ((it.getNextExpirationBatch() == null) || (it.getNextExpirationBatch().after(dt)))
						it.setNextExpirationBatch(dt);
				}
			}
		}
		
		if ((order != null) && (order.startsWith("neb"))) {
			if (order.endsWith("desc"))
				 orderMult = -1;
			else orderMult = 1;
			for (StockSource ss: sources)
				Collections.sort(ss.getItems(), this);			
		}
		
		loadMinBufferStock();
	}

	
	/**
	 * Load information about minimum buffer stock
	 */
	protected void loadMinBufferStock() {
		List<Object[]> lst = entityManager.createQuery("select mu.source.id, mu.medicine.id, mu.minBufferStock " + 
				"from MedicineUnit mu " +
				"where mu.tbunit.id = #{userSession.tbunit.id} and mu.minBufferStock is not null")
				.getResultList();
		
		for (Object[] vals: lst) {
			Integer sid = (Integer)vals[0];
			Integer mid = (Integer)vals[1];
			Integer qtd = (Integer)vals[2];
			
			StockPosItem item = findItemByMedicineId(sid, mid);
			if (item != null) {
				item.setMinBufferStock(qtd);
				item.setUnderBufferStock(item.getQuantity() <= qtd);
			}
		}
	}
	
	protected void calcTotal(StockPosItem it) {
		StockPosItem tot = total.findByMedicine(it.getMedicine());
		if (tot == null) {
			tot = new StockPosItem();
			tot.setMedicine(it.getMedicine());
			total.getItems().add(tot);
		}
		
		if ((tot.getLastMovement() == null) || (it.getLastMovement().after(tot.getLastMovement())))
			tot.setLastMovement(it.getLastMovement());
		tot.setQuantity(tot.getQuantity() + it.getQuantity());
		tot.setTotalPrice(tot.getTotalPrice() + it.getTotalPrice());
	}
	
	/**
	 * Search for source in the report list
	 * @param s
	 * @return
	 */
	protected StockSource findSource(Source s) {
		if (sources != null) {
			for (StockSource ss: sources) {
				if (ss.getSource().equals(s))
					return ss;
			}
		}
		else sources = new ArrayList<StockSource>();
		
		StockSource ss = new StockSource();
		ss.setSource(s);
		sources.add(ss);
		
		return ss;
	}
	
	/**
	 * Used internally to find the medicine (for batch information)
	 * @param sourceId
	 * @param medId
	 * @return
	 */
	protected StockPosItem findItemByMedicineId(Integer sourceId, Integer medId) {
		if (sources == null)
			return null;
		
		for (StockSource ss: sources) {
			if (ss.getSource().getId().equals(sourceId)) {
				for (StockPosItem item: ss.getItems()) {
					if (item.getMedicine().getId().equals(medId)) {
						return item;
					}
				}
			}
		}
		
		return null;
	}

	public List<StockSource> getReport() {
		if (sources == null)
			createReport();
		return sources;
	}
	
	public boolean isShowBatch() {
		return showBatch;
	}

	public void setShowBatch(boolean showBatch) {
		this.showBatch = showBatch;
	}

	public StockSource getTotal() {
		return total;
	}


	public void setTotal(StockSource total) {
		this.total = total;
	}


	public Source getSource() {
		return source;
	}


	public void setSource(Source source) {
		this.source = source;
		sources = null;
	}
	
	public boolean isExecuting() {
		return executing;
	}

	public StockPosition getItem() {
		return item;
	}

	public void setId(Integer id) {
		if (id == null)
			item = null;
		else item = (StockPosition) entityManager
				.createQuery("from StockPosition sp where sp.id = :id")
				.setParameter("id", id)
				.getSingleResult();
	}
	
	public Integer getId() {
		return (item != null? item.getId(): null);
	}

	public Movement getLastMovement() {
		if (lastMovement == null)
			recoverLastMovement();
		return lastMovement;
	}

	public Batch getFirstBatch() {
		if (firstBatch == null)
			recoverFirstBatch();
		return firstBatch;
	}
	
	protected void recoverLastMovement() {
		lastMovement = (Movement)entityManager.createQuery("from Movement m where m.medicine = :med " +  
				"and m.source = :source " +
				"and m.tbunit = :tbunit " + 
				"and m.date = :date " + 
				"and m.recordDate = (select max(aux.recordDate) from Movement aux " + 
					"where aux.medicine = m.medicine " + 
					"and aux.source = m.source " +
					"and aux.tbunit = m.tbunit " + 
					"and aux.date = m.date)")
				.setParameter("med", item.getMedicine())
				.setParameter("source", item.getSource())
				.setParameter("tbunit", item.getTbunit())
				.setParameter("date", item.getDate())
				.getSingleResult();
	}
	
	protected void recoverFirstBatch() {
		List<Batch> lst = entityManager.createQuery("from Batch b " +
				"where b.medicine = :med " + 
				"and b.source = :source " +
				"and b.tbunit = :tbunit " + 
				"and b.expirationDate = (select min(aux.expirationDate) " + 
					"from Batch aux where aux.medicine = b.medicine " + 
					"and aux.source = b.source " + 
					"and aux.tbunit = b.tbunit " +
					"and aux.remainingQuantity > 0")
				.getResultList();
		if (lst.size() > 0)
			firstBatch = lst.get(0);
	}

	public int compare(StockPosItem it1, StockPosItem it2) {
		if (it1.getNextExpirationBatch().equals(it2.getNextExpirationBatch()))
			return 0;
		
		if (it1.getNextExpirationBatch().before(it2.getNextExpirationBatch()))
			return -1 * orderMult;
		else return 1 * orderMult;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public ProductGroup getProductGroup() {
		return productGroup;
	}

	public void setProductGroup(ProductGroup productGroup) {
		this.productGroup = productGroup;
	}
	
	public void setSourceId(Integer id) {
		source = entityManager.find(Source.class, id);
	}
	
	public Integer getSourceId() {
		return (source != null? source.getId(): null);
	}
}
