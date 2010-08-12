package org.msh.tb.medicines.movs;

import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.enums.MovementType;

/**
 * Control the report execution between batch and medicine display
 * @author Ricardo
 *
 */
@Name("movementFilters")
public class MovementFilters {

	@In(create=true) MovementsQuery movements;
	@In(create=true) BatchMovementsQuery batchMovements;
	
	private boolean executing;
	private boolean batchesOutput;
	private Date date;
	private MovementType type;

	
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
		if (date == null)
			date = new Date();
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
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
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
	}
}
