package org.msh.tb.medicines.movs;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.msh.tb.entities.Batch;
import org.msh.tb.entities.Movement;
import org.msh.tb.entities.enums.MovementType;
import org.msh.tb.login.UserSession;
import org.msh.tb.medicines.movs.DispensingMovementDetail.DispensingPatientDetail;
import org.msh.utils.EntityQuery;

import java.util.Arrays;
import java.util.Date;
import java.util.List;




@Name("movements")
public class MovementsQuery extends EntityQuery<Movement> {
    private static final long serialVersionUID = -3122668237591767310L;

    private static final String[] restrictions = {"m.date >= #{movementFilters.dateIni}",
            "m.date <= #{movementFilters.dateEnd}",
            "exists (select id from BatchMovement b where b.movement.id = m.id and b.batch.id = #{movementFilters.batch.id})",
            "m.tbunit.id = #{movementFilters.showAllUnits ? userSession.tbunitselection.tbunit.id : userSession.tbunit.id}",
            "m.tbunit.adminUnit.code like #{movementFilters.showAllUnits ? userSession.adminUnitCodeLike : null}",
            "m.source.id = #{sourceHome.id}",
            "m.type = #{movementFilters.type}",
            "m.medicine.id = #{movementFilters.medicine.id}",
            "m.adjustmentType.id = #{movementFilters.adjustmentInfo.value.id}",
            "m.comment like #{movements.getAdjustmentInfoComplement()}"};

    @In(create=true) UserSession userSession;
    @In(create=true) MovementFilters movementFilters;

    private DispensingMovementDetail dispMovtDetail;
    private List resultList;

    // list of batches available when medicine is selected
    private List<Batch> medicineBatches;

    /**
     * @return
     */
    @Factory("movementTypes")
    public MovementType[] getMovementTypes() {
        return MovementType.values();
    }


    /** {@inheritDoc}
     */
    @Override
    public String getOrder() {
        return "m.date desc, m.recordDate desc";
    }

    /** {@inheritDoc}
     */
    @Override
    public List<String> getStringRestrictions() {
        return Arrays.asList(restrictions);
    }

    /** {@inheritDoc}
     */
    @Override
    public Integer getMaxResults() {
        return 40;
    }

    /** {@inheritDoc}
     */
    @Override
    protected String getCountEjbql() {
        return "select count(*) from Movement m";
    }

    /** {@inheritDoc}
     */
    @Override
    public String getEjbql() {
        return "from Movement m join fetch m.medicine join fetch m.source where m.medicine.id is not null";
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

    public void loadDispBatchMovDetail(){
		/*				.createNativeQuery("select m.mov_date, med.name1, med.dosageForm, s.name1, bm.quantity, p.patient_name, mdc.quantity, b.batchNumber, b.manufacturer, c.id" +
						"from medicinedispensingcase mdc " +
						"inner join medicinedispensing d on d.id = mdc.dispensing_id " +
						"inner join movements_dispensing md on md.dispensing_id = d.id " +
						"inner join movement m on m.id = md.movement_id " +
						"inner join batchmovement bm on bm.movement_id = m.id " +
						"inner join medicine med on med.id = m.medicine_id " +
						"inner join source s on s.id = m.source_id " +
						"inner join tbcase c on c.id = mdc.case_id " +
						"inner join patient p on p.id = c.patient_id " +
						"inner join batch b on b.id = bm.batch_id " +
						"where mdc.batch_id = bm.batch_id and bm.id = :batchMovId ")*/
        List<Object[]> lst = (List<Object[]>) getEntityManager()
                .createQuery("select m.date, m.medicine.genericName.name1, m.medicine.dosageForm, m.source.name.name1, bm.quantity, p.tbcase.patient.name, " +
                        "p.quantity, p.batch.batchNumber, p.batch.manufacturer, p.tbcase.id " +
                        "from MedicineDispensingCase p, BatchMovement bm " +
                        "inner join p.dispensing.movements m " +
                        "where bm.movement.id = m.id and bm.batch.id = p.batch.id and bm.id = :batchMovId ")
                .setParameter("batchMovId", movementFilters.getSelectedBatchMovement())
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


    /**
     * Return the list of available batches to the report
     * @return {@link List} of {@link Batch} objects
     */
    public List<Batch> getMedicineBatches() {
        if (medicineBatches == null) {
            createMedicineBatchesList();
        }
        return medicineBatches;
    }


    /**
     * Create the list of batches to be selected by the user. The list is created only
     * if the medicine is selected
     */
    protected void createMedicineBatchesList() {
        if (movementFilters.getMedicine() != null) {
            String hql = "from Batch b where b.id in (select distinct m.batch.id from BatchMovement m "
                    + "where m.movement.tbunit.id = #{userSession.tbunit.id}) and b.medicine.id = #{movementFilters.medicine.id} "
                    + "order by b.batchNumber";
            medicineBatches = getEntityManager().createQuery(hql).getResultList();
        }
    }


}
