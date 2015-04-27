package org.msh.etbm.services.quantb;

/**
 * Created by ricardo on 09/12/14.
 */
public class QTBCases {
    private int month;
    private int year;
    private Integer regimenId;
    private Integer medicineId;
    private int numCases;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getRegimenId() {
        return regimenId;
    }

    public void setRegimenId(Integer regimenId) {
        this.regimenId = regimenId;
    }

    public int getNumCases() {
        return numCases;
    }

    public void setNumCases(int numCases) {
        this.numCases = numCases;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }
}
