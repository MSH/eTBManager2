package org.msh.tb.cases;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.jboss.seam.international.Messages;
import org.msh.mdrtb.entities.TbCase;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.mdrtb.entities.TreatmentHealthUnit;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.tb.cases.treatment.PrescribedMedicineHome;
import org.msh.tb.cases.treatment.PrescriptionTable;
import org.msh.tb.tbunits.TBUnitFilter;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Handle the transfer of cases from one health unit to another health unit.
 * Actions available: Transfer out, Transfer in and Roll back transfer 
 * @author Ricardo Memoria
 *
 */
@Name("caseMoveHome")
@Scope(ScopeType.CONVERSATION)
public class CaseMoveHome extends Controller {
	private static final long serialVersionUID = 5372648329946431824L;

	@In(required=true) CaseHome caseHome;
	@In(create=true) EntityManager entityManager;
	@In(create=true) FacesMessages facesMessages;
	@In(required=false) PrescriptionTable prescriptionTable;
	@In(create=true) PrescribedMedicineHome prescribedMedicineHome;
	
	private Date moveDate;
	private TBUnitSelection tbunitselection;
	private TreatmentHealthUnit currentHealthUnit;


	/**
	 * Execute the transfer of a case to another health unit 
	 * @return
	 */
	@Transactional
	public String transferOut() {
		if (!caseHome.isCanTransferOut())
			return "error";
		
		if (!validateTransferOut())
			return "error";
		
		TbCase tbcase = caseHome.getInstance();
		Tbunit tbunit = getTbunitselection().getTbunit();

		// create the period of treatment for the new health unit
		Period newPeriod = new Period(DateUtils.incDays(moveDate, 1), tbcase.getTreatmentPeriod().getEndDate());
		Period prevPeriod = new Period(currentHealthUnit.getPeriod().getIniDate(), moveDate);
		
		// create new treatment health unit
		TreatmentHealthUnit newhu = new TreatmentHealthUnit();
		newhu.setPeriod(newPeriod);
		newhu.setTbCase(caseHome.getInstance());
		newhu.setTransferring(true);
		tbcase.getHealthUnits().add(newhu);
		newhu.setTbunit(tbunit);

		currentHealthUnit.getPeriod().intersect(prevPeriod);
		
		entityManager.persist(newhu);

		prescribedMedicineHome.splitPeriod(newPeriod.getIniDate());
		
		tbcase.setState(CaseState.TRANSFERRING);

		caseHome.setDisplayMessage(false);
		caseHome.persist();

		if (prescriptionTable != null)
			prescriptionTable.refresh();

		return "transferred-out";
	}

	
	/**
	 * Validate the variables for the transfer out action
	 * @return true if transfer out can be executed
	 */
	protected boolean validateTransferOut() {
		Tbunit tbunit = getTbunitselection().getTbunit();
		if (tbunit == null)
			return false;
		
		// search for previous treatment health unit
		TreatmentHealthUnit prev = findTransferOutHealthUnit();		
		if (prev == null) 
			return false;
		
		// checks if date is before beginning treatment date
		if (!prev.getPeriod().isDateInside(moveDate)) {
			facesMessages.addFromResourceBundle("cases.move.errortreatdate");
			return false;
		}
		
		if (prev.getTbunit().equals(tbunit)) {
			facesMessages.addFromResourceBundle("cases.move.errorunit");
			return false;
		}
		
		currentHealthUnit = prev;
		return true;
	}

	
	/**
	 * Roll back an on-going transfer from one unit to another, restoring the previous state before the transfer
	 * @return
	 */
	@Transactional
	public String rollbackTransferOut() {
		TbCase tbcase = caseHome.getInstance();
		if (tbcase.getHealthUnits().size() <= 1) {
			facesMessages.add("No unit to roll back transfer");
			return "error";
		}

		List<TreatmentHealthUnit> hus = tbcase.getSortedTreatmentHealthUnits();
		TreatmentHealthUnit tout = hus.get(hus.size() - 2);
		TreatmentHealthUnit tin = hus.get(hus.size() - 1);

		hus.remove(tin);
		entityManager.remove(tin);
		
		tout.getPeriod().setEndDate(tin.getPeriod().getEndDate());
		
		Date movdt = tin.getPeriod().getIniDate();
		
		Date iniContPhase = tbcase.getIniContinuousPhase();
		if ((iniContPhase != null) && (!iniContPhase.equals(movdt))) {
			prescribedMedicineHome.joinPeriods(movdt);
/*			// check prescriptions to merge
			int i = 0;
			while (i < tbcase.getPrescribedMedicines().size()) {
				PrescribedMedicine pm = tbcase.getPrescribedMedicines().get(i);
				
				if (pm.getPeriod().getIniDate().equals(movdt)) {
					PrescribedMedicine aux = findAdjacentPrescription(pm);
					if (aux != null) {
						aux.getPeriod().setEndDate(pm.getPeriod().getEndDate());
						entityManager.remove(pm);
						tbcase.getPrescribedMedicines().remove(pm);
					}
					else i++;
				}
				else i++;
			}
*/		}

		tbcase.setState(CaseState.ONTREATMENT);
		caseHome.persist();
		
		if (prescriptionTable != null)
			prescriptionTable.refresh();

		return "success";
	}
	
	
	/**
	 * Find left adjacent prescription compatible with the given prescription
	 * @param pm
	 * @return
	 */
/*	protected PrescribedMedicine findAdjacentPrescription(PrescribedMedicine pm) {
		TbCase tbcase = caseHome.getInstance();
		Date dt = DateUtils.incDays( pm.getPeriod().getIniDate(), -1);
		for (PrescribedMedicine aux: tbcase.getPrescribedMedicines()) {
			if ((aux.getPeriod().getEndDate().equals(dt)) && (aux.getMedicine().equals(pm.getMedicine())) &&
			   (aux.getSource().equals(pm.getSource())) &&
			   (aux.getDoseUnit() == pm.getDoseUnit()) && (aux.getFrequency() == pm.getFrequency()))
			{
				return aux;
			}
		}
		return null;
	}
*/
	
