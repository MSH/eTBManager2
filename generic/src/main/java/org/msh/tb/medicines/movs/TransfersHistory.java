package org.msh.tb.medicines.movs;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Transfer;
import org.msh.tb.entities.enums.ShippedReceivedDiffTypes;
import org.msh.tb.entities.enums.TransferStatus;
import org.msh.utils.EntityQuery;


/**
 * Maintain a list of transfers that are not open
 * @author Ricardo Memoria
 *
 */
@Name("transfersHistory")
public class TransfersHistory extends EntityQuery<Transfer> {
	private static final long serialVersionUID = 8019981871075829822L; 

	private Integer month;
	private Integer year;
	private TransferStatus status;
	private boolean executing;
	
	private ShippedReceivedDiffTypes diffType;
	
	private static final String[] restrictions = {
			"month(tr.shippingDate) = #{transfersHistory.month} + 1",
			"year(tr.shippingDate) = #{transfersHistory.year}",
			"tr.status = #{transfersHistory.status}"};
	
	private static final TransferStatus[] statusOptions = {TransferStatus.CANCELLED, TransferStatus.DONE};

	public void execute() {
		executing = true;
		refresh();
	}
	
	/**
	 * Return transfer status options
	 * @return
	 */
	public TransferStatus[] getTransferStatusOptions() {
		return statusOptions;
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
		String cond = "where tr.status in (1, 2) and (tr.unitTo.id = #{userSession.tbunit.id} or tr.unitFrom.id = #{userSession.tbunit.id})";
	
		if(diffType != null){
			if(diffType.equals(ShippedReceivedDiffTypes.RECEIVED_BT_SHIPPED)){
				cond += " and exists(from TransferBatch tb where tb.transferItem.transfer.id = tr.id and tb.quantity < tb.quantityReceived)";
			}else if(diffType.equals(ShippedReceivedDiffTypes.SHIPPED_BT_RECEIVED)){
				cond += " and exists(from TransferBatch tb where tb.transferItem.transfer.id = tr.id and tb.quantity > tb.quantityReceived)";
			}else if(diffType.equals(ShippedReceivedDiffTypes.BOTH)){
				cond += " and exists(from TransferBatch tb where tb.transferItem.transfer.id = tr.id and tb.quantity <> tb.quantityReceived and tb.quantityReceived is not null)";
			}else if(diffType.equals(ShippedReceivedDiffTypes.NONE)){
				cond += " and exists(from TransferBatch tb where tb.transferItem.transfer.id = tr.id and (tb.quantity = tb.quantityReceived or tb.quantityReceived is null))";
			}
		}

		return cond;
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
			 return "tr.id desc";
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
	
	public boolean isExecuting() {
		return executing;
	}

	public ShippedReceivedDiffTypes getDiffType() {
		return diffType;
	}

	public void setDiffType(ShippedReceivedDiffTypes diffType) {
		this.diffType = diffType;
	}

}
