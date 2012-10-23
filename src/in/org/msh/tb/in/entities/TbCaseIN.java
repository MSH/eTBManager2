package org.msh.tb.in.entities;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.msh.tb.entities.FieldValueComponent;
import org.msh.tb.entities.TbCase;

@Entity
@Table(name="tbcasein")
public class TbCaseIN extends TbCase {

	private static final long serialVersionUID = 3063224193417455569L;
	
	@Embedded
	@AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "SUSPECT_CRITERIA")) })
	@AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "SUSPECT_CRITERIA_Complement")) })
	private FieldValueComponent suspectCriteria;

	public FieldValueComponent getSuspectCriteria() {
		if (suspectCriteria == null)
			suspectCriteria = new FieldValueComponent();
		return suspectCriteria;
	}

	public void setSuspectCriteria(FieldValueComponent suspectCriteria) {
		this.suspectCriteria = suspectCriteria;
	}

	
		
}