	/**
	 * Initialize the transfer in registration
	 */
	public void initializeTransferIn() {
		if (!caseHome.isCanTransferIn())
			return;

		TreatmentHealthUnit hu = caseHome.getTransferInHealthUnit();
		moveDate = hu.getPeriod().getIniDate();
	}


	/**
	 * Register the transfer in of the case
	 * @return
	 */
	@Transactional
	public String transferIn() {
		TreatmentHealthUnit prev = findTransferOutHealthUnit();
		
		if (prev == null)
			return "error";
		
		if (!moveDate.after(prev.getPeriod().getEndDate())) {
			facesMessages.addFromResourceBundle("cases.move.errordt-transferin");
			return "error";
		}

		TreatmentHealthUnit hu = caseHome.getTransferInHealthUnit();
		if (!moveDate.before(hu.getPeriod().getEndDate())) {
			facesMessages.addFromResourceBundle("cases.move.notreat", moveDate);
			return "error";
		}

		hu.getPeriod().setIniDate(moveDate);
		hu.setTransferring(false);
		entityManager.persist(hu);

		caseHome.getInstance().setState(CaseState.ONTREATMENT);
		
		caseHome.setDisplayMessage(false);
		caseHome.persist();

		return "trasnferred-in";
	}
	
	
	/**
	 * Returns the treatment health unit that is transferring out 
	 * @return
	 */
	protected TreatmentHealthUnit findTransferOutHealthUnit() {
		TbCase tbcase = caseHome.getInstance();
		
		Date dt = null;
		TreatmentHealthUnit aux = null;
		for (TreatmentHealthUnit hu: tbcase.getHealthUnits()) {
			if ((!hu.isTransferring()) && 
				((dt == null) || (hu.getPeriod().getIniDate().after(dt))))
			{
				dt = hu.getPeriod().getIniDate();
				aux = hu;
			}
		}
		
		return aux;
	}

	public Date getMoveDate() {
		return moveDate;
	}

	public void setMoveDate(Date moveDate) {
		this.moveDate = moveDate;
	}

	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			tbunitselection = new TBUnitSelection(false, TBUnitFilter.HEALTH_UNITS);
			tbunitselection.setApplyHealthSystemRestrictions(false);
			tbunitselection.setHealthSystem(null);
		}
		return tbunitselection;
	}
	
	/**
	 * Return message to confirm roll back of the transfer out
	 * @return
	 */
	public String getConfirmMsgRollbackTransfer() {
		TreatmentHealthUnit tu = caseHome.getTransferInHealthUnit();
		if (tu == null)
			return null;

		String msg = Messages.instance().get("cases.move.cancel-confirm");
		msg = MessageFormat.format(msg, tu.getTbunit().getName().toString());
		return msg;
	}
}
