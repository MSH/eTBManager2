package org.msh.etbm.services.quantb;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.App;
import org.msh.tb.entities.BatchQuantity;
import org.msh.tb.entities.Medicine;
import org.msh.tb.entities.MedicineRegimen;
import org.msh.tb.entities.Regimen;
import org.msh.tb.entities.enums.CaseState;
import org.msh.tb.webservices.RemoteActionHandler;
import org.msh.utils.date.DateUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Integration service that generates the data to export to QuanTB
 *
 * Created by ricardo on 09/12/14.
 */
@Name("quantbServices")
@BypassInterceptors
public class QuantbServices {

    /**
     * Generate all data to be sent to QuanTB
     * @return
     */
    @Transactional
    public QuantbData export() {
        QuantbData data = new QuantbData();

        data.setMedicines(getMedicines());
        data.setRegimens(getRegimens());
        data.setCases(getCases());
        data.setInventory(getInventory());

        return data;
    }


    protected List<QTBMedicine> getMedicines() {
        EntityManager em = App.getEntityManager();

        List<Medicine> lst = em.createQuery("from Medicine where workspace.id = #{defaultWorkspace.id}")
                .getResultList();

        List<QTBMedicine> res = new ArrayList<QTBMedicine>();
        for (Medicine med: lst) {
            QTBMedicine it = new QTBMedicine();
            it.setName(med.toString());
            it.setAbbrevName(med.getAbbrevName());
            it.setDosage(med.getDosageForm());
            it.setType(Messages.instance().get( med.getCategory().getKey() ) );

            res.add(it);
        }

        return res;
    }


    /**
     * Return the list of available regimens
     * @return list of {@link QTBRegimen}
     */
    private List<QTBRegimen> getRegimens() {
        EntityManager em = App.getEntityManager();

        List<Regimen> lst = em.createQuery("from Regimen where workspace.id = #{defaultWorkspace.id}")
                .getResultList();

        List<QTBRegimen> res = new ArrayList<QTBRegimen>();
        for (Regimen reg: lst) {
            QTBRegimen it = new QTBRegimen();
            it.setId(reg.getId());
            it.setName(reg.getName());
            it.setContinousPhaseMedicines(getListMedicinesRegimen(reg.getContinuousPhaseMedicines()));
            it.setIntensivePhaseMedicines(getListMedicinesRegimen(reg.getIntensivePhaseMedicines()));

            res.add(it);
        }

        return res;
    }

    /**
     * Return list of medicines of a regimen phase
     * @param lst
     * @return
     */
    private List<QTBMedicineRegimen> getListMedicinesRegimen(List<MedicineRegimen> lst) {
        List<QTBMedicineRegimen> res = new ArrayList<QTBMedicineRegimen>();

        for (MedicineRegimen m: lst) {
            QTBMedicineRegimen medreg = new QTBMedicineRegimen();
            medreg.setDefaultDoseUnit(m.getDefaultDoseUnit());
            medreg.setDefaultFrequency(m.getDefaultFrequency());
            medreg.setMedicineId(m.getMedicine().getId());
            medreg.setMonthsTreatment(m.getMonthsTreatment());

            res.add(medreg);
        }

        return res;
    }


    /**
     * Return the list of cases
     * @return
     */
    protected List<QTBCases> getCases() {
        List<QTBCases> cases = new ArrayList<QTBCases>();

        EntityManager em = App.getEntityManager();

        // load information about cases on treatment
        String hql = "select pm.period.iniDate, pm.period.endDate, pm.doseUnit, pm.medicine.id, pm.frequency, tb.id, tb.regimen.id " +
                "from PrescribedMedicine pm " +
                "join pm.tbcase tb " +
                "where pm.medicine.workspace.id = #{defaultWorkspace.id} " +
                "and tb.state = :val " +
                "and pm.period.endDate >= :dtIni " +
                "order by tb.id, pm.medicine.id";

        List<Object[]> lst = em.createQuery(hql)
                .setParameter("val", CaseState.ONTREATMENT)
                .setParameter("dtIni", DateUtils.incMonths( DateUtils.getDate(), -24) )
                .getResultList();

        for (Object[] vals: lst) {
            Date dtini = (Date)vals[0];
            Date dtend = (Date)vals[1];
            Integer dose = (Integer)vals[2];
            Integer medId = (Integer)vals[3];
            Integer freq = (Integer)vals[4];
            Integer regId = (Integer)vals[5];

            // just a workaround to avoid the work of comparing null to int values
            regId = regId == null? 0: regId;
            medId = medId == null? 0: medId;

            int month = DateUtils.monthOf(dtini);
            int year = DateUtils.yearOf(dtini);

            while (true) {
                incCases(cases, month, year, medId, regId);

                // next month
                month++;
                if (month == 12) {
                    month = 0;
                    year++;
                }

                // check if the loop has ended
                Date dtref = DateUtils.newDate(year, month, 1);
                if (dtref.after(dtend)) {
                    break;
                }
            }
        }

        return cases;
    }

    /**
     * Increment the number of cases in the list of cases x month x year x medicine
     * @param lst
     * @param month
     * @param year
     * @param medId
     * @param regId
     */
    private void incCases(List<QTBCases> lst, int month, int year, Integer medId, Integer regId) {
        QTBCases item = null;

        // search for record
        for (QTBCases c: lst) {
            // object found ?
            if ((c.getMonth() == month) && (c.getYear() == year) && (c.getMedicineId() == medId) && (regId == c.getRegimenId())) {
                item = c;
                break;
            }
        }

        if (item == null) {
            item = new QTBCases();
            item.setMonth(month);
            item.setYear(year);
            item.setMedicineId(medId);
            item.setRegimenId(regId);
            item.setNumCases(1);
            lst.add(item);
        }
        else {
            item.setNumCases( item.getNumCases() + 1 );
        }
    }


    protected List<QTBInventory> getInventory() {
        List<QTBInventory> lst = new ArrayList<QTBInventory>();

        EntityManager em = App.getEntityManager();

        List<BatchQuantity> batches = em.createQuery("from BatchQuantity s join fetch s.batch b " +
                "where s.quantity > 0 and b.expiryDate > :dt " +
                " and b.medicine.workspace.id = #{defaultWorkspace.id} " +
                "group by b.medicine.id, b.expiryDate")
                .setParameter("dt", DateUtils.getDate())
                .getResultList();

        for (BatchQuantity bq: batches) {
            QTBInventory item = new QTBInventory();
            item.setMedicineId( bq.getBatch().getMedicine().getId() );
            item.setBatchNumber( bq.getBatch().getBatchNumber() );
            item.setExpiryDate( bq.getBatch().getExpiryDate() );
            item.setManufacturer( bq.getBatch().getManufacturer() );
            item.setQuantity( bq.getQuantity() );

            lst.add(item);
        }

        return lst;
    }

}
