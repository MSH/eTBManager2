package org.msh.mdrtb.entities;

import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.msh.mdrtb.entities.enums.DiagnosisSource;
import org.msh.mdrtb.entities.enums.ExtraOutcomeInfo;
import org.msh.mdrtb.entities.enums.MBTResult;
import org.msh.mdrtb.entities.enums.YesNoType;

@Entity
public class CaseDataUA {

	@Id
	private Integer id;

	// specific information by country
	@OneToOne(cascade={CascadeType.ALL})
	@PrimaryKeyJoinColumn
	private TbCase tbcase;	

	@Temporal(TemporalType.DATE) private Date dateFirstVisitGMC;
	@Temporal(TemporalType.DATE) private Date dateFirstVisitTB;
	@Temporal(TemporalType.DATE) private Date dateEndHospitalization;
	@Temporal(TemporalType.DATE) private Date hospitalizationDate;
	@Temporal(TemporalType.DATE) private Date dateHospitalization;
	@Temporal(TemporalType.DATE) private Date dateFirstSymptoms;
	
	@Column(length=100)
	private String closestContact;
	
	private YesNoType pulmonaryDestruction;
	
	private YesNoType pulmonaryMBT;
	
	private MBTResult mbtResult;

	// urkraine flags
	private boolean alcoholAbuse;
	private boolean injectableDrugUse;
	private boolean tbContact;
	private boolean homeless;
	private boolean unemployed;
	private boolean healthWorker;
	private boolean healthWorkerTB;
	private boolean healthWorkerGMC;
	private boolean migrant;
	private boolean refugee;
	private boolean prisioner;
	private String otherFeature;

	@Column(length=100)
	private String employerName;
	
	private DiagnosisSource diagnosisSource;

	private ExtraOutcomeInfo extraOutcomeInfo;
	
