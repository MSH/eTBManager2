package org.msh.tb.ua;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;
import org.msh.tb.entities.ForecastingCasesOnTreat;
import org.msh.tb.forecasting.CasesRegimenTable;
import org.msh.tb.forecasting.ForecastingHome;
import org.msh.tb.forecasting.ForecastingView;
import org.msh.utils.date.DateUtils;

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
	
	public CasesRegimenTable getCasesRegimenTable() {
		CasesRegimenTable casesRegimenTable = forecastingView.getCasesRegimenTable();
		CasesRegimenTable casesRegimenTable_new = new CasesRegimenTable(forecastingHome.getInstance());
		/*for (int i = 0; i < casesRegimenTable.getRows().size(); i++) {
			for (int j = 0; j < casesRegimenTable.getRows().get(i).getMonths().size(); j++) {
				casesRegimenTable.getRows().get(i).getMonths().get(j);
				//Date dt = DateUtils.incMonths(refDate, casesRegimenTable.getRows().get(i).getMonths().get(j).getMonthIndex());
				//if (dt.getMonth()==2 || dt.getMonth()==5 || dt.getMonth()==8 || dt.getMonth()==11){
					
				}
			}
		}*/
		return casesRegimenTable;
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
	
}
