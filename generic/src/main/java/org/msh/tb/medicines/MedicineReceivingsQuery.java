package org.msh.tb.medicines;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.MedicineReceiving;
import org.msh.tb.entities.Source;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.List;


@Name("medicineReceivings")
public class MedicineReceivingsQuery extends EntityQuery<MedicineReceiving> {
	private static final long serialVersionUID = 65860235384350379L;
	
	private static final String[] restrictions = {
			"dr.tbunit.id = #{userSession.tbunit.id}",
			"dr.source.id = #{medicineReceivings.source.id}" 
	};
	private Source source;

	@Override
	public Integer getMaxResults() {
		return 40;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s == null)
			 return "dr.receivingDate desc";
		else return s;
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from MedicineReceiving dr";
	}

	@Override
	public String getEjbql() {
		return "from MedicineReceiving dr join fetch dr.tbunit join fetch dr.source";
	}

	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
	
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
}
