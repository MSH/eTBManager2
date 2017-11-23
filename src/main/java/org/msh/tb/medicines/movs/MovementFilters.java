package org.msh.tb.medicines.movs;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.Source;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * Control the report execution between batch and medicine display
 * @author Ricardo
 *
 */
@Name("movementFilters")
@Scope(ScopeType.SESSION)
public class MovementFilters {

	@In(create=true) MovementsQuery movements;
	@In(create=true) BatchMovementsQuery batchMovements;
    @In(create=true) EntityManager entityManager;
    @In(create=true) UserSession userSession;
	
	private boolean showAllUnits;
	private boolean executing;
	private boolean batchesOutput;
	private Date dateIni;
	private Date dateEnd;
	private MovementType type;
	private String batchNumber;
	private Integer selectedMovement;
    private Integer selectedBatchMovement;
	private FieldValueComponent adjustmentInfo = new FieldValueComponent();
    private Medicine medicine;
    private Batch batch;
    private List<Batch> batchList;
    private Source source;

    /**
	 * Return if the executing report is by movements of medicines
	 * @return true if it's medicine output report
	 */
	public boolean isMedicineMovsExecuting() {
		return (!batchesOutput) && (isExecuting());
	}
	
	
	/**
	 * Check if the executing report is by batch movements
	 * @return true if it's batch output report
	 */
	public boolean isBatchMovsExecuting() {
		return (batchesOutput) && (isExecuting());
	}


	/**
	 * Initialize the filters for executing
	 */
	public void initialize() {

	}


	/**
	 * Refresh the list to be displayed and set the report is ready for executing
	 */
	public void refresh() {
		executing = true;
		if (batchesOutput)
			 batchMovements.refresh();
		else movements.refresh();
	}

	/**
	 * @param batchesOutput the batchesOutput to set
	 */
	public void setBatchesOutput(boolean batchesOutput) {
		this.batchesOutput = batchesOutput;
	}

	/**
	 * @return the batchesOutput
	 */
	public boolean isBatchesOutput() {
		return batchesOutput;
	}


	/**
	 * @return the executing
	 */
	public boolean isExecuting() {
		if (!executing)
			checkGetMethod();
		
		return executing;
	}
	

	/**
	 * Check if it's a GET or a POST
	 */
	public void checkGetMethod() {
		FacesContext faces = FacesContext.getCurrentInstance();
		boolean bGet = ((HttpServletRequest)(faces.getExternalContext().getRequest())).getMethod().equalsIgnoreCase("GET");
		if (bGet)
			executing = true;
	}

	/**
	 * @return the type
	 */
	public MovementType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MovementType type) {
		this.type = type;
		if(this.type != MovementType.ADJUSTMENT)
			this.adjustmentInfo = null;
	}


	/**
	 * @return the dateIni
	 */
	public Date getDateIni() {
		return dateIni;
	}


	/**
	 * @param dateIni the dateIni to set
	 */
	public void setDateIni(Date dateIni) {
		this.dateIni = dateIni;
	}


	/**
	 * @return the dateEnd
	 */
	public Date getDateEnd() {
		return dateEnd;
	}


	/**
	 * @param dateEnd the dateEnd to set
	 */
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}


	/**
	 * @return the batchNumber
	 */
	public String getBatchNumber() {
		return batchNumber;
	}


	/**
	 * @param batchNumber the batchNumber to set
	 */
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}


	/**
	 * @return the selectedMovement
	 */
	public Integer getSelectedMovement() {
		return selectedMovement;
	}


	/**
	 * @param selectedMovement the selectedMovement to set
	 */
	public void setSelectedMovement(Integer selectedMovement) {
		this.selectedMovement = selectedMovement;
	}
	
	public FieldValueComponent getAdjustmentInfo() {
		if (adjustmentInfo == null)
			adjustmentInfo = new FieldValueComponent();
		return adjustmentInfo;
	}
	
	public void setAdjustmentInfo(FieldValueComponent adjustmentInfo) {
		this.adjustmentInfo = adjustmentInfo;
	}


	/**
	 * @return the showAllUnits
	 */
	public boolean isShowAllUnits() {
		return showAllUnits;
	}


	/**
	 * @param showAllUnits the showAllUnits to set
	 */
	public void setShowAllUnits(boolean showAllUnits) {
		this.showAllUnits = showAllUnits;
	}


    /**
     * @return the selectedBatchMovement
     */
    public Integer getSelectedBatchMovement() {
        return selectedBatchMovement;
    }


    /**
     * @param selectedBatchMovement the selectedBatchMovement to set
     */
    public void setSelectedBatchMovement(Integer selectedBatchMovement) {
        this.selectedBatchMovement = selectedBatchMovement;
    }


    /**
     * @return the medicine
     */
    public Medicine getMedicine() {
        return medicine;
    }


    /**
     * @param medicine the medicine to set
     */
    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
        batch = null;
    }


    /**
     * @return the batch
     */
    public Batch getBatch() {
        return batch;
    }


    /**
     * @param batch the batch to set
     */
    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public List<Batch> getBatchList() {
        batchList = null;
        if(getMedicine() != null)
            updateBatchList(getMedicine().getId());
        return batchList;
    }

    public void setBatchList(List<Batch> batchList) {
        this.batchList = batchList;
    }

    private void updateBatchList(Integer medicineId){
        batchList = (List<Batch>) entityManager.createQuery("select b from BatchMovement bm join bm.batch b " +
                                                            "where b.medicine.id = :medicineId and bm.movement.tbunit.id = :unitId " +
                                                            "group by bm.batch")
                .setParameter("medicineId", medicineId)
                .setParameter("unitId", userSession.getWorkingTbunit().getId())
                .getResultList();
    }
}
