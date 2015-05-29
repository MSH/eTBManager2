package org.msh.tb.reports2.variables;

import org.jboss.seam.Component;
import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.filters.ValueHandler;
import org.msh.reports.filters.ValueIteratorInt;
import org.msh.reports.query.SQLDefs;
import org.msh.tb.entities.Medicine;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class PrescMedicineVariable extends VariableImpl {

	private List<Medicine> medicines;
	
	public PrescMedicineVariable() {
		super("prescMed", "PrescribedMedicine", "prescribedmedicine.medicine_id");
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
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		def.table("tbcase").join("id", "prescribedmedicine.case_id");
		def.addRestriction("prescribedmedicine.id = (select min(pm1.id) from prescribedmedicine pm1 where pm1.medicine_id = prescribedmedicine.medicine_id " +
				"and pm1.case_id = " + def.getMasterTable().getAlias() + ".id )");
		super.prepareVariableQuery(def, iteration);
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#prepareFilterQuery(org.msh.reports.query.SQLDefs, org.msh.reports.filters.FilterOperation, java.lang.Object)
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, ValueHandler value) {
        // generate the sql IN
        String sql = value.mapSqlIN(new ValueIteratorInt() {
            @Override
            public String iterateInt(Integer value, int index) {
                return value != null? value.toString(): null;
            }
        });

/*
        String sql;
		if (value.getClass().isArray()) {
            String[] lst = (String[])value;
            sql = "";
            for (String s: lst) {
                if (!sql.isEmpty()) {
                    sql += ",";
                }
                sql += s;
            }
            sql = " in (" + sql + ")";
		}
        else {
            sql = "= :prescmed ";
            Integer id = Integer.parseInt(value.toString());
            def.addParameter("prescmed", id);
        }
*/
        def.addRestriction("exists(select * from prescribedmedicine pm1 where pm1.medicine_id " + sql +
                " and pm1.case_id =" + def.getMasterTable().getAlias() + ".id)");
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

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterType()
	 */
	@Override
	public String getFilterType() {
		return FilterType.REMOTE_OPTIONS;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#getFilterOptions(java.lang.Object)
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		List<FilterOption> lst = new ArrayList<FilterOption>();
		for (Medicine med: getMedicines()) {
			lst.add(new FilterOption(med.getId(), med.toString()));
		}
		return lst;
	}

	/* (non-Javadoc)
	 * @see org.msh.tb.reports2.VariableImpl#isTotalEnabled()
	 */
	@Override
	public boolean isTotalEnabled() {
		return false;
	}

}
