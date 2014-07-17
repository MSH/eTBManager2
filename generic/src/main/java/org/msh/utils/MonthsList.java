package org.msh.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;

import javax.faces.model.SelectItem;
import java.text.DateFormatSymbols;
import java.util.*;

@Name("monthsUtils")
public class MonthsList {

	@In(create=true) Locale locale;
	@In(create=true) Map<String, String> messages;
	
	@Factory("months")
	public List<SelectItem> getResultList() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String[] values = symbols.getShortMonths();
		
		int numMes = 0;
		
		SelectItem si = new SelectItem();
		si.setLabel("-");
		lst.add(si);
		
		for (String val: values) {
			si = new SelectItem(); 
			String s = StringEscapeUtils.escapeHtml(val);
			si.setLabel(s);
			si.setValue(numMes);
			lst.add(si);

			numMes++;
			if (numMes >= 12)
				break;
		}
		
		return lst;
	}
	
	@Factory("monthsAll")
	public List<SelectItem> getMonthsAll() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String[] values = symbols.getShortMonths();
		
		int numMes = 0;
		
		SelectItem si = new SelectItem();
		si.setLabel(messages.get("form.noselection"));
		lst.add(si);
		
		for (String val: values) {
			si = new SelectItem(); 
			String s = StringEscapeUtils.escapeHtml(val);
			si.setLabel(s);
			si.setValue(numMes);
			lst.add(si);

			numMes++;
			if (numMes >= 12)
				break;
		}
		
		return lst;
	}


	@Factory("years")
	public List<SelectItem> getYears() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);
		
		SelectItem si = new SelectItem();
		si.setLabel("-");
		lst.add(si);
		
		for (int i = ano; i >= ano - 50; i--) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i));
			it.setValue(i);
			lst.add(it);
		}
		
		return lst;
	}
	
	@Factory("yearsAll")
	public List<SelectItem> getYearsAll() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);
		
		SelectItem si = new SelectItem();
		si.setLabel(messages.get("form.noselection"));
		lst.add(si);
		
		for (int i = ano; i >= ano - 50; i--) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i));
			it.setValue(i);
			lst.add(it);
		}
		
		return lst;
	}
	
	@Factory("futureYears")
	public List<SelectItem> getFutureYears() {
		List<SelectItem> lst = new ArrayList<SelectItem>();
		Calendar c = Calendar.getInstance();
		int ano = c.get(Calendar.YEAR);
		
		SelectItem si = new SelectItem();
		si.setLabel("-");
		lst.add(si);
		
		for (int i = ano; i <= ano + 10; i++) {
			SelectItem it = new SelectItem();
			it.setLabel(Integer.toString(i));
			it.setValue(i);
			lst.add(it);
		}
		
		return lst;
	}
	
	@Factory("shortWeekNames")
	public String[] getShortWeekNames() {
		DateFormatSymbols df = new DateFormatSymbols(LocaleSelector.instance().getLocale());
		return df.getShortWeekdays();
	}
}
