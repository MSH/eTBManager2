package org.msh.entities;

import java.util.Date;

import javax.persistence.Entity;

import org.msh.entities.enums.XRayContactBR;
import org.msh.mdrtb.entities.TbContact;
import org.msh.mdrtb.entities.enums.YesNoType;
import org.msh.mdrtb.entities.enums.SputumSmearResult;
import org.msh.mdrtb.entities.enums.CultureResult;

@Entity
public class TbContactBR extends TbContact {
	private static final long serialVersionUID = -6430798901737624608L;

	private YesNoType tbSymptoms;
	
	private YesNoType scarBCG;
	
	private YesNoType tuberculinTest;
	
	private String tuberculinTestResult;

	private XRayContactBR xray;
	
	private SputumSmearResult microscopyResult;
	
	private CultureResult cultureResult;
	
	private Date dateIniTreat;
	
	private Date dateEndTreat;



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

	public SputumSmearResult getMicroscopyResult() {
		return microscopyResult;
	}

	public void setMicroscopyResult(SputumSmearResult microscopyResult) {
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
}
