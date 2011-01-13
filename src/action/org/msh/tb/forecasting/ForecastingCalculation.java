package org.msh.tb.forecasting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.RaiseEvent;
import org.msh.mdrtb.entities.Forecasting;
import org.msh.mdrtb.entities.ForecastingBatch;
import org.msh.mdrtb.entities.ForecastingCasesOnTreat;
import org.msh.mdrtb.entities.ForecastingMedicine;
import org.msh.mdrtb.entities.ForecastingNewCases;
import org.msh.mdrtb.entities.ForecastingOrder;
import org.msh.mdrtb.entities.ForecastingRegimen;
import org.msh.mdrtb.entities.ForecastingResult;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.MedicineRegimen;
import org.msh.mdrtb.entities.PrescribedMedicine;
import org.msh.mdrtb.entities.Regimen;
import org.msh.mdrtb.entities.WeeklyFrequency;
import org.msh.mdrtb.entities.enums.CaseState;
import org.msh.mdrtb.entities.enums.MedicineLine;
import org.msh.mdrtb.entities.enums.RegimenPhase;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


/**
 * Responsible for calculating and generation of the forecasting results
 * @author Ricardo Memoria
 *
 */
@Name("forecastingCalculation")
public class ForecastingCalculation {

	@In(create=true) ForecastingHome forecastingHome;
	@In EntityManager entityManager;
	
	/**
	 * total number of months of the forecasting
	 */
	private int numMonths;

	
	/**
	 * Store the ending date of the forecasting, i.e, the forecasting.endDate + forecasting.bufferStock
	 */
	private Date endForecasting;


	/**
	 * The forecasting object being calculated
	 */
	private Forecasting forecasting;
	
	/**
	 * List of cases on treatment, used during calculations if cases are from database
	 */
	private List<Object[]> casesOnTreatment;

	/**
	 * Used inside the calculation
	 */
	private PrescribedMedicine pm;
	
	/**
	 * month index of the initial date of review period (and end of lead time)
	 * according to the reference date, starting in 0 and going up to positive values
	 */
	private int monthIndexIniDate;

	/**
	 * Execute the forecasting
	 */
	@RaiseEvent("forecasting-execute")
	public String execute() {
		forecasting = forecastingHome.getInstance();

		// calculate the number of months for the whole forecasting calculation
		endForecasting = DateUtils.incMonths(forecasting.getEndDate(), forecasting.getBufferStock());
		numMonths = forecasting.getNumMonths();

		startCalculation();
		try {
			calculate();
		}
		finally {
			finishCalculation();
		}
		
		return "executed";
	}


