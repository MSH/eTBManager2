package org.msh.tb.entities;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ricardo on 12/12/14.
 */
@Entity
@Table(name = "gxalertdata")
public class GxalertData extends WSObject {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @Length(max = 255)
    private String systemName;

    @Length(max = 255)
    @NotNull
    private String hostId;

    @Length(max = 255)
    private String assay;

    @Length(max = 10)
    private String assayVersion;
    @Length(max = 45)
    private String sampleId;

    private String patientId;
    private String user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date testStartedOn;

    @Temporal(TemporalType.TIMESTAMP)
    private Date testEndedOn;
    private String reagentLotId;

    @Temporal(TemporalType.DATE)
    private Date cartridgeExpirationDate;
    private String cartridgeSerial;
    private String moduleSerial;
    private String instrumentSerial;
    private String softwareVersion;
    private Integer resultIdMtb;
    private Integer resultIdRif;
    private String deviceSerial;
    private String notes;
    @Temporal(TemporalType.TIMESTAMP)
    private Date messageSentOn;
    private String assayHostTestCode;
    private String resultText;
    private String computerName;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date recordDate;


    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
    }

    public String getAssayVersion() {
        return assayVersion;
    }

    public void setAssayVersion(String assayVersion) {
        this.assayVersion = assayVersion;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTestStartedOn() {
        return testStartedOn;
    }

    public void setTestStartedOn(Date testStartedOn) {
        this.testStartedOn = testStartedOn;
    }

    public Date getTestEndedOn() {
        return testEndedOn;
    }

    public void setTestEndedOn(Date testEndedOn) {
        this.testEndedOn = testEndedOn;
    }

    public String getReagentLotId() {
        return reagentLotId;
    }

    public void setReagentLotId(String reagentLotId) {
        this.reagentLotId = reagentLotId;
    }

    public Date getCartridgeExpirationDate() {
        return cartridgeExpirationDate;
    }

    public void setCartridgeExpirationDate(Date cartridgeExpirationDate) {
        this.cartridgeExpirationDate = cartridgeExpirationDate;
    }

    public String getCartridgeSerial() {
        return cartridgeSerial;
    }

    public void setCartridgeSerial(String cartridgeSerial) {
        this.cartridgeSerial = cartridgeSerial;
    }

    public String getModuleSerial() {
        return moduleSerial;
    }

    public void setModuleSerial(String moduleSerial) {
        this.moduleSerial = moduleSerial;
    }

    public String getInstrumentSerial() {
        return instrumentSerial;
    }

    public void setInstrumentSerial(String instrumentSerial) {
        this.instrumentSerial = instrumentSerial;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public Integer getResultIdMtb() {
        return resultIdMtb;
    }

    public void setResultIdMtb(Integer resultIdMtb) {
        this.resultIdMtb = resultIdMtb;
    }

    public Integer getResultIdRif() {
        return resultIdRif;
    }

    public void setResultIdRif(Integer resultIdRif) {
        this.resultIdRif = resultIdRif;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getMessageSentOn() {
        return messageSentOn;
    }

    public void setMessageSentOn(Date messageSentOn) {
        this.messageSentOn = messageSentOn;
    }

    public String getAssayHostTestCode() {
        return assayHostTestCode;
    }

    public void setAssayHostTestCode(String assayHostTestCode) {
        this.assayHostTestCode = assayHostTestCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }
}
