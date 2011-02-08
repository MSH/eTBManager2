package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.security.Identity;
import org.msh.mdrtb.entities.Batch;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.mdrtb.entities.CaseDispensing;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.MedicineDispensing;
import org.msh.mdrtb.entities.MedicineDispensingBatch;
import org.msh.mdrtb.entities.MedicineDispensingItem;
import org.msh.mdrtb.entities.Movement;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.StockPosition;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.Workspace;
import org.msh.mdrtb.entities.enums.MovementType;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.MedicinesQuery;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.BatchSelection;
import org.msh.tb.medicines.movs.MovementHome;
import org.msh.utils.date.DateUtils;


@Name("medicineDispensingHome")
public class MedicineDispensingHome extends EntityHomeEx<MedicineDispensing>{
	private static final long serialVersionUID = 4506387699471166674L;

	@In(create=true) EntityManager entityManager;
	@In(create=true) FacesMessages facesMessages;
	@In(create=true) MedicinesQuery medicines;
	@In(create=true) BatchSelection batchSelection;
	@In(create=true) UserSession userSession;
	@In(create=true) MovementHome movementHome;

	private List<SourceGroup> sources;
	private Integer month;
	private Integer year;
	private Integer dayIni;
	private Integer dayEnd;
	private MedicineDispensingItem item;
	private List<SelectItem> days;
	private String clientError;
	private boolean initialized;
	
	public class SourceGroup extends org.msh.tb.SourceGroup<DispItem> {}

	
	@Factory("medicineDispensing")
	public MedicineDispensing getMedicineDispensing() {
		return getInstance();
	}


	/**
	 * Initialize a new dispensing
	 */
	public void initializeDispensing() {
		// check if it was already initialized
		if (initialized)
			return;
		
		initialized = true;
		MedicineDispensing dispensing = getInstance();
		if (isManaged()) {
			year = DateUtils.yearOf( dispensing.getEndDate() );
			month = DateUtils.monthOf( dispensing.getEndDate() );			
			dayIni = DateUtils.dayOf( dispensing.getIniDate() );			
			dayEnd = DateUtils.dayOf( dispensing.getEndDate() );
			return;
		}
		
        if (!isManaged())
        	initializeDays();
	}

    
    /**
     * Save dispensing information
     * @return
     */
    @Transactional
    public String saveDispensing()
    {
    	MedicineDispensing dispensing = getInstance();

    	// check if any batch were entered
    	boolean hasBatch = false;
    	for (SourceGroup grp: sources) {
    		for (DispItem item: grp.getItems()) {
        		if (item.getItem().getBatches().size() > 0) {
        			hasBatch = true;
        			break;
        		}
    		}
    	}
    	
    	if (!hasBatch) {
    		facesMessages.addFromResourceBundle("medicines.orders.nobatchsel");
    		return "error";
    	}
    	
    	if (!validateDates()) {
    		facesMessages.addFromResourceBundle("medicines.dispensing.perioderror");
    		return "error";
    	}
    	
    	if (!isManaged()) {
    		Tbunit unit = entityManager.merge(userSession.getTbunit());
    		dispensing.setTbunit(unit);
    	}
    	
    	Calendar c = Calendar.getInstance();
    	c.clear();
    	c.set(year, month, dayIni);
    	dispensing.setIniDate(c.getTime());
    	
    	c.set(year, month, dayEnd);
    	dispensing.setEndDate(c.getTime());

    	// check if can decrease the quantity in stock
/*    	boolean validMovements = true;

    	for (SourceGroup grp: sources) {
        	for (DispItem aux: grp.getItems()) {
        		MedicineDispensingItem item = aux.getItem(); 
        		if (item.getBatches().size() > 0) {
        			int qtd = item.getQuantity();
        			Movement mov = item.getMovement();
        			if (mov != null)
        				qtd -= mov.getQuantity();
        			
        			boolean erroQuantity = false;
        			if (qtd > 0) {
        				erroQuantity = !movementHome.canDecreaseStock(dispensing.getTbunit(), item.getSource(), 
        						item.getMedicine(), item.getQuantity(), dispensing.getEndDate());
        			}
      
        			aux.setInvalid(erroQuantity);
        			if (erroQuantity)
        				validMovements = false;
        		}
        	}
    	}
    	
    	if (!validMovements)
    		return "error";
*/
    	boolean validMovements = true;
    	// mount dispensing object with items 
    	for (SourceGroup grp: sources) {
    		for (DispItem aux: grp.getItems()) {
    			MedicineDispensingItem item = aux.getItem();
    			// has any batch in the medicine item ?
    			if (item.getBatches().size() == 0) {
    				// is managed ?
    				if (item.getDispensing() != null) {
    					entityManager.remove(item);
    					entityManager.flush();
    					dispensing.getItems().remove(item);
    				}
    			}
    			else {
    				if (item.getDispensing() == null) {
    					dispensing.getItems().add(item);
    					item.setDispensing(dispensing);
    				}
    			}
    		}
    	}
/*
     	int index = 0;
    	while (index < dispensing.getItems().size()) {
    		if (dispensing.getItems().get(index).getBatches().size() == 0)
    			 dispensing.getItems().remove(index);
    		else index++;
    	}
*/
    	// create movements
    	movementHome.initMovementRecording();
		for (MedicineDispensingItem it: dispensing.getItems()) {
			// check if movement has to be created
			if ((it.getMovement() == null) || (it.getMovement().getQuantity() != it.getQuantity())) {
				Movement mov = movementHome.prepareNewMovement(c.getTime(), dispensing.getTbunit(), 
						it.getSource(), it.getMedicine(), MovementType.DISPENSING, 
						getBatchesMap(it), null);				

				// there is already a movement ?
				if (it.getMovement() != null) {
					Movement movold = it.getMovement();
					// remove movement
					it.setMovement(mov);
					entityManager.flush();
					movementHome.removeMovement(movold);
				}
				else {
					it.setMovement(mov);
				}
			}
		}
		movementHome.savePreparedMovements();
    	
        return persist();
    }
	

