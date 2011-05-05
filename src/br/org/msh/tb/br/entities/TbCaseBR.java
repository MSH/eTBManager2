package org.msh.tb.br.entities;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.msh.tb.br.entities.enums.FailureType;
import org.msh.tb.br.entities.enums.TipoResistencia;
import org.msh.tb.entities.AdministrativeUnit;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;

@Entity
public class TbCaseBR extends TbCase {
	private static final long serialVersionUID = -9217679039838707990L;

/*	@Id
	private Integer id;
	
	// specific information by country
	@OneToOne(cascade={CascadeType.ALL})
	@PrimaryKeyJoinColumn
	@FieldLog(logEntityFields=true)
	private TbCase tbcase;
*/
	@Column(length=100)
	private String numSinan;
	
	@Column(length=100)
	private String usOrigem;

	@ManyToOne
	@JoinColumn(name="ADMINUNIT_USORIGEM_ID")
	private AdministrativeUnit adminUnitUsOrigem;	
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="SCHEMACHANGETYPE"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="SCHEMACHANGETYPE_Complement"))})
	private FieldValueComponent schemaChangeType = new FieldValueComponent();
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="RESISTANCETYPE"))})
	@AttributeOverrides({ 	@AttributeOverride(name="complement", column=@Column(name="RESISTANCETYPE_Complement"))})
	private FieldValueComponent resistanceType;
	
	private FailureType failureType;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="SKINCOLOR"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="skinColor_Complement"))})
	private FieldValueComponent skinColor;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="CONTAGPLACE"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="contagPlace_Complement"))})
	private FieldValueComponent contagPlace;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="EDUCATIONALDEGREE"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="educationalDegree_Complement"))})
	private FieldValueComponent educationalDegree;
	
	@ManyToOne
	@JoinColumn(name="PREGNANCEPERIOD")
	private FieldValue pregnancePeriod;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="MICROBACTERIOSE"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="MICROBACTERIOSE_Complement"))})
	private FieldValueComponent microbacteriose;
	
	@Embedded
	@AssociationOverrides({	@AssociationOverride(name="value", joinColumns=@JoinColumn(name="POSITION"))})
	@AttributeOverrides({	@AttributeOverride(name="complement", column=@Column(name="positionOther"))})
	private FieldValueComponent position;
	
	@Column(length=10)
	private String prefixPhone;
	
	@Column(length=10)
	private String prefixMobile;
	
	@Column(length=100)
	private String country;

	@Column(length=50)
	private String notifAddressNumber;
	
	@Column(length=50)
	private String currAddressNumber;
	
	private TipoResistencia tipoResistencia;
	
	@Column(length=100)
	private String notifDistrict;
	
	@Column(length=100)
	private String currDistrict;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="OUTCOME_REGIMENCHANGED")
	private FieldValue outcomeRegimenChanged;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="OUTCOME_RESISTANCETYPE")
	private FieldValue outcomeResistanceType;


	/**
	 * @return the failureType
	 */
	public FailureType getFailureType() {
		return failureType;
	}

	/**
	 * @param failureType the failureType to set
	 */
	public void setFailureType(FailureType failureType) {
		this.failureType = failureType;
	}

	/**
	 * @return the schemaChangeType
	 */
	public FieldValueComponent getSchemaChangeType() {
		if (schemaChangeType == null)
			schemaChangeType = new FieldValueComponent();
		return schemaChangeType;
	}

	/**
	 * @param schemaChangeType the schemaChangeType to set
	 */
	public void setSchemaChangeType(FieldValueComponent schemaChangeType) {
		this.schemaChangeType = schemaChangeType;
	}

	/**
	 * @return the resistanceType
	 */
	public FieldValueComponent getResistanceType() {
		if (resistanceType == null)
			resistanceType = new FieldValueComponent();
		return resistanceType;
	}

	/**
	 * @param resistanceType the resistanceType to set
	 */
	public void setResistanceType(FieldValueComponent resistanceType) {
		this.resistanceType = resistanceType;
	}


	/**
	 * @return the contagPlace
	 */
	public FieldValueComponent getContagPlace() {
		if (contagPlace == null)
			contagPlace = new FieldValueComponent();
		return contagPlace;
	}

	/**
	 * @param contagPlace the contagPlace to set
	 */
	public void setContagPlace(FieldValueComponent contagPlace) {
		this.contagPlace = contagPlace;
	}

	/**
	 * @return the educationalDegree
	 */
	public FieldValueComponent getEducationalDegree() {
		if (educationalDegree == null)
			educationalDegree = new FieldValueComponent();
		return educationalDegree;
	}

	/**
	 * @param educationalDegree the educationalDegree to set
	 */
	public void setEducationalDegree(FieldValueComponent educationalDegree) {
		this.educationalDegree = educationalDegree;
	}

	/**
	 * @return the pregnancePeriod
	 */
	public FieldValue getPregnancePeriod() {
		return pregnancePeriod;
	}

	/**
	 * @param pregnancePeriod the pregnancePeriod to set
	 */
	public void setPregnancePeriod(FieldValue pregnancePeriod) {
		this.pregnancePeriod = pregnancePeriod;
	}


	/**
	 * @return the prefixPhone
	 */
	public String getPrefixPhone() {
		return prefixPhone;
	}

	/**
	 * @param prefixPhone the prefixPhone to set
	 */
	public void setPrefixPhone(String prefixPhone) {
		this.prefixPhone = prefixPhone;
	}

	/**
	 * @return the prefixMobile
	 */
	public String getPrefixMobile() {
		return prefixMobile;
	}

	/**
	 * @param prefixMobile the prefixMobile to set
	 */
	public void setPrefixMobile(String prefixMobile) {
		this.prefixMobile = prefixMobile;
	}

	/**
	 * @return the numSinan
	 */
	public String getNumSinan() {
		return numSinan;
	}

	/**
	 * @param numSinan the numSinan to set
	 */
	public void setNumSinan(String numSinan) {
		this.numSinan = numSinan;
	}

	/**
	 * @return the usOrigem
	 */
	public String getUsOrigem() {
		return usOrigem;
	}

	/**
	 * @param usOrigem the usOrigem to set
	 */
	public void setUsOrigem(String usOrigem) {
		this.usOrigem = usOrigem;
	}

	/**
	 * @return the adminUnitUsOrigem
	 */
	public AdministrativeUnit getAdminUnitUsOrigem() {
		return adminUnitUsOrigem;
	}

	/**
	 * @param adminUnitUsOrigem the adminUnitUsOrigem to set
	 */
	public void setAdminUnitUsOrigem(AdministrativeUnit adminUnitUsOrigem) {
		this.adminUnitUsOrigem = adminUnitUsOrigem;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the notifAddressNumber
	 */
	public String getNotifAddressNumber() {
		return notifAddressNumber;
	}

	/**
	 * @param notifAddressNumber the notifAddressNumber to set
	 */
	public void setNotifAddressNumber(String notifAddressNumber) {
		this.notifAddressNumber = notifAddressNumber;
	}

	/**
	 * @return the currAddressNumber
	 */
	public String getCurrAddressNumber() {
		return currAddressNumber;
	}

	/**
	 * @param currAddressNumber the currAddressNumber to set
	 */
	public void setCurrAddressNumber(String currAddressNumber) {
		this.currAddressNumber = currAddressNumber;
	}

	/**
	 * @return the tipoResistencia
	 */
	public TipoResistencia getTipoResistencia() {
		return tipoResistencia;
	}

	/**
	 * @param tipoResistencia the tipoResistencia to set
	 */
	public void setTipoResistencia(TipoResistencia tipoResistencia) {
		this.tipoResistencia = tipoResistencia;
	}

	/**
	 * @return the notifDistrict
	 */
	public String getNotifDistrict() {
		return notifDistrict;
	}

	/**
	 * @param notifDistrict the notifDistrict to set
	 */
	public void setNotifDistrict(String notifDistrict) {
		this.notifDistrict = notifDistrict;
	}

	/**
	 * @return the currDistrict
	 */
	public String getCurrDistrict() {
		return currDistrict;
	}

	/**
	 * @param currDistrict the currDistrict to set
	 */
	public void setCurrDistrict(String currDistrict) {
		this.currDistrict = currDistrict;
	}

	/**
	 * @return the microbacteriose
	 */
	public FieldValueComponent getMicrobacteriose() {
		if (microbacteriose == null)
			microbacteriose = new FieldValueComponent();
		return microbacteriose;
	}

	/**
	 * @param microbacteriose the microbacteriose to set
	 */
	public void setMicrobacteriose(FieldValueComponent microbacteriose) {
		this.microbacteriose = microbacteriose;
	}

	/**
	 * @return the skinColor
	 */
	public FieldValueComponent getSkinColor() {
		if (skinColor == null)
			skinColor = new FieldValueComponent();
		return skinColor;
	}

	/**
	 * @param skinColor the skinColor to set
	 */
	public void setSkinColor(FieldValueComponent skinColor) {
		this.skinColor = skinColor;
	}

	/**
	 * @return the position
	 */
	public FieldValueComponent getPosition() {
		if (position == null)
			position = new FieldValueComponent();
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(FieldValueComponent position) {
		this.position = position;
	}

	public FieldValue getOutcomeRegimenChanged() {
		return outcomeRegimenChanged;
	}

	public void setOutcomeRegimenChanged(FieldValue outcomeRegimenChanged) {
		this.outcomeRegimenChanged = outcomeRegimenChanged;
	}

	public FieldValue getOutcomeResistanceType() {
		return outcomeResistanceType;
	}

	public void setOutcomeResistanceType(FieldValue outcomeResistanceType) {
		this.outcomeResistanceType = outcomeResistanceType;
	}

}
