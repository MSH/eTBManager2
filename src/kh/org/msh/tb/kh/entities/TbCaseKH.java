package org.msh.tb.kh.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.msh.tb.entities.CaseSideEffect;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;

@Entity
@Table(name="tbcasekh")
public class TbCaseKH extends TbCase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4502733327442767580L;
	
	//@OneToMany(cascade={CascadeType.MERGE, CascadeType.PERSIST}, mappedBy="tbcase")
	//@PropertyLog(ignore=true)
	//private List<CaseDispensing_Kh> dispkh = new ArrayList<CaseDispensing_Kh>();
	
//	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcasekh")
//	private List<CaseSideEffectKH> sideEffectsKh = new ArrayList<CaseSideEffectKH>();
	
	
	@Override
	public CaseSideEffectKH findSideEffectData(FieldValue sideEffect) {
		CaseSideEffect se = findSideEffectData(sideEffect);
		if ((se != null) && (se instanceof CaseSideEffectKH)) {
			return (CaseSideEffectKH)se;
		}
		return null;
		
	}

//	public List<CaseDispensing_Kh> getDispkh() {
//		return dispkh;
//	}
//
//	public void setDispkh(List<CaseDispensing_Kh> dispkh) {
//		this.dispkh = dispkh;
//	}

/*	public List<CaseSideEffectKH> getSideEffectsKh() {
		return sideEffectsKh;
	}
	
	public void setSideEffectsKh(List<CaseSideEffectKH> sideEffectsKh) {
		this.sideEffectsKh = sideEffectsKh;
	}
*/}