    /**
     * Estimate quantity based on case dispensing
     */
    public void estimateQuantity() {
    	List<CaseDispensing> lstDisp = entityManager.createQuery("from CaseDispensing c " +
    			"left join fetch c.dispensingDays " +
    			"where c.month = :month and c.year = :year " +
    			"and c.tbcase.id in (select hu.tbcase.id from TreatmentHealthUnit hu " +
    			"where hu.tbunit.id = :huid and tbcase.id = c.tbcase.id)")
    			.setParameter("month", month + 1)
    			.setParameter("year", year)
    			.setParameter("huid", userSession.getTbunit().getId())
    			.getResultList();
    	if (lstDisp.size() == 0) {
    		clientError = Messages.instance().get("medicines.dispensing.esterror1");
    		return;
    	}
   
    	clientError = null;
    }

    
    @Override
    public String remove() {
    	List<Movement> movs = new ArrayList<Movement>();
    	
    	for (MedicineDispensingItem item: getInstance().getItems()) {
    		if (item.getMovement() != null)
    			movs.add(item.getMovement());
    		item.setMovement(null);
    	}
    	
    	String ret = super.remove();
    	
    	// it was removed?
    	if (ret.equals("removed")) {
    		for (Movement mov: movs)
    			movementHome.removeMovement(mov);
    	}
    	
    	return ret;
    }

    /**
     * Initialize the batches to be selected by the user
     * @param item
     */
    public void initializeBatchSelection(MedicineDispensingItem item)
    {
        this.item = item;
        batchSelection.clear();
        batchSelection.setTbunit(userSession.getTbunit());
        batchSelection.setMedicine(item.getMedicine());
        batchSelection.setSource(item.getSource());
        batchSelection.setAllowQtdOverStock(false);
        batchSelection.setSelectedBatches(getBatchesMap(item));
    }

   
    private Map<Batch, Integer> getBatchesMap(MedicineDispensingItem item)
    {
        Map<Batch, Integer> sels = new HashMap<Batch, Integer>();
        for (MedicineDispensingBatch b: item.getBatches()) {
        	sels.put(b.getBatch(), b.getQuantity());
        }

        return sels;
    }

   
    public void selectBatches()
    {
        Map<BatchQuantity, Integer> sels = batchSelection.getSelectedBatchesQtds();
        for (BatchQuantity b: sels.keySet()) {
            Integer val = (Integer)sels.get(b);
            if(val == null || val.intValue() == 0) {
                return;
            }
        }

        item.getBatches().clear();
        for (BatchQuantity b: sels.keySet()) {
        	MedicineDispensingBatch dispBatch = new MedicineDispensingBatch();
        	dispBatch.setItem(item);
            dispBatch.setBatch(b.getBatch());
            dispBatch.setQuantity(((Integer)sels.get(b)).intValue());
            item.getBatches().add(dispBatch);
        }

        batchSelection.clear();
        item = null;
    }

    

