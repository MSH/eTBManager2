/**
 * 
 */
package org.msh.tb.tbunits;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;
import org.msh.tb.application.App;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.entities.UserWorkspace;
import org.msh.utils.date.DateUtils;

/**
 * This is a set of services to support TB unit operations.
 * 
 * @author Ricardo Memoria
 *
 */
@Name("unitServices")
@AutoCreate
public class UnitServices {

	public enum StatisticItem {
		UNIT_CASES, TREATED_CASES;
	}
	
	/**
	 * Transfer all cases references from a unit to another unit
	 *  
	 * @param originUnit {@link Tbunit} instance representing the origin unit
	 * @param destUnit {@link Tbunit} instance representing the destination unit
	 */
	public void transferCases(Tbunit originUnit, Tbunit destUnit) {
		EntityManager em = App.getEntityManager();

		em.createNativeQuery("update tbcase set owner_unit_id = :destid where owner_unit_id = :oriid")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", originUnit.getId())
			.executeUpdate();

		em.createNativeQuery("update tbcase set notification_unit_id = :destid where notification_unit_id = :oriid")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", originUnit.getId())
			.executeUpdate();

		em.createNativeQuery("update treatmenthealthunit set unit_id = :destid where unit_id = :oriid")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", originUnit.getId())
			.executeUpdate();
		
		em.createNativeQuery("update transactionlog set unit_id = :destid where unit_id = :oriid " +
			"and role_id in (select id from userrole where code like '01%')")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", originUnit.getId())
			.executeUpdate();
		
		Events.instance().raiseEvent("unit.transfer.cases", originUnit, destUnit);
	}
	
	
	/**
	 * Transfer users from one unit to another
	 * 
	 * @param originUnit {@link Tbunit} instance representing the origin unit
	 * @param destUnit {@link Tbunit} instance representing the destination unit
	 */
	public void transferUsers(Tbunit originUnit, Tbunit destUnit) {
		EntityManager em = App.getEntityManager();

		em.createNativeQuery("update userworkspace set tbunit_id = :destid where tbunit_id = :oriid")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", originUnit.getId())
			.executeUpdate();
		
		em.createNativeQuery("update transactionlog set unit_id = :destid " +
			"where userlog_id in (select distinct user_id from userworkspace where unit_id = :oriid) ")
			.setParameter("destid", destUnit.getId())
			.setParameter("oriid", destUnit.getId())
			.executeUpdate();

		Events.instance().raiseEvent("unit.transfer.users", originUnit, destUnit);
	}
	
	/**
	 * Return the list of users of a TB unit
	 * @return list of {@link UserWorkspace} instance
	 */
	public List<UserWorkspace> getUsers(Tbunit unit) {
		EntityManager em = App.getEntityManager();
		return em.createQuery("from UserWorkspace uw join fetch uw.user join fetch uw.profile "
				+ "where uw.tbunit.id = :id order by uw.user.name")
				.setParameter("id", unit.getId())
				.getResultList();
	}
	
	
	/**
	 * Return a simple statistics about the references of this unit in the case module
	 * @param unit
	 * @return
	 */
	public Map<StatisticItem, Long> getCaseStatistics(Tbunit unit) {
		EntityManager em = App.getEntityManager();

		Map<StatisticItem, Long> res = new HashMap<StatisticItem, Long>();
		
		// number of cases notified or owned by this unit
		Number val = (Number)em.createNativeQuery("select count(*) from tbcase where notification_unit_id = :id or owner_unit_id = :id")
			.setParameter("id", unit.getId())
			.getSingleResult();
		res.put(StatisticItem.UNIT_CASES, val.longValue());
		
		// number of cases treated or on treatment
		val = (Number)em.createNativeQuery("select count(*) from treatmenthealthunit where unit_id = :id")
			.setParameter("id", unit.getId())
			.getSingleResult();
		res.put(StatisticItem.TREATED_CASES, val.longValue());
		
		return res;
	}
	
	/**
	 * Return a simple statistic of activities in the last three months of transaction log for this unit
	 * @param unit instance of {@link Tbunit} representing the unit
	 * @return map with the last 3 months, starting from 0 (the current month) until -3 (the previous months)
	 */
	public Map<Integer, Long> getTransactionLogStatistics(Tbunit unit) {
		EntityManager em = App.getEntityManager();
		
		String sql = "select month(transactiondate), year(transactiondate), count(*) " +
				"from transactionlog " +
				"where transactiondate > :dt " +
				"group by month(transactiondate), year(transactiondate) ";
		// return the previous month
		Date dt = DateUtils.incMonths(DateUtils.getDate(), -4);
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.set(Calendar.DAY_OF_MONTH, 1);
		
		List<Object[]> lst = em.createNativeQuery(sql)
			.setParameter("dt", c.getTime())
			.getResultList();
		
		// mount report
		int index = c.get(Calendar.MONTH) + (c.get(Calendar.YEAR) * 12);
		
		Map<Integer, Long> res = new HashMap<Integer, Long>();
		for (Object[] vals: lst) {
			int n = (((Integer)vals[0]) * 12) + (Integer)vals[1];
			n -= index;
			long num = ((Number)vals[2]).longValue();
			
			res.put(n, num);
		}
		
		return res;
	}
}
