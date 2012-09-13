package org.msh.tb.bd.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.msh.tb.bd.entities.enums.Occupation;
import org.msh.tb.bd.entities.enums.PulmonaryTypesBD;
import org.msh.tb.bd.entities.enums.SalaryRange;
import org.msh.tb.entities.FieldValue;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.enums.YesNoType;


@Entity
@Table(name="tbcasebd")
public class TbCaseBD extends TbCase{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7151770665202536352L;
	
	private YesNoType bcgScar;
	
	private Occupation occupation;
	
	private SalaryRange salary;
	
	private PulmonaryTypesBD pulmonaryTypesBD;
	
	@OneToMany(cascade={CascadeType.ALL}, mappedBy="tbcasebd")
	private List<CaseSideEffectBD> sideEffectsBd = new ArrayList<CaseSideEffectBD>();
	

	public SalaryRange getSalary() {
		return salary;
	}

	public void setSalary(SalaryRange salary) {
		this.salary = salary;
	}

	public Occupation getOccupation() {
		return occupation;
	}

	public void setOccupation(Occupation occupation) {
		this.occupation = occupation;
	}

	public YesNoType getBcgScar() {
		return bcgScar;
	}

	public void setBcgScar(YesNoType bcgScar) {
		this.bcgScar = bcgScar;
	}

	/**
	 * Search for side effect data by the side effect
	 * @param sideEffect - FieldValue object representing the side effect
	 * @return - CaseSideEffect instance containing side effect data of the case, or null if there is no side effect data
	 */
	@Override
	public CaseSideEffectBD findSideEffectData(FieldValue sideEffect) {
		for (CaseSideEffectBD se: getSideEffectsBd()) {
			if (se.getSideEffect().getValue().equals(sideEffect))
				return se;
		}
		return null;
	}

	
	public List<CaseSideEffectBD> getSideEffectsBd() {
		return sideEffectsBd;
	}
	
	public void setSideEffectsBd(List<CaseSideEffectBD> sideEffectsBd) {
		this.sideEffectsBd = sideEffectsBd;
	}

	public PulmonaryTypesBD getPulmonaryTypesBD() {
		return pulmonaryTypesBD;
	}
	
	public void setPulmonaryTypesBD(PulmonaryTypesBD pulmonaryTypesBD) {
		this.pulmonaryTypesBD = pulmonaryTypesBD;
	}
		
}
