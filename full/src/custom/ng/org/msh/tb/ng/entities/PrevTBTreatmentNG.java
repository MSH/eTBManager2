package org.msh.tb.ng.entities;

import org.msh.tb.entities.PrevTBTreatment;
import org.msh.tb.ng.entities.enums.HealthFacility;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("ng")
@Table(name="prevtbtreatmentng")
public class PrevTBTreatmentNG extends PrevTBTreatment{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1299071215049086698L;
	
	private HealthFacility healthFacility;

	public HealthFacility getHealthFacility() {
		return healthFacility;
	}
	
	public void setHealthFacility(HealthFacility healthFacility) {
		this.healthFacility = healthFacility;
	}
}
