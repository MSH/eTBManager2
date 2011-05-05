package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.msh.tb.entities.enums.MedicineCategory;
import org.msh.tb.entities.enums.MedicineLine;
import org.msh.tb.log.FieldLog;

@Entity
public class Medicine extends WSObject implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@Embedded
	@FieldLog(key="form.name")
	private LocalizedNameComp genericName= new LocalizedNameComp();
	
	@Column(length=30)
	private String abbrevName;
	
	@Column(length=30)
	private String strength;
	
	@Column(length=50)
	private String strengthUnit;
	
	@Column(length=50)
	private String dosageForm;

	@Column(length=50)
	@FieldLog(key="global.legacyId")
	private String legacyId;

	private MedicineCategory category;
	
	private MedicineLine line;

	@OneToMany(mappedBy="medicine", cascade={CascadeType.ALL})
	private List<MedicineComponent> components = new ArrayList<MedicineComponent>();

	@ManyToOne
	@JoinColumn(name="GROUP_ID")
	private ProductGroup group;
	
	public String getTbInfoKey() {
		return line != null? line.getKey(): null;
	}
	
	public String getFullAbbrevName() {
		String s = abbrevName;
		
		if (strength == null)
			return s;
		s = s + " " + strength;

		if (strengthUnit == null)
			return s;
		s = s + strengthUnit;
		
		if (dosageForm == null)
			return s;

		return s; 
	}
	
	@Override
	public String toString() {
		String s;
		if (getGenericName() == null) 
			return null;
		s = genericName.getDefaultName();
		
		if (strength == null)
			return s;
		s = s + " " + strength;

		if (strengthUnit == null)
			return s;
		s = s + strengthUnit;
		
		if (dosageForm == null)
			return s;

		return s + " (" + dosageForm + ")"; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) 
			return true;
		
		if (!(obj instanceof Medicine))
			return false;
		
		return ((Medicine)obj).getId().equals(getId());
	}


	public MedicineCategory getCategory() {
		return category;
	}

	public void setCategory(MedicineCategory category) {
		this.category = category;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAbbrevName() {
		return abbrevName;
	}

	public void setAbbrevName(String abbrevName) {
		this.abbrevName = abbrevName;
	}

	public String getStrength() {
		return strength;
	}

	public void setStrength(String strength) {
		this.strength = strength;
	}

	public String getStrengthUnit() {
		return strengthUnit;
	}

	public void setStrengthUnit(String strengthUnit) {
		this.strengthUnit = strengthUnit;
	}

	public String getDosageForm() {
		return dosageForm;
	}

	public void setDosageForm(String dosageForm) {
		this.dosageForm = dosageForm;
	}


	public ProductGroup getGroup() {
		return group;
	}


	public void setGroup(ProductGroup group) {
		this.group = group;
	}


	public LocalizedNameComp getGenericName() {
		return genericName;
	}


	public void setGenericName(LocalizedNameComp genericName) {
		this.genericName = genericName;
	}

	public MedicineLine getLine() {
		return line;
	}

	public void setLine(MedicineLine line) {
		this.line = line;
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
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}

	public void setComponents(List<MedicineComponent> components) {
		this.components = components;
	}

	public List<MedicineComponent> getComponents() {
		return components;
	}

}
