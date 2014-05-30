package org.msh.tb.uz.entities;

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
@Table(name="tbcaseuz")
public class TbCaseUZ extends TbCase {
	private static final long serialVersionUID = 556597949887506121L;

	@Embedded
	@AssociationOverrides({ @AssociationOverride(name = "value", joinColumns = @JoinColumn(name = "anothertb_id")) })
	@AttributeOverrides({ @AttributeOverride(name = "complement", column = @Column(name = "anothertb_Complement")) })
	private FieldValueComponent anothertb = new FieldValueComponent();

	/**
	 * @return the anothertb
	 */
	public FieldValueComponent getAnothertb() {
		if (anothertb == null)
			anothertb = new FieldValueComponent();
		return anothertb;
	}

	/**
	 * @param anothertb the anothertb to set
	 */
	public void setAnothertb(FieldValueComponent anothertb) {
		this.anothertb = anothertb;
	}

}
