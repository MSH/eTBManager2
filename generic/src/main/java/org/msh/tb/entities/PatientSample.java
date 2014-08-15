package org.msh.tb.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.validator.NotNull;
import org.msh.tb.transactionlog.Operation;
import org.msh.tb.transactionlog.PropertyLog;
import org.msh.tb.workspaces.customizable.WorkspaceCustomizationService;
import org.msh.utils.date.LocaleDateConverter;

@Entity
@Table(name="patientsample")
public class PatientSample implements Serializable {
    private static final long serialVersionUID = -752454819778461744L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    @Temporal(TemporalType.DATE)
    @NotNull
    @PropertyLog(messageKey="PatientSample.dateCollected", operations={Operation.ALL})
    @Index(name="dateCollectedIndex")
    private Date dateCollected;

    @Column(length=50)
    @PropertyLog(messageKey="PatientSample.sampleNumber", operations={Operation.ALL})
    private String sampleNumber;

    @Column(length=250)
    @PropertyLog(messageKey="global.comments")
    private String comments;

    private Integer sampleType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CASE_ID")
    @PropertyLog(ignore=true)
    private TbCase tbcase;

    @OneToMany(mappedBy="sample", cascade={CascadeType.ALL})
    private List<ExamMicroscopy> microscopyExams = new ArrayList<ExamMicroscopy>();

    @OneToMany(mappedBy="sample", cascade={CascadeType.ALL})
    private List<ExamCulture> cultureExams = new ArrayList<ExamCulture>();

    @OneToMany(mappedBy="sample", cascade={CascadeType.ALL})
    private List<ExamDST> dstExams = new ArrayList<ExamDST>();

    @OneToMany(mappedBy="sample", cascade={CascadeType.ALL})
    private List<ExamXpert> xpertExams = new ArrayList<ExamXpert>();


    /**
     * Return month of treatment based on the start treatment date and the collected date
     * @return
     */
    public Integer getMonthTreatment() {
        Date dt = getDateCollected();

        if (getTbcase() == null)
            return null;

        return tbcase.getMonthTreatment(dt);
    }

    /**
     * Returns a key related to the system messages to display the month
     * @return
     */
    public String getMonthDisplay() {
        WorkspaceCustomizationService wsservice = WorkspaceCustomizationService.instance();
        return wsservice.getExamControl().getMonthDisplay(getTbcase(), getDateCollected());
    }

    /**
     * Return the time in a displayable format
     * @return
     */
    public String getDisplayDateCollected() {
        return LocaleDateConverter.getDisplayDate(dateCollected, false);
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
     * @return the dateCollected
     */
    public Date getDateCollected() {
        return dateCollected;
    }

    /**
     * @param dateCollected the dateCollected to set
     */
    public void setDateCollected(Date dateCollected) {
        this.dateCollected = dateCollected;
    }

    /**
     * @return the sampleNumber
     */
    public String getSampleNumber() {
        return sampleNumber;
    }

    /**
     * @param sampleNumber the sampleNumber to set
     */
    public void setSampleNumber(String sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @return the tbcase
     */
    public TbCase getTbcase() {
        return tbcase;
    }

    /**
     * @param tbcase the tbcase to set
     */
    public void setTbcase(TbCase tbcase) {
        this.tbcase = tbcase;
    }

    /**
     * @return the microscopyExams
     */
    public List<ExamMicroscopy> getMicroscopyExams() {
        return microscopyExams;
    }

    /*
     * @return the cultureExams
     */
    public List<ExamCulture> getCultureExams() {
        return cultureExams;
    }

    /*
     * @return the dstExams
     */
    public List<ExamDST> getDstExams() {
        return dstExams;
    }

    /*
     * @param microscopyExams the microscopyExams to set
     */
    public void setMicroscopyExams(List<ExamMicroscopy> microscopyExams) {
        this.microscopyExams = microscopyExams;
    }

    /*
     * @param cultureExams the cultureExams to set
     */
    public void setCultureExams(List<ExamCulture> cultureExams) {
        this.cultureExams = cultureExams;
    }

    /*
     * @param dstExams the dstExams to set
     */
    public void setDstExams(List<ExamDST> dstExams) {
        this.dstExams = dstExams;
    }

    /**
     * @return the sampleType
     */
    public Integer getSampleType() {
        return sampleType;
    }

    /**
     * @param sampleType the sampleType to set
     */
    public void setSampleType(Integer sampleType) {
        this.sampleType = sampleType;
    }


    public List<ExamXpert> getXpertExams() {
        return xpertExams;
    }

    public void setXpertExams(List<ExamXpert> xpertExams) {
        this.xpertExams = xpertExams;
    }
}
