package org.msh.tb.kh;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.dispensing.CaseDispensingHome;
import org.msh.tb.cases.dispensing.WeekInfo;
import org.msh.tb.kh.entities.TreatmentMonitoringKH;

import java.util.List;


/**
 * Handle registering dispensing days of a TB case
 * @author Ricardo Memoria
 *
 */
@Name("caseDispensingHomeKH")
@Scope(ScopeType.CONVERSATION)
public class CaseDispensingHomeKH extends CaseDispensingHome {

	private int weight;
	protected CaseDispensingInfoKH caseDispensingInfoKH;
	

	
	/**
	 * Save dispensing for the month/year of the selected case
	 * @return "dispensing-saved" if it was successfully saved
	 */
	@Override
	@Transactional
	public String saveDispensing() {
		if (caseDispensingInfoKH == null)
			return "error";
		
		caseDispensingInfoKH.updateDispensingDays();

		TreatmentMonitoringKH treatMonitoring = (TreatmentMonitoringKH) caseDispensingInfoKH.getTreatmentMonitoring();
		treatMonitoring.setWeight(weight);
		entityManager.persist(treatMonitoring);
		entityManager.flush();

		return "dispensing-saved";
	}
	
	/**
	 * Return list of weeks
	 * @return
	 */
	@Override
	public List<WeekInfo> getWeeks() {
		if (caseDispensingInfoKH == null)
			createDispensingInfo();
	
		return (caseDispensingInfoKH == null? null: caseDispensingInfoKH.getWeeks());
	}


	
	/**
	 * Load dispensing data and create week structure
	 */
	@Override
	@Transactional
	protected void createDispensingInfo() {
		if ((month == null) || (year == null))
			return;
		
		List<TreatmentMonitoringKH> lst = entityManager.createQuery("from TreatmentMonitoringKH d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfoKH = new CaseDispensingInfoKH(lst.get(0));
		else {
			TreatmentMonitoringKH treatMonitoring = new TreatmentMonitoringKH();
			treatMonitoring.setTbcase(caseHome.getInstance());
			treatMonitoring.setMonth(month + 1);
			treatMonitoring.setYear(year);
			
			caseDispensingInfoKH = new CaseDispensingInfoKH(treatMonitoring);
		}
	}


	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}









	
	

	
}
