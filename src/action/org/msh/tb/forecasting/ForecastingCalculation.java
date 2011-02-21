package org.msh.tb.forecasting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.msh.mdrtb.entities.enums.UserView;
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

	
	private Period leadTimePeriod;
	
	private Period reviewPeriod;
	
	
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
		forecasting.initialize();

		Date dt = forecasting.getIniDateLeadTime();
		
		leadTimePeriod = new Period(forecasting.getReferenceDate(), DateUtils.incDays(dt, -1));
		reviewPeriod = new Period(dt, endForecasting);
		
		monthIndexIniDate = forecasting.getMonthIndex(forecasting.getIniDateLeadTime());

		// cases are from the database ?
		// if so, load list of prescribed medicines in the period
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

			// set restriction by medicine line
			if (MedicineLine.FIRST_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.FIRST_LINE.ordinal();
			else
			if (MedicineLine.SECOND_LINE.equals(forecasting.getMedicineLine()))
				hql += " and pm.medicine.line = " + MedicineLine.SECOND_LINE.ordinal();

			// set restriction by view
			if ((forecasting.getView() == UserView.TBUNIT) && (forecastingHome.getTbunitSelection().getTbunit() != null)) 
				hql += "and exists(from TreatmentHealthUnit hu where hu.tbcase.id = pm.tbcase.id and hu.period.endDate = pm.tbcase.treatmentPeriod.endDate " +
					"and hu.tbunit.id = " + forecastingHome.getTbunitSelection().getTbunit().getId().toString() +")";
			else
			if ((forecasting.getView() == UserView.ADMINUNIT) && (forecastingHome.getAdminUnitSelection().getSelectedUnit() != null))
				hql += "and exists(from TreatmentHealthUnit hu where hu.tbcase.id = pm.tbcase.id and hu.period.endDate = pm.tbcase.treatmentPeriod.endDate " +
					"and hu.tbunit.adminUnit.code like '" + forecastingHome.getAdminUnitSelection().getSelectedUnit().getCode() + "%')";

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
		calculateStockOnOrder();

		calculateCasesOnTreatment();
		calculateNewCases();

		updateStockQuantities();
	}


	/**
	 * Calculate the quantity of medicine expired for each month of the medicine
	 * @param forMedicine
	 */
	protected void updateStockQuantities() {
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			int stockOnHand = fm.getStockOnHand();
			System.out.println(fm.getMedicine().toString() + " = " + fm.getStockOnHand());

			for (ForecastingResult res: fm.getResults()) {
				res.setStockOnHand(stockOnHand);
				
				// update batches to expire
				for (ForecastingBatch b: fm.getBatchesToExpire())
					if (forecasting.getMonthIndex(b.getExpiryDate()) == res.getMonthIndex())
						res.setQuantityToExpire(res.getQuantityToExpire() + b.getQuantity());
				
				// get consumption in the month
				int consumptionMonth = res.getConsumptionCases() + res.getConsumptionNewCases();
				if (consumptionMonth > res.getStockOnHand())
					consumptionMonth = res.getStockOnHand();

				int qtdExpired = calcExpiredQuantity(fm, res, consumptionMonth);
				stockOnHand -= (consumptionMonth + qtdExpired); 
			}
		}
	}
	
	
	/**
	 * Calculates quantity expired of a specific medicine for a specific month
	 * based on the quantity available for each batch
	 * @param fm
	 * @param res
	 * @param consumptionMonth
	 * @return
	 */
	protected int calcExpiredQuantity(ForecastingMedicine fm, ForecastingResult res, int consumptionMonth) {
		Date dtini = forecasting.getIniDateMonthIndex(res.getMonthIndex());
		Date dtend = forecasting.getEndDateMonthIndex(res.getMonthIndex());

		int qtdExpired = 0;
		// consumption to be distributed by batches
		int conBatch = consumptionMonth;
		
		// update batch consumption
		while (true) {
			ForecastingBatch b = fm.findAvailableBatch(dtini);
			
			if (b == null)
				break;
			
			boolean expiresThisMonth = !b.getExpiryDate().after(dtend);
			
			// quantity available of the current batch
			int quantityAvailable = b.getQuantityAvailable();
			
			// batch expires this month ?
			if (expiresThisMonth) {
				int qtdBatchExp = 0;
				// available is bigger than consumption
				if (quantityAvailable > b.getConsumptionInMonth()) {
					qtdBatchExp = quantityAvailable - b.getConsumptionInMonth();
					if (b.getExpiryDate().before(forecasting.getIniDateLeadTime()))
						 fm.addQuantityExpiredLT(qtdBatchExp);
					else fm.addQuantityExpired(qtdBatchExp);
				}

				b.setQuantityAvailable(0);
				b.setQuantityExpired(qtdBatchExp);

				conBatch -= quantityAvailable;
				qtdExpired += qtdBatchExp;
			}
			else {
				if (quantityAvailable > conBatch) {
					b.setQuantityAvailable( quantityAvailable - conBatch );
					conBatch = 0;
				}
				else {
					b.setQuantityAvailable(0);
					conBatch -= quantityAvailable;
				}
			}

			// calculate quantity consumed of the batch
			int con = quantityAvailable - b.getQuantityAvailable() - b.getQuantityExpired();
			if (con > 0)
				res.addBatchConsumption(b, con);

			if (conBatch == 0)
				break;
		}
		
		res.setQuantityExpired(qtdExpired);
		
		return qtdExpired;
	}
		
