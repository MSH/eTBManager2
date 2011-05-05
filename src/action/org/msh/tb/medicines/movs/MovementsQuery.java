package org.msh.tb.medicines.movs;

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
public class MovementsQuery extends EntityQuery<Movement> {
	private static final long serialVersionUID = -3122668237591767310L;

	private static final String[] restrictions = {"m.date <= #{movementFilters.date}",
			"m.tbunit.id = #{userSession.tbunit.id}",
			"m.source.id = #{sourceHome.id}",
			"m.type = #{movementFilters.type}",
			"m.medicine.id = #{medicineHome.id}"};

	@In(create=true) UserSession userSession;
	
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
		return "from Movement m join fetch m.medicine join fetch m.source";
	}

}
