package org.msh.tb;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.enums.RegimenPhase;
import org.msh.tb.log.LogInfo;
import org.msh.tb.medicines.MedicineSelection;
import org.msh.utils.EntityQuery;


@Name("regimenHome")
@LogInfo(roleName="REGIMENS")
public class RegimenHome extends EntityHomeEx<Regimen> {
	private static final long serialVersionUID = 6609373789452825016L;

	@In(create=true) MedicineSelection medicineSelection;

	private RegimenPhase phase;
	private List<SelectItem> months;
	
	@Factory("regimen")
	public Regimen getRegimen() {
		return getInstance();
	}


	@Override
	public EntityQuery<Regimen> getEntityQuery() {
		return (RegimensQuery)Component.getInstance("regimens", false);
	}


	/**
	 * Inclume medicines selected
	 * @return
	 */
	public String addMedicines() {
		if (phase == null)
			return "error";

		Regimen reg = getInstance();
		
		List<Medicine> lst =  medicineSelection.getSelectedMedicines();
		for (Medicine m: lst) {
			MedicineRegimen mr = new MedicineRegimen();
			mr.setPhase(phase);
			mr.setMedicine(m);
			
			reg.addMedicine(mr);
		}
		
		return "medadded";
	}


	
	/**
	 * avoid displaying medicines already selected 
	 */
	public void filterMedicines(RegimenPhase phase) {
		this.phase = phase;
		
		List<MedicineRegimen> lstMeds;
		if (phase == RegimenPhase.INTENSIVE)
			 lstMeds = getInstance().getIntensivePhaseMedicines();
		else lstMeds = getInstance().getContinuousPhaseMedicines();
		
		List<Medicine> lst = new ArrayList<Medicine>();
		for (MedicineRegimen mr: lstMeds)
			lst.add(mr.getMedicine());

		medicineSelection.applyFilter(lst);
	}

	public void removeMedicine(MedicineRegimen med) {
		getInstance().remMedicine(med);
	}
	
	public RegimenPhase getPhase() {
		return phase;
	}
	
	public List<SelectItem> getMonths() {
		if (months == null) {
			months = new ArrayList<SelectItem>();
			
			months.add(new SelectItem(null, "-"));
			for (int i = 1; i <= 24; i++) {
				SelectItem item = new SelectItem();
				item.setValue(i);
				item.setLabel(Integer.toString(i));
				months.add(item);
			}
		}
		return months;
	}
}