/*		// accumulated consumption
		int c = 0;
		int total = 0;

		for (ForecastingResult res: forMedicine.getResults()) {
			// lost in the month
			int pm = 0;
			int qtdToExpire = 0;
			
			// quanto foi o consumo até o último lote do mês
			int cbatch = 0;

			for (ForecastingBatch batch: res.getBatchesToExpire()) {
				qtdToExpire += batch.getQuantity();
				c += batch.getConsumptionInMonth();
				if (c < batch.getQuantity()) {
					pm += batch.getQuantity() - c;
					c = 0;
				}
				else c -= batch.getQuantity();
				cbatch += batch.getConsumptionInMonth();
			}

			res.setQuantityExpired(pm);
			res.setQuantityToExpire(qtdToExpire);
			total += pm;
			c += res.getQuantityCasesOnTreatment() + res.getQuantityNewCases() - cbatch;
		}
		
		forMedicine.setQuantityExpired( total );
	}
*/	
	
	/**
	 * Calculate the quantity to expire of the medicine in the month index (the quantity of all batches in the month are summed to get the total quantity)
	 * @param med
	 * @param monthIndex
	 * @return
	 */
/*	protected int quantityToExpire(ForecastingResult result) {
		int res = 0;
		Medicine med = result.getMedicine();
		
		for (ForecastingBatch b: result.getBatchesToExpire())
			if ((b.getForecastingMedicine().getMedicine().equals(med)) && (forecasting.getMonthIndex(b.getExpiryDate()) == result.getMonthIndex()))
				res += b.getQuantity();
	
		return res;
	}
*/
	
	/**
	 * Calculate stock on order for all medicines in every month of forecasting
	 */
	protected void calculateStockOnOrder() {
		Date dt = forecasting.getIniDateLeadTime();
		
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			for (ForecastingOrder fo: fm.getOrders()) {
				int qtd = fo.getQuantity();
				
				int index = forecasting.getMonthIndex(fo.getArrivalDate());
				ForecastingResult res = fm.findResultByMonthIndex(index);
				
				if (res != null)
					res.addStockOnOrder(qtd);

				// is during lead time ?
				if ((fo.getArrivalDate().before(dt)) && (!fo.getArrivalDate().before(forecasting.getReferenceDate()))) {
					fm.addStockOnOrderLT(qtd);
				}
				else {
					// add order if is during review period
					if ((!fo.getArrivalDate().before(dt)) && (fo.getArrivalDate().before(endForecasting)))
						fm.addStockOnOrder(qtd);
				}
			}
		}
	}
	
	
	/**
	 * Quantity of medicine on order to be received in the month. Medicine and month
	 * are indicated in the {@link ForecastingResult} result parameter
	 * @param result
	 * @return
	 */
	protected int quantityOnOrder(ForecastingResult result, boolean leadTimeOnly) {
		int qtd = 0;
		Date dt = (leadTimeOnly? forecasting.getIniDateLeadTime(): null);

		for (ForecastingMedicine med: forecasting.getMedicines()) {
			for (ForecastingOrder order: med.getOrders()) {
				if ((result.getMedicine().equals(med.getMedicine())) && (forecasting.getMonthIndex(order.getArrivalDate()) == result.getMonthIndex())) {
					if ((dt == null) || ((dt != null) && (order.getArrivalDate().before(dt))))
					qtd += order.getQuantity();
				}
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
				// calculate consumption for a prescribed medicine of a specific case
				int qtd = calcConsumptionCaseOnTreatment(prescDrug, new Period(dtIni, dtEnd));
				Integer caseId = (Integer)prescDrug[5];
				Integer medId = (Integer)prescDrug[3];
				
				// there is consumption for this prescription in the month ?
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
						fm.addConsumptionLT( qtd );
					else
					if (i == monthIndexIniDate) { // is last month of lead time ?
						int qtd2 = calcConsumptionCaseOnTreatment(prescDrug, new Period(dtIni, DateUtils.incDays( forecasting.getIniDateLeadTime(), -1)));

						fm.addConsumptionLT( qtd2 );
						fm.addConsumptionCases(qtd - qtd2);
					}
					else fm.addConsumptionCases( qtd);
					
					// update batches consumption in the month they expire
					List<ForecastingBatch> lst = getBatchesMonth(fm, i);
					Date ini = dtIni;
					for (ForecastingBatch bt: lst) {
						Date end = bt.getExpiryDate();

						int qtdBatch = calcConsumptionCaseOnTreatment(prescDrug, new Period(ini, end));
						bt.setConsumptionInMonth(qtdBatch + bt.getConsumptionInMonth());
						ini = DateUtils.incDays(end, 1);
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
		fm.setConsumptionCases(fm.getConsumptionCases() + qty);

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
			fm.setConsumptionLT(fm.getConsumptionLT() + qtdLeadTime);
		
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
		if (newCases == 0)
			return;

		// calculate number of new cases and quantity for each medicine
		for (ForecastingMedicine fm: forecasting.getMedicines()) {
			if (reg.isMedicineInRegimen(fm.getMedicine())) {
				for (int i = 0; i < len; i++) {
					int index = monthIndex + i;
					
					Date dtIni = forecasting.getIniDateMonthIndex(index);
					Date dtEnd = forecasting.getEndDateMonthIndex(index);
					
					if (dtIni == null)
						break;
					if (dtEnd == null)
						dtEnd = DateUtils.incMonths(forecasting.getEndDate(), forecasting.getBufferStock());
					
					float qty = calcQuantityRegimen(reg, dtIni, dtEnd, i, fm.getMedicine());
					qty = qty * newCases;
					
					ForecastingResult res = forecasting.findResult(fm.getMedicine(), index);
					if (res != null) {
						// if it's the first month of treatment, increase number of new cases
						res.increaseNewCases(newCases, qty);
					}
					
					int q = Math.round(qty);

//					fm.setEstimatedQtyNewCases(fm.getEstimatedQtyNewCases() + q);
					// update consumption up to end of lead time
					// during lead time ?
					if (index < monthIndexIniDate) 
						fm.setConsumptionLT(fm.getConsumptionLT() + q);
					else
					if (index == monthIndexIniDate) { // is last month of lead time ?
						int qtd2 = Math.round( calcQuantityRegimen(reg, dtIni, DateUtils.incDays( forecasting.getIniDateLeadTime(), -1), index, fm.getMedicine()) );
						fm.setConsumptionLT(fm.getConsumptionLT() + qtd2);
						fm.setConsumptionNewCases(q - qtd2);
					}
					else fm.setConsumptionNewCases(fm.getConsumptionNewCases() + q);
					
					// update batches consumption of new cases
					List<ForecastingBatch> lst = getBatchesMonth(fm, index);
					Date ini = dtIni;
					for (ForecastingBatch bt: lst) {
						Date end = bt.getExpiryDate();

						int qtdBatch = calcQuantityRegimen(reg, ini, end, index, fm.getMedicine());
						bt.setConsumptionInMonth(qtdBatch + bt.getConsumptionInMonth());
						ini = DateUtils.incDays(end, 1);
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
		
		Collections.sort(lst, new Comparator<ForecastingBatch>() {
			public int compare(ForecastingBatch o1, ForecastingBatch o2) {
				return o1.getExpiryDate().compareTo(o2.getExpiryDate());
			}
			
		});
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


	/**
	 * @return the leadTimePeriod
	 */
	public Period getLeadTimePeriod() {
		return leadTimePeriod;
	}


	/**
	 * @return the reviewPeriod
	 */
	public Period getReviewPeriod() {
		return reviewPeriod;
	}
	
}
