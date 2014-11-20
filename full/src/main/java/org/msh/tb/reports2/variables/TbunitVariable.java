/**
 * 
 */
package org.msh.tb.reports2.variables;

import org.msh.reports.filters.FilterOperation;
import org.msh.reports.filters.FilterOption;
import org.msh.reports.query.SQLDefs;
import org.msh.reports.query.TableJoin;
import org.msh.tb.application.App;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.Tbunit;
import org.msh.tb.reports2.FilterType;
import org.msh.tb.reports2.VariableImpl;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Variable and filter to support TB units
 * 
 * @author Ricardo Memoria
 *
 */
public class TbunitVariable extends VariableImpl {

	private List<Tbunit> units;
	private Integer adminUnitId;

	
	/**
	 * Default constructor
	 * @param id
	 * @param keylabel
	 * @param fieldName
	 */
	public TbunitVariable(String id, String keylabel, String fieldName) {
		super(id, keylabel, fieldName);
	}


	/**
	 * Return the list of units of the variable
	 * @return
	 */
	public List<Tbunit> getUnits(Integer adminUnitId) {
		if (units == null) {
			String hql = "from Tbunit where workspace.id = #{defaultWorkspace.id}";

			AdministrativeUnit au = null;
			String code;
			if (adminUnitId != null) {
				au = App.getEntityManager().find(AdministrativeUnit.class, adminUnitId);
				code = au.getCode() + "%";
				if (au != null) {
					hql += " and adminUnit.code like :code";
				}
			}
			else {
				code = null;
			}
			
			hql += " order by name.name1";

			Query qry = App.getEntityManager().createQuery(hql);
			
			if (code != null) {
				qry.setParameter("code", code);
			}
			
			units = qry.getResultList();
		}
		return units;
	}
	
	
	/** {@inheritDoc}
	 */
	@Override
	public void prepareVariableQuery(SQLDefs def, int iteration) {
		String[] s = getFieldName().split("\\.");
		def.table(s[0]).join(s[1], "tbunit.id").select("id");
	}


	/** {@inheritDoc}
	 */
	@Override
	public void prepareFilterQuery(SQLDefs def, FilterOperation oper, Object value) {
		adminUnitId = null;
		if (value instanceof String) {
			Key key = getKeysFromString((String)value);

			// TB unit was defined ?
			if (key.getTbunitId() != null) {
				def.addRestriction(getFieldName() + " = :" + getId());
				def.addParameter(getId(), key.getTbunitId());
			}
			else {
				// administrative unit was defined ?
				if (key.getAdminUnitId() != null) {
					String[] s = getFieldName().split("\\.");
					TableJoin tbl = def.table(s[0]).join(s[1], "tbunit.id").select("id");
					adminUnitId = key.getAdminUnitId();
					AdministrativeUnit au = App.getEntityManager().find(AdministrativeUnit.class, key.getAdminUnitId());
					
					tbl.join("adminunit_id", "administrativeunit.id");
					def.addRestriction("administrativeunit.code like :codeau");
					def.addParameter("codeau", au.getCode() + "%");
				}
			}
		}
	}


	/** {@inheritDoc}
	 */
	@Override
	public String getDisplayText(Object key) {
		for (Tbunit unit: getUnits(adminUnitId)) {
			if (unit.getId().equals(key)) {
				return unit.getName().toString();
			}
		}
		return super.getDisplayText(key);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object createKey(Object values) {
		return super.createKey(values);
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getFilterType() {
		return FilterType.FILTER_TBUNIT;
	}

	/** {@inheritDoc}
	 */
	@Override
	public List<FilterOption> getFilterOptions(Object param) {
		Integer adminunitId = null;
		if (param instanceof Number) {
			adminunitId = ((Number)param).intValue();
		}
		else {
			if (param != null) {
				adminunitId = Integer.parseInt( param.toString() );
			}
		}

		// check if ID was defined
		if (adminunitId != null) {
			return getUnitOptions(adminunitId);
		}
		else {
			return getAdminUnitOptions();
		}
	}
	
	
	/**
	 * Return the list of administrative units as options
	 * @return list of {@link FilterOperation} instances
	 */
	protected List<FilterOption> getAdminUnitOptions() {
		String hql = "from AdministrativeUnit where workspace.id = #{defaultWorkspace.id} " +
				"and parent.id is null";
		List<AdministrativeUnit> lst = App.getEntityManager().createQuery(hql).getResultList();
		
		List<FilterOption> opts = new ArrayList<FilterOption>();
		for (AdministrativeUnit unit: lst) {
			opts.add(new FilterOption(unit.getId(), unit.getName().toString()));
		}

		return opts;
	}
	
	
	/**
	 * Return the list of units as options
	 * @return
	 */
	protected List<FilterOption> getUnitOptions(Integer adminUnitId) {
		List<Tbunit> lst = getUnits(adminUnitId);
		
		List<FilterOption> opts = new ArrayList<FilterOption>();
		for (Tbunit unit: lst) {
			opts.add(new FilterOption(unit.getId(), unit.getName().toString()));
		}

		return opts;
	}


	/** {@inheritDoc}
	 */
	@Override
	public boolean isFilterLazyInitialized() {
		return true;
	}

	

	
	/**
	 * Retrieve information about the admin unit and TB unit keys from a string
	 * @param value a string containing an admin unit or a TB key
	 * @return instance of {@link Key}
	 */
	protected Key getKeysFromString(String value) {
		if (value == null) {
			return new Key(null, null);
		}
		
		String[] s = value.split(",");

		return new Key(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
	}
	
	/**
	 * Information about the keys from a variable
	 * @author Ricardo Memoria
	 *
	 */
	public class Key {
		private Integer tbunitId;
		private Integer adminUnitId;
		
		public Key(Integer adminUnitId, Integer tbunitId) {
			super();
			this.tbunitId = tbunitId;
			this.adminUnitId = adminUnitId;
		}
		/**
		 * @return the tbunitId
		 */
		public Integer getTbunitId() {
			return tbunitId;
		}
		/**
		 * @param tbunitId the tbunitId to set
		 */
		public void setTbunitId(Integer tbunitId) {
			this.tbunitId = tbunitId;
		}
		/**
		 * @return the adminUnitId
		 */
		public Integer getAdminUnitId() {
			return adminUnitId;
		}
		/**
		 * @param adminUnitId the adminUnitId to set
		 */
		public void setAdminUnitId(Integer adminUnitId) {
			this.adminUnitId = adminUnitId;
		}
	}
}
