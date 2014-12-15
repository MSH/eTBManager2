package org.msh.tb.webservices.quantb;

import java.util.List;

/**
 * Created by ricardo on 09/12/14.
 */
public class QTBRegimen {

    private Integer id;
    private String name;
    private List<QTBMedicineRegimen> intensivePhaseMedicines;
    private List<QTBMedicineRegimen> continousPhaseMedicines;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QTBMedicineRegimen> getIntensivePhaseMedicines() {
        return intensivePhaseMedicines;
    }

    public void setIntensivePhaseMedicines(List<QTBMedicineRegimen> intensivePhaseMedicines) {
        this.intensivePhaseMedicines = intensivePhaseMedicines;
    }

    public List<QTBMedicineRegimen> getContinousPhaseMedicines() {
        return continousPhaseMedicines;
    }

    public void setContinousPhaseMedicines(List<QTBMedicineRegimen> continousPhaseMedicines) {
        this.continousPhaseMedicines = continousPhaseMedicines;
    }
}
