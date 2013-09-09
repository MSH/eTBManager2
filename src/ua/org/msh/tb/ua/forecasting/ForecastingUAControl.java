package org.msh.tb.ua.forecasting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.Forecasting;
import org.msh.tb.entities.ForecastingCasesOnTreat;
import org.msh.tb.entities.ForecastingMedicine;
import org.msh.tb.entities.ForecastingNewCases;
import org.msh.tb.entities.ForecastingPeriod;
import org.msh.tb.entities.ForecastingRegimen;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.forecasting.ForecastingCalculation;
import org.msh.tb.forecasting.ForecastingHome;
import org.msh.tb.forecasting.ForecastingView;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;

/**
 * Controller to provide UA specific for forecasting
 * Uses international forecasting components
 * 
 * @author alexey
 *
 */
@Name("forecastingUAControl")
public class ForecastingUAControl {
	@In(create=true) ForecastingView forecastingView;
	@In(create=true) ForecastingHome forecastingHome;
	@In(create=true) ForecastingCalculation forecastingCalculation;

	private Forecasting forecasting;
	private List<ForecastingPeriod> fplist;
	private List<Integer> ecAll; //estimatedConsuption for all regimens
	private Regimen regimen;
	private boolean allRegimens;
	
	public List<String> getMonths(){
		Locale locale = LocaleSelector.instance().getLocale();
		SimpleDateFormat df = new SimpleDateFormat("MMM-yyy", locale);

		Date refDate = forecastingView.getCasesRegimenTable().getForecasting().getReferenceDate();
		DateUtils.incMonths(refDate, -1);
		List<String> kvart = new ArrayList<String>();
		int numberOfMonths=forecastingView.getCasesRegimenTable().getNumberOfMonths();
		int monthIndexIni=forecastingView.getCasesRegimenTable().getMonthIndexIni();

		for (int i = 1; i <= numberOfMonths; i++) {
			int index = monthIndexIni - numberOfMonths + i;
			Date dt = DateUtils.incMonths(refDate, index);
			if (dt.getMonth()==2 || dt.getMonth()==5 || dt.getMonth()==8 || dt.getMonth()==11){
				String s = df.format(dt);
				kvart.add(s);
			}
		}
		return kvart;
	}

	public boolean kvartMonth(ForecastingCasesOnTreat mon){
		Date refDate = forecastingView.getCasesRegimenTable().getForecasting().getReferenceDate();
		DateUtils.incMonths(refDate, -1);

		Date dt = DateUtils.incMonths(refDate, mon.getMonthIndex());
		if (dt.getMonth()==2 || dt.getMonth()==5 || dt.getMonth()==8 || dt.getMonth()==11){
			return true;
		}
		return false;
	}

	/**
	 * Create periods to be calculated
	 */
	private void createPeriods() {
		getForecasting();
		fplist = new ArrayList<ForecastingPeriod>();
		ForecastingMedicine fm = forecastingView.getMedicine();
		for (int i = 0; i < fm.getPeriods().size(); i++) {
			ForecastingPeriod fp = new ForecastingPeriod();
			fp.setPeriod(fm.getPeriods().get(i).getPeriod());
			fplist.add(fp);
		}
		reCalcEstConsumptionCases();
		reCalcEstConsumptionNewCases();
	}

