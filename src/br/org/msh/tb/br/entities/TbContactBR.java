package org.msh.tb.br.entities;

import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.msh.tb.br.entities.enums.XRayContactBR;
import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbContact;
import org.msh.tb.entities.enums.CultureResult;
import org.msh.tb.entities.enums.MicroscopyResult;
import org.msh.tb.entities.enums.YesNoType;

@Entity
@Table(name="tbcontactbr")
public class TbContactBR extends TbContact {
	private static final long serialVersionUID = -6430798901737624608L;

	private YesNoType tbSymptoms;
	
	private YesNoType scarBCG;
	
	private YesNoType tuberculinTest;
	
	private String tuberculinTestResult;

	private XRayContactBR xray;
	
	private MicroscopyResult microscopyResult;
	
	private CultureResult cultureResult;
	
	private Date dateIniTreat;
	
	private Date dateEndTreat;
	
	@Embedded
	@AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "XRAY_RESULT")) })
	@AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "XRAY_RESULT_Complement")) })
	private FieldValueComponent xrayResult;



	public YesNoType getTbSymptoms() {
		return tbSymptoms;
	}

	public void setTbSymptoms(YesNoType tbSymptoms) {
		this.tbSymptoms = tbSymptoms;
	}

	public YesNoType getScarBCG() {
		return scarBCG;
	}

	public void setScarBCG(YesNoType scarBCG) {
		this.scarBCG = scarBCG;
	}

	public YesNoType getTuberculinTest() {
		return tuberculinTest;
	}

	public void setTuberculinTest(YesNoType tuberculinTest) {
		this.tuberculinTest = tuberculinTest;
	}

	public XRayContactBR getXray() {
		return xray;
	}

	public void setXray(XRayContactBR xray) {
		this.xray = xray;
	}

	public MicroscopyResult getMicroscopyResult() {
		return microscopyResult;
	}

	public void setMicroscopyResult(MicroscopyResult microscopyResult) {
		this.microscopyResult = microscopyResult;
	}

	public CultureResult getCultureResult() {
		return cultureResult;
	}

	public void setCultureResult(CultureResult cultureResult) {
		this.cultureResult = cultureResult;
	}

	public Date getDateIniTreat() {
		return dateIniTreat;
	}

	public void setDateIniTreat(Date dateIniTreat) {
		this.dateIniTreat = dateIniTreat;
	}

	public Date getDateEndTreat() {
		return dateEndTreat;
	}

	public void setDateEndTreat(Date dateEndTreat) {
		this.dateEndTreat = dateEndTreat;
	}

	public String getTuberculinTestResult() {
		return tuberculinTestResult;
	}

	public void setTuberculinTestResult(String tuberculinTestResult) {
		this.tuberculinTestResult = tuberculinTestResult;
	}

	/**
	 * @return the xrayResult
	 */
	public FieldValueComponent getXrayResult() {
		if (xrayResult == null)
			xrayResult = new FieldValueComponent();
		return xrayResult;
	}

	/**
	 * @param xrayResult the xrayResult to set
	 */
	public void setXrayResult(FieldValueComponent xrayResult) {
		this.xrayResult = xrayResult;
	}
}
