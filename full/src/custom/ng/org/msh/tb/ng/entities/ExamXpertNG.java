package org.msh.tb.ng.entities;

import org.msh.tb.entities.ExamXpert;
import org.msh.tb.entities.FieldValueComponent;

import javax.persistence.*;

/**
 * Created by rmemoria on 10/2/15.
 */
@Entity
@DiscriminatorValue("ng")
public class ExamXpertNG extends ExamXpert{

    @Embedded
    @AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "reason_id")) })
    @AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "otherReason")) })
    private FieldValueComponent reasonTest;

    public FieldValueComponent getReasonTest() {
        return reasonTest;
    }

    public void setReasonTest(FieldValueComponent reasonTest) {
        this.reasonTest = reasonTest;
    }
}
