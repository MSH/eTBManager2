package org.msh.tb.vi;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.cases.dispensing.CaseDispensingHome;
import org.msh.tb.cases.dispensing.WeekInfo;
import org.msh.tb.vi.entities.TreatmentMonitoringVI;


/**
 * Handle registering dispensing days of a TB case for vietnam
 * @author Utkarsh Srivastava
 *
 */
@Name("caseDispensingHomeVI")
@Scope(ScopeType.CONVERSATION)
public class CaseDispensingHomeVI extends CaseDispensingHome {

	private int weight;
	protected CaseDispensingInfoVI caseDispensingInfoVI;
	

	
	/**
	 * Save dispensing for the month/year of the selected case
	 * @return "dispensing-saved" if it was successfully saved
	 */
	@Override
	@Transactional
	public String saveDispensing() {
		if (caseDispensingInfoVI == null)
			return "error";
		
		caseDispensingInfoVI.updateDispensingDays();

		TreatmentMonitoringVI treatMonitoring = (TreatmentMonitoringVI) caseDispensingInfoVI.getTreatmentMonitoring();
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
		if (caseDispensingInfoVI == null)
			createDispensingInfo();
	
		return (caseDispensingInfoVI == null? null: caseDispensingInfoVI.getWeeks());
	}


	
	/**
	 * Load dispensing data and create week structure
	 */
	@Override
	@Transactional
	protected void createDispensingInfo() {
		if ((month == null) || (year == null))
			return;
		
		List<TreatmentMonitoringVI> lst = entityManager.createQuery("from TreatmentMonitoringVI d join fetch d.tbcase c " +
				"join fetch c.patient p " +
				"where c.id = #{caseHome.id} " +
				"and d.month = " + (month + 1) + " and d.year = " + year)
				.getResultList();

		if (lst.size() > 0)
			caseDispensingInfoVI = new CaseDispensingInfoVI(lst.get(0));
		else {
			TreatmentMonitoringVI treatMonitoring = new TreatmentMonitoringVI();
			treatMonitoring.setTbcase(caseHome.getInstance());
			treatMonitoring.setMonth(month + 1);
			treatMonitoring.setYear(year);
			
			caseDispensingInfoVI = new CaseDispensingInfoVI(treatMonitoring);
		}
	}


	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}









	
	

	
}
