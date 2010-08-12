package org.msh.mdrtb.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Table;


@Entity
@Table(appliesTo="AdministrativeUnit",indexes={@Index(name="idxcode", columnNames={"code"})})
public class AdministrativeUnit extends WSObject {
	private static final long serialVersionUID = 7777075173601864769L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Embedded
	private LocalizedNameComp name = new LocalizedNameComp();
	
	@ManyToOne
	@JoinColumn(name="PARENT_ID")
	private AdministrativeUnit parent;
	
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY)
	@OrderBy(clause="NAME1")
	private List<AdministrativeUnit> units = new ArrayList<AdministrativeUnit>();

	@Column(length=50)
	private String legacyId;
	
	// properties to help dealing with trees
	private int unitsCount;
	
	@Column(length=15, nullable=false)
	private String code;
	
	@ManyToOne
	@JoinColumn(name="COUNTRYSTRUCTURE_ID")
	private CountryStructure countryStructure;
	

	/**
	 * Return the parent list including the own object
	 * @return List of {@link AdministrativeUnit} instance
	 */
	public List<AdministrativeUnit> getParents() {
		return getParentsTreeList(true);
	}

	/**
	 * Return a list with parents administrative unit, where the first is the upper level administrative unit and
	 * the last the lowest level
	 * @return {@link List} of {@link AdministrativeUnit} instances
	 */
	public List<AdministrativeUnit> getParentsTreeList(boolean includeThis) {
		ArrayList<AdministrativeUnit> lst = new ArrayList<AdministrativeUnit>();

		AdministrativeUnit aux;
		if (includeThis)
			 aux = this;
		else aux = getParent();

		while (aux != null) {
			lst.add(0, aux);
			aux = aux.getParent();
		}
		return lst;
	}


	/**
	 * Check if an administrative unit code (passed as the code parameter) is a child of the current administrative unit
	 * @param code of the unit
	 * @return true if code is of a child unit, otherwise return false
	 */
	public boolean isSameOrChildCode(String code) {
		int len = this.code.length();
		if (len > code.length())
			return false;
		return (this.code.equals(code.substring(0, this.code.length())));
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public LocalizedNameComp getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(LocalizedNameComp name) {
		this.name = name;
	}

	/**
	 * @return the parent
	 */
	public AdministrativeUnit getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(AdministrativeUnit parent) {
		this.parent = parent;
	}

	/**
	 * @return the units
	 */
	public List<AdministrativeUnit> getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(List<AdministrativeUnit> units) {
		this.units = units;
	}

	/**
	 * @return the legacyId
	 */
	public String getLegacyId() {
		return legacyId;
	}

	/**
	 * @param legacyId the legacyId to set
	 */
	public void setLegacyId(String legacyCode) {
		this.legacyId = legacyCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName().toString();
	}

	/**
	 * @return the unitsCount
	 */
	public int getUnitsCount() {
		return unitsCount;
	}

	/**
	 * @param unitsCount the unitsCount to set
	 */
	public void setUnitsCount(int unitsCount) {
		this.unitsCount = unitsCount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		
		if (!(obj instanceof AdministrativeUnit))
			return false;
		
		return ((AdministrativeUnit)obj).getId().equals(getId());
	}

	/**
	 * @return the countryStructure
	 */
	public CountryStructure getCountryStructure() {
		return countryStructure;
	}

	/**
	 * @param countryStructure the countryStructure to set
	 */
	public void setCountryStructure(CountryStructure countryStructure) {
		this.countryStructure = countryStructure;
	}


	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	public int getLevel() {
		String s = getCode();
		if (s == null)
			return 0;

		return s.length()/3;
	}
}
