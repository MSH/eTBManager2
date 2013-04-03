package org.msh.tb.reports2.variables;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.Medicine;
import org.msh.tb.reports2.VariableImpl;

public class PrescMedicineVariable extends VariableImpl {

	private List<Medicine> medicines;
	
	public PrescMedicineVariable() {
		super("prescMed", "PrescribedMedicine", null, null);
	}
	
	/**
	 * Return the list of medicines available in the workspace
	 * @return list of {@link Medicine} objects
	 */
	public List<Medicine> getMedicines() {
		if (medicines == null) {
			EntityManager em = (EntityManager)Component.getInstance("entityManager");
			medicines = em.createQuery("from Medicine where workspace.id = #{defaultWorkspace.id}")
				.getResultList();
		}
		return medicines;
	}
	
	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareVariableQuery(org.msh.reports.query.SQLDefs)
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def) {
		String tbl = def.addJoin("prescribedmedicine", "case_id", "tbcase", "id").getAlias();
		def.addField(tbl + ".medicine_id");
//		def.addRestriction("tbcase.initreatmentdate is not null");
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getDisplayText(java.lang.Object)
	 */
	@Override
	public String getDisplayText(Object key) {
		// if it's null, return undefined
		if (key == null)
			return super.getDisplayText(key);

		for (Medicine med: getMedicines()) {
			if (med.getId().equals(key))
				return med.getFullAbbrevName();
		}
		
		return super.getDisplayText(key);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#createKey(java.lang.Object)
	 */
	@Override
	public Object createKey(Object value) {
		return value;
	}

}
