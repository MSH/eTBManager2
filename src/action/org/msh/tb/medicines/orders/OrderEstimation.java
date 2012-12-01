package org.msh.tb.medicines.orders;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.MedicineUnitHome;
import org.msh.tb.entities.MedicineUnit;
import org.msh.tb.entities.Order;
import org.msh.tb.entities.OrderItem;
import org.msh.tb.entities.PrescribedMedicine;
import org.msh.tb.entities.StockPosition;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.entities.enums.OrderStatus;
import org.msh.tb.entities.enums.ValidationState;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.orders.SourceOrderItem.OrderItemAux;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("orderEstimation")
public class OrderEstimation {

	@In(create=true) EntityManager entityManager;
	@In(required=true) OrderHome orderHome;
	@In(required=true) UserSession userSession;
	@In(create=true) MedicineUnitHome medicineUnitHome;
	@In(create=true) FacesMessages facesMessages;
	
	private List<PrescribedMedicine> prescmeds;
	private List<Tbunit> units;
	private List<Tbunit> checkedUnits;
	private Date iniDate;
	private Date endDate;
	private boolean existsPendingOrder;
	private boolean initializeEditing = false;

	
	/**
	 * Initializes a new order estimation
	 */
	public void initialize() {
		Order ord = orderHome.getInstance();
		boolean isNewOrder = (ord.getId() == null || ord.getId().equals(0));
		
		if(isNewOrder){
			existsPendingOrder = verifyPendingOrder();
			if(existsPendingOrder)
				return;
				
			if (ord.getUnitFrom() != null)
				return;
			
			Tbunit unit = entityManager.find(Tbunit.class, userSession.getTbunit().getId());

			if (unit.getNumDaysOrder() == null) {
				facesMessages.addFromResourceBundle("meds.orders.initerror");
				return;
			}

			ord.setUnitFrom(unit);
			
			initializeAddress(unit);
			
			// get the current date
			iniDate = DateUtils.getDate();
			
			int numEstimatingDays = unit.getNumDaysOrder();

			// calculate the end date
			Calendar c = Calendar.getInstance(); 
			c.setTime(iniDate);
			c.add(Calendar.DAY_OF_YEAR, numEstimatingDays - 1);
			endDate = c.getTime();

			orderHome.getSources().clear();

			ord.setUnitTo(ord.getUnitFrom().getSecondLineSupplier());
			
			loadPrescribedMedicines();
			
			// is there any unit to consider ?
			if (units.size() > 0) {
				createOrderItems();
				loadStockPosition();			
			}

		}else{
			if (!initializeEditing)
				return;
			
			orderHome.getSources().clear();
			
			units = new ArrayList<Tbunit>();
			checkedUnits = new ArrayList<Tbunit>();
			mountTbList(ord.getUnitFrom());	
			
			initializeAddress(ord.getUnitFrom());
			
			if (units.size() > 0) {
				createOrderItemsForEditing();
				loadStockPosition();			
			}
			
			initializeEditing = false;
		}

	}
	
	
	/**
	 * Initialize order address based on last order or the current address set in the unit record
	 * @param unit
	 */
	public void initializeAddress(Tbunit unit) {
		List<Order> lst = entityManager.createQuery("from Order a where a.unitFrom.id = :id " +
				"and a.orderDate = (select max(b.orderDate) from Order b where b.unitFrom.id = a.unitFrom.id)")
				.setParameter("id", unit.getId())
				.getResultList();

		Order order = orderHome.getInstance();
		Order lastOrder;
		if (lst.size() > 0)
			lastOrder = lst.get(0);
		else lastOrder = null;
		
		boolean orderAddr = lastOrder != null;
		if (orderAddr) {
			orderAddr = lastOrder.isHasShipAddress();
		}

		if (orderAddr) {
			order.setShipAddress(lastOrder.getShipAddress());
			order.setShipAddressCont(lastOrder.getShipAddressCont());
			order.setShipInstitutionName(lastOrder.getShipInstitutionName());
			order.setShipZipCode(lastOrder.getShipZipCode());
			orderHome.getAuselection().setSelectedUnit(lastOrder.getShipAdminUnit());
		}
		else {
			order.setShipInstitutionName(unit.getName().toString());
			order.setShipAddress(unit.getAddress());
			order.setShipAddressCont(unit.getAddressCont());
			order.setShipZipCode(unit.getZipCode());
			orderHome.getAuselection().setSelectedUnit(unit.getAdminUnit());
		}
		
		if (lastOrder != null) {
			order.setShipContactName(lastOrder.getShipContactName());
			order.setShipContactPhone(lastOrder.getShipContactPhone());
		}
	}

	
	/**
	 * Create estimated order items for new order from the prescription list
	 */
	public void createOrderItems() {
		for (PrescribedMedicine p: prescmeds) {
			SourceOrderItem s = orderHome.sourceOrderBySource(p.getSource());

			Period period = new Period(iniDate, endDate);
			int qtd = p.calcEstimatedDispensing(period);

			period.intersect(p.getPeriod());

			OrderItemAux item = s.itemByMedicine(p.getMedicine());

			int tot = qtd + item.getItem().getEstimatedQuantity();

			item.getItem().setEstimatedQuantity(tot);
			item.getItem().setRequestedQuantity(tot);
			
			TbCase tbcase = p.getTbcase();

			item.getItem().addCase(tbcase, qtd);
		}
	}
	
