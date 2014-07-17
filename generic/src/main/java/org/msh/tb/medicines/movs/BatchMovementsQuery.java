package org.msh.tb.medicines.movs;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.BatchMovement;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;
import org.msh.utils.RowGrouping;
import org.msh.utils.RowGroupingComparator;
import org.msh.utils.RowGroupingItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Name("batchMovements")
public class BatchMovementsQuery extends EntityQuery<MovementItem> implements RowGroupingComparator {
	private static final long serialVersionUID = -5729874123204457562L;

	private static final String[] restrictions = {"m.date >= #{movementFilters.dateIni}",
			"m.date <= #{movementFilters.dateEnd}",
			"bm.batch.batchNumber = #{movementFilters.batchNumber}",
			"m.tbunit.id = #{userSession.tbunit.id}",
			"m.medicine.id = #{movementFilters.medicine.id}",
			"m.source.id = #{movementFilters.source.id}",
			"m.type = #{movementFilters.type}"};

	private List<RowGroupingItem> rows;
	private Date date;
	private List<MovementItem> resultList;

	@In(create=true) UserSession userSession;
	
	public void initialize() {
		if (date == null)
			date = new Date();
	}
	
	@Override
	public String getOrder() {
		return "m.date desc, m.recordDate desc";
	}

	@Override
	public List<String> getStringRestrictions() {
		return Arrays.asList(restrictions);
	}
	
	@Override
	public Integer getMaxResults() {
		return 40;
	}

	@Override
	protected String getCountEjbql() {
		return "select count(*) from BatchMovement bm join bm.movement m";
	}
	
	@Override
	public String getEjbql() {
        return "select bm, bm.movement.availableQuantity, bm.availableQuantity " +
			"from BatchMovement bm join fetch bm.movement m " + 
			"join fetch bm.batch b " +
			"join fetch m.medicine join fetch m.source where m.medicine.id is not null";
	}
	
	public List<RowGroupingItem> getRows() {
		if (rows == null)
			rows = RowGrouping.createRows(getResultList(), this);
		return rows;
	}

	public boolean compare(Object item1, Object item2) {
		return ((MovementItem)item1).getBatchMovement().getMovement().getId().equals(((MovementItem)item2).getBatchMovement().getMovement().getId());
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}


	@Override
	public List getResultList() {
		if (resultList == null)
		{
			javax.persistence.Query query = createQuery();
	        List<Object[]> lst = query==null ? null : query.getResultList();
	        fillResultList(lst);
	    }
		return resultList;
	}


	private void fillResultList(List<Object[]> lst) {
        resultList = new ArrayList<MovementItem>();

        for (Object[] vals: lst) {
            BatchMovement bm = (BatchMovement)vals[0];
            int val = ((Number)vals[1]).intValue();
            int val2 = ((Number)vals[2]).intValue();
            MovementItem it = new MovementItem();

            it.setBatchMovement(bm);
            it.setStockQuantity(val);
            it.setBatchQuantity(val2);
            resultList.add(it);
		}
	}
}