    /**
     * Return list of sources
     * @return
     */
    public List<SourceGroup> getSources()  {
        if(sources == null)
            createSources();
        return sources;
    }


    /**
     * Create the list of sources to be displayed in the form
     */
    protected void createSources()  {
    	MedicineDispensing dispensing = getInstance();
        
        sources = new ArrayList<SourceGroup>();
    	
    	// is for editing ?
    	if (initialized) {
    		Integer wsid = ((Workspace)Component.getInstance("defaultWorkspace")).getId();
            List<StockPosition> lst = entityManager.createQuery("from StockPosition sp " +
            		"join fetch sp.source s join fetch sp.medicine m " +
            		"where s.workspace.id = " + wsid + " and sp.tbunit.id = " + userSession.getTbunit().getId() +
            		" and sp.date = (select max(aux.date) from StockPosition aux " +
            		"where aux.tbunit.id = sp.tbunit.id and aux.medicine.id = sp.medicine.id and aux.source.id = sp.source.id) " +
            		"order by s.name.name1, m.genericName.name1")
            		.getResultList();
            
            for (StockPosition sp: lst) {
            	Source source = sp.getSource();
            	Medicine medicine = sp.getMedicine();

            	// search for group
            	SourceGroup grp = null;
            	for (SourceGroup aux: sources) {
            		if (aux.getSource().equals(source)) {
            			grp = aux;
            			break;
            		}
            	}
            	
            	if (grp == null) {
            		grp = new SourceGroup();
            		grp.setSource(source);
            		sources.add(grp);
            	}
            	
            	DispItem dispItem = new DispItem();
            	
            	MedicineDispensingItem item = dispensing.findItem(source, medicine);
            	if (item == null) {
            		item = new MedicineDispensingItem();
            		item.setMedicine(medicine);
            		item.setSource(source);
            	}
            	
            	dispItem.setItem(item);
            	dispItem.setAvailableQuantity(sp.getQuantity());
            	grp.getItems().add(dispItem);
            }
    	}
        
        // check if there is any item recorded in the dispensing that was not included in 
        // the list of sources and medicines
        for (MedicineDispensingItem item: dispensing.getItems()) {
        	SourceGroup grp = findSource(item.getSource());
        	if (grp == null) {
        		grp = new SourceGroup();
        		grp.setSource(item.getSource());
        		sources.add(grp);
        	}

        	DispItem aux = findItem(grp, item);
        	if (aux == null) {
        		aux = new DispItem();
        		aux.setItem(item);
        		aux.setAvailableQuantity(0);
        		grp.getItems().add(aux);
        	}
        }
    }

    
    /**
     * Search for an item of the {@link SourceGroup} class by its {@link MedicineDispensingItem} property
     * @param grp
     * @param item
     * @return
     */
    protected DispItem findItem(SourceGroup grp, MedicineDispensingItem item) {
    	for (DispItem aux: grp.getItems()) {
    		if (aux.getItem() == item) {
    			return aux;
    		}
    	}
    	return null;
    }
        
    protected SourceGroup findSource(Source source)
    {
        for(SourceGroup grp: sources)   {
            if(grp.getSource().getId().equals(source.getId()))   {
                return grp;
            }
        }

        return null;
    }


	/**
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	/**
	 * @return the dayIni
	 */
	public Integer getDayIni() {
		return dayIni;
	}

	/**
	 * @param dayIni the dayIni to set
	 */
	public void setDayIni(Integer dayIni) {
		this.dayIni = dayIni;
	}

