package org.msh.tb.cases;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.Controller;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.ViewService;
import org.msh.tb.cases.treatment.PrescribedMedicineHome;
import org.msh.tb.cases.treatment.PrescriptionTable;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.TreatmentHealthUnit;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.tbunits.TBUnitSelection2;
import org.msh.tb.tbunits.TBUnitType;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

import javax.persistence.EntityManager;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;


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
	@In EntityManager entityManager;
	@In FacesMessages facesMessages;
	@In(required=false) PrescriptionTable prescriptionTable;
	@In(create=true) PrescribedMedicineHome prescribedMedicineHome;
	
	private Date moveDate;
	private TBUnitSelection2 tbunitselection2;
	private TreatmentHealthUnit currentHealthUnit;


	/**
	 * Called when the transfer out form is displayed to the user
	 */
	public void initializeTransferOut() {
		// if it's an HTTP POST, ignore the initialize method
		if (ViewService.instance().isFormPost())
			return;
		
		getTbunitselection2().setAdminUnit(null);
	}
	
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
		Tbunit tbunit = getTbunitselection2().getSelected();
//		Tbunit unitFrom = tbcase.getOwnerUnit();

		// create the period of treatment for the new health unit
		Period newPeriod = new Period(DateUtils.incDays(moveDate, 1), tbcase.getTreatmentPeriod().getEndDate());
		Period prevPeriod = new Period(currentHealthUnit.getPeriod().getIniDate(), moveDate);
		
		// create new treatment health unit
		TreatmentHealthUnit newhu = new TreatmentHealthUnit();
		newhu.setPeriod(newPeriod);
		newhu.setTbcase(caseHome.getInstance());
		newhu.setTransferring(true);
		tbcase.getHealthUnits().add(newhu);
		newhu.setTbunit(tbunit);

		currentHealthUnit.getPeriod().intersect(prevPeriod);
		
//		entityManager.persist(newhu);

		prescribedMedicineHome.splitPeriod(newPeriod.getIniDate());
		
		tbcase.setState(CaseState.TRANSFERRING);

		caseHome.setDisplayMessage(false);
		caseHome.persist();

		if (prescriptionTable != null)
			prescriptionTable.refresh();

		Events.instance().raiseEvent("case.transferout");

		OwnerUnitChecker.checkOwnerId(tbcase);

		return "transferred-out";
	}

	
	/**
	 * Validate the variables for the transfer out action
	 * @return true if transfer out can be executed
	 */
	protected boolean validateTransferOut() {
		Tbunit tbunit = getTbunitselection2().getSelected();
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

		// information to be logged
		Contexts.getEventContext().set("transferdate", tin.getPeriod().getIniDate());
		Contexts.getEventContext().set("transferunit", tin.getTbunit());
		
		Events.instance().raiseEvent("case.transferout.rollback");

		OwnerUnitChecker.checkOwnerId(tbcase);

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
		// just initialize it if it's an HTTP GET
		if (ViewService.instance().isFormPost())
			return;

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

		TbCase tbcase = caseHome.getInstance();
		
		tbcase.setState(CaseState.ONTREATMENT);

		caseHome.setDisplayMessage(false);
		caseHome.persist();

		Events.instance().raiseEvent("case.transferin");

		OwnerUnitChecker.checkOwnerId(tbcase);

		return "trasnferred-in";
	}
	
	
	/**
	 * Returns the treatment health unit that is transferring out 
	 * @return
	 */
	public TreatmentHealthUnit findTransferOutHealthUnit() {
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

	public TBUnitSelection2 getTbunitselection2() {
		if (tbunitselection2 == null) {
			tbunitselection2 = new TBUnitSelection2("unitid", false, TBUnitType.HEALTH_UNITS);
			tbunitselection2.setApplyHealthSystemRestrictions(false);
//			tbunitselection.setHealthSystem(null);
//			tbunitselection.setIgnoreReadOnlyRule(true);
		}
		return tbunitselection2;
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
