package org.msh.tb.entities;

import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Store information about an exam request registered in the laboratory
 *
 * Created by ricardo on 13/08/14.
 */
@Entity
@Table(name = "examrequest")
public class ExamRequest {

    public enum RegisteredBy {  HEALTH_FACILITY, LABORATORY  }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="case_id")
    private TbCase tbcase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="unit_id")
    private Tbunit tbunit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="laboratory_id")
    @NotNull
    private Laboratory laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @NotNull
    private User user;

    @OneToMany(mappedBy = "request")
    private List<ExamMicroscopy> examsMicroscopy = new ArrayList<ExamMicroscopy>();

    @OneToMany(mappedBy = "request")
    private List<ExamCulture> examsCulture = new ArrayList<ExamCulture>();

    @OneToMany(mappedBy = "request")
    private List<ExamDST> examsDST = new ArrayList<ExamDST>();

    @OneToMany(mappedBy = "request")
    private List<ExamXpert> examsXpert = new ArrayList<ExamXpert>();

    private RegisteredBy registeredBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public TbCase getTbcase() {
        return tbcase;
    }

    public void setTbcase(TbCase tbcase) {
        this.tbcase = tbcase;
    }

    public Tbunit getTbunit() {
        return tbunit;
    }

    public void setTbunit(Tbunit tbunit) {
        this.tbunit = tbunit;
    }

    public List<ExamMicroscopy> getExamsMicroscopy() {
        return examsMicroscopy;
    }

    public void setExamsMicroscopy(List<ExamMicroscopy> examsMicroscopy) {
        this.examsMicroscopy = examsMicroscopy;
    }

    public List<ExamCulture> getExamsCulture() {
        return examsCulture;
    }

    public void setExamsCulture(List<ExamCulture> examsCulture) {
        this.examsCulture = examsCulture;
    }

    public List<ExamDST> getExamsDST() {
        return examsDST;
    }

    public void setExamsDST(List<ExamDST> examsDST) {
        this.examsDST = examsDST;
    }

    public List<ExamXpert> getExamsXpert() {
        return examsXpert;
    }

    public void setExamsXpert(List<ExamXpert> examsXpert) {
        this.examsXpert = examsXpert;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Laboratory getLaboratory() {
        return laboratory;
    }

    public void setLaboratory(Laboratory laboratory) {
        this.laboratory = laboratory;
    }

    public RegisteredBy getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(RegisteredBy registeredBy) {
        this.registeredBy = registeredBy;
    }
}
