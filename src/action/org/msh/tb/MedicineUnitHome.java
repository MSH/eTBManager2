package org.msh.tb;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.framework.Controller;
import org.msh.mdrtb.entities.Medicine;
import org.msh.mdrtb.entities.MedicineUnit;
import org.msh.mdrtb.entities.Source;
import org.msh.mdrtb.entities.Tbunit;
import org.msh.tb.login.UserSession;


@Name("medicineUnitHome")
@Scope(ScopeType.CONVERSATION)
public class MedicineUnitHome  extends Controller {
	private static final long serialVersionUID = -4844087043923195879L;

	public class SourceMedicineUnit extends SourceGroup<MedicineUnit> {};

	@In(create=true) EntityManager entityManager;
	@In(create=true) MedicinesQuery medicines;
	@In(create=true) UserSession userSession;

	private List<SourceMedicineUnit> sources;
	
	/**
	 * Returns list of medicine configuration of the unit segmented by source
	 * @return
	 */
	public List<SourceMedicineUnit> getSources() {
		if (sources == null)
			createMedicineList();
		return sources;
	}


	/**
	 * Save the minimum buffer stock for the unit and source 
	 * @return
	 */
	@Transactional
	public String persist() {
		if (sources == null)
			return "error";
		
		for (SourceMedicineUnit s: sources) {
			for (MedicineUnit mu: s.getItems())
				entityManager.persist(mu);
		}
		
		entityManager.flush();
		
		return "persisted";
	}
	
	private void createMedicineList() {
		sources = new ArrayList<SourceMedicineUnit>();
		
		Tbunit unit = entityManager.find(Tbunit.class, userSession.getTbunit().getId());
		
		List<MedicineUnit> items = entityManager.createQuery("from MedicineUnit mu " +
				"join fetch mu.source s join fetch mu.tbunit " +
				"where mu.tbunit.id = #{userSession.tbunit.id}")
				.getResultList();
		
		List<Source> lst = entityManager.createQuery("from Source s where s.workspace.id = #{defaultWorkspace.id}").getResultList();

		for (Source s: lst) {
			SourceMedicineUnit sourceMed = new SourceMedicineUnit();
			sourceMed.setSource(s);
			sources.add(sourceMed);
			
			for (Medicine m: medicines.getResultList()) {
				MedicineUnit mu = findCreateMedicine(items, m, s);
				sourceMed.getItems().add(mu);
			}
		}
		
	}
	
	private MedicineUnit findCreateMedicine(List<MedicineUnit> meds, Medicine med, Source source) {
		for (MedicineUnit mu: meds) {
			if ((mu.getMedicine().equals(med)) && (mu.getSource().equals(source)))
				return mu;
		}
		
		MedicineUnit mu = new MedicineUnit();
		mu.setMedicine(med);
		mu.setTbunit(userSession.getTbunit());
		mu.setSource(source);
		
		return mu;
	}

	
	/**
	 * Search for a medicine unit information by its source and medicine
	 * @param source
	 * @param medicine
	 * @return
	 */
	public MedicineUnit searchMedicineUnit(Source source, Medicine medicine) {
		for (SourceMedicineUnit s: getSources()) {
			if (s.getSource().equals(source)) {
				for (MedicineUnit mu: s.getItems()) {
					if (mu.getMedicine().equals(medicine))
						return mu;
				}
			}
		}
		
		return null;
	}
}
