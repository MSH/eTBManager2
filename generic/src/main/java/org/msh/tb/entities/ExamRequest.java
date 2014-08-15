package org.msh.tb.entities;

import org.msh.tb.transactionlog.PropertyLog;

import javax.persistence.*;
import java.util.Date;

/**
 * Store information about an exam request registered in the laboratory
 *
 * Created by ricardo on 13/08/14.
 */
@Entity
@Table(name = "examrequest")
public class ExamRequest {

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
}