	/**
	 * @return the dayEnd
	 */
	public Integer getDayEnd() {
		return dayEnd;
	}

	/**
	 * @param dayEnd the dayEnd to set
	 */
	public void setDayEnd(Integer dayEnd) {
		this.dayEnd = dayEnd;
	}
	
	public List<SelectItem> getDays() {
		if (days == null) {
			days = new ArrayList<SelectItem>();
			SelectItem item = new SelectItem();
			item.setValue(null);
			item.setLabel("-");
			days.add(item);
			
			int numdays = DateUtils.daysInAMonth(year, month);
			for (int i = 1; i <= numdays; i++) {
				item = new SelectItem();
				item.setValue(i);
				item.setLabel(((Integer)i).toString());
				days.add(item);
			}
		}
		return days;
	}
	
	
	/**
	 * Initialize the days for a new dispensing
	 */
	protected void initializeDays() {
		if ((month == null) || (year == null))
			return;
		
		Date dt = (Date)entityManager.createQuery("select max(m.endDate) from MedicineDispensing m " +
				"where month(m.endDate) = :month " +
				"and year(m.endDate) = :year " +
				"and m.tbunit.id = #{userSession.tbunit.id}")
				.setParameter("month", month + 1)
				.setParameter("year", year)
				.getSingleResult();
		
		Date dtToday = new Date();
		int currMonth = DateUtils.monthOf( dtToday );
		int currYear = DateUtils.yearOf( dtToday );
		int numDays = DateUtils.daysInAMonth(month, year);

		if (dt != null) {
			int n = DateUtils.dayOf( dt ) + 1;
			dayIni = (n > numDays? numDays: n);
		}
		else dayIni = 1;
		
		if ((month == currMonth) && (year == currYear)) {
			dayEnd = DateUtils.dayOf( dtToday );
		}
		else {
			dayEnd = numDays;
		}
		if (dayEnd < dayIni)
			dayEnd = dayIni;
	}


	/**
	 * Check if there is any dispensing within the selected period
	 * @return
	 */
	protected boolean validateDates() {
		Date dtIni = DateUtils.newDate(year, month, dayIni);
		Date dtEnd = DateUtils.newDate(year, month, dayEnd);
		
		String cond = (isManaged()? " and md.id <> " + getId().toString(): "");
		
		Long num = (Long)entityManager.createQuery("select count(*) from MedicineDispensing md " +
				"where md.iniDate <= :dt1 and md.endDate >= :dt2" +
				cond +
				" and md.tbunit.id = #{userSession.tbunit.id}")
				.setParameter("dt1", dtEnd)
				.setParameter("dt2", dtIni)
				.getSingleResult();
		
		return num == 0;
	}

	
	
	/**
	 * Check if user can change the dispensing
	 * @return
	 */
	public boolean isCanChange() {
		if (!Identity.instance().hasRole("DISP_PAC_EDT"))
			return false;
		
		Tbunit userunit = userSession.getWorkingTbunit();
		Tbunit dispunit = getInstance().getTbunit();
		
		return ((dispunit != null) && (dispunit.getId().equals(userunit.getId())));
	}


	/**
	 * @return the clientError
	 */
	public String getClientError() {
		return clientError;
	}
	
	public class DispItem {
		private MedicineDispensingItem item;
		private int availableQuantity;
		private boolean invalid;
		/**
		 * @return the item
		 */
		public MedicineDispensingItem getItem() {
			return item;
		}
		/**
		 * @param item the item to set
		 */
		public void setItem(MedicineDispensingItem item) {
			this.item = item;
		}
		/**
		 * @return the availableQuantity
		 */
		public int getAvailableQuantity() {
			return availableQuantity;
		}
		/**
		 * @param availableQuantity the availableQuantity to set
		 */
		public void setAvailableQuantity(int availableQuantity) {
			this.availableQuantity = availableQuantity;
		}
		/**
		 * @return the invalid
		 */
		public boolean isInvalid() {
			return invalid;
		}
		/**
		 * @param invalid the invalid to set
		 */
		public void setInvalid(boolean invalid) {
			this.invalid = invalid;
		}
	}
}
