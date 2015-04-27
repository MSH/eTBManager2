package org.msh.etbm.services.quantb;


/**
 * Created by ricardo on 09/12/14.
 */
public class QTBMedicineRegimen {

    private Integer medicineId;

    private Integer defaultDoseUnit;

    private Integer defaultFrequency;

    private Integer monthsTreatment;

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getDefaultDoseUnit() {
        return defaultDoseUnit;
    }

    public void setDefaultDoseUnit(Integer defaultDoseUnit) {
        this.defaultDoseUnit = defaultDoseUnit;
    }

    public Integer getDefaultFrequency() {
        return defaultFrequency;
    }

    public void setDefaultFrequency(Integer defaultFrequency) {
        this.defaultFrequency = defaultFrequency;
    }

    public Integer getMonthsTreatment() {
        return monthsTreatment;
    }

    public void setMonthsTreatment(Integer monthsTreatment) {
        this.monthsTreatment = monthsTreatment;
    }
}
