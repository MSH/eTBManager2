package org.msh.tb.medicines.dispensing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.Source;
import org.msh.tb.SourceGroup;

/**
 * @author Ricardo
 *
 * Returns the consolidated month dispensing of a TB Unit 
 */
@Name("monthDispensingReport")
public class MonthDispensingReport {

	@In(create=true) EntityManager entityManager;
	private List<SourceDispensing> sources;
	private List<DispensingInfo> dispensings;
	

	public class SourceDispensing extends SourceGroup<MonthDispensingInfo> {
		protected MonthDispensingInfo findMedicine(Integer medId) {
			for (MonthDispensingInfo info: getItems()) {
				if (info.getMedicine().getId().equals(medId))
					return info;
			}
			return null;
		}
	};
	

	/**
	 * Return list of sources and its dispensing in a specific month
	 * @return
	 */
	public List<SourceDispensing> getSources() {
		if (sources == null)
			createReport();
		return sources;
	}


	/**
	 * Create report
	 */
	protected void createReport() {
		sources = new ArrayList<SourceDispensing>();
		
		DispensingView view = (DispensingView)Component.getInstance("dispensingView");
		String hql = "select it.medicine.id, it.medicine.genericName.name1, it.source.name.name1, it.source.id, " +
				"it.medicine.strength, it.medicine.strengthUnit, disp.endDate, disp.id, " +
				"sum(mov.quantity), sum(mov.unitPrice * mov.quantity), disp.iniDate " +
				"from MedicineDispensingItem it " +
				"join it.movement mov " +
				"join it.dispensing disp " +
				"where disp.tbunit.id = #{userSession.tbunit.id} " +
				"and month(disp.endDate) = " + Integer.toString(view.getMonth() + 1) +
				" and year(disp.endDate) = " + view.getYear() +
				" group by it.medicine.id, it.medicine.genericName.name1, it.source.name.name1, it.source.id, " +
				"it.source.name, it.medicine.strength, it.medicine.strengthUnit, disp.endDate, disp.id, disp.iniDate " +
				"order by it.source.name.name1, it.medicine.genericName.name1, disp.endDate";
		
		List<Object[]> lst = entityManager.createQuery(hql)
			.getResultList();
	
		dispensings = new ArrayList<DispensingInfo>();

		// search for dates
		int numDates = 0;
		for (Object[] vals: lst) {
			Date dt = (Date)vals[6];
			DispensingInfo info = findDispensingDate(dt);
			if (info == null) {
				info = new DispensingInfo();
				info.setDate(dt);
				info.setDispensingId((Integer)vals[7]);
				info.setIniDate( (Date)vals[10] );

				dispensings.add(info);
				numDates++;
			}
		}
		
		// mount report
		for (Object[] vals: lst) {
			SourceDispensing disp = findSource((Integer)vals[3]);
			if (disp == null) {
				disp = new SourceDispensing();
				Source source = new Source();
				source.setId((Integer)vals[3]);
				source.getName().setName1((String)vals[2]);
				disp.setSource(source);
				sources.add(disp);
			}
			
			Integer medId = (Integer)vals[0];
			MonthDispensingInfo info = disp.findMedicine(medId);
			if (info == null) {
				info = new MonthDispensingInfo();
				info.setNumberOfDates(numDates);
				disp.getItems().add(info);
				Medicine med = new Medicine();
				med.setId((Integer)vals[0]);
				med.getGenericName().setName1((String)vals[1]);
				med.setStrength((String)vals[4]);
				med.setStrengthUnit((String)vals[5]);
				info.setMedicine(med);
			}
			
			Date dt = (Date)vals[6];
			Long qtd = (Long)vals[8];
			int index = dispensings.indexOf(findDispensingDate(dt));
			info.getQuantities().set(index, qtd.intValue());
		}
	}

	
	/**
	 * Clear the report forcing it to be generated again
	 */
	public void refresh() {
		sources = null;
	}
	
	protected DispensingInfo findDispensingDate(Date dt) {
		for (DispensingInfo info: dispensings) {
			if (info.getDate().equals(dt))
				return info;
		}
		return null;
	}
	
	protected SourceDispensing findSource(Integer sourceId) {
		for (SourceDispensing disp: sources) {
			if (disp.getSource().getId().equals(sourceId)) {
				return disp;
			}
		}
		return null;
	}
	
	public class DispensingInfo {
		private Integer dispensingId;
		private Date date;
		private Date iniDate;
		/**
		 * @return the dispensingId
		 */
		public Integer getDispensingId() {
			return dispensingId;
		}
		/**
		 * @param dispensingId the dispensingId to set
		 */
		public void setDispensingId(Integer dispensingId) {
			this.dispensingId = dispensingId;
		}
		/**
		 * @return the date
		 */
		public Date getDate() {
			return date;
		}
		/**
		 * @param date the date to set
		 */
		public void setDate(Date date) {
			this.date = date;
		}
		/**
		 * @return the iniDate
		 */
		public Date getIniDate() {
			return iniDate;
		}
		/**
		 * @param iniDate the iniDate to set
		 */
		public void setIniDate(Date iniDate) {
			this.iniDate = iniDate;
		}
	}


	/**
	 * @return the dispensings
	 */
	public List<DispensingInfo> getDispensings() {
		return dispensings;
	}

}