	/**
	 * Create estimated order items for an editing order from the prescription list
	 */
	public void createOrderItemsForEditing() {
		for (OrderItem o: orderHome.getInstance().getItems()) {
			SourceOrderItem s = orderHome.sourceOrderBySource(o.getSource());
			
			OrderItemAux item = s.itemByMedicine(o.getMedicine());
			
			item.setItem(o);
		}
		
	}


	/**
	 * Loads the prescribed medicines during the period
	 */
	protected void loadPrescribedMedicines() {
		Order order = orderHome.getInstance();
		units = new ArrayList<Tbunit>();
		checkedUnits = new ArrayList<Tbunit>();
		mountTbList(order.getUnitFrom());		
		
		if (units.size() == 0)
			return;
		
		String s = "";
		for (Tbunit unit: units) {
			if (s.length() > 0)
				s = s + ",";
			s = s + unit.getId().toString();
		}
		
		String hql = "from PrescribedMedicine pm " +
				"join fetch pm.tbcase c join fetch pm.source s join fetch pm.medicine m " +
				"where pm.tbcase.ownerUnit.id in (" + s + ") " +
				"and pm.tbcase.state = :state " +
				"and pm.tbcase.validationState = :validate " +
				"and (pm.period.endDate >= :dtini) " +
				"and (pm.period.iniDate <= :dtend)";

		prescmeds = entityManager.createQuery(hql)
				.setParameter("dtini", iniDate)
				.setParameter("dtend", endDate)
				.setParameter("state", CaseState.ONTREATMENT)
				.setParameter("validate", ValidationState.VALIDATED)
				.getResultList();
	}


	/**
	 * Mount list of tbUnits used for the order
	 * @param unit
	 */
	protected void mountTbList(Tbunit unit) {
		checkedUnits.add(unit);
		if (unit.isTreatmentHealthUnit())
			units.add(unit);
		
		if (unit.isMedicineSupplier()) {
			List<Tbunit> lst = entityManager
				.createQuery("from Tbunit u where u.secondLineSupplier.id = :id and u.secondLineSupplier.id != u.id")
				.setParameter("id", unit.getId())
				.getResultList();

			for (Tbunit aux: lst) {
				if (!checkedUnits.contains(aux))
					mountTbList(aux);
			}
		}
	}
	
	
	protected void loadStockPosition() {
		Order order = orderHome.getInstance();
		Tbunit unitFrom = order.getUnitFrom();
		
		// monta instrução HQL IN
		String hql = "from StockPosition sp join fetch sp.medicine m " +
			"join fetch sp.source s " +
			"where sp.tbunit.id = #{order.unitFrom.id}";
	
		List<StockPosition> lst = entityManager.createQuery(hql).getResultList();
		for (StockPosition sp: lst) {
			SourceOrderItem s = orderHome.sourceOrderBySource(sp.getSource());
			OrderItemAux it = s.findItemByMedicine(sp.getMedicine());
			if (it != null) {
				int qtd = sp.getQuantity();

				// order the medicine if quantity is over min buffer stock ?
				if (!unitFrom.isOrderOverMinimum()) {
					MedicineUnit mu = medicineUnitHome.searchMedicineUnit(sp.getSource(), sp.getMedicine());
					if ((mu != null) && (mu.getMinBufferStock() != null) && (qtd > mu.getMinBufferStock())) {
						s.getItems().remove(it);
						it = null;
					}
				}
				
				if (it != null) {
					it.setStockQuantity(qtd);
					qtd = it.getItem().getEstimatedQuantity() - qtd;
					if (qtd < 0)
						qtd = 0;
					it.getItem().setRequestedQuantity(qtd);
					it.getItem().setStockQuantity(sp.getQuantity());
				}
			}
		}
	}
	
	private boolean verifyPendingOrder(){
		List<Order> o = (List<Order>) entityManager.createQuery("from Order o where o.unitFrom.id = :id and (o.status != :received and o.status != :cancelled)")
							.setParameter("id", userSession.getTbunit().getId())
							.setParameter("received", OrderStatus.RECEIVED)
							.setParameter("cancelled", OrderStatus.CANCELLED).getResultList();
		
		if(o == null || o.size() == 0)
			return false;
		else
			return true;
	}
	
	public boolean isExistsPendingOrder() {
		return existsPendingOrder;
	}


	public void setExistsPendingOrder(boolean existsPendingOrder) {
		this.existsPendingOrder = existsPendingOrder;
	}


	public boolean isInitializeEditing() {
		return initializeEditing;
	}


	public void setInitializeEditing(boolean initializeEditing) {
		this.initializeEditing = initializeEditing;
	}
	
	}
