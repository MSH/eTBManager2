package org.msh.tb.reports;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.mdrtb.entities.BatchQuantity;
import org.msh.utils.EntityQuery;


@Name("batchesReport")
public class BatchesReportQuery extends EntityQuery<BatchQuantity> {
	private static final long serialVersionUID = 2716061240331114210L;

	private static final String[] restrictions = {"b.source = #{stockPosHome.source}",
			"b.tbunit = #{userSession.tbunit}"};
	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from BatchQuantity b";
	}

	@Override
	public String getEjbql() {
		return "from BatchQuantity b";
	}

	@Override
	public Integer getMaxResults() {
		return 40;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();
		if (s==null)
			 return "b.source.name, b.batch.medicine.genericName, b.batch.medicine.strength, b.batch.expiryDate";
		else return super.getOrder();
	}
}