	@Column(length=100)
	private String transferOutDescription;
	
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(name="value", joinColumns=@JoinColumn(name="DETECTION_ID"))})
	@AttributeOverrides({
		@AttributeOverride(name="complement", column=@Column(name="detectionOther"))})
	private FieldValueComponent detection = new FieldValueComponent();
	
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(name="value", joinColumns=@JoinColumn(name="POSITION_ID"))})
	@AttributeOverrides({
		@AttributeOverride(name="complement", column=@Column(name="positionOther"))})
	private FieldValueComponent position = new FieldValueComponent();
	
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(name="value", joinColumns=@JoinColumn(name="DIAGNOSIS_ID"))})
	@AttributeOverrides({
		@AttributeOverride(name="complement", column=@Column(name="diagnosisOther"))})
	private FieldValueComponent diagnosis = new FieldValueComponent();
	
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(name="value", joinColumns=@JoinColumn(name="REGISTRATION_CATEGORY"))})
	@AttributeOverrides({
		@AttributeOverride(name="complement", column=@Column(name="regcategory_other"))})
	private FieldValueComponent registrationCategory = new FieldValueComponent();

	private boolean hiv;
	
	@Temporal(TemporalType.DATE)
	private Date startedVCTdate;



	public boolean isVCTstarted() {
		return startedVCTdate != null;
	}
	
	
	public void setVCTstarted(boolean value) {
		if (!value)
			startedVCTdate = null;
	}

	/**
	 * @return the startedVCTdate
	 */
	public Date getStartedVCTdate() {
		return startedVCTdate;
	}

	/**
	 * @param startedVCTdate the startedVCTdate to set
	 */
	public void setStartedVCTdate(Date startedVCTdate) {
		this.startedVCTdate = startedVCTdate;
	}


	public YesNoType getPulmonaryDestruction() {
		return pulmonaryDestruction;
	}


	public void setPulmonaryDestruction(YesNoType pulmonaryDestruction) {
		this.pulmonaryDestruction = pulmonaryDestruction;
	}


	public YesNoType getPulmonaryMBT() {
		return pulmonaryMBT;
	}


	public void setPulmonaryMBT(YesNoType pulmonaryMBT) {
		this.pulmonaryMBT = pulmonaryMBT;
	}


	/**
	 * @return the alcoholAbuse
	 */
	public boolean isAlcoholAbuse() {
		return alcoholAbuse;
	}


	/**
	 * @param alcoholAbuse the alcoholAbuse to set
	 */
	public void setAlcoholAbuse(boolean alcoholAbuse) {
		this.alcoholAbuse = alcoholAbuse;
	}


	/**
	 * @return the injectableDrugUse
	 */
	public boolean isInjectableDrugUse() {
		return injectableDrugUse;
	}


	/**
	 * @param injectableDrugUse the injectableDrugUse to set
	 */
	public void setInjectableDrugUse(boolean injectableDrugUse) {
		this.injectableDrugUse = injectableDrugUse;
	}


	/**
	 * @return the tbContact
	 */
	public boolean isTbContact() {
		return tbContact;
	}


	/**
	 * @param tbContact the tbContact to set
	 */
	public void setTbContact(boolean tbContact) {
		this.tbContact = tbContact;
	}


	/**
	 * @return the homeless
	 */
	public boolean isHomeless() {
		return homeless;
	}


	/**
	 * @param homeless the homeless to set
	 */
	public void setHomeless(boolean homeless) {
		this.homeless = homeless;
	}


	/**
	 * @return the unemployed
	 */
	public boolean isUnemployed() {
		return unemployed;
	}


	/**
	 * @param unemployed the unemployed to set
	 */
	public void setUnemployed(boolean unemployed) {
		this.unemployed = unemployed;
	}


	/**
	 * @return the healthWorker
	 */
	public boolean isHealthWorker() {
		return healthWorker;
	}


	/**
	 * @param healthWorker the healthWorker to set
	 */
	public void setHealthWorker(boolean healthWorker) {
		this.healthWorker = healthWorker;
	}


	/**
	 * @return the healthWorkerTB
	 */
	public boolean isHealthWorkerTB() {
		return healthWorkerTB;
	}


	/**
	 * @param healthWorkerTB the healthWorkerTB to set
	 */
	public void setHealthWorkerTB(boolean healthWorkerTB) {
		this.healthWorkerTB = healthWorkerTB;
	}


	/**
	 * @return the healthWorkerGMC
	 */
	public boolean isHealthWorkerGMC() {
		return healthWorkerGMC;
	}


	/**
	 * @param healthWorkerGMC the healthWorkerGMC to set
	 */
	public void setHealthWorkerGMC(boolean healthWorkerGMC) {
		this.healthWorkerGMC = healthWorkerGMC;
	}


	/**
	 * @return the migrant
	 */
	public boolean isMigrant() {
		return migrant;
	}


	/**
	 * @param migrant the migrant to set
	 */
	public void setMigrant(boolean migrant) {
		this.migrant = migrant;
	}


	/**
	 * @return the refugee
	 */
	public boolean isRefugee() {
		return refugee;
	}


	/**
	 * @param refugee the refugee to set
	 */
	public void setRefugee(boolean refugee) {
		this.refugee = refugee;
	}


	/**
	 * @return the prisioner
	 */
	public boolean isPrisioner() {
		return prisioner;
	}


	/**
	 * @param prisioner the prisioner to set
	 */
	public void setPrisioner(boolean prisioner) {
		this.prisioner = prisioner;
	}


	/**
	 * @return the other feature of the TB case
	 */
	public String getOtherFeature() {
		return otherFeature;
	}


	/**
	 * specify other feature of the TB Case
	 * @param <i>otherFeature</i> the other feature to set
	 */
	public void setOtherFeature(String otherFeature) {
		this.otherFeature = otherFeature;
	}

	/**
	 * @return the hospitalizationDate
	 */
	public Date getHospitalizationDate() {
		return hospitalizationDate;
	}


	/**
	 * @param hospitalizationDate the hospitalizationDate to set
	 */
	public void setHospitalizationDate(Date hospitalizationDate) {
		this.hospitalizationDate = hospitalizationDate;
	}


	/**
	 * @param employerName the employerName to set
	 */
	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}


	/**
	 * @return the employerName
	 */
	public String getEmployerName() {
		return employerName;
	}


	/**
	 * @param diagnosisSource the diagnosisSource to set
	 */
	public void setDiagnosisSource(DiagnosisSource diagnosisSource) {
		this.diagnosisSource = diagnosisSource;
	}


	/**
	 * @return the diagnosisSource
	 */
	public DiagnosisSource getDiagnosisSource() {
		return diagnosisSource;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}


	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}


	/**
	 * @param extraOutcomeInfo the extraOutcomeInfo to set
	 */
	public void setExtraOutcomeInfo(ExtraOutcomeInfo extraOutcomeInfo) {
		this.extraOutcomeInfo = extraOutcomeInfo;
	}


	/**
	 * @return the extraOutcomeInfo
	 */
	public ExtraOutcomeInfo getExtraOutcomeInfo() {
		return extraOutcomeInfo;
	}



	/**
	 * @return the dateFirstVisitGMC
	 */
	public Date getDateFirstVisitGMC() {
		return dateFirstVisitGMC;
	}


	/**
	 * @param dateFirstVisitGMC the dateFirstVisitGMC to set
	 */
	public void setDateFirstVisitGMC(Date dateFirstVisitGMC) {
		this.dateFirstVisitGMC = dateFirstVisitGMC;
	}


	/**
	 * @return the dateFirstVisitTB
	 */
	public Date getDateFirstVisitTB() {
		return dateFirstVisitTB;
	}


	/**
	 * @param dateFirstVisitTB the dateFirstVisitTB to set
	 */
	public void setDateFirstVisitTB(Date dateFirstVisitTB) {
		this.dateFirstVisitTB = dateFirstVisitTB;
	}


	/**
	 * @return the dateHospitalization
	 */
	public Date getDateHospitalization() {
		return dateHospitalization;
	}


	/**
	 * @param dateHospitalization the dateHospitalization to set
	 */
	public void setDateHospitalization(Date dateHospitalization) {
		this.dateHospitalization = dateHospitalization;
	}


	/**
	 * @param closestContact the closestContact to set
	 */
	public void setClosestContact(String closestContact) {
		this.closestContact = closestContact;
	}


	/**
	 * @return the closestContact
	 */
	public String getClosestContact() {
		return closestContact;
	}


	/**
	 * @param dateEndHospitalization the dateEndHospitalization to set
	 */
	public void setDateEndHospitalization(Date dateEndHospitalization) {
		this.dateEndHospitalization = dateEndHospitalization;
	}


	/**
	 * @return the dateEndHospitalization
	 */
	public Date getDateEndHospitalization() {
		return dateEndHospitalization;
	}


	/**
	 * @param tbcase the tbcase to set
	 */
	public void setTbcase(TbCase tbcase) {
		this.tbcase = tbcase;
	}


	/**
	 * @return the tbcase
	 */
	public TbCase getTbcase() {
		return tbcase;
	}



	/**
	 * @return the transferOutDescription
	 */
	public String getTransferOutDescription() {
		return transferOutDescription;
	}


	/**
	 * @param transferOutDescription the transferOutDescription to set
	 */
	public void setTransferOutDescription(String transferOutDescription) {
		this.transferOutDescription = transferOutDescription;
	}


	/**
	 * @return the dateFirstSymptoms
	 */
	public Date getDateFirstSymptoms() {
		return dateFirstSymptoms;
	}


	/**
	 * @param dateFirstSymptoms the dateFirstSymptoms to set
	 */
	public void setDateFirstSymptoms(Date dateFirstSymptoms) {
		this.dateFirstSymptoms = dateFirstSymptoms;
	}


	/**
	 * @return the hiv
	 */
	public boolean isHiv() {
		return hiv;
	}


	/**
	 * @param hiv the hiv to set
	 */
	public void setHiv(boolean hiv) {
		this.hiv = hiv;
	}


	/**
	 * @return the mbtResult
	 */
	public MBTResult getMbtResult() {
		return mbtResult;
	}


	/**
	 * @param mbtResult the mbtResult to set
	 */
	public void setMbtResult(MBTResult mbtResult) {
		this.mbtResult = mbtResult;
	}


	/**
	 * @return the diagnosis
	 */
	public FieldValueComponent getDiagnosis() {
		if (diagnosis == null)
			diagnosis = new FieldValueComponent();
		return diagnosis;
	}


	/**
	 * @param diagnosis the diagnosis to set
	 */
	public void setDiagnosis(FieldValueComponent diagnosis) {
		this.diagnosis = diagnosis;
	}


	/**
	 * @return the detection
	 */
	public FieldValueComponent getDetection() {
		if (detection == null)
			detection = new FieldValueComponent();
		return detection;
	}


	/**
	 * @param detection the detection to set
	 */
	public void setDetection(FieldValueComponent detection) {
		this.detection = detection;
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


	/**
	 * @return the registrationCategory
	 */
	public FieldValueComponent getRegistrationCategory() {
		if (registrationCategory == null)
			registrationCategory = new FieldValueComponent();
		return registrationCategory;
	}


	/**
	 * @param registrationCategory the registrationCategory to set
	 */
	public void setRegistrationCategory(FieldValueComponent registrationCategory) {
		this.registrationCategory = registrationCategory;
	}
}
