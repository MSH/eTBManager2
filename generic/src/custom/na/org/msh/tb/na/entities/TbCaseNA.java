package org.msh.tb.na.entities;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="tbcasena")
public class TbCaseNA extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2227321155884824528L;
	
	@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	private List<CaseDispensingNA> dispna = new ArrayList<CaseDispensingNA>();
	
	private boolean hospitalized;

	@Temporal(TemporalType.DATE)
	private Date hospitalizedDt;
	
	@Temporal(TemporalType.DATE)
	private Date dischargeDt;
	
	@Temporal(TemporalType.DATE)
	@Column(name="EVENT_DATE")
	private Date date;	
	
	@Column(length=50)
	private String unitRegCode;
	
	
	private String comments;
	
	
	private boolean socialDisabilityAwarded;
	
	@Temporal(TemporalType.DATE)
	private Date startDateSocialAward;
	
	@Lob
	private String commentSocialAward;
	
	
	private boolean foodPackageAwarded;
	
	@Temporal(TemporalType.DATE)
	private Date startDateFoodPackageAward;
	
	@Lob
	private String commentFoodPackageAward;
	
	private boolean transportAssistProvided;
	
	@Temporal(TemporalType.DATE)
	private Date startDateTransportAssist;
	
	@Lob
	private String commentTransportAssist;
	
	/**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	@Override
	public CaseSideEffectNA findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffect se: getSideEffects()) {
			if (se.getSideEffect().getValue().getId().equals(sideEffect.getId()))
				return (CaseSideEffectNA)se;
		}
		return null;
	}	

	public List<CaseDispensingNA> getDispna() {
		return dispna;
	}



	public void setDispna(List<CaseDispensingNA> dispna) {
		this.dispna = dispna;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comment) {
		this.comments = comment;
	}

	public String getUnitRegCode() {
		return unitRegCode;
	}

	public void setUnitRegCode(String unitRegCode) {
		this.unitRegCode = unitRegCode;
	}

	public boolean isHospitalized() {
		return hospitalized;
	}

	public void setHospitalized(boolean hospitalized) {
		this.hospitalized = hospitalized;
	}

	public Date getHospitalizedDt() {
		return hospitalizedDt;
	}

	public void setHospitalizedDt(Date hospitalizedDt) {
		this.hospitalizedDt = hospitalizedDt;
	}

	public Date getDischargeDt() {
		return dischargeDt;
	}

	public void setDischargeDt(Date dischargeDt) {
		this.dischargeDt = dischargeDt;
	}

	/**
	 * @return the socialDisabilityAwarded
	 */
	public boolean isSocialDisabilityAwarded() {
		return socialDisabilityAwarded;
	}

	/**
	 * @param socialDisabilityAwarded the socialDisabilityAwarded to set
	 */
	public void setSocialDisabilityAwarded(boolean socialDisabilityAwarded) {
		this.socialDisabilityAwarded = socialDisabilityAwarded;
	}

	/**
	 * @return the startDateSocialAward
	 */
	public Date getStartDateSocialAward() {
		return startDateSocialAward;
	}

	/**
	 * @param startDateSocialAward the startDateSocialAward to set
	 */
	public void setStartDateSocialAward(Date startDateSocialAward) {
		this.startDateSocialAward = startDateSocialAward;
	}

	/**
	 * @return the commentSocialAward
	 */
	public String getCommentSocialAward() {
		return commentSocialAward;
	}

	/**
	 * @param commentSocialAward the commentSocialAward to set
	 */
	public void setCommentSocialAward(String commentSocialAward) {
		this.commentSocialAward = commentSocialAward;
	}

	/**
	 * @return the foodPackageAwarded
	 */
	public boolean isFoodPackageAwarded() {
		return foodPackageAwarded;
	}

	/**
	 * @param foodPackageAwarded the foodPackageAwarded to set
	 */
	public void setFoodPackageAwarded(boolean foodPackageAwarded) {
		this.foodPackageAwarded = foodPackageAwarded;
	}

	/**
	 * @return the startDateFoodPackageAward
	 */
	public Date getStartDateFoodPackageAward() {
		return startDateFoodPackageAward;
	}

	/**
	 * @param startDateFoodPackageAward the startDateFoodPackageAward to set
	 */
	public void setStartDateFoodPackageAward(Date startDateFoodPackageAward) {
		this.startDateFoodPackageAward = startDateFoodPackageAward;
	}

	/**
	 * @return the commentFoodPackageAward
	 */
	public String getCommentFoodPackageAward() {
		return commentFoodPackageAward;
	}

	/**
	 * @param commentFoodPackageAward the commentFoodPackageAward to set
	 */
	public void setCommentFoodPackageAward(String commentFoodPackageAward) {
		this.commentFoodPackageAward = commentFoodPackageAward;
	}

	/**
	 * @return the transportAssistProvided
	 */
	public boolean isTransportAssistProvided() {
		return transportAssistProvided;
	}

	/**
	 * @param transportAssistProvided the transportAssistProvided to set
	 */
	public void setTransportAssistProvided(boolean transportAssistProvided) {
		this.transportAssistProvided = transportAssistProvided;
	}

	/**
	 * @return the startDateTransportAssist
	 */
	public Date getStartDateTransportAssist() {
		return startDateTransportAssist;
	}

	/**
	 * @param startDateTransportAssist the startDateTransportAssist to set
	 */
	public void setStartDateTransportAssist(Date startDateTransportAssist) {
		this.startDateTransportAssist = startDateTransportAssist;
	}

	/**
	 * @return the commentTransportAssist
	 */
	public String getCommentTransportAssist() {
		return commentTransportAssist;
	}

	/**
	 * @param commentTransportAssist the commentTransportAssist to set
	 */
	public void setCommentTransportAssist(String commentTransportAssist) {
		this.commentTransportAssist = commentTransportAssist;
	}

	
}