	private void reCalcEstConsumptionNewCases() {
		Regimen reg = regimen;
		ForecastingRegimen reg2=null;
		for (ForecastingRegimen fr: getForecasting().getRegimens()){
			if (fr.getRegimen().equals(regimen)){
				reg2 = fr;
				break;
			}
		}
		ForecastingMedicine fm = forecastingView.getMedicine();
		if (reg.isMedicineInRegimen(fm.getMedicine())) {
			for (ForecastingNewCases c: forecasting.getNewCases()) {
				// calculate total percentage of regimens
				float totalPerc = forecastingView.getControlTotal();
				if (totalPerc == 0)
					return;		

				float newCases = Math.round( c.getNumNewCases() * reg2.getPercNewCases() / totalPerc );
				if (newCases == 0)
					continue;

				Date dtini = forecasting.getIniDateMonthIndex(c.getMonthIndex());

				// calculate number of new cases and quantity for each medicine
				for (ForecastingPeriod forPer: fplist) {
					int qty = forecastingCalculation.calcEstimatedConsumptionRegimen(reg, fm.getMedicine(), dtini, forPer.getPeriod());

					if (qty > 0) {
						qty = Math.round( (float)qty * newCases );
						forPer.setEstConsumptionNewCases(forPer.getEstConsumptionNewCases() + qty);
					}
				}
			}
		}
	}

	private void reCalcEstConsumptionCases(){
		ForecastingMedicine fm = forecastingView.getMedicine();
		Regimen reg = regimen;
		for (int monthIndex = 0; monthIndex <= forecasting.getNumMonths(); monthIndex++) {
			for (ForecastingCasesOnTreat c: forecasting.getCasesOnTreatment()) 
				if (c.getNumCases()!=null)
				{
					if (reg.equals(c.getRegimen())){
						int numMonths = reg.getMonthsPhase(RegimenPhase.INTENSIVE) + reg.getMonthsPhase(RegimenPhase.CONTINUOUS);
						int monthTreat = monthIndex - c.getMonthIndex();
						if (monthTreat < numMonths) {
							if (!reg.isMedicineInRegimen(fm.getMedicine()))
								return;

							Date dtIni = forecasting.getIniDateMonthIndex(monthIndex);
							Date dtEnd = forecasting.getEndDateMonthIndex(monthIndex);

							// calculate the consumption of all periods in the month
							Period p = new Period(dtIni, dtEnd);
							for (ForecastingPeriod forper:fplist) {
								if (forper.getPeriod().isInside(p)) {
									int qtyperiod = forecastingCalculation.calcQuantityRegimen(reg, forper.getPeriod(), monthTreat, fm.getMedicine()) * c.getNumCases();
									forper.setEstConsumptionCases( forper.getEstConsumptionCases() + qtyperiod );
								}
							}
						}
					}
				}
		}
	}

	public List<ForecastingPeriod> getPeriods(){
		if (getRegimen()!=null)
			return getFmlist();
		if (forecastingView.getMedicine() != null){
			ecAll = new ArrayList<Integer>();
			for (ForecastingPeriod fp:forecastingView.getMedicine().getPeriods())
				ecAll.add(fp.getEstimatedConsumption());
			return forecastingView.getMedicine().getPeriods();
		}
		return null;
	}

	public List<ForecastingPeriod> getFmlist() {
		if (fplist == null){	
			createPeriods();
		}
		return fplist;
	}

	public Regimen getRegimen() {
		return regimen;
	}

	public void setRegimen(Regimen regimen) {
		if (regimen!=null)	
			allRegimens = false;
		else allRegimens = true;
		this.regimen = regimen;
	}

	public List<Regimen> getRegimenItems(){
		List<Regimen> lst = new ArrayList<Regimen>();
		if (forecastingView.getMedicine()!=null){
			allRegimens = true;
			for (ForecastingRegimen fr: getForecasting().getRegimens()){
				if (!lst.contains(fr.getRegimen()))
					if (fr.getRegimen().isMedicineInRegimen(forecastingView.getMedicine().getMedicine()))
						lst.add(fr.getRegimen());
			}
		}
		return lst;
	}

	public Forecasting getForecasting() {
		if (forecasting == null)
			forecasting = forecastingHome.getForecasting();
		return forecasting;
	}

	public List<Integer> getEcAll() {
		return ecAll;
	}

	public boolean isAllRegimens() {
		return allRegimens;
	}

	public void setAllRegimens(boolean allRegimens) {
		this.allRegimens = allRegimens;
	}

}
