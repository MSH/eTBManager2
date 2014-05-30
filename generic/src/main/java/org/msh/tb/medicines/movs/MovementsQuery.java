package org.msh.tb.medicines.movs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.DispensingMovementDetail.DispensingPatientDetail;
import org.msh.utils.EntityQuery;




@Name("movements")
public class MovementsQuery extends EntityQuery<MovementItem> {
	private static final long serialVersionUID = -3122668237591767310L;

	private static final String[] restrictions = {"m.date >= #{movementFilters.dateIni}",
			"m.date <= #{movementFilters.dateEnd}",
			"exists (select id from BatchMovement b where b.movement.id = m.id and b.batch.batchNumber = #{movementFilters.batchNumber})",
			"m.tbunit.id = #{movementFilters.showAllUnits ? userSession.tbunitselection.tbunit.id : userSession.tbunit.id}",
			"m.tbunit.adminUnit.code like #{movementFilters.showAllUnits ? userSession.adminUnitCodeLike : null}",
			"m.source.id = #{sourceHome.id}",
			"m.type = #{movementFilters.type}",
			"m.medicine.id = #{medicineHome.id}",
			"m.adjustmentType.id = #{movementFilters.adjustmentInfo.value.id}",
			"m.comment like #{movements.getAdjustmentInfoComplement()}"};

	@In(create=true) UserSession userSession;
	@In(create=true) MovementFilters movementFilters;

	private DispensingMovementDetail dispMovtDetail;
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

	public void loadDispMovDetail(){
		List<Object[]> lst = (List<Object[]>) getEntityManager()
				.createQuery("select m.date, m.medicine.genericName.name1, m.medicine.dosageForm, m.source.name.name1, m.quantity, p.tbcase.patient.name, " +
								"p.quantity, p.batch.batchNumber, p.batch.manufacturer, p.tbcase.id " +
									"from MedicineDispensingCase p " +
									"inner join p.dispensing.movements m " +
									"where m.id=:movId and m.medicine.id = p.batch.medicine.id")
				.setParameter("movId", movementFilters.getSelectedMovement())
				.getResultList();
		
		fillDispMovDetails(lst);
		
	}
	
	private void fillDispMovDetails(List<Object[]> lst){
		dispMovtDetail = new DispensingMovementDetail();
		
		if(lst.size()>0){
			dispMovtDetail.setMovementDate((Date) lst.get(0)[0]);
			dispMovtDetail.setMedicineName((String) lst.get(0)[1]);
			dispMovtDetail.setDosageForm((String) lst.get(0)[2]);
			dispMovtDetail.setSourceName((String) lst.get(0)[3]);
			dispMovtDetail.setTotalQuantity((Integer) lst.get(0)[4]);
			
			for(Object[] obj : lst){
				DispensingPatientDetail aux = dispMovtDetail.addDispensingPatientDetail();
				aux.setPatientName((String) obj[5]);
				aux.setQuantity((Integer) obj[6]);
				aux.setBatchNumber((String) obj[7]);
				aux.setManufacturerName((String) obj[8]);
				aux.setCaseId((Integer) obj[9]);
			}
			
		}
	}
	
	/**
	 * @return the dispMovtDetail
	 */
	public DispensingMovementDetail getDispMovtDetail() {
		if(dispMovtDetail == null)
			return new DispensingMovementDetail();
		return dispMovtDetail;
	}
	
	public String getAdjustmentInfoComplement(){
		if(movementFilters.getAdjustmentInfo().getComplement() != null && !movementFilters.getAdjustmentInfo().getComplement().isEmpty()){
			return '%' + movementFilters.getAdjustmentInfo().getComplement() + '%';
		}
		return null;
	}

}
