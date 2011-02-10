package org.msh.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.Messages;
import org.msh.utils.date.DateUtils;
import org.msh.utils.date.Period;


@Name("periodConverter")
@org.jboss.seam.annotations.faces.Converter(id="periodConverter")
@BypassInterceptors
public class PeriodConverter implements Converter{

	private Period period;
	
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		return null;
	}
	
	public String getAsString(FacesContext facesContext, UIComponent comp, Object obj) {

		UIParameter p = findParam(comp, "type");
	
		period = getPeriod(comp, obj);
		
		if ((p == null) || (p.getValue().equals("period")))
			 return getAsPeriod(comp, obj);
		else if (p.getValue().equals("length"))
			return getAsLength(comp, obj);
		else if (p.getValue().equals("time-length"))
			return getAsTimeLength(comp, obj);
		else return null;
	}

	
	/**
	 * Convert the period to a format "initial date...end date (number of days)"
	 * @param comp
	 * @param obj
	 * @return
	 */
	protected String getAsPeriod(UIComponent comp, Object obj) {
		long days = period.getDays() + 1;
		
		Locale locale = LocaleSelector.instance().getLocale();

		Map<String, String> messages = Messages.instance();

		String dayTxt;
		if (days == 0)
			dayTxt = "";
		else
		if (days == 1)
			dayTxt = " (" + days + " " + messages.get("global.day") + ")";
		else dayTxt = " (" + days + " " + messages.get("global.days") + ")";
		
		DateFormat dtf = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);		
		String s = dtf.format(period.getIniDate()) + "..." + dtf.format(period.getEndDate()) + dayTxt;
		
		return s;
	}


	protected String getAsTimeLength(UIComponent comp, Object obj) {
/*		if (obj == null)
			return null;
		
		Date dtIni = (Date)obj;
		
		UIParameter param = findParam(comp, "endDate");
		Date dtEnd;
		if (param == null)
			 dtEnd = new Date();
		else dtEnd = (Date)param.getValue();
*/

		if (period.getEndDate() == null)
			return "-";
		
		Map<String, String> messages = Messages.instance();

		String res = "";
		
		// calculate number of seconds between dates
		int num = DateUtils.secondsBetween(period.getIniDate(), period.getEndDate());
		
		// write seconds
		int val = num % 60;
		if (val != 0)
			res = Integer.toString(val) + messages.get("global.sec");

		// calculate number of minutes
		num = Math.round(num / 60);
		if (num == 0) {
			if (res.isEmpty())
				 return "-";
			else return res;
		}
		
		// write minutes
		val = num % 60;
		if (val != 0)
			res = Integer.toString(val) + messages.get("global.min") + " " + res;
		
		// calculate number of hours
		num = Math.round(num / 60);
		if (num == 0)
			return res;
		
		// write hours
		val = num % 24;
		if (val != 0)
			res = Integer.toString(val) + (val == 1? messages.get("global.hour"): messages.get("global.hours")) + " " + res;

		// calculate number of days
		num = Math.round(num / 24);
		if (num == 0)
			return res;
		
		// write days
		res = Integer.toString(num) + (num == 1? messages.get("global.day"): messages.get("global.days")) + " " + res;

		return res;
	}


	/**
	 * Converts to a string containing the length of two dates or in a number of days
	 * @param comp - the component containing the value
	 * @param obj - Can be a Date object indicating the beginning date or an Integer object indicating the number of days to be formated
	 * @return
	 */
	protected String getAsLength(UIComponent comp, Object obj) {
/*		if (obj == null)
			return null;

		Date dtIni = (Date)obj;
		Date dtEnd = (Date)findParam(comp, "endDate").getValue();
		
		if (dtEnd == null)
			dtEnd = new Date();
*/

		// make adjustment in the final date including 1 more day to count the exactly period of month or year
		
		Date dtIni = period.getIniDate();
		Date dtEnd = period.getEndDate();
		if ((dtIni == null) || (dtEnd == null))
			return "<null>";
		dtEnd = DateUtils.incDays(dtEnd, 1);
		
		Map<String, String> messages = Messages.instance();

		int years = DateUtils.yearsBetween(dtIni, dtEnd);

		String s = "";
		if (years > 0) {
			if (years == 1)
				 s = years + " " + messages.get("global.year");
			else s = years + " " + messages.get("global.years");
			dtIni = DateUtils.incYears(dtIni, years);
		}
		
		int months = DateUtils.monthsBetween(dtIni, dtEnd);
		if (months > 0) {
			if (!s.isEmpty())
				s = s + ", ";
			if (months == 1)
				 s = s + months + " " + messages.get("global.month");
			else s = s + months + " " + messages.get("global.months");
			dtIni = DateUtils.incMonths(dtIni, months);
		}

		int days = DateUtils.daysBetween(dtIni, dtEnd);
		
		if (days > 0) {
			if (!s.isEmpty())
				s = s + ", ";
			if (days == 1)
				 s = s + days + " " + messages.get("global.day");
			else s = s + days + " " + messages.get("global.days");
		}
	
		return (s.isEmpty()? "-": s);
	}

	

	/**
	 * Search for a {@link UIParameter} child of the comp parameter 
	 * @param comp Component that has the parameter as a child
	 * @param pname Name of the {@link UIParameter} component
	 * @return {@link UIParameter} instance
	 */
	public UIParameter findParam(UIComponent comp, String pname) {
		for (UIComponent c: comp.getChildren()) {
			if ((c instanceof UIParameter) && (((UIParameter)c).getName().equals(pname))) {
				return (UIParameter)c;
			}
		}
		return null;
	}


	/**
	 * Return the period indicated in the component
	 * @param comp
	 * @param obj
	 * @return
	 */
	protected Period getPeriod(UIComponent comp, Object obj) {
		if (obj == null)
			return null;

		Period p;
		
		if (obj instanceof Period)
			p = (Period)obj;
		else {
			Date dt = (Date)obj;

			p = new Period();
			p.setIniDate(dt);
	
			UIParameter paramDtEnd = findParam(comp, "endDate");
			if (paramDtEnd != null)
				p.setEndDate( (Date)paramDtEnd.getValue() );
			
			if (p.getEndDate() == null)
				p.setEndDate( new Date() );
		}
		
		return p;
	}
}
