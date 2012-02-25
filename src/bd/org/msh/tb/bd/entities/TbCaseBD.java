package org.msh.tb.bd.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

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

	public YesNoType getBcgScar() {
		return bcgScar;
	}

	public void setBcgScar(YesNoType bcgScar) {
		this.bcgScar = bcgScar;
	}






	
	
}
