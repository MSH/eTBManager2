package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.utils.EntityQuery;




@Name("movements")
public class MovementsQuery extends EntityQuery<MovementItem> {
	private static final long serialVersionUID = -3122668237591767310L;

	private static final String[] restrictions = {"m.date <= #{movementFilters.date}",
			"m.tbunit.id = #{userSession.tbunit.id}",
			"m.source.id = #{sourceHome.id}",
			"m.type = #{movementFilters.type}",
			"m.medicine.id = #{medicineHome.id}"};

	@In(create=true) UserSession userSession;

	private List resultList;
	
	@Factory("movementTypes")
	public MovementType[] getMovementTypes() {
		return MovementType.values();
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
		return "select count(*) from Movement m";
	}
	
	@Override
	public String getEjbql() {
		return "select m, " +
				"(select sum(a.quantity * a.oper) from Movement a where a.tbunit.id=m.tbunit.id " +
				"and a.source.id=m.source.id and a.medicine.id=m.medicine.id " +
				"and ((a.date < m.date) or (a.date = m.date and a.recordDate <= m.recordDate))) " +
				"from Movement m join fetch m.medicine join fetch m.source where m.medicine.id is not null";
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
		resultList = new ArrayList();
		for (Object[] vals: lst) {
			Movement m = (Movement)vals[0];
			Long val = (Long)vals[1];
			MovementItem it = new MovementItem();

			it.setMovement(m);
			it.setStockQuantity(val.intValue());
			resultList.add(it);
		}
	}

}