	/**
	 * Start procedures prior to execute the calculation loop
	 */
	protected void startCalculation() {
		// clear current results
		forecasting.clearResults();

		monthIndexIniDate = forecasting.getMonthIndex(forecasting.getIniDateLeadTime());
		
		forecasting.initializeResults();
/*		for (ForecastingMedicine med: forecasting.getMedicines()) {
			int expiryQtd = 0;
			for (int i = 0; i <= numMonths; i++) {
				ForecastingResult res = new ForecastingResult();
				res.setMedicine(med.getMedicine());
				res.setMonthIndex(i);
				expiryQtd += quantityToExpire(res);
				int stockOnHand = med.getStockOnHand() - expiryQtd;
				res.setStockOnHand(stockOnHand);
				forecasting.getResults().add(res);
			}
		}
*/
		// cases are from the database ?
		if (forecasting.isCasesFromDatabase()) {
			// update number of cases on treatment
			Long count = (Long)entityManager.createQuery("select count(*) from TbCase c " + 
					"where c.notificationUnit.workspace.id = #{defaultWorkspace.id} " +
					"and c.state = :state")
					.setParameter("state", CaseState.ONTREATMENT)
					.getSingleResult();
			forecasting.setNumCasesOnTreatment(count.intValue());

			// load information about cases on treatment
			String hql = "select pm.period.iniDate, pm.period.endDate, pm.doseUnit, pm.medicine.id, pm.frequency, tb.id " +
					"from PrescribedMedicine pm " +
					"join pm.tbcase tb " +
					"where pm.medicine.workspace.id = #{defaultWorkspace.id} " +
					"and tb.state = :val " +
					"and pm.period.endDate >= :dtIni ";
			if (MedicineLine.FIRST_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.FIRST_LINE.ordinal();
			else
			if (MedicineLine.SECOND_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.SECOND_LINE.ordinal();

			hql += " order by tb.id, pm.medicine.id";
			casesOnTreatment = entityManager.createQuery(hql)
					.setParameter("val", CaseState.ONTREATMENT)
					.setParameter("dtIni", forecasting.getReferenceDate())
					.getResultList();
			
			pm = new PrescribedMedicine();
		}

		// remove unused new cases that are blank
		int i = 0;
		while (i < forecasting.getCasesOnTreatment().size()) {
			ForecastingCasesOnTreat c = forecasting.getCasesOnTreatment().get(i);
			if (c.getNumCases() == null)
				 forecasting.getCasesOnTreatment().remove(i);
			else i++;
		}
	}


	/**
	 * Procedures to be executed after calculation loop
	 */
	protected void finishCalculation() {
		// free internal variables
		casesOnTreatment = null;
		pm = null;
	}


	/**
	 * Calculate the forecasting for an specific month of the year
	 * @param monthIndex
	 */
	protected void calculate() {
		calculateCasesOnTreatment();
		calculateNewCases();

		// update stock quantity
		for (ForecastingMedicine med: forecasting.getMedicines()) {
			int stockOnHand = med.getStockOnHand();
			int totalConsumption = 0;

			for (ForecastingResult res: med.getResults()) {
				int qtdOrder = quantityOnOrder(res);
				stockOnHand += qtdOrder;
				
				res.setStockOnHand(stockOnHand);
				int cons = Math.round(res.getQuantityCasesOnTreatment() + res.getQuantityNewCases());
				stockOnHand -= cons;

				int qtdToExpire = quantityToExpire(res);
				totalConsumption += cons;
				
				int qtdExpired = 0;
				if (totalConsumption < qtdToExpire) {
					qtdExpired = qtdToExpire - totalConsumption;
					stockOnHand -= qtdExpired;
					totalConsumption = 0;
				}
				res.setQuantityToExpire(qtdToExpire);
				res.setQuantityExpired(qtdExpired);
				res.setQuantityOnOrder(qtdOrder);

				if (stockOnHand < 0)
					stockOnHand = 0;

				ForecastingMedicine fmed = forecasting.findMedicineById(res.getMedicine().getId());
				if (fmed != null) {
					fmed.setStockOnOrder( fmed.getStockOnOrder() + qtdOrder );
					fmed.setQuantityExpired( fmed.getQuantityExpired() + qtdExpired);
				}
			}
		}
	}

	
	
	/**
	 * Calculate the quantity to expire for the medicine in the month index
	 * @param med
	 * @param monthIndex
	 * @return
	 */
	protected int quantityToExpire(ForecastingResult result) {
		int res = 0;
		Medicine med = result.getMedicine();
		
		for (ForecastingBatch b: result.getBatchesToExpire())
			if ((b.getForecastingMedicine().getMedicine().equals(med)) && (forecasting.getMonthIndex(b.getExpiryDate()) == result.getMonthIndex()))
				res += b.getQuantity();
	
		return res;
	}
	
	
	/**
	 * Quantity of medicine on order to be received in the month. Medicine and month
	 * are indicated in the {@link ForecastingResult} result parameter
	 * @param result
	 * @return
	 */
	protected int quantityOnOrder(ForecastingResult result) {
		int qtd = 0;
		for (ForecastingMedicine med: forecasting.getMedicines()) {
			for (ForecastingOrder order: med.getOrders()) {
				if ((result.getMedicine().equals(med.getMedicine())) && (forecasting.getMonthIndex(order.getArrivalDate()) == result.getMonthIndex()))
					qtd += order.getQuantity();
			}
		}
		return qtd;
	}



	/**
	 * Calculate the quantity necessary for the cases under treatment
	 */
	protected void calculateCasesOnTreatment() {
		if (forecasting.isCasesFromDatabase())
			 calculateCasesOnTreatmentFromDB();
		else calculateCasesOnTreatmentInForecasting();
	}
	

	/**
	 * Calculate the forecasting for new cases
	 */
	protected void calculateNewCases() {
		for (ForecastingNewCases c: forecasting.getNewCases()) {
			for (ForecastingRegimen reg: forecasting.getRegimens()) {
				calculateNewCasesRegimen(c, reg);
			}
		}
	}


	
	/**
	 * Calculate the consumption of medicine for each one of the cases registered in the database
	 */
	private void calculateCasesOnTreatmentFromDB() {
		for (int i = 0; i <= numMonths; i++) {
			Date dtIni = forecasting.getIniDateMonthIndex(i);
			Date dtEnd = forecasting.getEndDateMonthIndex(i);

			if (dtIni == null)
				break;
			
			if ((i == numMonths) || (dtEnd == null))
				dtEnd = endForecasting;
			
			Integer prevCaseId = null;
			Integer prevMedId = null;
			
			for (Object[] prescDrug: casesOnTreatment) {
				// calculate consumption for a prescribed medicine
				int qtd = calcConsumptionCaseOnTreatment(prescDrug, new Period(dtIni, dtEnd));
				Integer caseId = (Integer)prescDrug[5];
				Integer medId = (Integer)prescDrug[3];

				if (qtd > 0) {
					// update results
					ForecastingMedicine fm = forecasting.findMedicineById((Integer)prescDrug[3]);
					ForecastingResult res = forecasting.findResult(fm.getMedicine(), i);
					if (res == null)
						RaiseResultException(fm.getMedicine(), i);
					
					int numcases = ((caseId.equals(prevCaseId) && (medId.equals(prevMedId)))? 0: 1);
					res.increaseCasesOnTreatment(numcases, qtd);

					prevCaseId = caseId;
					prevMedId = medId;
					
					// update consumption up to end of lead time
					// during lead time ?
					if (i < monthIndexIniDate) 
						fm.setDispensingLeadTime(fm.getDispensingLeadTime() + qtd);
					else
					if (i == monthIndexIniDate) { // is last month of lead time ?
						int qtd2 = calcConsumptionCaseOnTreatment(prescDrug, new Period(dtIni, forecasting.getIniDate()));
						fm.setDispensingLeadTime(fm.getDispensingLeadTime() + qtd2);
						fm.setEstimatedQtyCases(qtd - qtd2);
					}
					else fm.setEstimatedQtyCases(fm.getEstimatedQtyCases() + qtd);
					
					// update batches consumption
					List<ForecastingBatch> lst = getBatchesMonth(fm, i);
					Date ini = dtIni;
					for (ForecastingBatch bt: lst) {
						Date end = bt.getExpiryDate();
						int qtdBatch = calcConsumptionCaseOnTreatment(prescDrug, new Period(ini, end));
//						bt.setConsumptionInMonth(bt.getConsumptionInMonth() + qtdBatch);
						bt.setQuantityToExpire(bt.getQuantityToExpire() + qtdBatch);
					}
				}
			}
		}
	}

	
	/**
	 * Calculate the quantity of medicine necessary for a specific case in a period
	 * @param caseInfo record from the array casesOnTreatment
	 * @param dtIni
	 * @param dtEnd
	 * @return quantity of the medicine
	 */
	private int calcConsumptionCaseOnTreatment(Object[] caseInfo, Period period) {
		pm.getPeriod().setIniDate((Date)caseInfo[0]);
		pm.getPeriod().setEndDate((Date)caseInfo[1]);

		if (!pm.getPeriod().isIntersected(period))
			return 0;

		pm.setDoseUnit((Integer)caseInfo[2]);
		pm.setFrequency((Integer)caseInfo[4]);
		Integer medId = (Integer)caseInfo[3];
		ForecastingMedicine fm = forecasting.findMedicineById(medId);
		pm.setMedicine(fm.getMedicine());

		Period p = new Period(period);
		p.intersect(pm.getPeriod());
		
		return pm.calcEstimatedDispensing(p);
	}

	
	/**
	 * Calculate the consumption based on the number of cases on treatment entered by the user in the forecasting tool
	 * @param monthIndex
	 */
	private void calculateCasesOnTreatmentInForecasting() {
		for (int monthIndex = 0; monthIndex < numMonths; monthIndex++) {
			for (ForecastingCasesOnTreat c: forecasting.getCasesOnTreatment()) {
				Regimen reg = c.getRegimen();
				int numMonths = reg.getMonthsPhase(RegimenPhase.INTENSIVE) + reg.getMonthsPhase(RegimenPhase.CONTINUOUS);

				// get month of treatment in the regimen starting in 0
				int monthTreat = monthIndex - c.getMonthIndex();

				// is month of treatment valid for this regimen ?
				if (monthTreat < numMonths) {
					for (ForecastingMedicine fm: forecasting.getMedicines()) {
						calculateConsumptionCasesInForecasting(monthIndex, monthTreat, c, fm);
					}
				}
			}
		}
	}
	
	
	/**
	 * Calculate the consumption for cases defined in the forecasting.
	 * It was broke from the original method just to make it simpler
	 */
	private void calculateConsumptionCasesInForecasting(int monthIndex, int monthTreat, ForecastingCasesOnTreat fcot, ForecastingMedicine fm) {
		Regimen reg = fcot.getRegimen();
		if (!reg.isMedicineInRegimen(fm.getMedicine()))
			return;
		ForecastingResult res = forecasting.findResult(fm.getMedicine(), monthIndex);
		if (res == null)
			RaiseResultException(fm.getMedicine(), monthIndex);

		Date dtIni = forecasting.getIniDateMonthIndex(monthIndex);
		Date dtEnd = forecasting.getEndDateMonthIndex(monthIndex);
		
		// get consumption of the month
		int qty = calcQuantityRegimen(reg, dtIni, dtEnd, monthTreat, fm.getMedicine()) * fcot.getNumCases();
			
		if (qty == 0) 
			return;
		
		res.increaseCasesOnTreatment(fcot.getNumCases(), qty);
		fm.setEstimatedQtyCases(fm.getEstimatedQtyCases() + qty);

		// update dispensing in lead time month
		int qtdLeadTime = 0;
		
		// is in lead time ?
		if (monthIndex < monthIndexIniDate) {
			qtdLeadTime = qty;
		}
		else
		if (monthIndex == monthIndexIniDate) {
			int days = DateUtils.daysBetween(dtIni, forecasting.getIniDate());
			if (days > 0) {
				int daysMonth = DateUtils.daysBetween(dtIni, dtEnd);
				qtdLeadTime = Math.round(daysMonth * qty / days);
			}
		}
		
		if (qtdLeadTime > 0)
			fm.setDispensingLeadTime(fm.getDispensingLeadTime() + qtdLeadTime);
		
		// update batches consumption
		List<ForecastingBatch> lst = res.getBatchesToExpire();
		for (ForecastingBatch bt: lst) {
			int qtdBatch = calcQuantityRegimen(reg, dtIni, bt.getExpiryDate(), monthTreat, fm.getMedicine());
			bt.setConsumptionInMonth(bt.getConsumptionInMonth() + qtdBatch);
		}		
	}



	/**
	 * Calculates the quantity dispensed by regimen
	 * @param forRegimen
	 * @param dtini
	 * @param dtfim
	 */
	protected void calculateNewCasesRegimen(ForecastingNewCases forNewCases, ForecastingRegimen forRegimen) {
		int monthIndex = forNewCases.getMonthIndex();
		
		Regimen reg = forRegimen.getRegimen();
		int len = reg.getMonthsPhase(RegimenPhase.INTENSIVE) + reg.getMonthsPhase(RegimenPhase.CONTINUOUS);
		
		// calculate total percentage of regimens
		float totalPerc = 0;
		for (ForecastingRegimen aux: forecasting.getRegimens())
			totalPerc += aux.getPercNewCases();

		if (totalPerc == 0)
			return;
		
		float newCases = forNewCases.getNumNewCases() * forRegimen.getPercNewCases() / totalPerc;

		// calculate number of new cases and quantity for each medicine
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			if (reg.isMedicineInRegimen(fm.getMedicine())) {
				for (int i = 0; i < len; i++) {
					Date dtIni = forecasting.getIniDateMonthIndex(monthIndex + i);
					if (dtIni == null)
						break;
					
					Date dtEnd = forecasting.getEndDateMonthIndex(monthIndex + i);
					if (dtEnd == null)
						dtEnd = endForecasting;
					float qty = calcQuantityRegimen(reg, dtIni, dtEnd, i, fm.getMedicine());
					qty = qty * newCases;
					
					ForecastingResult res = forecasting.findResult(fm.getMedicine(), monthIndex + i);
					if (res != null) {
						// if it's the first month of treatment, increase number of new cases
						res.increaseNewCases(newCases, qty);
						fm.setEstimatedQtyNewCases(fm.getEstimatedQtyNewCases() + Math.round(qty));
					}
				}
			}
		}
	}


	/**
	 * Calculate the estimated quantity based on the initial month (0 is the first) and the medicine
	 * @param month
	 * @param medicine
	 * @return
	 */
	private int calcQuantityRegimen(Regimen reg, Date dtIni, Date dtEnd, int monthOfTreatment, Medicine medicine) {
		int intPhaseMonths = reg.getMonthsPhase(RegimenPhase.INTENSIVE);
		RegimenPhase phase = (monthOfTreatment < intPhaseMonths? RegimenPhase.INTENSIVE: RegimenPhase.CONTINUOUS);

		MedicineRegimen medReg = null;
		for (MedicineRegimen aux: reg.getMedicines()) {
			if ((aux.getMedicine().equals(medicine)) && (aux.getPhase().equals(phase))) {
				medReg = aux;
				break;
			}
		}
		
		if (medReg == null)
			return 0;
		
		WeeklyFrequency wf = forecastingHome.getWorkspace().getWeeklyFrequency(medReg.getDefaultFrequency());
		int numDays = wf.calcNumDays(new Period(dtIni, dtEnd));
		return numDays * medReg.getDefaultDoseUnit();
	}

	
	/**
	 * Return batches of a medicine in a specific month of treatment
	 * @param fm
	 * @param monthIndex
	 * @return
	 */
	private List<ForecastingBatch> getBatchesMonth(ForecastingMedicine fm, int monthIndex) {
		List<ForecastingBatch> lst = new ArrayList<ForecastingBatch>();
		for (ForecastingBatch b: fm.getBatchesToExpire()) {
			if (forecasting.getMonthIndex(b.getExpiryDate()) == monthIndex) {
				lst.add(b);
			}
		}
		return lst;
	}
	
	/**
	 * Raise exception if result is not found for an specific medicine and monthIndex (it's always suposed to be found)
	 * @param med
	 * @param monthIndex
	 */
	private void RaiseResultException(Medicine med, int monthIndex) {
		throw new RuntimeException("Forecasting result not found for med=" + med.toString() + " monthIndex=" + monthIndex);
	}

	public int getNumMonths() {
		return numMonths;
	}


	public Date getEndForecasting() {
		return endForecasting;
	}
}
