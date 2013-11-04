package org.msh.tb.ua.utils;

import java.util.Date;

import org.msh.tb.entities.Batch;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.login.UserSession;
import org.msh.utils.date.DateUtils;
/**
 * The class for necessary functions in medicine unit
 * @author A.M.
 */
public class MedicineFunctions {
	
	/**
	 * Check if the given batch is in attention period for expiration.
	 * @param o - object of Batch or BatchQuantity. Else return null
	 */
	public static boolean isExpiringBatch(Object o){

		Batch b = null;
		BatchQuantity bq = null;
		if(o instanceof Batch)
			b = (Batch) o;
		else if (o instanceof BatchQuantity){
			bq = (BatchQuantity) o;
			b = bq.getBatch();
		}

		if(b != null)
			if(b.getExpiryDate()!=null){
				
				if(b.getExpiryDate().before(DateUtils.getDate()))
					return false;
				
				double diffInMonths = DateUtils.monthsBetween(b.getExpiryDate(), DateUtils.getDate());
				int monthsToAlert = (UserSession.getWorkspace().getMonthsToAlertExpiredMedicines() == null ? 0 : UserSession.getWorkspace().getMonthsToAlertExpiredMedicines());
				
				if(monthsToAlert == 0)
					return false;
				
				if(diffInMonths <= monthsToAlert)
					return true;				
			}

		return false;
	}
	
	/**
	 * Check if the registration card is in attention period (2 months) for expiration.
	 * @param o - object of Batch or BatchQuantity. Else return null
	 */
	public static boolean isExpiringRegistCard(Object o){

		Batch b = null;
		BatchQuantity bq = null;
		if(o instanceof Batch)
			b = (Batch) o;
		else if (o instanceof BatchQuantity){
			bq = (BatchQuantity) o;
			b = bq.getBatch();
		}

		if(b != null)
			if(b.getRegistCardEndDate()!=null){
				/*			Calendar now  = Calendar.getInstance();
			Calendar batchExpiringDate = Calendar.getInstance();
			batchExpiringDate.setTime(b.getExpiryDate());

			long diff = batchExpiringDate.getTimeInMillis() - now.getTimeInMillis();
			diff = diff / (24*60*60*1000);
			double diffInDouble = diff;
			double diffInMonths = diffInDouble / 30.0;
				 */
				// calculate the number of months between two dates
				int diffInMonths = DateUtils.monthsBetween(b.getRegistCardEndDate(), DateUtils.getDate());

				if (diffInMonths<2)
					return true;
			}

		return false;
	}

	/**
	 * Check if batch is expired relative to stated date
	 * @param b - batch object
	 * @param d - stated date 
	 * @return
	 */
	public static boolean isExpiredRelativeToDate(Batch b, Date d){
		if (b==null) return false;
		Date dd = d;
		if (dd == null) dd = DateUtils.getDate();
		return (b.getExpiryDate() != null) && (b.getExpiryDate().before(dd));
	}
	
}
