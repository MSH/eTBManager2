package org.msh.utils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
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
		String typeName = (p == null? null: p.getValue().toString());
	
		period = getPeriod(comp, obj);

		if ((typeName == null) || (typeName.equals("period")))
			 return getAsPeriod();
		else if (typeName.equals("length"))
			return getAsLength(comp, obj);
		else if (typeName.equals("time-length"))
			return getAsTimeLength(comp, obj);
		else if (typeName.equals("elapsed-time"))
			return getAsElapsedTime();
		else return null;
	}

	
	/**
	 * Convert the period to a format "initial date...end date (number of days)"
	 * @param comp
	 * @param obj
	 * @return
	 */
	protected String getAsPeriod() {
		long days = period.getDays() + 1;
		
		Map<String, String> messages = Messages.instance();

		String dayTxt;
		if (days == 0)
			dayTxt = "";
		else
		if (days == 1)
			 dayTxt = " (" + days + " " + messages.get("global.day") + ")";
		else dayTxt = " (" + days + " " + messages.get("global.days") + ")";

		SimpleDateFormat dtf = new SimpleDateFormat(Messages.instance().get("locale.outputDatePattern"));
		String s = dtf.format(period.getIniDate()) + "..." + dtf.format(period.getEndDate()) + dayTxt;
		
		return s;
	}


	protected String getAsTimeLength(UIComponent comp, Object obj) {
		Calendar c = DateUtils.calcDifference(period.getIniDate(), period.getEndDate());

		long len = c.getTimeInMillis();

		String s = "";

		Map<String, String> messages = Messages.instance();
		
		// is bigger than 24 hours ?
		if (len > 1000 * 60 * 60 * 24) {
			Date dt = period.getIniDate();
			
			int years = DateUtils.yearsBetween(dt, period.getEndDate());

			dt = DateUtils.incYears(dt, years);
			int months = DateUtils.monthsBetween(dt, period.getEndDate());
			
			dt = DateUtils.incMonths(dt, months);
			int days = DateUtils.daysBetween(dt, period.getEndDate());

			if (years > 0) {
				if (years == 1)
					 s = years + " " + messages.get("global.year");
				else s = years + " " + messages.get("global.years");
			}
			
			if (months > 0) {
				if (!s.isEmpty())
					s = s + ", ";
				if (months == 1)
					 s = s + months + " " + messages.get("global.month");
				else s = s + months + " " + messages.get("global.months");
			}

			if (days > 0) {
				if (!s.isEmpty())
					s = s + ", ";
				if (days == 1)
					 s = s + days + " " + messages.get("global.day");
				else s = s + days + " " + messages.get("global.days");
			}
		}
		else {
			int hours = (int)Math.floor((float)len / (1000F * 60F * 60F));
			if (hours > 0)
				len %= (hours * 1000 * 60 * 60);
			
			int min = (int)Math.floor((float)len / (1000F * 60F));
			if (min > 0)
				len %= (min * 1000 * 60);
			
			int sec = (int)Math.floor((float)len / 1000F);
			
			if (hours > 0) {
				if (hours > 1)
					 s = hours + " " + messages.get("global.hour");
				else s = hours + " " + messages.get("global.hours");
			}
			
			if (min > 0) {
				if (!s.isEmpty())
					s += " ";
				 s += min + " " + messages.get("global.min");
			}
			
			if (sec > 0) {
				if (!s.isEmpty())
					s += " ";
				s += sec + " " + messages.get("global.sec");
			}
		}
		return s;
	}


	/**
	 * Converts to a string containing the length of two dates or in a number of days
	 * @param comp - the component containing the value
	 * @param obj - Can be a Date object indicating the beginning date or an Integer object indicating the number of days to be formated
	 * @return
	 */
	protected String getAsLength(UIComponent comp, Object obj) {
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

	
	public String getAsElapsedTime() {
		String s = getAsTimeLength(null, null);

		Map<String, String> messages = Messages.instance();
/*		Calendar c = DateUtils.calcDifference(period.getIniDate(), period.getEndDate());

		long len = c.getTimeInMillis();

		String s = "";

		Map<String, String> messages = Messages.instance();
		
		// is bigger than 24 hours ?
		if (len > 1000 * 60 * 60 * 24) {
			Date dt = period.getIniDate();
			
			int years = DateUtils.yearsBetween(dt, period.getEndDate());

			dt = DateUtils.incYears(dt, years);
			int months = DateUtils.monthsBetween(dt, period.getEndDate());
			
			dt = DateUtils.incMonths(dt, months);
			int days = DateUtils.daysBetween(dt, period.getEndDate());

			if (years > 0) {
				if (years == 1)
					 s = years + " " + messages.get("global.year");
				else s = years + " " + messages.get("global.years");
			}
			
			if (months > 0) {
				if (!s.isEmpty())
					s = s + ", ";
				if (months == 1)
					 s = s + months + " " + messages.get("global.month");
				else s = s + months + " " + messages.get("global.months");
			}

			if (days > 0) {
				if (!s.isEmpty())
					s = s + ", ";
				if (days == 1)
					 s = s + days + " " + messages.get("global.day");
				else s = s + days + " " + messages.get("global.days");
			}
		}
		else {
			int hours = Math.round((float)len / (1000F * 60F * 60F));
			int min = Math.round((float)len / (1000F * 60F));
			int sec = Math.round((float)len / 1000F);
			
			if (hours > 0) {
				if (hours > 1)
					 s = hours + " " + messages.get("global.hour");
				else s = hours + " " + messages.get("global.hours");
			}
			
			if (min > 0) {
				if (min > 1)
					 s += min + " " + messages.get("global.min");
				else s += min + " " + messages.get("global.mins");
			}
			
			if (sec > 0) {
				if (sec > 1)
					 s += sec + " " + messages.get("global.sec");
				else s += sec + " " + messages.get("global.secs");
			}
		}
*/
		if (!s.isEmpty())
			s = MessageFormat.format(messages.get("global.timeago"), s);
		
		return s;
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
