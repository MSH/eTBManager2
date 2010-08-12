package org.msh.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.international.LocaleSelector;

@Name("timezones")
public class Timezones {

	@In(create=true) LocaleSelector localeSelector;
	private DecimalFormat df = new DecimalFormat("00");
	private List<SelectItem> selectItems;
	
	
	/**
	 * Verifica o formato da data para a localidade em uso
	 * @return DMY para formato dia/mes/ano, ou MDY para formato mes/dia/ano ou YMD para format ano/mes/dia
	 */
	public String getDateFormat() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2005);
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 3);
		DateFormat sp = DateFormat.getDateInstance(DateFormat.SHORT, localeSelector.getLocale());
		String s = sp.format(c.getTime());
	
		// verifica a posição de cada elemento da data na formatação
		int dp = s.indexOf("3");
		int mp = s.indexOf("1");
		int yp = s.indexOf("5");
		
		if ((dp < mp) && (mp < yp))
			return "DMY";
		else
		if ((dp > mp ) && (dp < yp))
			return "MDY";
		else
		if ((yp < mp) && (mp < dp))
			return "YMD";
		
		return null;
	}
	
	public char getDecimalSeparator() {
		DecimalFormatSymbols s = new DecimalFormatSymbols(localeSelector.getLocale());
		return s.getDecimalSeparator();
	}
	

	public TimeZone getDefault() {
		return TimeZone.getDefault();
	}
	
	public Date getDate() {
		return new Date();
	}
	
	public List<TimeZone> getList() {
		String[] tzs = TimeZone.getAvailableIDs();
		
		List<TimeZone> lst = new ArrayList<TimeZone>();
		for (String tz: tzs) {
			lst.add(TimeZone.getTimeZone(tz));
		}
		
		return lst;
	}
	
	public List<SelectItem> getSelectItems() {
		if (selectItems == null)
			createSelectItems();
		return selectItems;
	}

	
	/**
	 * Cria a lista de itens
	 */
	public void createSelectItems() {
		List<TimeZone> lst = getList();
		
		selectItems = new ArrayList<SelectItem>();
		for (TimeZone tm: lst) {
			String s = getGMTDisplay(tm) + ": " + tm.getID();
			if (!contains(s)) {
				SelectItem it = new SelectItem(tm.getID(), s);
				selectItems.add(it);
			}
		}		
	}
	
	/**
	 * Retorna o nome de exibição do GMT
	 * @param tm
	 * @return
	 */
	private String getGMTDisplay(TimeZone tm) {
		int rawOffset = tm.getRawOffset();
        int hour = rawOffset / (60*60*1000);
        int min = Math.abs(rawOffset / (60*1000)) % 60;

        if (hour == 0) 
        	 return "(GMT)";
        else return "(GMT" + df.format(hour) + ":" + df.format(min) + ")";
	}
	
	/**
	 * Verifica se a descrição já está na lista
	 * @param name
	 * @return
	 */
	private boolean contains(String name) {
		for (SelectItem si: selectItems) {
			if (si.getLabel().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
