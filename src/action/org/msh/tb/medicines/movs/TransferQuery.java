package org.msh.tb.medicines.movs;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.utils.EntityQuery;


@Name("transfers")
public class TransferQuery extends EntityQuery<Transfer> {
	private static final long serialVersionUID = 8019981871075829822L; 

	private Integer month;
	private Integer year;
	private TransferStatus status;
	private int folder;
	private boolean executing;
	
	private static final String[] restrictions = {
			"month(tr.shippingDate) = #{transfers.month} + 1",
			"year(tr.shippingDate) = #{transfers.year}",
			"tr.status = #{transfers.status}"};

	public void execute() {
		executing = true;
		refresh();
	}
	
	@Override
	public String getEjbql() {
		return "from Transfer tr join fetch tr.unitFrom join fetch tr.unitTo ".concat(conditions());
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from Transfer tr ".concat(conditions());
	}

	protected String conditions() {
		String s;
		// is received folder ?
		if (folder == 0)
			 s = "where tr.unitTo.id = #{userSession.tbunit.id}";
		else s = "where tr.unitFrom.id = #{userSession.tbunit.id}";

		return s;
	}
	
	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}

	@Override
	public Integer getMaxResults() {
		return 30;
	}

	@Override
	public String getOrder() {
		String s = super.getOrder();

		if (s == null)
			 return "tr.shippingDate desc";
		else return s;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public TransferStatus getStatus() {
		return status;
	}

	public void setStatus(TransferStatus status) {
		this.status = status;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public int getFolder() {
		return folder;
	}

	public void setFolder(int folder) {
		this.folder = folder;
		refresh();
	}
	
	public boolean isExecuting() {
		return executing;
	}
}
