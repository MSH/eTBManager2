package org.msh.tb.medicines.movs;

import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.enums.MovementType;

/**
 * Control the report execution between batch and medicine display
 * @author Ricardo
 *
 */
@Name("movementFilters")
@Scope(ScopeType.CONVERSATION)
public class MovementFilters {

	@In(create=true) MovementsQuery movements;
	@In(create=true) BatchMovementsQuery batchMovements;
	
	private boolean executing;
	private boolean batchesOutput;
	private Date dateIni;
	private Date dateEnd;
	private MovementType type;
	private String batchNumber;
	private Integer selectedMovement;
	private FieldValueComponent adjustmentInfo = new FieldValueComponent();

	
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
		if (dateIni == null)
			dateIni = new Date();
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
	
}
