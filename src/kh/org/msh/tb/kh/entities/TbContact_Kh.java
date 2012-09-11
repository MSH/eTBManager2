package org.msh.tb.kh.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.TbContact;

/**
* @author Vani Rao
*
* Records information about additional TbContact details for Cambodian workspace
*/
@Entity
@DiscriminatorValue("kh")
public class TbContact_Kh extends TbContact{

	/**
	 * 
	 */
	private static final long serialVersionUID = 278356967471487323L;
	
	private boolean hasTBSymptom;
	
	private boolean sampleSentForCultureTest;
	
	private boolean sampleSentForDSTTest;

	public boolean isHasTBSymptom() {
		return hasTBSymptom;
	}
	
	public void setHasTBSymptom(boolean hasTBSymptom) {
		this.hasTBSymptom = hasTBSymptom;
	}

	public void setSampleSentForCultureTest(boolean sampleSentForCultureTest) {
		this.sampleSentForCultureTest = sampleSentForCultureTest;
	}

	public boolean isSampleSentForCultureTest() {
		return sampleSentForCultureTest;
	}

	public void setSampleSentForDSTTest(boolean sampleSentForDSTTest) {
		this.sampleSentForDSTTest = sampleSentForDSTTest;
	}

	public boolean isSampleSentForDSTTest() {
		return sampleSentForDSTTest;
	}
}
